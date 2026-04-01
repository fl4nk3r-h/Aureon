package com.aureon.backend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.aureon.backend.service.EmailService;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String mailFrom;

    @Override
    public void sendOtp(String toEmail, String otp, int expiryMinutes) {
        try {
            var message = new SimpleMailMessage();
            if (mailFrom != null && !mailFrom.isBlank()) {
                message.setFrom(mailFrom.trim());
            }
            message.setTo(toEmail);
            message.setSubject("Your Aureon Dashboard OTP");
            message.setText(buildEmailBody(otp, expiryMinutes));
            mailSender.send(message);
            log.info("OTP email sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send OTP email to {} from {}: {}", toEmail, mailFrom, e.getMessage(), e);
            // Re-throw so the caller can handle it gracefully
            throw new RuntimeException("Failed to send OTP email. Verify SMTP credentials/app password and sender settings.", e);
        }
    }

    private String buildEmailBody(String otp, int expiryMinutes) {
        return """
                Hello,

                Your Aureon Dashboard login code is:

                  %s

                This code is valid for %d minutes.

                If you did not request this, please ignore this email.

                — Aureon Dashboard
                """.formatted(otp, expiryMinutes);
    }
}
