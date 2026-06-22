/**
 *  ++ giens Product ++
 */
package com.vims.common.group;

import com.system.common.base.AbstractCommonService;
import com.system.common.exception.CustomException;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SysDeptGroupService extends AbstractCommonService<SysDeptGroup> {
    private final SysDeptGroupMapper sysDeptGroupMapper;
    private final MessageSource messageSource;

    private String getMessage(String code) {
        return messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
    }

    @Override
    protected List<SysDeptGroup> selectPage(SysDeptGroup request) throws Exception {
        return sysDeptGroupMapper.SELECT_PAGE(request);
    }

    @Override
    protected int selectPagingTotalNumber(SysDeptGroup request) throws Exception {
        return sysDeptGroupMapper.SELECT_PAGING_TOTAL_NUMBER(request);
    }

    @Override
    protected List<SysDeptGroup> findImpl(SysDeptGroup request) throws Exception {
        return sysDeptGroupMapper.SELECT(request);
    }

    protected List<SysDeptGroup> findNotExistsSysAccsGroupMenu(SysDeptGroup request) throws Exception {
        return sysDeptGroupMapper.SELECT_NOT_EXISTS_SYS_ACS_GRP_MENU(request);
    }

    @Override
    protected int removeImpl(SysDeptGroup request) throws Exception {
        List<SysDeptGroup> list = null;
        try {
            var sysDeptGroup = SysDeptGroup.builder().top_grp_id(request.getGrp_id()).build();
            list = sysDeptGroupMapper.SELECT(sysDeptGroup);
            if (list.isEmpty()) {
                return sysDeptGroupMapper.DELETE(request);
            } else {
                throw new CustomException(getMessage("EXCEPTION.DELETE.EXIST.SBU_DATA"));
            }
        } catch (CustomException e) {
            throw e;
        }
    }

    @Override
    protected int updateImpl(SysDeptGroup request) {
        return sysDeptGroupMapper.UPDATE(request);
    }

    @Override
    protected int registerImpl(SysDeptGroup request) {
        try {
            return sysDeptGroupMapper.INSERT(request);
        } catch (DuplicateKeyException dke) {
            throw new CustomException(getMessage("EXCEPTION.PK.EXIST"));
        }
    }

    @Override
    protected int excelUploadImpl(MultipartFile file) throws Exception {
        return 0;
    }
}
