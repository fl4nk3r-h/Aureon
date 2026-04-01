package com.aureon.backend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.aureon.backend.service.EmailService;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendOtp(String toEmail, String otp, int expiryMinutes) {
        try {
            var message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Your Finance Dashboard OTP");
            message.setText(buildEmailBody(otp, expiryMinutes));
            mailSender.send(message);
            log.info("OTP email sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send OTP email to {}: {}", toEmail, e.getMessage());
            // Re-throw so the caller can handle it gracefully
            throw new RuntimeException("Failed to send OTP email. Please try again.", e);
        }
    }

    private String buildEmailBody(String otp, int expiryMinutes) {
        return """
                Hello,

                Your Finance Dashboard login code is:

                  %s

                This code is valid for %d minutes.

                If you did not request this, please ignore this email.

                — Finance Dashboard
                """.formatted(otp, expiryMinutes);
    }
}
