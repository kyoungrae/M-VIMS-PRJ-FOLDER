package com.vims.common.codegroup;

import com.system.common.base.AbstractCommonService;
import com.system.common.exception.CustomException;
import com.vims.common.code.SysCode;
import com.vims.common.code.SysCodeMapper;

import lombok.AllArgsConstructor;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@AllArgsConstructor
public class SysCodeGroupService extends AbstractCommonService<SysCodeGroup> {
    private final SysCodeGroupMapper sysCodeGroupMapper;
    private final MessageSource messageSource;
    private final SysCodeMapper sysCodeMapper;

    private String getMessage(String code) {
        return messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
    }

    protected List<SysCodeGroup> findByGroupId(SysCodeGroup request) throws Exception {
        try {
            return sysCodeGroupMapper.SELECT(request);
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    @Override
    protected List<SysCodeGroup> selectPage(SysCodeGroup request) throws Exception {
        return sysCodeGroupMapper.SELECT_PAGE(request);
    }

    @Override
    protected int selectPagingTotalNumber(SysCodeGroup request) throws Exception {
        return sysCodeGroupMapper.SELECT_PAGING_TOTAL_NUMBER(request);
    }

    @Override
    protected List<SysCodeGroup> findImpl(SysCodeGroup request) throws Exception {
        return sysCodeGroupMapper.SELECT(request);
    }

    @Override
    protected int removeImpl(SysCodeGroup request) {
        List<SysCode> list = null;
        try {
            var sysCode = SysCode.builder().grp_id(request.getGrp_id()).build();
            list = sysCodeMapper.SELECT(sysCode);
            if (list.isEmpty()) {
                return sysCodeGroupMapper.DELETE(request);
            } else {
                throw new CustomException(getMessage("EXCEPTION.DELETE.EXIST.SBU_DATA"));
            }
        } catch (CustomException e) {
            throw e;
        }
    }

    @Override
    protected int updateImpl(SysCodeGroup request) {
        return sysCodeGroupMapper.UPDATE(request);
    }

    @Override
    protected int registerImpl(SysCodeGroup request) {
        return sysCodeGroupMapper.INSERT(request);
    }

    @Override
    protected int excelUploadImpl(MultipartFile file) throws Exception {
        return 0;
    }
}
