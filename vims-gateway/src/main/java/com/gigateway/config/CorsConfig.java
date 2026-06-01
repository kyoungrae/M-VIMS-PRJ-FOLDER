package com.gigateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * Gateway CORS 설정
 * - withCredentials: true를 사용하는 요청을 위한 CORS 설정
 * - 허용 Origin은 cors.allowed-origins 프로퍼티(쉼표 구분)로 제어한다.
 *   운영 환경에서는 실제 서비스 도메인으로 지정 권장. 미설정 시 기본값 "*"(전체 허용).
 */
@Configuration
public class CorsConfig {

    @Value("${cors.allowed-origins:*}")
    private String allowedOrigins;

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();

        // 허용 Origin (프로퍼티로 제어, 기본 "*"). 운영 시 특정 도메인으로 제한 권장.
        for (String origin : allowedOrigins.split(",")) {
            String trimmed = origin.trim();
            if (!trimmed.isEmpty()) {
                corsConfig.addAllowedOriginPattern(trimmed);
            }
        }

        // 모든 HTTP 메서드 허용
        corsConfig.addAllowedMethod("*");

        // 모든 헤더 허용
        corsConfig.addAllowedHeader("*");

        // 자격증명(쿠키 등) 허용
        corsConfig.setAllowCredentials(true);

        // Preflight 요청 캐시 시간 (초)
        corsConfig.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}
