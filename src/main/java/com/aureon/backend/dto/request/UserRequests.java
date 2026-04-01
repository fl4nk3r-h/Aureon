package com.aureon.backend.dto.request;

import com.aureon.backend.enums.Role;
import com.aureon.backend.enums.UserStatus;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class UserRequests {

    public record CreateUserRequest(
            @NotBlank(message = "Name is required")
            @Size(min = 2, max = 100, message = "Name must be 2–100 characters")
            String name,

            @NotBlank(message = "Email is required")
            @Email(message = "Invalid email format")
            String email,

            @NotNull(message = "Role is required")
            Role role
    ) {}

    public record UpdateUserRequest(
            @Size(min = 2, max = 100, message = "Name must be 2–100 characters")
            String name,

            Role role,

            UserStatus status
    ) {}
}
