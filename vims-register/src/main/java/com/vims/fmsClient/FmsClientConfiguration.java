package com.vims.fmsClient;

import feign.Logger;
import feign.Request;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * FMS Feign Client 설정
 * 
 * - 타임아웃 설정
 * - 재시도 정책
 * - 로깅 레벨
 * - 에러 핸들링
 */
@Configuration
// Recompile force
public class FmsClientConfiguration {

    /**
     * Feign 요청 타임아웃 설정
     * - 파일 업로드는 시간이 걸릴 수 있으므로 넉넉하게 설정
     */
    @Bean
    public Request.Options requestOptions() {
        return new Request.Options(
                30000, // 연결 타임아웃: 30초 (milliseconds)
                60000 // 읽기 타임아웃: 60초 (파일 업로드 고려, milliseconds)
        );
    }

    /**
     * 재시도 정책
     * - 네트워크 불안정 시 자동 재시도
     * - 최대 3번 재시도, 1초 간격으로 시도
     */
    @Bean
    public Retryer retryer() {
        return new Retryer.Default(
                1000, // 시작 간격: 1초
                3000, // 최대 간격: 3초
                3 // 최대 재시도 횟수: 3번
        );
    }

    /**
     * 로깅 레벨 설정
     * - 개발 환경: FULL (모든 요청/응답 로깅)
     * - 운영 환경: BASIC (상태 코드만 로깅)
     */
    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC; // 운영 환경에서는 BASIC으로 변경
    }

    /**
     * 에러 디코더
     * - FMS 서비스의 에러를 적절히 변환
     */
    @Bean
    public ErrorDecoder errorDecoder() {
        return (methodKey, response) -> {
            switch (response.status()) {
                case 400:
                    return new IllegalArgumentException("잘못된 파일 형식입니다.");
                case 401:
                case 403:
                    return new SecurityException("FMS 서비스 접근 권한이 없습니다.");
                case 404:
                    return new IllegalStateException("FMS 서비스를 찾을 수 없습니다.");
                case 500:
                    return new RuntimeException("FMS 서비스 내부 오류가 발생했습니다.");
                default:
                    return new RuntimeException("FMS 서비스 호출 중 오류가 발생했습니다.");
            }
        };
    }
}
