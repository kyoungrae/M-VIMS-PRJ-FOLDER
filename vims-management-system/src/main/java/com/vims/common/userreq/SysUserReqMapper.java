package com.vims.common.userreq;

import com.system.common.base.CommonMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SysUserReqMapper extends CommonMapper<SysUserReq> {
    List<SysUserReq> SELECT_PAGE(SysUserReq vo);

    int SELECT_PAGING_TOTAL_NUMBER(SysUserReq vo);
}
