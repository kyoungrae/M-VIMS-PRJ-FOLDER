package com.login;

import com.system.auth.dto.AuthenticationRequest;
import com.system.auth.dto.AuthenticationResponse;
import com.system.auth.jwt.JwtService;
import com.system.auth.service.AuthenticationService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;

import com.system.accslog.SysAccsLog;
import com.system.accslog.SysAccsLogService;
import com.system.auth.authuser.AuthUser;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class LoginController {
    private final AuthenticationService authenticationService;
    private final JwtService jwtService;

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    // 비밀번호 찾기 전용 재설정 페이지 (비인증 접근 가능: /api/v1/auth/** 화이트리스트)
    // 실제 재설정은 재설정 토큰 검증으로만 허용된다.
    @GetMapping("/api/v1/auth/password/reset-page")
    public String passwordResetPage() {
        return "login/passwordReset";
    }

    @GetMapping("")
    public String loginPage(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return "login/login";
        } else {
            Cookie[] cookies = request.getCookies();
            Optional<Cookie> optionalCookie = Arrays.stream(cookies)
                    .filter(cookie -> "Authorization".equals(cookie.getName())).findFirst();

            if (optionalCookie.isEmpty()) {
                return "login/login";
            }
            String jwt = optionalCookie.get().getValue();
            String userEmail;
            try {
                userEmail = jwtService.extractUsername(jwt);
            } catch (Exception e) {
                return "redirect:/api/v1/auth/logout";
            }
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() != null) {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null && authentication.isAuthenticated()
                        && !(authentication instanceof AnonymousAuthenticationToken)) {
                    return "layout/home";
                } else {
                    return "redirect:/api/v1/auth/logout";
                }
            } else {
                return "redirect:/api/v1/auth/logout";
            }
        }
    }

    private final SysAccsLogService sysAccsLogService;

    @PostMapping("login")
    public ResponseEntity<?> login(@RequestBody Map<String, Object> param, HttpServletResponse response,
            HttpServletRequest request) {
        AuthenticationRequest ar = AuthenticationRequest.builder()
                .email((String) param.get("email"))
                .password((String) param.get("password"))
                .build();
        AuthenticationResponse authenticationResponse = authenticationService.authenticate(ar);

        String jwtToken = authenticationResponse.getToken();
        Cookie authrizationCookie = new Cookie("Authorization", jwtToken);
        authrizationCookie.setHttpOnly(false);
        authrizationCookie.setSecure(false);
        authrizationCookie.setPath("/"); // 쿠키 경로 설정
        response.addCookie(authrizationCookie);

        // Log Access
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof AuthUser) {
                AuthUser user = (AuthUser) authentication.getPrincipal();

                String userAgent = request.getHeader("User-Agent");
                String ip = request.getHeader("X-Forwarded-For");
                if (ip == null)
                    ip = request.getRemoteAddr();

                SysAccsLog log = new SysAccsLog();
                log.setId(UUID.randomUUID().toString());
                log.setUser_id(user.getUser_id());
                log.setEmail(user.getEmail());
                log.setIp_addr(ip);

                // Simple User-Agent parsing
                String os = "Unknown";
                if (userAgent.toLowerCase().contains("windows"))
                    os = "Windows";
                else if (userAgent.toLowerCase().contains("mac"))
                    os = "Mac OS";
                else if (userAgent.toLowerCase().contains("x11"))
                    os = "Unix";
                else if (userAgent.toLowerCase().contains("android"))
                    os = "Android";
                else if (userAgent.toLowerCase().contains("iphone"))
                    os = "iOS";
                log.setOs_nm(os);

                String browser = "Unknown";
                if (userAgent.toLowerCase().contains("edg"))
                    browser = "Edge";
                else if (userAgent.toLowerCase().contains("chrome"))
                    browser = "Chrome";
                else if (userAgent.toLowerCase().contains("firefox"))
                    browser = "Firefox";
                else if (userAgent.toLowerCase().contains("safari"))
                    browser = "Safari";
                log.setBrwsr_nm(browser);

                log.setDev_type("PC"); // Default or infer from UA
                if (userAgent.toLowerCase().contains("mobile"))
                    log.setDev_type("Mobile");

                sysAccsLogService.logAccess(log);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 리다이렉트 URL 포함
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("redirectUrl", "/");

        // 사용자 이름 추가 (예외 처리 포함)
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof AuthUser) {
                AuthUser user = (AuthUser) authentication.getPrincipal();
                responseBody.put("userName", user.getUser_nm());
            }
        } catch (Exception e) {
            logger.error("Failed to retrieve user name during login", e);
            // 사용자 이름 조회 실패해도 로그인은 성공 처리 (userName은 null 또는 없음)
        }

        return new ResponseEntity<>(responseBody, HttpStatus.OK);
    }
}