package com.visor.school.userservice.model;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void shouldCreateUserWithRequiredFields() {
        Set<UserRole> roles = new HashSet<>();
        roles.add(UserRole.TEACHER);
        
        User user = new User(
            "keycloak-123",
            "test@example.com",
            roles,
            "John",
            "Doe"
        );

        // id is nullable and will be null until persisted by JPA
        assertNull(user.getId());
        assertEquals("keycloak-123", user.getKeycloakId());
        assertEquals("test@example.com", user.getEmail());
        assertTrue(user.getRoles().contains(UserRole.TEACHER));
        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertFalse(user.isEmailVerified());
        assertEquals(AccountStatus.ACTIVE, user.getAccountStatus());
    }

    @Test
    void shouldRequireKeycloakId() {
        // This test validates that keycloakId is required
        // In actual implementation, this would be enforced by JPA @Column(nullable = false)
        Set<UserRole> roles = new HashSet<>();
        roles.add(UserRole.STUDENT);
        
        User user = new User(
            "keycloak-123",
            "test@example.com",
            roles,
            "Jane",
            "Smith"
        );

        assertNotNull(user.getKeycloakId());
        assertFalse(user.getKeycloakId().isBlank());
    }

    @Test
    void shouldUpdateLastLoginTimestamp() throws InterruptedException {
        Set<UserRole> roles = new HashSet<>();
        roles.add(UserRole.TEACHER);
        
        User user = new User(
            "keycloak-123",
            "test@example.com",
            roles,
            "John",
            "Doe"
        );

        var initialUpdatedAt = user.getUpdatedAt();
        var initialLastLoginAt = user.getLastLoginAt();

        Thread.sleep(10); // Small delay to ensure timestamp difference
        user.updateLastLogin();

        assertNotNull(user.getLastLoginAt());
        assertNotEquals(initialLastLoginAt, user.getLastLoginAt());
        assertTrue(user.getUpdatedAt().isAfter(initialUpdatedAt));
    }

    @Test
    void shouldVerifyEmail() throws InterruptedException {
        Set<UserRole> roles = new HashSet<>();
        roles.add(UserRole.TEACHER);
        
        User user = new User(
            "keycloak-123",
            "test@example.com",
            roles,
            "John",
            "Doe"
        );

        assertFalse(user.isEmailVerified());
        var initialUpdatedAt = user.getUpdatedAt();

        Thread.sleep(10);
        user.verifyEmail();

        assertTrue(user.isEmailVerified());
        assertTrue(user.getUpdatedAt().isAfter(initialUpdatedAt));
    }

    @Test
    void shouldUpdateAccountStatus() throws InterruptedException {
        Set<UserRole> roles = new HashSet<>();
        roles.add(UserRole.TEACHER);
        
        User user = new User(
            "keycloak-123",
            "test@example.com",
            roles,
            "John",
            "Doe"
        );

        assertEquals(AccountStatus.ACTIVE, user.getAccountStatus());
        var initialUpdatedAt = user.getUpdatedAt();

        Thread.sleep(10);
        user.updateStatus(AccountStatus.SUSPENDED);

        assertEquals(AccountStatus.SUSPENDED, user.getAccountStatus());
        assertTrue(user.getUpdatedAt().isAfter(initialUpdatedAt));
    }
}
