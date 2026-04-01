package com.aureon.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.aureon.backend.dto.request.AuthRequests;
import com.aureon.backend.dto.response.Responses;
import com.aureon.backend.service.AuthService;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Email OTP-based authentication")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/send-otp")
    @Operation(
        summary = "Request an OTP",
        description = "Sends a one-time password to the given email. The account must already exist in the system."
    )
    public ResponseEntity<Responses.MessageResponse> sendOtp(
            @Valid @RequestBody AuthRequests.SendOtpRequest request) {
        return ResponseEntity.ok(authService.sendOtp(request));
    }

    @PostMapping("/verify-otp")
    @Operation(
        summary = "Verify OTP and receive JWT",
        description = "Validates the OTP and returns a Bearer JWT token for subsequent authenticated requests."
    )
    public ResponseEntity<Responses.AuthResponse> verifyOtp(
            @Valid @RequestBody AuthRequests.VerifyOtpRequest request) {
        return ResponseEntity.ok(authService.verifyOtp(request));
    }
}
