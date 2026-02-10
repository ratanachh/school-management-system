package com.visor.school.userservice.service;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.visor.school.userservice.integration.KeycloakClient;
import com.visor.school.userservice.model.User;
import com.visor.school.userservice.repository.UserRepository;

/**
 * Password reset service that delegates to Keycloak Admin API
 */
@Service
@Transactional
public class PasswordResetService {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final UserRepository userRepository;
    private final KeycloakClient keycloakClient;
    private final EmailService emailService;

    public PasswordResetService(
        UserRepository userRepository,
        KeycloakClient keycloakClient,
        EmailService emailService
    ) {
        this.userRepository = userRepository;
        this.keycloakClient = keycloakClient;
        this.emailService = emailService;
    }

    /**
     * Initiate password reset flow
     * Generates reset token and sends email (Keycloak handles the actual reset)
     */
    public void initiatePasswordReset(String email) {
        logger.info("Initiating password reset for: {}", email);

        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

        // Generate reset token (in production, this would be handled by Keycloak)
        String resetToken = generateResetToken();
        
        // Store reset token (in production, use a proper token store)
        // For now, we'll use Keycloak's built-in password reset flow
        
        // Send password reset email
        String resetUrl = "http://localhost:8080/auth/realms/school-management/account/reset-password?token=" + resetToken;
        emailService.sendPasswordResetEmail(user.getEmail(), user.getFirstName(), resetUrl);

        logger.info("Password reset email sent to: {}", email);
    }

    /**
     * Reset password using Keycloak Admin API
     */
    public void resetPassword(String keycloakId, String newPassword, boolean temporary) {
        logger.info("Resetting password for Keycloak user: {}", keycloakId);

        keycloakClient.resetPassword(keycloakId, newPassword, temporary);

        logger.info("Password reset completed for Keycloak user: {}", keycloakId);
    }

    /**
     * Generate reset token (simplified - in production, Keycloak handles this)
     */
    private String generateResetToken() {
        // In production, this would integrate with Keycloak's password reset flow
        return UUID.randomUUID().toString();
    }
}
