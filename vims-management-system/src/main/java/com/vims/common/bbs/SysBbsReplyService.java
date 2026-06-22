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
public class SysBbsReplyService extends AbstractCommonService<SysBbsReply> {
    private final SysBbsReplyMapper sysBbsReplyMapper;

    @Override
    public Map<String, List<?>> findPage(SysBbsReply request) throws Exception {
        List<SysBbsReply> list = new ArrayList<>();
        Map<String, List<?>> result = new HashMap<>();
        try {
            list = selectPage(request);
            result.put("DATA", list);
        } catch (Exception e) {
            throw new Exception(e);
        }
        return result;
    }

    @Override
    protected List<SysBbsReply> selectPage(SysBbsReply request) throws Exception {
        return sysBbsReplyMapper.SELECT_PAGE(request);
    }

    @Override
    protected int selectPagingTotalNumber(SysBbsReply request) throws Exception {
        return sysBbsReplyMapper.SELECT_PAGING_TOTAL_NUMBER(request);
    }

    @Override
    protected List<SysBbsReply> findImpl(SysBbsReply request) throws Exception {
        return sysBbsReplyMapper.SELECT(request);
    }

    /**
     * 게시물 ID로 댓글 목록 조회 (계층 구조 정렬)
     */
    public List<SysBbsReply> findByBoardId(SysBbsReply request) throws Exception {
        return sysBbsReplyMapper.SELECT_BY_BOARD_ID(request);
    }

    /**
     * 게시물 ID로 댓글 수 조회
     */
    public int countByBoardId(SysBbsReply request) {
        return sysBbsReplyMapper.COUNT_BY_BOARD_ID(request);
    }

    @Override
    protected int removeImpl(SysBbsReply request) {
        // 권한 검증: 작성자 본인 확인
        try {
            List<SysBbsReply> replies = sysBbsReplyMapper.SELECT(request);
            if (replies != null && !replies.isEmpty()) {
                SysBbsReply targetReply = replies.get(0);

                // 현재 로그인한 사용자 정보 가져오기
                org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder
                        .getContext().getAuthentication();

                if (authentication != null
                        && authentication.getPrincipal() instanceof com.system.auth.authuser.AuthUser) {
                    com.system.auth.authuser.AuthUser user = (com.system.auth.authuser.AuthUser) authentication
                            .getPrincipal();
                    String currentUserEmail = user.getEmail();

                    // 관리자 권한이 있는 경우 삭제 허용 (선택 사항, 여기서는 작성자만 허용)
                    // if (!currentUserEmail.equals(targetReply.getSys_crt_usr_id()) &&
                    // !user.getRole().name().equals("ADMIN")) {

                    if (!currentUserEmail.equals(targetReply.getSys_crt_usr_id())) {
                        // 작성자가 아니면 삭제 불가 (0 반환)
                        return 0;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }

        return sysBbsReplyMapper.DELETE(request);
    }

    @Override
    protected int updateImpl(SysBbsReply request) {
        return sysBbsReplyMapper.UPDATE(request);
    }

    @Override
    protected int registerImpl(SysBbsReply request) {
        if (request.getCmnt_id() == null || request.getCmnt_id().isEmpty()) {
            request.setCmnt_id(UUID.randomUUID().toString());
        }
        return sysBbsReplyMapper.INSERT(request);
    }

    @Override
    protected int excelUploadImpl(MultipartFile file) throws Exception {
        return 0;
    }
}
