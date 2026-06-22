package com.gigateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;

@Component
public class AuthorizationHeaderGatewayFilterFactory
        extends AbstractGatewayFilterFactory<AuthorizationHeaderGatewayFilterFactory.Config> {
    private static final Logger logger = LoggerFactory.getLogger(AuthorizationHeaderGatewayFilterFactory.class);
    private final Environment env;

    public AuthorizationHeaderGatewayFilterFactory(Environment env) {
        super(Config.class);
        this.env = env;
    }

    public static class Config {

    }

    private static final String API_KEY_HEADER = "X-API-KEY";

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            try {
                ServerHttpRequest request = exchange.getRequest();
                String path = request.getURI().getPath();

                // 1. Public Endpoints (로그인, 회원가입, 정적 리소스 등 인증 불필요 경로)
                if (path.startsWith("/api/v1/auth") ||
                        path.startsWith("/common") ||
                        path.startsWith("/assets") || // 정적 리소스 추가
                        path.startsWith("/login") || // 로그인 페이지 추가
                        path.startsWith("/web") || // 대국민 홈페이지 추가 (로그인 없이 접근 가능)
                        path.startsWith("/error") ||
                        path.equals("/") ||
                        path.endsWith(".html") ||
                        path.endsWith(".css") ||
                        path.endsWith(".js") ||
                        path.endsWith(".ico") ||
                        path.endsWith(".png") ||
                        path.endsWith(".jpg") ||
                        path.endsWith(".svg")) {
                    return chain.filter(exchange);
                }

                // 설정값 로드 및 검증
                String systemApiKey = env.getProperty("gateway.api-key");
                String jwtSecret = env.getProperty("token.secret");

                if (systemApiKey == null || jwtSecret == null) {
                    logger.error(
                            "CRITICAL: Gateway configuration is missing. token.secret or gateway.api-key is NULL. Check application.yml");
                    return onError(exchange, "Internal Server Configuration Error", HttpStatus.INTERNAL_SERVER_ERROR);
                }

                // 2. API Key 인증 (시스템 간 통신용)
                if (request.getHeaders().containsKey(API_KEY_HEADER)) {
                    String reqApiKey = request.getHeaders().getFirst(API_KEY_HEADER);
                    if (systemApiKey.equals(reqApiKey)) {
                        ServerHttpRequest mutatedRequest = request.mutate()
                                .header("X-System-Auth", "true")
                                .header("X-User-Roles", "SYSTEM_ADMIN")
                                .build();
                        return chain.filter(exchange.mutate().request(mutatedRequest).build());
                    } else {
                        return onError(exchange, "Invalid API Key", HttpStatus.UNAUTHORIZED);
                    }
                }

                // 3. JWT 토큰 인증 (사용자 통신용)
                String jwt = null;

                // 3-1. Header에서 찾기
                if (request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    String authorizationHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
                    if (authorizationHeader != null && authorizationHeader.startsWith("Bearer")) {
                        // NOTE: prefix("Bearer")만 제거. replace 사용 시 토큰 내부의 동일 문자열까지 제거되는 문제 방지
                        jwt = authorizationHeader.substring("Bearer".length()).trim();
                    }
                }

                // 3-2. Cookie에서 찾기 (Header에 없을 경우)
                if (jwt == null && request.getCookies().containsKey("Authorization")) {
                    var cookie = request.getCookies().getFirst("Authorization");
                    if (cookie != null) {
                        String cookieVal = cookie.getValue();
                        // 쿠키 값에 Bearer prefix가 있을 수도 있고 없을 수도 있음.
                        // prefix("Bearer")가 있으면 그 부분만 제거하고, 없으면 값을 그대로 사용
                        if (cookieVal != null && cookieVal.startsWith("Bearer")) {
                            cookieVal = cookieVal.substring("Bearer".length());
                        }
                        jwt = (cookieVal != null) ? cookieVal.trim() : null;
                    }
                }

                // 토큰이 없으면 에러
                if (jwt == null || jwt.isEmpty()) {
                    return onError(exchange, "No Authorization header or cookie", HttpStatus.UNAUTHORIZED);
                }

                if (!isJwtValid(jwt, jwtSecret)) {
                    return onError(exchange, "JWT token is not valid", HttpStatus.UNAUTHORIZED);
                }

                try {
                    Key key = getSignInKey(jwtSecret);
                    Claims claims = Jwts.parserBuilder()
                            .setSigningKey(key)
                            .build()
                            .parseClaimsJws(jwt)
                            .getBody();
                    String userId = claims.getSubject();
                    String userRole = claims.get("role", String.class);
                    // ... (이후 로직 동일)

                    ServerHttpRequest mutatedRequest = request.mutate()
                            .header("X-User-Id", userId)
                            .header("X-User-Roles", userRole)
                            .build();
                    return chain.filter(exchange.mutate().request(mutatedRequest).build());
                } catch (Exception e) {
                    logger.error("JWT Parsing Error: ", e);
                    return onError(exchange, "JWT token error: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
                }
            } catch (Exception e) {
                logger.error("Unhandled Exception in AuthorizationHeaderGatewayFilterFactory: ", e);
                return onError(exchange, "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        };
    }

    private boolean isJwtValid(String jwt, String secret) {
        try {
            if (secret == null)
                return false;
            Key key = getSignInKey(secret);
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(jwt);
            return true;
        } catch (Exception e) {
            logger.warn("JWT Validation Failed: {}", e.getMessage());
            return false;
        }
    }

    private Key getSignInKey(String secret) {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        logger.error("Authorization Error: {} - Status: {}", err, httpStatus);
        return response.setComplete();
    }
}
