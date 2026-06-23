package com.login.password;

import com.login.mail.MailService;
import com.system.common.exception.CustomException;
import com.system.common.util.passwordvalidation.PasswordPolicy;
import com.system.common.util.passwordvalidation.PasswordValidationUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @title : 비밀번호 찾기(재설정) 서비스
 * @text : 인증코드 생성/발송/검증, 단기 재설정 토큰 발급, 정책 검증 후 비밀번호 갱신
 *         코드와 토큰은 서버 메모리에 저장하여 서버에서만 검증한다.
 * @writer : 이경태
 */
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private static final Logger logger = LoggerFactory.getLogger(PasswordResetService.class);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final PasswordResetMapper passwordResetMapper;
    private final MailService mailService;
    private final PasswordEncoder passwordEncoder;
    private final MessageSource messageSource;

    @Value("${app.password-reset.code-ttl-seconds:180}")
    private long codeTtlSeconds;
    @Value("${app.password-reset.token-ttl-seconds:300}")
    private long tokenTtlSeconds;
    @Value("${app.password-reset.max-verify-attempts:5}")
    private int maxVerifyAttempts;
    @Value("${app.password-reset.resend-interval-seconds:30}")
    private long resendIntervalSeconds;

    // email -> 인증코드 엔트리
    private final Map<String, CodeEntry> codeStore = new ConcurrentHashMap<>();
    // resetToken -> 토큰 엔트리
    private final Map<String, TokenEntry> tokenStore = new ConcurrentHashMap<>();

    /* ===================== 1. 인증코드 요청 ===================== */
    public void requestCode(String email) {
        if (email == null || email.isBlank()) {
            throw new CustomException(getMessage("EXCEPTION.NOT.FOUND.USER"));
        }
        email = email.trim();

        // 사용자 존재 확인
        if (passwordResetMapper.COUNT_USER_BY_EMAIL(email) <= 0) {
            throw new CustomException(getMessage("EXCEPTION.NOT.FOUND.USER"));
        }

        // 재발송 간격 제한
        CodeEntry existing = codeStore.get(email);
        long now = System.currentTimeMillis();
        if (existing != null && (now - existing.createdAt) < resendIntervalSeconds * 1000L) {
            throw new CustomException(getMessage("EXCEPTION.PWD.RESET.CODE.TOO_FREQUENT"));
        }

        String code = generateCode();
        codeStore.put(email, new CodeEntry(code, now, now + codeTtlSeconds * 1000L));

        boolean sent = mailService.sendHtmlMail(
                email,
                "[M-VIMS] 비밀번호 재설정 인증코드",
                buildMailBody(code));

        if (!sent) {
            codeStore.remove(email);
            throw new CustomException(getMessage("EXCEPTION.PWD.RESET.CODE.SEND_FAIL"));
        }
    }

    /* ===================== 2. 인증코드 검증 ===================== */
    public String verifyCode(String email, String code) {
        if (email == null || email.isBlank() || code == null || code.isBlank()) {
            throw new CustomException(getMessage("EXCEPTION.PWD.RESET.CODE.MISMATCH"));
        }
        email = email.trim();

        CodeEntry entry = codeStore.get(email);
        if (entry == null) {
            throw new CustomException(getMessage("EXCEPTION.PWD.RESET.CODE.NOT_REQUESTED"));
        }
        long now = System.currentTimeMillis();
        if (now > entry.expireAt) {
            codeStore.remove(email);
            throw new CustomException(getMessage("EXCEPTION.PWD.RESET.CODE.EXPIRED"));
        }
        if (entry.attempts >= maxVerifyAttempts) {
            codeStore.remove(email);
            throw new CustomException(getMessage("EXCEPTION.PWD.RESET.CODE.ATTEMPTS_EXCEEDED"));
        }
        entry.attempts++;
        if (!entry.code.equals(code.trim())) {
            throw new CustomException(getMessage("EXCEPTION.PWD.RESET.CODE.MISMATCH"));
        }

        // 검증 성공 → 코드 폐기, 단기 재설정 토큰 발급
        codeStore.remove(email);
        String token = UUID.randomUUID().toString().replace("-", "");
        tokenStore.put(token, new TokenEntry(email, now + tokenTtlSeconds * 1000L));
        return token;
    }

    /* ===================== 3. 비밀번호 재설정 ===================== */
    public void resetPassword(String token, String newPassword, String newPasswordConfirm) {
        if (token == null || token.isBlank()) {
            throw new CustomException(getMessage("EXCEPTION.PWD.RESET.TOKEN.INVALID"));
        }
        TokenEntry entry = tokenStore.get(token);
        if (entry == null) {
            throw new CustomException(getMessage("EXCEPTION.PWD.RESET.TOKEN.INVALID"));
        }
        if (System.currentTimeMillis() > entry.expireAt) {
            tokenStore.remove(token);
            throw new CustomException(getMessage("EXCEPTION.PWD.RESET.TOKEN.EXPIRED"));
        }
        if (newPassword == null || newPasswordConfirm == null || !newPassword.equals(newPasswordConfirm)) {
            throw new CustomException(getMessage("EXCEPTION.PWD.CONFIRM_NOT_MATCH"));
        }

        // 비밀번호 정책 검증
        validatePasswordPolicy(newPassword);

        // 갱신
        int updated = passwordResetMapper.UPDATE_PASSWORD(entry.email, passwordEncoder.encode(newPassword));
        if (updated <= 0) {
            throw new CustomException(getMessage("EXCEPTION.PWD.RESET"));
        }
        // 1회용 토큰 폐기
        tokenStore.remove(token);
    }

    /* ===================== 내부 유틸 ===================== */
    private void validatePasswordPolicy(String newPassword) {
        try {
            PasswordPolicy policy = buildPolicyFromConfig();
            PasswordValidationUtil validator = new PasswordValidationUtil();
            List<String> errors = validator.validatePassword(newPassword, policy, messageSource);
            if (!errors.isEmpty()) {
                throw new CustomException(String.join(" / ", errors));
            }
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Password policy load failed", e);
            throw new CustomException(getMessage("EXCEPTION.PWD.POLICY.LOAD_FAILED"));
        }
    }

    private PasswordPolicy buildPolicyFromConfig() {
        // site config(PWD_POLICY) 의 cfg_key/cfg_val 목록을 맵으로 변환 후
        // 정책 변환은 Core-lib PasswordValidationUtil.buildPolicy 로 위임 (정책 파싱 로직 단일화)
        List<Map<String, String>> configList = passwordResetMapper.SELECT_PWD_POLICY();
        Map<String, String> configMap = new HashMap<>();
        if (configList != null) {
            for (Map<String, String> row : configList) {
                String key = row.get("cfg_key");
                if (key != null) {
                    configMap.put(key, row.get("cfg_val"));
                }
            }
        }
        return new PasswordValidationUtil().buildPolicy(configMap);
    }

    private String generateCode() {
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }

    private String buildMailBody(String code) {
        return "<div style=\"font-family:'Apple SD Gothic Neo','Malgun Gothic',sans-serif;max-width:480px;margin:0 auto;padding:24px;\">"
                + "<h2 style=\"color:#1d1d1f;\">M-VIMS 비밀번호 재설정 인증코드</h2>"
                + "<p style=\"color:#555;\">비밀번호 재설정을 위한 인증 코드를 발송하였습니다.<br>"
                + "아래 6자리 코드를 인증번호 입력란에 3분 이내에 입력해 주세요.</p>"
                + "<div style=\"font-size:32px;font-weight:700;letter-spacing:8px;color:#0071e3;"
                + "text-align:center;margin:24px 0;padding:16px;background:#f5f5f7;border-radius:12px;\">"
                + code + "</div>"
                + "<p style=\"color:#999;font-size:12px;\">본 메일은 발신 전용입니다.</p>"
                + "</div>";
    }

    private String getMessage(String code) {
        Locale locale = LocaleContextHolder.getLocale();
        try {
            return messageSource.getMessage(code, null, locale);
        } catch (Exception e) {
            return code;
        }
    }

    /* ===================== 내부 엔트리 클래스 ===================== */
    private static class CodeEntry {
        final String code;
        final long createdAt;
        final long expireAt;
        int attempts;

        CodeEntry(String code, long createdAt, long expireAt) {
            this.code = code;
            this.createdAt = createdAt;
            this.expireAt = expireAt;
            this.attempts = 0;
        }
    }

    private static class TokenEntry {
        final String email;
        final long expireAt;

        TokenEntry(String email, long expireAt) {
            this.email = email;
            this.expireAt = expireAt;
        }
    }
}
