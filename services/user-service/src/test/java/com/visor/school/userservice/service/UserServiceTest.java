package com.visor.school.userservice.service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.visor.school.userservice.event.UserEventPublisher;
import com.visor.school.userservice.integration.KeycloakClient;
import com.visor.school.userservice.integration.KeycloakException;
import com.visor.school.userservice.model.User;
import com.visor.school.userservice.model.UserRole;
import com.visor.school.userservice.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private KeycloakClient keycloakClient;

    @Mock
    private UserEventPublisher eventPublisher;

    @Mock
    private SecurityContextService securityContextService;

    private UserService userService;

    private final String testKeycloakId = "keycloak-123";
    private final String testEmail = "test@example.com";
    private final String testPassword = "password123";

    @BeforeEach
    void setup() {
        userService = new UserService(userRepository, keycloakClient, eventPublisher, securityContextService);
    }

    @Test
    void shouldCreateUserSuccessfullyWithKeycloakIntegration() {
        // Given
        when(userRepository.existsByEmail(testEmail)).thenReturn(false);
        when(keycloakClient.createUser(
            eq(testEmail),
            eq("John"),
            eq("Doe"),
            eq(testPassword),
            eq(false),
            any()
        )).thenReturn(testKeycloakId);
        doNothing().when(keycloakClient).assignRealmRolesAsAdmin(any(), any());
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(eventPublisher).publishUserCreated(any());

        Set<UserRole> roles = new HashSet<>();
        roles.add(UserRole.TEACHER);

        // When
        User result = userService.createUser(
            testEmail,
            "John",
            "Doe",
            roles,
            testPassword,
            null
        );

        // Then
        assertNotNull(result);
        assertEquals(testKeycloakId, result.getKeycloakId());
        assertEquals(testEmail, result.getEmail());
        assertTrue(result.getRoles().contains(UserRole.TEACHER));
        verify(keycloakClient).createUser(
            eq(testEmail),
            eq("John"),
            eq("Doe"),
            eq(testPassword),
            eq(false),
            any()
        );
        verify(userRepository).save(any());
        verify(eventPublisher).publishUserCreated(any());
    }

    @Test
    void shouldThrowExceptionWhenUserAlreadyExists() {
        // Given
        when(userRepository.existsByEmail(testEmail)).thenReturn(true);

        Set<UserRole> roles = new HashSet<>();
        roles.add(UserRole.TEACHER);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(
                testEmail,
                "John",
                "Doe",
                roles,
                testPassword,
                null
            );
        });

        verify(keycloakClient, never()).createUser(any(), any(), any(), any(), any(), any());
    }

    @Test
    void shouldThrowExceptionWhenKeycloakUserCreationFails() {
        // Given
        when(userRepository.existsByEmail(testEmail)).thenReturn(false);
        when(keycloakClient.createUser(any(), any(), any(), any(), any(), any()))
            .thenThrow(new KeycloakException("Failed to create user in Keycloak"));

        Set<UserRole> roles = new HashSet<>();
        roles.add(UserRole.TEACHER);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            userService.createUser(
                testEmail,
                "John",
                "Doe",
                roles,
                testPassword,
                null
            );
        });

        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldFindUserById() {
        // Given
        UUID userId = UUID.randomUUID();
        Set<UserRole> roles = new HashSet<>();
        roles.add(UserRole.TEACHER);
        
        User user = new User(
            testKeycloakId,
            testEmail,
            "John",
            "Doe",
            roles,
            null,
            false,
            com.visor.school.userservice.model.AccountStatus.ACTIVE
        );
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        User result = userService.findById(userId);

        // Then
        assertNotNull(result);
        assertEquals(testEmail, result.getEmail());
        verify(userRepository).findById(userId);
    }

    @Test
    void shouldReturnNullWhenUserNotFound() {
        // Given
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When
        User result = userService.findById(userId);

        // Then
        assertNull(result);
    }

    @Test
    void shouldUpdateLastLoginTimestamp() {
        // Given
        UUID userId = UUID.randomUUID();
        Set<UserRole> roles = new HashSet<>();
        roles.add(UserRole.TEACHER);
        
        User user = new User(
            testKeycloakId,
            testEmail,
            "John",
            "Doe",
            roles,
            null,
            false,
            com.visor.school.userservice.model.AccountStatus.ACTIVE
        );
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        userService.updateLastLogin(userId);

        // Then
        assertNotNull(user.getLastLoginAt());
        verify(userRepository).save(user);
    }
}
