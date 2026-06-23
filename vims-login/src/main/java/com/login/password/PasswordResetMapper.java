package com.login.password;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @title : 비밀번호 찾기(재설정) 전용 매퍼
 * @text : 사용자 비밀번호 갱신, 대표이메일(공통코드) 및 비밀번호 정책 조회
 * @writer : 이경태
 */
@Mapper
public interface PasswordResetMapper {

    /** 이메일로 사용자 존재 여부 카운트 */
    int COUNT_USER_BY_EMAIL(@Param("email") String email);

    /** 비밀번호 갱신 (eml 기준) */
    int UPDATE_PASSWORD(@Param("email") String email, @Param("pwd") String pwd);

    /** 비밀번호 정책(PWD_POLICY site config) 조회 */
    List<Map<String, String>> SELECT_PWD_POLICY();
}
