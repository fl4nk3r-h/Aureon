package com.aureon.backend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aureon.backend.config.AppProperties;
import com.aureon.backend.dto.request.AuthRequests;
import com.aureon.backend.dto.response.Responses;
import com.aureon.backend.entity.OtpToken;
import com.aureon.backend.entity.User;
import com.aureon.backend.exception.BadRequestException;
import com.aureon.backend.exception.ResourceNotFoundException;
import com.aureon.backend.exception.UnauthorizedException;
import com.aureon.backend.repository.OtpTokenRepository;
import com.aureon.backend.repository.UserRepository;
import com.aureon.backend.security.JwtUtils;
import com.aureon.backend.service.AuthService;
import com.aureon.backend.service.EmailService;
import com.aureon.backend.util.OtpGenerator;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final OtpTokenRepository otpTokenRepository;
    private final EmailService emailService;
    private final OtpGenerator otpGenerator;
    private final JwtUtils jwtUtils;
    private final AppProperties appProperties;

    @Override
    @Transactional
    public Responses.MessageResponse sendOtp(AuthRequests.SendOtpRequest request) {
        String email = request.email().toLowerCase().trim();

        // User must already exist (pre-registered by admin, or first-time ADMIN seed)
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No account found for this email. Please contact your administrator."));

        if (!user.isActive()) {
            throw new BadRequestException("Your account is inactive. Please contact your administrator.");
        }

        // Invalidate any existing unused OTPs for this email
        otpTokenRepository.invalidateAllForEmail(email);

        // Generate and persist new OTP
        int otpLength  = appProperties.getOtp().getLength();
        int expiryMins = appProperties.getOtp().getExpiryMinutes();

        String rawOtp = otpGenerator.generate(otpLength);

        OtpToken token = OtpToken.builder()
                .email(email)
                .otp(rawOtp)
                .expiresAt(LocalDateTime.now().plusMinutes(expiryMins))
                .build();
        otpTokenRepository.save(token);

        // Send email
        emailService.sendOtp(email, rawOtp, expiryMins);

        log.info("OTP sent to {}", email);
        return Responses.MessageResponse.of("OTP sent to " + email + ". Valid for " + expiryMins + " minutes.");
    }

    @Override
    @Transactional
    public Responses.AuthResponse verifyOtp(AuthRequests.VerifyOtpRequest request) {
        String email = request.email().toLowerCase().trim();

        OtpToken otpToken = otpTokenRepository
                .findTopByEmailAndUsedFalseOrderByCreatedAtDesc(email)
                .orElseThrow(() -> new UnauthorizedException("No active OTP found. Please request a new one."));

        if (!otpToken.isValid()) {
            throw new UnauthorizedException(otpToken.isExpired()
                    ? "OTP has expired. Please request a new one."
                    : "OTP is invalid.");
        }

        if (!otpToken.getOtp().equals(request.otp())) {
            throw new UnauthorizedException("Incorrect OTP. Please try again.");
        }

        // Mark OTP as used
        otpToken.setUsed(true);
        otpTokenRepository.save(otpToken);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        String jwt = jwtUtils.generateToken(user.getEmail(), user.getRole().name(), user.getId());

        log.info("User {} authenticated successfully", email);
        return Responses.AuthResponse.of(jwt, user);
    }

    /**
     * Clean up expired OTP tokens nightly.
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void cleanupExpiredOtps() {
        otpTokenRepository.deleteExpiredOtps(LocalDateTime.now().minusHours(1));
        log.info("Expired OTP tokens cleaned up.");
    }
}
