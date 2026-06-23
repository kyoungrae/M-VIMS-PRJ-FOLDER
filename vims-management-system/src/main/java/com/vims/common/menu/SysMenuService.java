/**
 *  ++ giens Product ++
 */
package com.vims.common.menu;

import com.system.common.base.AbstractCommonService;
import com.system.common.exception.CustomException;
import com.system.common.util.userinfo.UserInfo;
import com.vims.common.accessgroupmenu.SysAccsGroupMenu;
import com.vims.common.accessgroupmenu.SysAccsGroupMenuMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SysMenuService extends AbstractCommonService<SysMenu> {
    private final SysMenuMapper sysMenuMapper;
    private final MessageSource messageSource;
    private final SysAccsGroupMenuMapper sysAccsGroupMenuMapper;

    private String getMessage(String code) {
        return messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
    }

    public List<SysMenu> findHierarchy(SysMenu request) throws Exception {
        return sysMenuMapper.SELECT_HIERARCHY(request);
    }

    public List<SysMenu> findAccessRightGroupForMenu(SysMenu request) throws Exception {
        String userEmail = UserInfo.getUserEmail();
        var sysMenu = SysMenu.builder().user_email(userEmail).build();
        return sysMenuMapper.SELECT_ACCESS_RIGHTS_GROUP_FOR_MENU(sysMenu);
    }

    @Override
    protected List<SysMenu> selectPage(SysMenu request) throws Exception {
        return sysMenuMapper.SELECT_PAGE(request);
    }

    @Override
    protected int selectPagingTotalNumber(SysMenu request) throws Exception {
        return sysMenuMapper.SELECT_PAGING_TOTAL_NUMBER(request);
    }

    @Override
    protected List<SysMenu> findImpl(SysMenu request) throws Exception {
        return sysMenuMapper.SELECT(request);
    }

    public int removeMenuCode(SysMenu request) throws Exception {
        var containTopMenuCode = SysMenu.builder()
                .top_menu_cd(request.getMenu_cd())
                .build();
        var containMenuCode = SysMenu.builder()
                .menu_cd(request.getMenu_cd())
                .menu_seq(request.getMenu_seq())
                .build();
        var containAccessRightGroupCode = SysAccsGroupMenu.builder()
                .menu_cd(request.getMenu_cd())
                .build();
        List<SysAccsGroupMenu> acList = sysAccsGroupMenuMapper.SELECT(containAccessRightGroupCode);
        List<SysMenu> list = sysMenuMapper.SELECT(containTopMenuCode);
        boolean childNodeExist = !list.isEmpty();
        boolean accessRightGroupExist = !acList.isEmpty();
        try {
            if (childNodeExist) {
                throw new CustomException(getMessage("EXCEPTION.DELETE.EXIST.SBU_DATA"));
            } else if (accessRightGroupExist) {
                throw new CustomException(getMessage("EXCEPTION.DELETE.EXIST.ACCESS_RIGHTS_GROUP_DATA"));
            } else {
                return sysMenuMapper.DELETE(containMenuCode);
            }
        } catch (CustomException ce) {
            throw ce;
        } catch (Exception e) {
            throw new Exception("FAIL TO REMOVE MENU", e);
        }
    }

    @Override
    protected int removeImpl(SysMenu request) {
        try {
            return sysMenuMapper.DELETE(request);
        } catch (Exception e) {
            throw new CustomException(getMessage("EXCEPTION.PK.EXIST.USER"));
        }
    }

    @Override
    protected int updateImpl(SysMenu request) {
        try {
            return sysMenuMapper.UPDATE(request);
        } catch (Exception e) {
            throw new CustomException(getMessage(""));
        }
    }

    @Override
    protected int registerImpl(SysMenu request) throws Exception {
        try {
            // 메뉴 고유번호(menu_unq_no = menu_seq)는 복합 PK 의 일부이므로,
            // 등록 시 값이 없으면 현재 최대값 + 1 로 자동 채번한다.
            if (request.getMenu_seq() == null || request.getMenu_seq().trim().isEmpty()) {
                Integer maxNo = sysMenuMapper.SELECT_MAX_MENU_UNQ_NO();
                request.setMenu_seq(String.valueOf((maxNo == null ? 0 : maxNo) + 1));
            }
            return sysMenuMapper.INSERT(request);
        } catch (DuplicateKeyException dke) {
            throw new CustomException(getMessage("EXCEPTION.PK.EXIST"));
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    @Override
    protected int excelUploadImpl(MultipartFile file) throws Exception {
        return 0;
    }
}
