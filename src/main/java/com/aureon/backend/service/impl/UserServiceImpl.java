package com.aureon.backend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aureon.backend.dto.request.UserRequests;
import com.aureon.backend.dto.response.Responses;
import com.aureon.backend.entity.User;
import com.aureon.backend.exception.BadRequestException;
import com.aureon.backend.exception.ConflictException;
import com.aureon.backend.exception.ResourceNotFoundException;
import com.aureon.backend.repository.UserRepository;
import com.aureon.backend.service.UserService;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public Responses.PagedResponse<Responses.UserResponse> getAllUsers(Pageable pageable) {
        return Responses.PagedResponse.from(
                userRepository.findAll(pageable).map(Responses.UserResponse::from)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Responses.UserResponse getUserById(Long id) {
        return Responses.UserResponse.from(findById(id));
    }

    @Override
    @Transactional
    public Responses.UserResponse createUser(UserRequests.CreateUserRequest request) {
        String email = request.email().toLowerCase().trim();

        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("A user with email '" + email + "' already exists.");
        }

        User user = User.builder()
                .email(email)
                .name(request.name().trim())
                .role(request.role())
                .build();

        User saved = userRepository.save(user);
        log.info("Created user {} with role {}", saved.getEmail(), saved.getRole());
        return Responses.UserResponse.from(saved);
    }

    @Override
    @Transactional
    public Responses.UserResponse updateUser(Long id, UserRequests.UpdateUserRequest request) {
        User user = findById(id);

        if (request.name() != null && !request.name().isBlank()) {
            user.setName(request.name().trim());
        }
        if (request.role() != null) {
            user.setRole(request.role());
        }
        if (request.status() != null) {
            user.setStatus(request.status());
        }

        User saved = userRepository.save(user);
        log.info("Updated user {}", saved.getEmail());
        return Responses.UserResponse.from(saved);
    }

    @Override
    @Transactional
    public Responses.MessageResponse deleteUser(Long id) {
        User user = findById(id);

        // Soft delete
        user.setDeletedAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("Soft-deleted user {}", user.getEmail());
        return Responses.MessageResponse.of("User deleted successfully.");
    }

    private User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("User", id));
    }
}
