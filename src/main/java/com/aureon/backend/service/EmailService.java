package com.aureon.backend.service;

public interface EmailService {
    void sendOtp(String toEmail, String otp, int expiryMinutes);
}
