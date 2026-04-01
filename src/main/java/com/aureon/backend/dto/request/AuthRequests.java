package com.aureon.backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class AuthRequests {

    public record SendOtpRequest(
            @NotBlank(message = "Email is required")
            @Email(message = "Invalid email format")
            String email
    ) {}

    public record VerifyOtpRequest(
            @NotBlank(message = "Email is required")
            @Email(message = "Invalid email format")
            String email,

            @NotBlank(message = "OTP is required")
            String otp
    ) {}
}
