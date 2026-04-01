package com.aureon.backend.service;

import com.aureon.backend.dto.request.AuthRequests;
import com.aureon.backend.dto.response.Responses;

public interface AuthService {
    Responses.MessageResponse sendOtp(AuthRequests.SendOtpRequest request);
    Responses.AuthResponse verifyOtp(AuthRequests.VerifyOtpRequest request);
}
