package com.visor.school.userservice.integration;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.visor.school.userservice.model.User;
import com.visor.school.userservice.model.UserRole;
import com.visor.school.userservice.service.PasswordResetService;
import com.visor.school.userservice.service.UserService;

/**
 * Integration test for password reset flow via Keycloak Admin API
 * 
 * Note: This test requires Keycloak to be running or Testcontainers
 * TODO: Add Testcontainers support for Keycloak
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Disabled("Requires Keycloak instance or Testcontainers setup")
class PasswordResetIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordResetService passwordResetService;

    @Autowired
    private KeycloakClient keycloakClient;

    @Test
    void shouldInitiatePasswordResetForExistingUser() {
        // Given - Create user
        String email = "reset-test-" + System.currentTimeMillis() + "@example.com";
        
        Set<UserRole> roles = new HashSet<>();
        roles.add(UserRole.TEACHER);
        
        User user = userService.createUser(
            email,
            "Reset",
            "Test",
            roles,
            "OriginalPassword123!",
            null
        );

        // When
        passwordResetService.initiatePasswordReset(email);

        // Then - Verify no exception is thrown
        // In a real test, we would verify that the email was sent
        assertNotNull(user.getKeycloakId());
    }

    @Test
    void shouldResetPasswordViaKeycloakAdminAPI() {
        // Given - Create user
        String email = "reset-keycloak-test-" + System.currentTimeMillis() + "@example.com";
        
        Set<UserRole> roles = new HashSet<>();
        roles.add(UserRole.STUDENT);
        
        User user = userService.createUser(
            email,
            "Keycloak",
            "Test",
            roles,
            "OldPassword123!",
            null
        );

        String newPassword = "NewPassword123!";

        // When
        passwordResetService.resetPassword(user.getKeycloakId(), newPassword, false);

        // Then - Verify no exception is thrown
        // In a real test with Keycloak running, we would verify the password was actually changed
        assertNotNull(user.getKeycloakId());
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundForPasswordReset() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            passwordResetService.initiatePasswordReset("nonexistent@example.com");
        });
    }
}
