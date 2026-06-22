/**
 *  ++ giens Product ++
 */
package com.vims.common.accessgroupmenu;

import com.system.common.base.AbstractCommonService;
import com.system.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SysAccsGroupMenuService extends AbstractCommonService<SysAccsGroupMenu> {
    private final SysAccsGroupMenuMapper sysAccsGroupMenuMapper;
    private final MessageSource messageSource;

    private String getMessage(String code) {
        return messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
    }

    @Override
    protected List<SysAccsGroupMenu> selectPage(SysAccsGroupMenu request) throws Exception {
        try {
            return sysAccsGroupMenuMapper.SELECT_PAGE(request);
        } catch (Exception e) {
            throw new CustomException(getMessage("EXCEPTION.SELECT"));
        }
    }

    @Override
    protected int selectPagingTotalNumber(SysAccsGroupMenu request) throws Exception {
        try {
            return sysAccsGroupMenuMapper.SELECT_PAGING_TOTAL_NUMBER(request);
        } catch (Exception e) {
            throw new CustomException(getMessage("EXCEPTION.SELECT"));
        }
    }

    @Override
    protected List<SysAccsGroupMenu> findImpl(SysAccsGroupMenu request) throws Exception {
        try {
            return sysAccsGroupMenuMapper.SELECT(request);
        } catch (Exception e) {
            throw new CustomException(getMessage("EXCEPTION.SELECT"));
        }

    }

    @Override
    protected int removeImpl(SysAccsGroupMenu request) {
        try {
            return sysAccsGroupMenuMapper.DELETE(request);
        } catch (Exception e) {
            throw new CustomException(getMessage("EXCEPTION.REMOVE"));
        }
    }

    @Override
    protected int updateImpl(SysAccsGroupMenu request) {
        try {
            return sysAccsGroupMenuMapper.UPDATE(request);
        } catch (Exception e) {
            throw new CustomException(getMessage("EXCEPTION.UPDATE"));
        }
    }

    @Override
    protected int registerImpl(SysAccsGroupMenu request) {
        try {
            return sysAccsGroupMenuMapper.INSERT(request);
        } catch (Exception e) {
            throw new CustomException(getMessage("EXCEPTION.REGIST"));
        }

    }

    @Override
    protected int excelUploadImpl(MultipartFile file) throws Exception {
        return 0;
    }
}
