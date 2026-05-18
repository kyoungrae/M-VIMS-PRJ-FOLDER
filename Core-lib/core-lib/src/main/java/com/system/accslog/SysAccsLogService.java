/**
 *  ++ giens Product ++
 */
package com.system.accslog;

import java.util.List;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.system.common.base.AbstractCommonService;
import com.system.common.exception.CustomException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SysAccsLogService extends AbstractCommonService<SysAccsLog> {
    private final SysAccsLogMapper sysAccsLogMapper;
    private final MessageSource messageSource;

    private String getMessage(String code) {
        return messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
    }

    @Override
    protected List<SysAccsLog> selectPage(SysAccsLog request) throws Exception {
        try {
            return sysAccsLogMapper.SELECT_PAGE(request);
        } catch (Exception e) {
            throw new CustomException(getMessage("EXCEPTION.SELECT"));
        }
    }

    @Override
    protected int selectPagingTotalNumber(SysAccsLog request) throws Exception {
        try {
            return sysAccsLogMapper.SELECT_PAGING_TOTAL_NUMBER(request);
        } catch (Exception e) {
            throw new CustomException(getMessage("EXCEPTION.SELECT"));
        }
    }

    @Override
    protected List<SysAccsLog> findImpl(SysAccsLog request) throws Exception {
        try {
            return sysAccsLogMapper.SELECT(request);
        } catch (Exception e) {
            throw new CustomException(getMessage("EXCEPTION.SELECT"));
        }

    }

    @Override
    protected int removeImpl(SysAccsLog request) {
        try {
            return sysAccsLogMapper.DELETE(request);
        } catch (Exception e) {
            throw new CustomException(getMessage("EXCEPTION.REMOVE"));
        }
    }

    @Override
    protected int updateImpl(SysAccsLog request) {
        try {
            return sysAccsLogMapper.UPDATE(request);
        } catch (Exception e) {
            throw new CustomException(getMessage("EXCEPTION.UPDATE"));
        }
    }

    @Override
    protected int registerImpl(SysAccsLog request) {
        try {
            return sysAccsLogMapper.INSERT(request);
        } catch (DuplicateKeyException dke) {
            throw new CustomException(getMessage("EXCEPTION.PK.EXIST"));
        }

    }

    @Override
    protected int excelUploadImpl(MultipartFile file) throws Exception {
        try {
            // FMS 서비스의 엑셀 업로드 API 호출
            return 0;

        } catch (IllegalArgumentException e) {
            throw new CustomException(getMessage("EXCEPTION.FMS.INVALID_FILE_FORMAT"));
        } catch (SecurityException e) {
            throw new CustomException(getMessage("EXCEPTION.FMS.ACCESS_DENIED"));
        } catch (Exception e) {
            throw new CustomException(getMessage("EXCEPTION.FMS.UPLOAD_ERROR"));
        }
    }

    public void logAccess(SysAccsLog log) {
        try {
            sysAccsLogMapper.INSERT(log);
        } catch (Exception e) {
            e.printStackTrace();
            // Logging failure shouldn't block login
        }
    }

    public void logLogout(SysAccsLog log) {
        try {
            sysAccsLogMapper.UPDATE(log);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void logLogoutByUser(String userId) {
        try {
            SysAccsLog log = new SysAccsLog();
            log.setUser_id(userId);
            sysAccsLogMapper.UPDATE_LOGOUT_BY_USER(log);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}