package com.system.auth.domain;

import com.system.auth.authuser.AuthUser;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Token {

    public Long id;

    public String token;

    public TokenType token_type = TokenType.AUTHORIZATION;

    public boolean revok;

    public boolean exp;

    public AuthUser auth_user;
}
