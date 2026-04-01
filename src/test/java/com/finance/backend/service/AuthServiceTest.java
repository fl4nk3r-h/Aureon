package com.finance.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.aureon.backend.config.AppProperties;
import com.aureon.backend.dto.request.AuthRequests;
import com.aureon.backend.dto.response.Responses;
import com.aureon.backend.entity.OtpToken;
import com.aureon.backend.entity.User;
import com.aureon.backend.enums.Role;
import com.aureon.backend.enums.UserStatus;
import com.aureon.backend.exception.BadRequestException;
import com.aureon.backend.exception.ResourceNotFoundException;
import com.aureon.backend.exception.UnauthorizedException;
import com.aureon.backend.repository.OtpTokenRepository;
import com.aureon.backend.repository.UserRepository;
import com.aureon.backend.security.JwtUtils;
import com.aureon.backend.service.EmailService;
import com.aureon.backend.service.impl.AuthServiceImpl;
import com.aureon.backend.util.OtpGenerator;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService")
class AuthServiceTest {

    @Mock UserRepository       userRepository;
    @Mock OtpTokenRepository   otpTokenRepository;
    @Mock EmailService         emailService;
    @Mock OtpGenerator         otpGenerator;
    @Mock JwtUtils             jwtUtils;
    @Mock AppProperties        appProperties;

    @InjectMocks AuthServiceImpl authService;

    private User activeUser;
    private AppProperties.Otp otpProps;

    @BeforeEach
    void setUp() {
        activeUser = User.builder()
                .id(1L).email("user@test.com")
                .name("Test User").role(Role.ANALYST)
                .status(UserStatus.ACTIVE).build();

        otpProps = new AppProperties.Otp();
        otpProps.setLength(6);
        otpProps.setExpiryMinutes(10);

        when(appProperties.getOtp()).thenReturn(otpProps);
    }

    // --- sendOtp ---

    @Test
    @DisplayName("sendOtp: succeeds for an active user")
    void sendOtp_activeUser_sendsEmailAndReturnsMessage() {
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(activeUser));
        when(otpGenerator.generate(6)).thenReturn("123456");

        Responses.MessageResponse response = authService.sendOtp(
                new AuthRequests.SendOtpRequest("user@test.com"));

        assertThat(response.message()).contains("OTP sent");
        verify(emailService).sendOtp(eq("user@test.com"), eq("123456"), eq(10));
        verify(otpTokenRepository).save(any(OtpToken.class));
    }

    @Test
    @DisplayName("sendOtp: throws ResourceNotFoundException for unknown email")
    void sendOtp_unknownEmail_throwsNotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.sendOtp(
                new AuthRequests.SendOtpRequest("nobody@test.com")))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(emailService);
    }

    @Test
    @DisplayName("sendOtp: throws BadRequestException for inactive user")
    void sendOtp_inactiveUser_throwsBadRequest() {
        activeUser.setStatus(UserStatus.INACTIVE);
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(activeUser));

        assertThatThrownBy(() -> authService.sendOtp(
                new AuthRequests.SendOtpRequest("user@test.com")))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("inactive");

        verifyNoInteractions(emailService);
    }

    // --- verifyOtp ---

    @Test
    @DisplayName("verifyOtp: returns JWT on valid OTP")
    void verifyOtp_validOtp_returnsJwt() {
        OtpToken token = OtpToken.builder()
                .email("user@test.com").otp("123456")
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .used(false).build();

        when(otpTokenRepository.findTopByEmailAndUsedFalseOrderByCreatedAtDesc("user@test.com"))
                .thenReturn(Optional.of(token));
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(activeUser));
        when(jwtUtils.generateToken(any(), any(), any())).thenReturn("mock-jwt-token");

        Responses.AuthResponse response = authService.verifyOtp(
                new AuthRequests.VerifyOtpRequest("user@test.com", "123456"));

        assertThat(response.token()).isEqualTo("mock-jwt-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(token.isUsed()).isTrue();
    }

    @Test
    @DisplayName("verifyOtp: throws UnauthorizedException on wrong OTP")
    void verifyOtp_wrongOtp_throwsUnauthorized() {
        OtpToken token = OtpToken.builder()
                .email("user@test.com").otp("999999")
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .used(false).build();

        when(otpTokenRepository.findTopByEmailAndUsedFalseOrderByCreatedAtDesc("user@test.com"))
                .thenReturn(Optional.of(token));

        assertThatThrownBy(() -> authService.verifyOtp(
                new AuthRequests.VerifyOtpRequest("user@test.com", "123456")))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Incorrect OTP");
    }

    @Test
    @DisplayName("verifyOtp: throws UnauthorizedException on expired OTP")
    void verifyOtp_expiredOtp_throwsUnauthorized() {
        OtpToken token = OtpToken.builder()
                .email("user@test.com").otp("123456")
                .expiresAt(LocalDateTime.now().minusMinutes(1))  // already expired
                .used(false).build();

        when(otpTokenRepository.findTopByEmailAndUsedFalseOrderByCreatedAtDesc("user@test.com"))
                .thenReturn(Optional.of(token));

        assertThatThrownBy(() -> authService.verifyOtp(
                new AuthRequests.VerifyOtpRequest("user@test.com", "123456")))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("expired");
    }

    @Test
    @DisplayName("verifyOtp: throws UnauthorizedException when no active OTP exists")
    void verifyOtp_noActiveOtp_throwsUnauthorized() {
        when(otpTokenRepository.findTopByEmailAndUsedFalseOrderByCreatedAtDesc(anyString()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.verifyOtp(
                new AuthRequests.VerifyOtpRequest("user@test.com", "123456")))
                .isInstanceOf(UnauthorizedException.class);
    }
}
