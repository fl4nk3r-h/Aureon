package com.aureon.backend.service;

import com.aureon.backend.dto.request.UserRequests;
import com.aureon.backend.dto.response.Responses;
import com.aureon.backend.entity.User;
import com.aureon.backend.enums.Role;
import com.aureon.backend.enums.UserStatus;
import com.aureon.backend.exception.ConflictException;
import com.aureon.backend.exception.ResourceNotFoundException;
import com.aureon.backend.repository.UserRepository;
import com.aureon.backend.service.impl.UserServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService")
class UserServiceTest {

    @Mock UserRepository userRepository;
    @InjectMocks UserServiceImpl userService;

    private User existingUser;

    @BeforeEach
    void setUp() {
        existingUser = User.builder()
                .id(1L).email("alice@test.com").name("Alice")
                .role(Role.VIEWER).status(UserStatus.ACTIVE)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("getAllUsers: returns paged results")
    void getAllUsers_returnsPaged() {
        var pageable = PageRequest.of(0, 10);
        when(userRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(existingUser)));

        var result = userService.getAllUsers(pageable);

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).email()).isEqualTo("alice@test.com");
    }

    @Test
    @DisplayName("getUserById: returns user when found")
    void getUserById_found_returnsUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));

        Responses.UserResponse resp = userService.getUserById(1L);

        assertThat(resp.id()).isEqualTo(1L);
        assertThat(resp.name()).isEqualTo("Alice");
    }

    @Test
    @DisplayName("getUserById: throws ResourceNotFoundException when not found")
    void getUserById_notFound_throws() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("createUser: creates and returns new user")
    void createUser_success() {
        when(userRepository.existsByEmail("bob@test.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u = User.builder().id(2L).email(u.getEmail()).name(u.getName())
                    .role(u.getRole()).status(UserStatus.ACTIVE)
                    .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
            return u;
        });

        var request = new UserRequests.CreateUserRequest("Bob", "bob@test.com", Role.ANALYST);
        Responses.UserResponse resp = userService.createUser(request);

        assertThat(resp.email()).isEqualTo("bob@test.com");
        assertThat(resp.role()).isEqualTo(Role.ANALYST);
    }

    @Test
    @DisplayName("createUser: throws ConflictException for duplicate email")
    void createUser_duplicateEmail_throwsConflict() {
        when(userRepository.existsByEmail("alice@test.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(
                new UserRequests.CreateUserRequest("Alice2", "alice@test.com", Role.VIEWER)))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("updateUser: updates name and role")
    void updateUser_updatesFields() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var request = new UserRequests.UpdateUserRequest("Alice Updated", Role.ADMIN, null);
        Responses.UserResponse resp = userService.updateUser(1L, request);

        assertThat(resp.name()).isEqualTo("Alice Updated");
        assertThat(resp.role()).isEqualTo(Role.ADMIN);
    }

    @Test
    @DisplayName("deleteUser: soft-deletes by setting deletedAt")
    void deleteUser_setsSoftDeleteTimestamp() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Responses.MessageResponse resp = userService.deleteUser(1L);

        assertThat(resp.message()).contains("deleted");
        assertThat(existingUser.getDeletedAt()).isNotNull();
    }
}
