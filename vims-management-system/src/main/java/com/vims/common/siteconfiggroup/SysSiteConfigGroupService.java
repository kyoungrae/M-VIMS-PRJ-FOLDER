/**
 *  ++ giens Product ++
 */
package com.vims.common.siteconfiggroup;

import com.system.common.base.AbstractCommonService;
import com.system.common.exception.CustomException;
import com.vims.common.siteconfig.SysSiteConfig;
import com.vims.common.siteconfig.SysSiteConfigMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SysSiteConfigGroupService extends AbstractCommonService<SysSiteConfigGroup> {
    private final SysSiteConfigGroupMapper sysSiteConfigGroupMapper;
    private final MessageSource messageSource;
    private final SysSiteConfigMapper sysSiteConfigMapper;

    private String getMessage(String code) {
        return messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
    }

    @Override
    protected List<SysSiteConfigGroup> selectPage(SysSiteConfigGroup request) throws Exception {
        try {
            return sysSiteConfigGroupMapper.SELECT_PAGE(request);
        } catch (Exception e) {
            throw new CustomException(getMessage(""));
        }
    }

    @Override
    protected int selectPagingTotalNumber(SysSiteConfigGroup request) throws Exception {
        try {
            return sysSiteConfigGroupMapper.SELECT_PAGING_TOTAL_NUMBER(request);
        } catch (Exception e) {
            throw new CustomException(getMessage(""));
        }
    }

    @Override
    protected List<SysSiteConfigGroup> findImpl(SysSiteConfigGroup request) throws Exception {
        try {
            return sysSiteConfigGroupMapper.SELECT(request);
        } catch (Exception e) {
            throw new CustomException(getMessage(""));
        }

    }

    @Override
    protected int removeImpl(SysSiteConfigGroup request) {
        List<SysSiteConfig> list = null;
        try {
            var isExitParam = SysSiteConfig.builder().cfg_grp_id(request.getCfg_grp_id()).build();
            list = sysSiteConfigMapper.SELECT(isExitParam);
            if (list.isEmpty()) {
                return sysSiteConfigGroupMapper.DELETE(request);
            } else {
                throw new CustomException(getMessage("EXCEPTION.DELETE.EXIST.SBU_DATA"));
            }
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    protected int updateImpl(SysSiteConfigGroup request) {
        try {
            return sysSiteConfigGroupMapper.UPDATE(request);
        } catch (Exception e) {
            throw new CustomException(getMessage(""));
        }
    }

    @Override
    protected int registerImpl(SysSiteConfigGroup request) {
        try {
            return sysSiteConfigGroupMapper.INSERT(request);
        } catch (DuplicateKeyException dke) {
            throw new CustomException(getMessage("EXCEPTION.PK.EXIST"));
        } catch (Exception e) {
            throw e;
        }

    }

    @Override
    protected int excelUploadImpl(MultipartFile file) throws Exception {
        return 0;
    }
}
