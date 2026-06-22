package com.system.common.eventlog;

import com.system.common.base.AbstractCommonService;
import com.system.common.exception.CustomException;
import lombok.RequiredArgsConstructor;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SysEventLogService extends AbstractCommonService<SysEventLog> {
    private final SysEventLogMapper sysEventLogMapper;
    private final MessageSource messageSource;

    private String getMessage(String code) {
        return messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
    }

    @Override
    protected List<SysEventLog> selectPage(SysEventLog request) throws Exception {
        try {
            return sysEventLogMapper.SELECT_PAGE(request);
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(getMessage("EXCEPTION.SELECT"));
        }
    }

    @Override
    protected int selectPagingTotalNumber(SysEventLog request) throws Exception {
        try {
            return sysEventLogMapper.SELECT_PAGING_TOTAL_NUMBER(request);
        } catch (Exception e) {
            throw new CustomException(getMessage("EXCEPTION.SELECT"));
        }
    }

    @Override
    protected List<SysEventLog> findImpl(SysEventLog request) throws Exception {
        try {
            return sysEventLogMapper.SELECT(request);
        } catch (Exception e) {
            throw new CustomException(getMessage("EXCEPTION.SELECT"));
        }

    }

    @Override
    protected int removeImpl(SysEventLog request) {
        try {
            return sysEventLogMapper.DELETE(request);
        } catch (Exception e) {
            throw new CustomException(getMessage("EXCEPTION.REMOVE"));
        }
    }

    @Override
    protected int updateImpl(SysEventLog request) {
        try {
            return sysEventLogMapper.UPDATE(request);
        } catch (Exception e) {
            throw new CustomException(getMessage("EXCEPTION.UPDATE"));
        }
    }

    // 로그 기록에서 제외할 테이블 목록
    private static final List<String> EXCLUDED_TABLES = Arrays.asList(
            "sysAccsLog");

    @Override
    protected int registerImpl(SysEventLog request) {
        // 제외 대상 테이블인지 확인
        if (isExcludedTable(request.getTgt_tbl())) {
            return 0;
        }

        try {
            return sysEventLogMapper.INSERT(request);
        } catch (DuplicateKeyException dke) {
            throw new CustomException(getMessage("EXCEPTION.PK.EXIST"));
        }

    }

    private boolean isExcludedTable(String tableName) {
        if (tableName == null || tableName.isEmpty()) {
            return false;
        }
        return EXCLUDED_TABLES.stream()
                .anyMatch(excluded -> excluded.equalsIgnoreCase(tableName));
    }

    @Override
    protected int excelUploadImpl(MultipartFile file) throws Exception {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'excelUploadImpl'");
    }

}
