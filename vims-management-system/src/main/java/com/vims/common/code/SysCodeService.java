package com.vims.common.code;

import com.system.common.base.AbstractCommonService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SysCodeService extends AbstractCommonService<SysCode> {

    private final SysCodeMapper sysCodeMapper;

    protected List<SysCode> findSysCode(SysCode request) throws Exception {
        try {
            return sysCodeMapper.SELECT(request);
        } catch (Exception e) {
            throw new Exception(e);
        }

    }

    @Override
    protected List<SysCode> selectPage(SysCode request) throws Exception {
        return sysCodeMapper.SELECT_PAGE(request);
    }

    @Override
    protected int selectPagingTotalNumber(SysCode request) throws Exception {
        return sysCodeMapper.SELECT_PAGING_TOTAL_NUMBER(request);
    }

    @Override
    protected List<SysCode> findImpl(SysCode request) throws Exception {
        return sysCodeMapper.SELECT(request);
    }

    @Override
    protected int removeImpl(SysCode request) {
        return sysCodeMapper.DELETE(request);
    }

    @Override
    protected int updateImpl(SysCode request) {
        return sysCodeMapper.UPDATE(request);
    }

    @Override
    protected int registerImpl(SysCode request) {
        return sysCodeMapper.INSERT(request);
    }

    @Override
    protected int excelUploadImpl(MultipartFile file) throws Exception {
        return 0;
    }

}
