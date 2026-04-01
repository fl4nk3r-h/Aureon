package com.aureon.backend.service;

import org.springframework.data.domain.Pageable;

import com.aureon.backend.dto.request.UserRequests;
import com.aureon.backend.dto.response.Responses;

public interface UserService {
    Responses.PagedResponse<Responses.UserResponse> getAllUsers(Pageable pageable);
    Responses.UserResponse getUserById(Long id);
    Responses.UserResponse createUser(UserRequests.CreateUserRequest request);
    Responses.UserResponse updateUser(Long id, UserRequests.UpdateUserRequest request);
    Responses.MessageResponse deleteUser(Long id);
}
