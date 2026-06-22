package com.vims.common.bbs;

import com.system.common.base.AbstractCommonService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SysBbsBoardService extends AbstractCommonService<SysBbsBoard> {
    private final SysBbsBoardMapper sysBbsBoardMapper;
    private final com.vims.fmsClient.FmsClient fmsClient;

    @Override
    public Map<String, List<?>> findPage(SysBbsBoard request) throws Exception {
        List<SysBbsBoard> list = new ArrayList<>();
        Map<String, List<?>> result = new HashMap<>();
        int pagingNum;
        int totalCount;
        try {
            list = selectPage(request);
            pagingNum = selectPagingTotalNumber(request);
            totalCount = sysBbsBoardMapper.SELECT_TOTAL_COUNT(request);

            List<Integer> pagingList = new ArrayList<>();
            pagingList.add(pagingNum);

            List<Integer> countList = new ArrayList<>();
            countList.add(totalCount);

            result.put("DATA", list);
            result.put("TOTAL_PAGING", pagingList);
            result.put("TOTAL_COUNT", countList);
        } catch (Exception e) {
            throw new Exception(e);
        }
        return result;
    }

    @Override
    protected List<SysBbsBoard> selectPage(SysBbsBoard request) throws Exception {
        return sysBbsBoardMapper.SELECT_PAGE(request);
    }

    @Override
    protected int selectPagingTotalNumber(SysBbsBoard request) throws Exception {
        return sysBbsBoardMapper.SELECT_PAGING_TOTAL_NUMBER(request);
    }

    @Override
    protected List<SysBbsBoard> findImpl(SysBbsBoard request) throws Exception {
        if (request.getPst_id() != null && !request.getPst_id().isEmpty()) {
            sysBbsBoardMapper.INCREMENT_HIT_CNT(request);
        }
        return sysBbsBoardMapper.SELECT(request);
    }

    @Override
    protected int removeImpl(SysBbsBoard request) {
        // 1. 게시글 정보 조회 (File UUID 확인 목적)
        List<SysBbsBoard> boardList = sysBbsBoardMapper.SELECT(request);
        if (boardList != null && !boardList.isEmpty()) {
            SysBbsBoard board = boardList.get(0);
            String fileUuid = board.getFile_uuid();

            // 2. 파일이 존재하면 FMS를 통해 삭제 요청
            if (fileUuid != null && !fileUuid.isEmpty()) {
                try {
                    java.util.Map<String, Object> fileParam = new java.util.HashMap<>();
                    fileParam.put("file_uuid", fileUuid);
                    fmsClient.removeByFileUuid(fileParam);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // 3. 썸네일이 존재하면 FMS를 통해 삭제 요청
            String thmbnlUuid = board.getThmb_path();
            if (thmbnlUuid != null && !thmbnlUuid.isEmpty()) {
                try {
                    java.util.Map<String, Object> thumbParam = new java.util.HashMap<>();
                    thumbParam.put("file_uuid", thmbnlUuid);
                    fmsClient.removeByFileUuid(thumbParam);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return sysBbsBoardMapper.DELETE(request);
    }

    @Override
    protected int updateImpl(SysBbsBoard request) {
        return sysBbsBoardMapper.UPDATE(request);
    }

    @Override
    protected int registerImpl(SysBbsBoard request) {
        if (request.getPst_id() == null || request.getPst_id().isEmpty()) {
            request.setPst_id(UUID.randomUUID().toString());
        }
        return sysBbsBoardMapper.INSERT(request);
    }

    @Override
    protected int excelUploadImpl(MultipartFile file) throws Exception {
        return 0;
    }
}
