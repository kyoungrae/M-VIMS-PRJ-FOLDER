package com.vims.common.siteconfig;

import com.system.common.base.CommonMapper;
import com.system.common.util.passwordvalidation.PasswordPolicy;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface SysSiteConfigMapper extends CommonMapper<SysSiteConfig> {
    String[] SELECT_GROUP();

    PasswordPolicy SELECT_PWD_POLICY();
}
