/**
 *  ++ giens Product ++
 */
package com.system.auth.authuser;

import java.util.Optional;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthUserService{
    private final AuthUserMapper authUserMapper;

    public Optional<AuthUser> findByUserName(String email) {
        AuthUser user = AuthUser.builder().email(email).build();
        return authUserMapper.SELECT_USER_INFO(user);
    }
    public int save(AuthUser authUser){
        return authUserMapper.INSERT_USER_INFO(authUser);
    }
}