/**
 *  ++ giens Product ++
 */
package com.vims.common.usergroup;

import com.system.common.base.AbstractCommonService;
import com.system.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SysUserGroupService extends AbstractCommonService<SysUserGroup> {
    private final SysUserGroupMapper sysUserGroupMapper;
    private final MessageSource messageSource;

    private String getMessage(String code) {
        return messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
    }

    @Override
    protected List<SysUserGroup> selectPage(SysUserGroup request) throws Exception {
        return sysUserGroupMapper.SELECT_PAGE(request);
    }

    @Override
    protected int selectPagingTotalNumber(SysUserGroup request) throws Exception {
        return sysUserGroupMapper.SELECT_PAGING_TOTAL_NUMBER(request);
    }

    @Override
    protected List<SysUserGroup> findImpl(SysUserGroup request) throws Exception {
        return sysUserGroupMapper.SELECT(request);
    }

    @Override
    public int removeImpl(SysUserGroup request) {
        return sysUserGroupMapper.DELETE(request);
    }

    @Override
    protected int updateImpl(SysUserGroup request) {
        return sysUserGroupMapper.UPDATE(request);
    }

    @Override
    protected int registerImpl(SysUserGroup request) throws Exception {
        int rtn = 0;
        try {
            rtn = sysUserGroupMapper.INSERT(request);
        } catch (DuplicateKeyException dke) {
            throw new CustomException(getMessage("EXCEPTION.PK.EXIST.USER"));
        }
        return rtn;
    }

    public Map<String, List<?>> findJoinSysUserGroupPage(SysUserGroup request) throws Exception {
        List<SysUserGroup> list = new ArrayList<>();
        Map<String, List<?>> result = new HashMap<>();
        int pagingNum;
        try {
            list = selectJoinSysUserGroupPage(request);
            pagingNum = selectJoinSysUserGroupPagingTotalNumber(request);

            List<Integer> pagingList = new ArrayList<>();
            pagingList.add(pagingNum);

            result.put("DATA", list);
            result.put("TOTAL_PAGING", pagingList);
        } catch (Exception e) {
            throw new Exception(e);
        }
        return result;
    }

    protected List<SysUserGroup> selectJoinSysUserGroupPage(SysUserGroup request) throws Exception {
        try {
            return sysUserGroupMapper.SELECT_JOIN_SYS_USER_GRP_PAGE(request);
        } catch (Exception e) {
            e.printStackTrace();
            ;
        }
        return null;
    }

    protected int selectJoinSysUserGroupPagingTotalNumber(SysUserGroup request) throws Exception {
        return sysUserGroupMapper.SELECT_JOIN_SYS_USER_GRP_PAGING_TOTAL_NUMBER(request);
    }

    @Override
    protected int excelUploadImpl(MultipartFile arg0) throws Exception {
        return 0;
    }
}
