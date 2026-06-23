package com.login.password;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @title : 비밀번호 찾기(재설정) 컨트롤러
 * @text : 로그인 전(비인증) 흐름. /api/v1/auth/** 화이트리스트 경로 사용.
 *         예외는 Core-lib GlobalExceptionHandler(CustomException -> 403 + 메시지)로 처리.
 * @writer : 이경태
 */
@RestController
@RequestMapping("/api/v1/auth/password")
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    /** 인증코드 발송 요청 */
    @PostMapping("/code/request")
    public ResponseEntity<?> requestCode(@RequestBody Map<String, Object> param) {
        passwordResetService.requestCode((String) param.get("email"));
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /** 인증코드 검증 -> 재설정 토큰 반환 */
    @PostMapping("/code/verify")
    public ResponseEntity<?> verifyCode(@RequestBody Map<String, Object> param) {
        String token = passwordResetService.verifyCode(
                (String) param.get("email"),
                (String) param.get("code"));
        Map<String, Object> body = new HashMap<>();
        body.put("resetToken", token);
        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    /** 비밀번호 재설정 */
    @PostMapping("/reset")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, Object> param) {
        passwordResetService.resetPassword(
                (String) param.get("resetToken"),
                (String) param.get("newPassword"),
                (String) param.get("newPasswordConfirm"));
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
