package com.system.common.util.passwordvalidation;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public class PasswordValidationUtil {
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile(".*[A-Z].*");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile(".*[a-z].*");
    private static final Pattern NUMBER_PATTERN = Pattern.compile(".*\\d.*");
    private static final Pattern SPECIAL_CHARACTER_PATTERN = Pattern.compile(".*[!@#$%^&*(),.?\":{}|<>].*");

    /**
     * 비밀번호가 정책을 준수하는지 검증한다.
     * 의도적으로 static하지 않게 인스턴스를 생성해서 사용하도록 만듦
     *
     * @param password 검증할 비밀번호
     * @param policy   적용할 비밀번호 정책
     * @return 실패 원인 메시지들
     */
    public List<String> validatePassword(String pwd, PasswordPolicy policy) {
        return validatePassword(pwd, policy, null);
    }

    /**
     * 비밀번호가 정책을 준수하는지 검증한다. (MessageSource를 사용한 국제화 지원)
     *
     * @param password      검증할 비밀번호
     * @param policy        적용할 비밀번호 정책
     * @param messageSource 국제화 메시지 소스 (null이면 기본 메시지 사용)
     * @return 실패 원인 메시지들
     */
    public List<String> validatePassword(String pwd, PasswordPolicy policy, MessageSource messageSource) {
        List<String> errors = new ArrayList<>();
        Locale locale = LocaleContextHolder.getLocale();

        if (policy == null) {
            errors.add(getMessage(messageSource, "EXCEPTION.PWD.POLICY.NOT_SET",
                    "비밀번호 정책이 설정되지 않았습니다.", locale));
            return errors;
        }

        if (pwd.length() < policy.getMinLength()) {
            errors.add(getMessage(messageSource, "EXCEPTION.PWD.POLICY.MIN_LENGTH",
                    "비밀번호는 최소 " + policy.getMinLength() + "자 이상이어야 합니다.", locale,
                    policy.getMinLength()));
        }
        if (policy.getMaxLength() < pwd.length()) {
            errors.add(getMessage(messageSource, "EXCEPTION.PWD.POLICY.MAX_LENGTH",
                    "비밀번호는 최대 " + policy.getMaxLength() + "자 이하여야 합니다.", locale,
                    policy.getMaxLength()));
        }
        if (policy.isRequireUppercase() && !UPPERCASE_PATTERN.matcher(pwd).matches()) {
            errors.add(getMessage(messageSource, "EXCEPTION.PWD.POLICY.REQUIRE_UPPERCASE",
                    "비밀번호에 최소 하나의 대문자가 포함되어야 합니다.", locale));
        }
        if (policy.isRequireLowercase() && !LOWERCASE_PATTERN.matcher(pwd).matches()) {
            errors.add(getMessage(messageSource, "EXCEPTION.PWD.POLICY.REQUIRE_LOWERCASE",
                    "비밀번호에 최소 하나의 소문자가 포함되어야 합니다.", locale));
        }
        if (policy.isRequireNumber() && !NUMBER_PATTERN.matcher(pwd).matches()) {
            errors.add(getMessage(messageSource, "EXCEPTION.PWD.POLICY.REQUIRE_NUMBER",
                    "비밀번호에 최소 하나의 숫자가 포함되어야 합니다.", locale));
        }
        if (policy.isRequireSpecialCharacter() && !SPECIAL_CHARACTER_PATTERN.matcher(pwd).matches()) {
            errors.add(getMessage(messageSource, "EXCEPTION.PWD.POLICY.REQUIRE_SPECIAL_CHARACTER",
                    "비밀번호에 최소 하나의 특수문자가 포함되어야 합니다.", locale));
        }

        return errors;
    }

    /**
     * 설정 맵(cfg_key -> cfg_val)으로부터 PasswordPolicy 를 생성한다.
     * site config(PWD_POLICY) 또는 공통코드 등 키-값 형태의 정책 소스를 공용으로 변환하기 위한 메서드.
     * 정책 키 추가/변경 시 이 한 곳만 수정하면 된다.
     *
     * @param config 정책 키-값 맵 (예: MIN_LENGTH=8, REQUIRE_NUMBER=1 ...)
     * @return 변환된 PasswordPolicy (config 가 비어있으면 기본값 정책)
     */
    public PasswordPolicy buildPolicy(Map<String, String> config) {
        PasswordPolicy policy = new PasswordPolicy();
        if (config == null || config.isEmpty()) {
            return policy;
        }
        for (Map.Entry<String, String> entry : config.entrySet()) {
            applyPolicyValue(policy, entry.getKey(), entry.getValue());
        }
        return policy;
    }

    /**
     * 단일 정책 키-값을 PasswordPolicy 에 반영한다.
     */
    private void applyPolicyValue(PasswordPolicy policy, String key, String value) {
        if (key == null || value == null) {
            return;
        }
        switch (key) {
            case "MAX_LENGTH":
                policy.setMaxLength(Integer.parseInt(value.trim()));
                break;
            case "MIN_LENGTH":
                policy.setMinLength(Integer.parseInt(value.trim()));
                break;
            case "REQUIRE_UPPERCASE":
                policy.setRequireUppercase(isPolicyEnabled(value));
                break;
            case "REQUIRE_LOWERCASE":
                policy.setRequireLowercase(isPolicyEnabled(value));
                break;
            case "REQUIRE_NUMBER":
                policy.setRequireNumber(isPolicyEnabled(value));
                break;
            case "REQUIRE_SPECIAL_CHARACTER":
                policy.setRequireSpecialCharacter(isPolicyEnabled(value));
                break;
            default:
                // 알 수 없는 설정 키는 무시
                break;
        }
    }

    private boolean isPolicyEnabled(String value) {
        return "1".equals(value) || "true".equalsIgnoreCase(value);
    }

    /**
     * MessageSource에서 메시지를 가져오거나, 없으면 기본 메시지 반환
     */
    private String getMessage(MessageSource messageSource, String code, String defaultMessage, Locale locale,
            Object... args) {
        if (messageSource == null) {
            return defaultMessage;
        }
        try {
            return messageSource.getMessage(code, args, locale);
        } catch (Exception e) {
            return defaultMessage;
        }
    }
}