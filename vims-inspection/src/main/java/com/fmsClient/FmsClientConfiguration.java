package com.fmsClient;

import feign.Logger;
import feign.Request;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FmsClientConfiguration {

    @Bean
    public Request.Options requestOptions() {
        return new Request.Options(30000, 60000);
    }

    @Bean
    public Retryer retryer() {
        return new Retryer.Default(1000, 3000, 3);
    }

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return (methodKey, response) -> switch (response.status()) {
            case 400 -> new IllegalArgumentException("잘못된 파일 형식입니다.");
            case 401, 403 -> new SecurityException("FMS 서비스 접근 권한이 없습니다.");
            case 404 -> new IllegalStateException("FMS 서비스를 찾을 수 없습니다.");
            case 500 -> new RuntimeException("FMS 서비스 내부 오류가 발생했습니다.");
            default -> new RuntimeException("FMS 서비스 호출 중 오류가 발생했습니다.");
        };
    }
}
