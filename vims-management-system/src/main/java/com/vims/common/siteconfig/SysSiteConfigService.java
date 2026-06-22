package com.vims.common.siteconfig;

import com.system.common.base.AbstractCommonService;
import com.system.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SysSiteConfigService extends AbstractCommonService<SysSiteConfig> {
    private final SysSiteConfigMapper sysSiteConfigMapper;
    private final MessageSource messageSource;

    private String getMessage(String code) {
        return messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
    }

    @Override
    protected List<SysSiteConfig> selectPage(SysSiteConfig request) throws Exception {
        return sysSiteConfigMapper.SELECT_PAGE(request);
    }

    @Override
    protected int selectPagingTotalNumber(SysSiteConfig request) throws Exception {
        return sysSiteConfigMapper.SELECT_PAGING_TOTAL_NUMBER(request);
    }

    @Override
    public List<SysSiteConfig> findImpl(SysSiteConfig request) throws Exception {
        return sysSiteConfigMapper.SELECT(request);
    }

    @Override
    @CacheEvict(value = "passwordPolicy", allEntries = true)
    protected int removeImpl(SysSiteConfig request) {
        return sysSiteConfigMapper.DELETE(request);
    }

    @Override
    @CacheEvict(value = "passwordPolicy", allEntries = true)
    protected int updateImpl(SysSiteConfig request) {
        return sysSiteConfigMapper.UPDATE(request);
    }

    @Override
    @CacheEvict(value = "passwordPolicy", allEntries = true)
    protected int registerImpl(SysSiteConfig request) throws Exception {
        try {
            return sysSiteConfigMapper.INSERT(request);
        } catch (DuplicateKeyException dke) {
            throw new CustomException(getMessage("EXCEPTION.PK.EXIST"));
        }
    }

    @Override
    protected int excelUploadImpl(MultipartFile file) throws Exception {
        return 0;
    }
}
