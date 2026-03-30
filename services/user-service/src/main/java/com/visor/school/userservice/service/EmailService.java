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
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.visor.school.keycloak.integration.KeycloakClient;
import com.visor.school.userservice.event.UserEventPublisher;
import com.visor.school.userservice.model.AccountStatus;
import com.visor.school.userservice.model.EmailVerificationToken;
import com.visor.school.userservice.model.User;
import com.visor.school.userservice.repository.EmailVerificationTokenRepository;
import com.visor.school.userservice.repository.UserRepository;

/**
 * Outbound email and email-verification flow (tokens, verify, scheduled cleanup).
 * Password reset / welcome use the delivery helpers below; verification links are built here.
 */
@Service
public class EmailService {

    private static final String VERIFY_EMAIL_TOKEN_QUERY = "?token=";

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final SecureRandom random = new SecureRandom();
    private final UserRepository userRepository;
    private final EmailVerificationTokenRepository verificationTokenRepository;
    private final KeycloakClient keycloakClient;
    private final UserEventPublisher userEventPublisher;
    private final TransactionTemplate requiresNewTransaction;
    private final String verificationBaseUrl;
    private final String verificationVerifyPath;
    private final long tokenExpiryHours;
    private final Clock clock;

    public EmailService(
        UserRepository userRepository,
        EmailVerificationTokenRepository verificationTokenRepository,
        KeycloakClient keycloakClient,
        UserEventPublisher userEventPublisher,
        PlatformTransactionManager transactionManager,
        @Value("${api.gateway.url}") String verificationBaseUrl,
        @Value("${email.verification.verify-path}") String verificationVerifyPath,
        @Value("${email.verification.token.expiry.hours:24}") long tokenExpiryHours,
        Clock clock
    ) {
        this.userRepository = userRepository;
        this.verificationTokenRepository = verificationTokenRepository;
        this.keycloakClient = keycloakClient;
        this.userEventPublisher = userEventPublisher;
        TransactionTemplate tt = new TransactionTemplate(transactionManager);
        tt.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        this.requiresNewTransaction = tt;
        this.verificationBaseUrl = verificationBaseUrl;
        this.verificationVerifyPath = verificationVerifyPath;
        this.tokenExpiryHours = tokenExpiryHours;
        this.clock = clock != null ? clock : Clock.systemUTC();
    }

    /**
     * Create a verification token, persist it, and send the verification email.
     */
    @Transactional
    public void sendVerificationEmail(User user) {
        logger.info("Generating verification token for user: {}", user.getId());

        String token = generateVerificationSecret();
        Instant now = Instant.now(clock);
        Instant expiresAt = now.plus(tokenExpiryHours, ChronoUnit.HOURS);

        verificationTokenRepository.deleteByUserId(user.getId());
        verificationTokenRepository.save(
            new EmailVerificationToken(token, user.getId(), user.getEmail(), expiresAt, now)
        );

        String verificationUrl = verificationBaseUrl + verificationVerifyPath + VERIFY_EMAIL_TOKEN_QUERY + token;
        deliverVerificationEmail(user.getEmail(), user.getFirstName(), verificationUrl);

        logger.info("Verification email sent to: {}", user.getEmail());
    }

    /**
     * Validate token, activate user if pending, sync Keycloak, remove token.
     */
    @Transactional
    public boolean verifyEmail(String token) {
        logger.info("Verifying email with token");

        EmailVerificationToken stored = verificationTokenRepository.findByToken(token)
            .orElseThrow(() -> new IllegalArgumentException("Invalid verification token"));

        if (stored.getExpiresAt().isBefore(Instant.now(clock))) {
            deleteVerificationTokenCommitted(token);
            throw new IllegalArgumentException("Verification token has expired");
        }

        User user = userRepository.findById(stored.getUserId())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!stored.getEmail().equalsIgnoreCase(user.getEmail())) {
            deleteVerificationTokenCommitted(token);
            throw new IllegalArgumentException("Invalid verification token");
        }

        user.verifyEmail();

        if (user.getAccountStatus() == AccountStatus.PENDING) {
            user.updateStatus(AccountStatus.ACTIVE);
            logger.info("Account activated for user: {} (email verified)", user.getId());
        }

        User savedUser = userRepository.save(user);
        verificationTokenRepository.deleteByToken(token);
        keycloakClient.updateEmailVerification(savedUser.getKeycloakId(), true);
        userEventPublisher.publishEmailVerified(savedUser);
        logger.info("Email verified and account activated for user: {}", user.getId());

        return true;
    }

    @Scheduled(cron = "${email.verification.cleanup.cron:0 0 3 * * *}")
    @Transactional
    public void cleanupExpiredVerificationTokens() {
        Instant now = Instant.now(clock);
        verificationTokenRepository.deleteByExpiresAtBefore(now);
        logger.debug("Cleaned up expired verification tokens");
    }

    /**
     * Send password reset email
     */
    public void sendPasswordResetEmail(String email, String firstName, String resetUrl) {
        logger.info("Sending password reset email to: {}", email);
        logger.debug("Reset URL: {}", resetUrl);
    }

    private void deliverVerificationEmail(String email, String firstName, String verificationUrl) {
        logger.info("Sending verification email to: {}", email);
        logger.debug("Verification URL: {}", verificationUrl);
    }

    private void deleteVerificationTokenCommitted(String token) {
        requiresNewTransaction.executeWithoutResult(status -> verificationTokenRepository.deleteByToken(token));
    }

    private String generateVerificationSecret() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
