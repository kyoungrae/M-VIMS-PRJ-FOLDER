package com.system.auth.mapper;

import com.system.auth.authuser.AuthUser;
import com.system.common.base.CommonMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SequenceMapper extends CommonMapper<AuthUser> {
    int INCREMENT_TOKEN_ID();

    Long SELECT_NEXT_TOKEN_ID();
}
