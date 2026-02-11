package com.visor.school.userservice.service;

import com.visor.school.userservice.integration.KeycloakClient;
import com.visor.school.userservice.model.User;
import com.visor.school.userservice.model.UserRole;
import com.visor.school.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private KeycloakClient keycloakClient;

    private EmailVerificationService emailVerificationService;

    private User testUser;

    /** Mutable clock for tests that need to simulate time (e.g. expired token). */
    private MutableClock clock;

    @BeforeEach
    void setup() {
        clock = new MutableClock(Instant.now());
        emailVerificationService = new EmailVerificationService(
            userRepository,
            emailService,
            keycloakClient,
            24,
            clock
        );
        
        Set<UserRole> roles = new HashSet<>();
        roles.add(UserRole.TEACHER);
        
        testUser = new User(
            "keycloak-123",
            "test@example.com",
            roles,
            "John",
            "Doe"
        );
        
        // Set ID using reflection since it's normally set by JPA
        try {
            Field idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(testUser, UUID.randomUUID());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void shouldSendVerificationEmailSuccessfully() {
        // Given
        doNothing().when(emailService).sendVerificationEmail(any(), any(), any());

        // When
        emailVerificationService.sendVerificationEmail(testUser);

        // Then
        verify(emailService).sendVerificationEmail(
            eq(testUser.getEmail()),
            eq(testUser.getFirstName()),
            any()
        );
    }

    @Test
    void shouldVerifyEmailWithValidToken() throws Exception {
        // Given
        emailVerificationService.sendVerificationEmail(testUser);
        String token = getVerificationTokens().keySet().iterator().next();

        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        boolean result = emailVerificationService.verifyEmail(token);

        // Then
        assertTrue(result);
        assertTrue(testUser.isEmailVerified());
        verify(userRepository).save(testUser);
    }

    @Test
    void shouldThrowExceptionForInvalidToken() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            emailVerificationService.verifyEmail("invalid-token");
        });
    }

    @Test
    void shouldThrowExceptionForExpiredToken() throws Exception {
        // Given: send email so token exists with expiry = now + 24h
        emailVerificationService.sendVerificationEmail(testUser);
        // Advance clock past token expiry (25 hours)
        clock.setInstant(clock.instant().plus(25, ChronoUnit.HOURS));

        // Get token from internal map (no reflection on final field)
        Map<String, ?> tokens = getVerificationTokens();
        String token = tokens.keySet().iterator().next();

        // When & Then: verifyEmail sees "now" as 25h later, so token is expired
        assertThrows(IllegalArgumentException.class, () -> {
            emailVerificationService.verifyEmail(token);
        });
    }

    @Test
    void shouldCleanupExpiredTokens() throws Exception {
        // Given: send email so token exists
        emailVerificationService.sendVerificationEmail(testUser);
        // Advance clock past token expiry
        clock.setInstant(clock.instant().plus(25, ChronoUnit.HOURS));

        Map<String, ?> tokens = getVerificationTokens();
        assertFalse(tokens.isEmpty());

        // When
        emailVerificationService.cleanupExpiredTokens();

        // Then
        assertTrue(getVerificationTokens().isEmpty());
    }

    @SuppressWarnings("unchecked")
    private Map<String, ?> getVerificationTokens() throws Exception {
        Field tokensField = EmailVerificationService.class.getDeclaredField("verificationTokens");
        tokensField.setAccessible(true);
        return (Map<String, ?>) tokensField.get(emailVerificationService);
    }

    /** Clock whose instant can be set for tests. */
    private static final class MutableClock extends Clock {
        private Instant instant;
        private final ZoneId zone = ZoneId.systemDefault();

        MutableClock(Instant initial) {
            this.instant = initial;
        }

        void setInstant(Instant instant) {
            this.instant = instant;
        }

        @Override
        public Instant instant() {
            return instant;
        }

        @Override
        public ZoneId getZone() {
            return zone;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return Clock.fixed(instant, zone);
        }
    }
}
