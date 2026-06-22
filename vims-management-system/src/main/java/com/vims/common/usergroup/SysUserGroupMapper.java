package com.vims.common.usergroup;

import com.system.common.base.CommonMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysUserGroupMapper extends CommonMapper<SysUserGroup> {
    int INSERT_OR_UPDATE(SysUserGroup vo) throws Exception;
    List<SysUserGroup> SELECT_BY_GRP_ID_LIST(@Param("targetGroups") List<String> targetGroups) throws Exception;
    List<SysUserGroup> SELECT_JOIN_SYS_USER_GRP_PAGE(SysUserGroup vo);
    int SELECT_JOIN_SYS_USER_GRP_PAGING_TOTAL_NUMBER(SysUserGroup vo);
}
