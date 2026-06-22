package com.system.auth.token;

import com.system.auth.domain.Token;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TokenService {
    private final TokenMapper tokenMapper;

    public Optional<Token> findByToken(String token) {
        return tokenMapper.SELECT_TOKEN(token);
    }

    public List<Token> findByAllToken(Token request) {
        return tokenMapper.SELECT_ALL_TOKEN(request);
    }

    public int save(Token token) {
        return tokenMapper.INSERT_TOKEN(token);
    }

    public int update(Token token) {
        return tokenMapper.UPDATE_TOKEN(token);
    }

    public int deleteExpiredTokens(Long userId) {
        return tokenMapper.DELETE_EXP_TOKEN_BY_USER_ID(userId);
    }

    public int revokeAllUserTokens(Long userId) {
        return tokenMapper.REVOKE_ALL_USER_TOKENS(userId);
    }
}
