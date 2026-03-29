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

import com.visor.school.userservice.integration.KeycloakClient;
import com.visor.school.userservice.model.AccountStatus;
import com.visor.school.userservice.model.EmailVerificationToken;
import com.visor.school.userservice.model.User;
import com.visor.school.userservice.repository.EmailVerificationTokenRepository;
import com.visor.school.userservice.repository.UserRepository;

/**
 * Email verification service with token generation and validation
 * When email is verified, account status is changed from PENDING to ACTIVE
 */
@Service
@Transactional
public class EmailVerificationService {

    private static final String VERIFY_EMAIL_TOKEN_QUERY = "?token=";

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final SecureRandom random = new SecureRandom();

    private final UserRepository userRepository;
    private final EmailVerificationTokenRepository verificationTokenRepository;
    private final EmailVerificationTokenDeletionService tokenDeletionService;
    private final EmailService emailService;
    private final KeycloakClient keycloakClient;
    private final String verificationBaseUrl;
    private final String verificationVerifyPath;
    private final long tokenExpiryHours;
    private final Clock clock;

    public EmailVerificationService(
        UserRepository userRepository,
        EmailVerificationTokenRepository verificationTokenRepository,
        EmailVerificationTokenDeletionService tokenDeletionService,
        EmailService emailService,
        KeycloakClient keycloakClient,
        @Value("${api.gateway.url}") String verificationBaseUrl,
        @Value("${email.verification.verify-path}") String verificationVerifyPath,
        @Value("${email.verification.token.expiry.hours:24}") long tokenExpiryHours,
        Clock clock
    ) {
        this.userRepository = userRepository;
        this.verificationTokenRepository = verificationTokenRepository;
        this.tokenDeletionService = tokenDeletionService;
        this.emailService = emailService;
        this.keycloakClient = keycloakClient;
        this.verificationBaseUrl = verificationBaseUrl;
        this.verificationVerifyPath = verificationVerifyPath;
        this.tokenExpiryHours = tokenExpiryHours;
        this.clock = clock != null ? clock : Clock.systemUTC();
    }

    /**
     * Generate and send verification email
     */
    public void sendVerificationEmail(User user) {
        logger.info("Generating verification token for user: {}", user.getId());

        String token = generateToken();
        Instant now = Instant.now(clock);
        Instant expiresAt = now.plus(tokenExpiryHours, ChronoUnit.HOURS);

        verificationTokenRepository.deleteByUserId(user.getId());
        verificationTokenRepository.save(
            new EmailVerificationToken(token, user.getId(), user.getEmail(), expiresAt, now)
        );

        String verificationUrl = verificationBaseUrl + verificationVerifyPath + VERIFY_EMAIL_TOKEN_QUERY + token;

        emailService.sendVerificationEmail(user.getEmail(), user.getFirstName(), verificationUrl);

        logger.info("Verification email sent to: {}", user.getEmail());
    }

    /**
     * Verify email with token
     */
    public boolean verifyEmail(String token) {
        logger.info("Verifying email with token");

        EmailVerificationToken stored = verificationTokenRepository.findByToken(token)
            .orElseThrow(() -> new IllegalArgumentException("Invalid verification token"));

        if (stored.getExpiresAt().isBefore(Instant.now(clock))) {
            tokenDeletionService.deleteByToken(token);
            throw new IllegalArgumentException("Verification token has expired");
        }

        User user = userRepository.findById(stored.getUserId())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!stored.getEmail().equalsIgnoreCase(user.getEmail())) {
            tokenDeletionService.deleteByToken(token);
            throw new IllegalArgumentException("Invalid verification token");
        }

        user.verifyEmail();

        // Activate account if it was pending
        if (user.getAccountStatus() == AccountStatus.PENDING) {
            user.updateStatus(AccountStatus.ACTIVE);
            logger.info("Account activated for user: {} (email verified)", user.getId());
        }

        userRepository.save(user);

        // Update Keycloak email verification status
        keycloakClient.updateEmailVerification(user.getKeycloakId(), true);

        verificationTokenRepository.deleteByToken(token);
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
     * Clean up expired tokens. Also scheduled so rows do not accumulate indefinitely.
     */
    @Scheduled(cron = "${email.verification.cleanup.cron:0 0 3 * * *}")
    public void cleanupExpiredTokens() {
        Instant now = Instant.now(clock);
        verificationTokenRepository.deleteByExpiresAtBefore(now);
        logger.debug("Cleaned up expired verification tokens");
    }
}
