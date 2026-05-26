package com.system.auth.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.system.auth.token.TokenService;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

@Component
@RequiredArgsConstructor // 선언하는 최종 필드를 사용하여 생성자를 생성 ex)private final String mystring 같은 생성자를 의미함
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final TokenService tokenService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        final Cookie[] cookies = request.getCookies();
        final String jwt;
        String userEmail;

        if (cookies == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // 'Authorization' 이름의 쿠키를 찾습니다.
        Optional<Cookie> optionalCookie = Arrays.stream(cookies)
                .filter(cookie -> "Authorization".equals(cookie.getName()))
                .findFirst();

        if (optionalCookie.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = optionalCookie.get().getValue();
        try {
            userEmail = jwtService.extractUsername(jwt); // JWT에서 사용자 이메일을 추출합니다.
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            Cookie cookie = new Cookie("Authorization", null);
            cookie.setMaxAge(0);
            cookie.setPath("/");
            response.addCookie(cookie);
            filterChain.doFilter(request, response);
            return;
        } catch (Exception e) {
            filterChain.doFilter(request, response);
            return;
        }
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            var isTokenValid = tokenService.findByToken(jwt)
                    .map(t -> !t.isExp() && !t.isRevok())
                    .orElse(false);

            if (jwtService.isTokenValid(jwt, userDetails) && isTokenValid) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } else {
                // 인증 실패 시 쿠키를 삭제합니다.
                Cookie cookie = new Cookie("Authorization", null);
                cookie.setMaxAge(0);
                cookie.setPath("/");
                response.addCookie(cookie);
            }
        }
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        return path.startsWith("/common/") || path.startsWith("/assets/") || path.startsWith("/favicon.ico")
                || path.startsWith("/error");
    }
}
