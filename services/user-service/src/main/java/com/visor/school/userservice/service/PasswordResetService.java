package com.visor.school.userservice.service;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.visor.school.keycloak.integration.KeycloakClient;
import com.visor.school.userservice.model.PasswordResetToken;
import com.visor.school.userservice.model.User;
import com.visor.school.userservice.repository.PasswordResetTokenRepository;
import com.visor.school.userservice.repository.UserRepository;

/**
 * Password reset service with app-managed one-time tokens and Keycloak password update.
 */
@Service
public class PasswordResetService {

    private static final String RESET_PASSWORD_TOKEN_QUERY = "?token=";

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final SecureRandom random = new SecureRandom();
    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final KeycloakClient keycloakClient;
    private final EmailService emailService;
    private final String resetBaseUrl;
    private final String resetPath;
    private final long tokenExpiryHours;
    private final Clock clock;

    public PasswordResetService(
        UserRepository userRepository,
        PasswordResetTokenRepository passwordResetTokenRepository,
        KeycloakClient keycloakClient,
        EmailService emailService,
        @Value("${api.gateway.url}") String resetBaseUrl,
        @Value("${password.reset.path:/api/v1/auth/reset-password/confirm}") String resetPath,
        @Value("${password.reset.token.expiry.hours:1}") long tokenExpiryHours,
        Clock clock
    ) {
        this.userRepository = userRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.keycloakClient = keycloakClient;
        this.emailService = emailService;
        this.resetBaseUrl = resetBaseUrl;
        this.resetPath = resetPath;
        this.tokenExpiryHours = tokenExpiryHours;
        this.clock = clock != null ? clock : Clock.systemUTC();
    }

    /**
     * Initiate password reset flow.
     * Does not reveal user existence.
     */
    @Transactional
    public void initiatePasswordReset(String email) {
        logger.info("Initiating password reset for: {}", email);

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            logger.info("Password reset requested for non-existent email");
            return;
        }

        String resetToken = generateResetSecret();
        Instant now = Instant.now(clock);
        Instant expiresAt = now.plus(tokenExpiryHours, ChronoUnit.HOURS);

        passwordResetTokenRepository.deleteByUserId(user.getId());
        passwordResetTokenRepository.save(
            new PasswordResetToken(resetToken, user.getId(), user.getEmail(), expiresAt, now)
        );

        String resetUrl = resetBaseUrl + resetPath + RESET_PASSWORD_TOKEN_QUERY + resetToken;
        emailService.sendPasswordResetEmail(user.getEmail(), user.getFirstName(), resetUrl);
        logger.info("Password reset email sent to: {}", email);
    }

    /**
     * Complete password reset using one-time token.
     */
    @Transactional
    public void completePasswordReset(String token, String newPassword, boolean temporary) {
        PasswordResetToken stored = passwordResetTokenRepository.findByToken(token)
            .orElseThrow(() -> new IllegalArgumentException("Invalid password reset token"));

        if (stored.isUsed()) {
            throw new IllegalArgumentException("Password reset token has already been used");
        }
        if (stored.getExpiresAt().isBefore(Instant.now(clock))) {
            throw new IllegalArgumentException("Password reset token has expired");
        }

        User user = userRepository.findById(stored.getUserId())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!stored.getEmail().equalsIgnoreCase(user.getEmail())) {
            throw new IllegalArgumentException("Invalid password reset token");
        }

        keycloakClient.resetPassword(user.getKeycloakId(), newPassword, temporary);
        stored.markUsed(Instant.now(clock));
        passwordResetTokenRepository.save(stored);
        logger.info("Password reset completed for user: {}", user.getId());
    }

    /**
     * Compatibility method for internal admin flows that already have keycloakId.
     */
    @Transactional
    public void resetPassword(String keycloakId, String newPassword, boolean temporary) {
        logger.info("Resetting password for Keycloak user: {}", keycloakId);
        keycloakClient.resetPassword(keycloakId, newPassword, temporary);
        logger.info("Password reset completed for Keycloak user: {}", keycloakId);
    }

    @Scheduled(cron = "${password.reset.cleanup.cron:0 30 3 * * *}")
    @Transactional
    public void cleanupExpiredResetTokens() {
        passwordResetTokenRepository.deleteByExpiresAtBefore(Instant.now(clock));
    }

    private String generateResetSecret() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
