package com.aureon.backend.security;

import com.aureon.backend.enums.Role;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthenticatedUser {
    private final Long id;
    private final String email;
    private final Role role;
}
