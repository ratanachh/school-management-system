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
import java.time.Instant;
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

    @BeforeEach
    void setup() {
        emailVerificationService = new EmailVerificationService(
            userRepository,
            emailService,
            keycloakClient,
            24
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
        
        Field tokensField = EmailVerificationService.class.getDeclaredField("verificationTokens");
        tokensField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, ?> tokensMap = (Map<String, ?>) tokensField.get(emailVerificationService);
        String token = tokensMap.keySet().iterator().next();

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
        // Given
        emailVerificationService.sendVerificationEmail(testUser);
        
        Field tokensField = EmailVerificationService.class.getDeclaredField("verificationTokens");
        tokensField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, ?> tokens = (Map<String, ?>) tokensField.get(emailVerificationService);

        // Manually expire the token
        String token = tokens.keySet().iterator().next();
        Object tokenData = tokens.get(token);
        Class<?> tokenClass = tokenData.getClass();
        Field expiresAtField = tokenClass.getDeclaredField("expiresAt");
        expiresAtField.setAccessible(true);
        expiresAtField.set(tokenData, Instant.now().minusSeconds(1));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            emailVerificationService.verifyEmail(token);
        });
    }

    @Test
    void shouldCleanupExpiredTokens() throws Exception {
        // Given
        emailVerificationService.sendVerificationEmail(testUser);
        
        Field tokensField = EmailVerificationService.class.getDeclaredField("verificationTokens");
        tokensField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, ?> tokens = (Map<String, ?>) tokensField.get(emailVerificationService);

        // Manually expire the token
        String token = tokens.keySet().iterator().next();
        Object tokenData = tokens.get(token);
        Class<?> tokenClass = tokenData.getClass();
        Field expiresAtField = tokenClass.getDeclaredField("expiresAt");
        expiresAtField.setAccessible(true);
        expiresAtField.set(tokenData, Instant.now().minusSeconds(1));

        // When
        emailVerificationService.cleanupExpiredTokens();

        // Then
        assertTrue(tokens.isEmpty());
    }
}
