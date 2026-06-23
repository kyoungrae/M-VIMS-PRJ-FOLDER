package com.vims.common.menu;

import com.system.common.base.CommonMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SysMenuMapper extends CommonMapper<SysMenu> {
    List<SysMenu> SELECT_HIERARCHY(SysMenu request);

    List<SysMenu> SELECT_ACCESS_RIGHTS_GROUP_FOR_MENU(SysMenu request);

    /** 메뉴 고유번호(menu_unq_no) 현재 최대값 (채번용) */
    Integer SELECT_MAX_MENU_UNQ_NO();
}