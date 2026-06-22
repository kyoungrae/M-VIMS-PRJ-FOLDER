package com.system.auth.service;

import com.system.auth.token.TokenService;
import com.system.accslog.SysAccsLogService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LogoutService implements LogoutHandler {

    private final TokenService tokenService;
    private final SysAccsLogService sysAccsLogService;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        final Cookie[] cookies = request.getCookies();

        // Handle case when no cookies are present
        if (cookies == null || cookies.length == 0) {
            return;
        }

        // Find Authorization cookie
        Optional<Cookie> optionalCookie = Arrays.stream(cookies)
                .filter(cookie -> "Authorization".equals(cookie.getName()))
                .findFirst();

        // Handle case when Authorization cookie is not found
        if (optionalCookie.isEmpty()) {
            return;
        }

        final String jwt = optionalCookie.get().getValue();

        // Find and invalidate the token in database
        var storedToken = tokenService.findByToken(jwt)
                .orElse(null);
        if (storedToken != null) {

            // Log logout event
            try {
                if (storedToken.getAuth_user() != null) {
                    sysAccsLogService.logLogoutByUser(storedToken.getAuth_user().getUser_id());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            SecurityContextHolder.clearContext();
        }
    }
}
