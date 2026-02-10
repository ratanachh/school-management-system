package com.visor.school.userservice.integration;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.visor.school.userservice.model.User;
import com.visor.school.userservice.model.UserRole;
import com.visor.school.userservice.repository.UserRepository;
import com.visor.school.userservice.service.EmailVerificationService;
import com.visor.school.userservice.service.UserService;

/**
 * Integration test for email verification flow
 * 
 * Note: This test requires Keycloak to be running or Testcontainers
 * TODO: Add Testcontainers support for Keycloak
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Disabled("Requires Keycloak instance or Testcontainers setup")
class EmailVerificationIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private EmailVerificationService emailVerificationService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldCompleteEmailVerificationFlow() {
        // Given - Create user
        String email = "verify-test-" + System.currentTimeMillis() + "@example.com";
        
        Set<UserRole> roles = new HashSet<>();
        roles.add(UserRole.STUDENT);
        
        User user = userService.createUser(
            email,
            "Verify",
            "Test",
            roles,
            "Password123!",
            null
        );

        assertFalse(user.isEmailVerified());

        // When - Send verification email
        emailVerificationService.sendVerificationEmail(user);

        // Note: In a real test, we would extract the token from the email service mock
        // For now, we'll test the verification service directly
        // The actual token would come from the email sent by EmailService
        
        // Verify that user exists
        Optional<User> savedUser = userRepository.findById(user.getId());
        assertTrue(savedUser.isPresent());
    }

    @Test
    void shouldVerifyEmailWithValidToken() {
        // Given - Create user and send verification email
        String email = "verify-token-test-" + System.currentTimeMillis() + "@example.com";
        
        Set<UserRole> roles = new HashSet<>();
        roles.add(UserRole.STUDENT);
        
        User user = userService.createUser(
            email,
            "Token",
            "Test",
            roles,
            "Password123!",
            null
        );

        emailVerificationService.sendVerificationEmail(user);

        // Note: In a real implementation, we would extract the token from EmailService
        // For integration testing, we might need to access the token storage directly
        // This is a placeholder for the actual test implementation
    }
}
