package com.system.auth.service;

import com.system.auth.authuser.AuthUser;
import com.system.auth.authuser.AuthUserService;
import com.system.auth.authuser.Role;
import com.system.auth.domain.Token;
import com.system.auth.domain.TokenType;
import com.system.auth.dto.AuthenticationRequest;
import com.system.auth.dto.AuthenticationResponse;
import com.system.auth.dto.RegisterRequest;
import com.system.auth.jwt.JwtService;
import com.system.auth.token.TokenService;
import com.system.common.util.userinfo.UserInfo;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import java.util.List;

@Service
@AllArgsConstructor
public class AuthenticationService {
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final AuthUserService AuthUserService;
    private final SequenceService sequenceService;

    @Transactional(rollbackFor = Exception.class)
    public AuthenticationResponse register(RegisterRequest request) throws Exception {
        UserInfo userInfo = new UserInfo();
        LocalDateTime localDateTime = LocalDateTime.now();
        Timestamp timestamp = Timestamp.valueOf(localDateTime);

        Long token_seq = sequenceService.selectTokenSequence();
        var user = AuthUser.builder()
                .id(token_seq)
                .user_id(request.getUser_id())
                .email(request.getEmail())
                .pwd(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .sys_crt_usr_id(userInfo.getUserEmail())
                .sys_crt_dt(timestamp)
                .build();
        AuthUserService.save(user);
        var jwtToken = jwtService.generateToken(user);
        var token = Token.builder()
                .id(token_seq)
                .token(jwtToken)
                .token_type(TokenType.AUTHORIZATION)
                .exp(false)
                .revok(false)
                .auth_user(user)
                .build();
        tokenService.save(token);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    @Transactional(rollbackFor = Exception.class)
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        try {
            var authToken = new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());

            var authentication = authenticationManager.authenticate(authToken);

            SecurityContextHolder.getContext().setAuthentication(authentication);
            var user = (AuthUser) authentication.getPrincipal();

            // 유효한 토큰 조회
            var tokenParam = Token.builder()
                    .auth_user(user)
                    .exp(false)
                    .revok(false)
                    .build();
            List<Token> validTokens = tokenService.findByAllToken(tokenParam);

            String jwtToken = null;
            if (validTokens != null && !validTokens.isEmpty()) {
                for (Token validToken : validTokens) {
                    // 실제 유효성 검증 (리다이렉트 없음)
                    if (jwtService.isTokenValid(validToken.getToken(), user)) {
                        jwtToken = validToken.getToken();
                        break;
                    } else {
                        // DB 상으론 유효한데 실제론 만료된 경우 -> 업데이트
                        validToken.setExp(true);
                        validToken.setRevok(true);
                        tokenService.update(validToken);
                    }
                }
            }

            if (jwtToken == null) {
                // 기존 유효한 토큰들을 모두 무효화 (EXP=1, REVOK=1)
                tokenService.revokeAllUserTokens(user.getId());

                jwtToken = jwtService.generateToken(user);

                // 새로운 토큰을 데이터베이스에 저장
                Long token_seq = sequenceService.selectTokenSequence();
                var token = Token.builder()
                        .id(token_seq)
                        .token(jwtToken)
                        .token_type(TokenType.AUTHORIZATION)
                        .exp(false)
                        .revok(false)
                        .auth_user(user)
                        .build();
                tokenService.save(token);
            }

            return AuthenticationResponse.builder()
                    .token(jwtToken)
                    .build();
        } catch (Exception e) {
            e.printStackTrace(); // 예외 로그 확인
            throw e; // 혹은 ResponseEntity.status(401).body("인증 실패")
        }
    }
}
