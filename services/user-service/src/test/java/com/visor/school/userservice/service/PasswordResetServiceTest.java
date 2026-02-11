package com.visor.school.userservice.service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.visor.school.userservice.integration.KeycloakClient;
import com.visor.school.userservice.model.AccountStatus;
import com.visor.school.userservice.model.User;
import com.visor.school.userservice.model.UserRole;
import com.visor.school.userservice.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private KeycloakClient keycloakClient;

    @Mock
    private EmailService emailService;

    private PasswordResetService passwordResetService;

    private User testUser;

    @BeforeEach
    void setup() {
        passwordResetService = new PasswordResetService(userRepository, keycloakClient, emailService);
        
        Set<UserRole> roles = new HashSet<>();
        roles.add(UserRole.TEACHER);
        
        testUser = new User(
            "keycloak-123",
            "test@example.com",
            "John",
            "Doe",
            roles,
            null,
            false,
            AccountStatus.ACTIVE
        );
    }

    @Test
    void shouldInitiatePasswordResetSuccessfully() {
        // Given
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        doNothing().when(emailService).sendPasswordResetEmail(any(), any(), any());

        // When
        passwordResetService.initiatePasswordReset(testUser.getEmail());

        // Then
        verify(userRepository).findByEmail(testUser.getEmail());
        verify(emailService).sendPasswordResetEmail(
            eq(testUser.getEmail()),
            eq(testUser.getFirstName()),
            any()
        );
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundForPasswordReset() {
        // Given
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            passwordResetService.initiatePasswordReset("nonexistent@example.com");
        });

        verify(emailService, never()).sendPasswordResetEmail(any(), any(), any());
    }

    @Test
    void shouldResetPasswordViaKeycloak() {
        // Given
        String keycloakId = "keycloak-123";
        String newPassword = "newPassword123";
        doNothing().when(keycloakClient).resetPassword(any(), any(), anyBoolean());

        // When
        passwordResetService.resetPassword(keycloakId, newPassword, false);

        // Then
        verify(keycloakClient).resetPassword(keycloakId, newPassword, false);
    }
}
