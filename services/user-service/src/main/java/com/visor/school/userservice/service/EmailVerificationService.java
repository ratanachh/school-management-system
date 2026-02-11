package com.visor.school.userservice.service;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.visor.school.userservice.integration.KeycloakClient;
import com.visor.school.userservice.model.AccountStatus;
import com.visor.school.userservice.model.User;
import com.visor.school.userservice.repository.UserRepository;

/**
 * Email verification service with token generation and validation
 * When email is verified, account status is changed from PENDING to ACTIVE
 */
@Service
@Transactional
public class EmailVerificationService {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Map<String, VerificationToken> verificationTokens = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final KeycloakClient keycloakClient;
    private final long tokenExpiryHours;
    private final Clock clock;

    public EmailVerificationService(
        UserRepository userRepository,
        EmailService emailService,
        KeycloakClient keycloakClient,
        @Value("${email.verification.token.expiry.hours:24}") long tokenExpiryHours,
        Clock clock
    ) {
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.keycloakClient = keycloakClient;
        this.tokenExpiryHours = tokenExpiryHours;
        this.clock = clock != null ? clock : Clock.systemUTC();
    }

    /**
     * Generate and send verification email
     */
    public void sendVerificationEmail(User user) {
        logger.info("Generating verification token for user: {}", user.getId());

        String token = generateToken();
        Instant expiresAt = Instant.now(clock).plus(tokenExpiryHours, ChronoUnit.HOURS);

        verificationTokens.put(token, new VerificationToken(user.getId(), user.getEmail(), expiresAt));

        String verificationUrl = "http://localhost:8080/api/v1/auth/verify-email?token=" + token;

        emailService.sendVerificationEmail(user.getEmail(), user.getFirstName(), verificationUrl);
        
        logger.info("Verification email sent to: {}", user.getEmail());
    }

    /**
     * Verify email with token
     */
    public boolean verifyEmail(String token) {
        logger.info("Verifying email with token");

        VerificationToken verificationToken = verificationTokens.get(token);
        if (verificationToken == null) {
            throw new IllegalArgumentException("Invalid verification token");
        }

        if (verificationToken.expiresAt.isBefore(Instant.now(clock))) {
            verificationTokens.remove(token);
            throw new IllegalArgumentException("Verification token has expired");
        }

        User user = userRepository.findById(verificationToken.userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.verifyEmail();
        
        // Activate account if it was pending
        if (user.getAccountStatus() == AccountStatus.PENDING) {
            user.updateStatus(AccountStatus.ACTIVE);
            logger.info("Account activated for user: {} (email verified)", user.getId());
        }
        
        userRepository.save(user);

        // Update Keycloak email verification status
        keycloakClient.updateEmailVerification(user.getKeycloakId(), true);

        verificationTokens.remove(token);
        logger.info("Email verified and account activated for user: {}", user.getId());

        return true;
    }

    /**
     * Generate secure random token
     */
    private String generateToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * Clean up expired tokens (should be called periodically)
     */
    public void cleanupExpiredTokens() {
        Instant now = Instant.now(clock);
        verificationTokens.entrySet().removeIf(entry -> entry.getValue().expiresAt.isBefore(now));
        logger.debug("Cleaned up expired verification tokens");
    }

    private record VerificationToken(UUID userId, String email, Instant expiresAt) {}
}
