package com.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 * Gateway를 통하지 않은 직접 접근(8086 포트)을 감지하여 Gateway URL로 리다이렉트하는 필터.
 */
@Component
public class DirectAccessFilter implements Filter {

    @Value("${gateway.url:http://localhost:8080}")
    private String gatewayUrl;

    @Value("${server.port:8086}")
    private String serverPort;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String forwardedPort = httpRequest.getHeader("X-Forwarded-Port");
        String forwardedHost = httpRequest.getHeader("X-Forwarded-Host");

        // Gateway를 거쳐온 요청이면 통과
        if (StringUtils.hasText(forwardedPort) || StringUtils.hasText(forwardedHost)) {
            chain.doFilter(request, response);
            return;
        }

        String host = httpRequest.getHeader("Host");
        String uri = httpRequest.getRequestURI();

        // 직접 접속이면 Gateway로 리다이렉트
        if (StringUtils.hasText(host) && host.contains(":" + serverPort)
                && !uri.startsWith("/common") && !uri.startsWith("/static") && !uri.startsWith("/favicon")) {
            httpResponse.sendRedirect(gatewayUrl + uri);
            return;
        }

        chain.doFilter(request, response);
    }
}
