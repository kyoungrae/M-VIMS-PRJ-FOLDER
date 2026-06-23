package com.login.mail;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

/**
 * @title : 메일 발송 서비스
 * @text : Spring JavaMailSender 기반 HTML 메일 발송. 발신(from)은 SMTP 인증 계정 사용.
 * @writer : 이경태
 */
@Service
@RequiredArgsConstructor
public class MailService {

    private static final Logger logger = LoggerFactory.getLogger(MailService.class);

    // SMTP 미설정 시 JavaMailSender 빈이 없을 수 있으므로 ObjectProvider 로 안전하게 주입
    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    // 발신(from)은 SMTP 인증 계정과 동일하게 사용 (서버 거부 방지)
    @Value("${spring.mail.username:}")
    private String fromEmail;

    /**
     * HTML 메일 발송
     *
     * @param toEmail  수신 이메일
     * @param subject  제목
     * @param htmlBody HTML 본문
     * @return 발송 성공 여부
     */
    public boolean sendHtmlMail(String toEmail, String subject, String htmlBody) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            logger.error("JavaMailSender is not configured. Check spring.mail.* settings.");
            return false;
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            if (fromEmail != null && !fromEmail.isBlank()) {
                try {
                    helper.setFrom(fromEmail, "M-VIMS");
                } catch (UnsupportedEncodingException e) {
                    helper.setFrom(fromEmail);
                }
            }
            mailSender.send(message);
            return true;
        } catch (Exception e) {
            logger.error("Failed to send mail to {}: {}", toEmail, e.getMessage());
            return false;
        }
    }
}
