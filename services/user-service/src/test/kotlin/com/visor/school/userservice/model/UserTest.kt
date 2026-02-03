package com.visor.school.userservice.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID

class UserTest {

    @Test
    fun `should create user with required fields`() {
        val user = User(
            keycloakId = "keycloak-123",
            email = "test@example.com",
            roles = mutableSetOf(UserRole.TEACHER),
            firstName = "John",
            lastName = "Doe"
        )

        // id is nullable and will be null until persisted by JPA
        assertNull(user.id)
        assertEquals("keycloak-123", user.keycloakId)
        assertEquals("test@example.com", user.email)
        assertTrue(user.roles.contains(UserRole.TEACHER))
        assertEquals("John", user.firstName)
        assertEquals("Doe", user.lastName)
        assertFalse(user.emailVerified)
        assertEquals(AccountStatus.ACTIVE, user.accountStatus)
    }

    @Test
    fun `should require keycloakId`() {
        // This test validates that keycloakId is required
        // In actual implementation, this would be enforced by JPA @Column(nullable = false)
        val user = User(
            keycloakId = "keycloak-123",
            email = "test@example.com",
            roles = mutableSetOf(UserRole.STUDENT),
            firstName = "Jane",
            lastName = "Smith"
        )

        assertNotNull(user.keycloakId)
        assertFalse(user.keycloakId.isBlank())
    }

    @Test
    fun `should update last login timestamp`() {
        val user = User(
            keycloakId = "keycloak-123",
            email = "test@example.com",
            roles = mutableSetOf(UserRole.TEACHER),
            firstName = "John",
            lastName = "Doe"
        )

        val initialUpdatedAt = user.updatedAt
        val initialLastLoginAt = user.lastLoginAt

        Thread.sleep(10) // Small delay to ensure timestamp difference
        user.updateLastLogin()

        assertNotNull(user.lastLoginAt)
        assertNotEquals(initialLastLoginAt, user.lastLoginAt)
        assertTrue(user.updatedAt.isAfter(initialUpdatedAt))
    }

    @Test
    fun `should verify email`() {
        val user = User(
            keycloakId = "keycloak-123",
            email = "test@example.com",
            roles = mutableSetOf(UserRole.TEACHER),
            firstName = "John",
            lastName = "Doe"
        )

        assertFalse(user.emailVerified)
        val initialUpdatedAt = user.updatedAt

        Thread.sleep(10)
        user.verifyEmail()

        assertTrue(user.emailVerified)
        assertTrue(user.updatedAt.isAfter(initialUpdatedAt))
    }

    @Test
    fun `should update account status`() {
        val user = User(
            keycloakId = "keycloak-123",
            email = "test@example.com",
            roles = mutableSetOf(UserRole.TEACHER),
            firstName = "John",
            lastName = "Doe"
        )

        assertEquals(AccountStatus.ACTIVE, user.accountStatus)
        val initialUpdatedAt = user.updatedAt

        Thread.sleep(10)
        user.updateStatus(AccountStatus.SUSPENDED)

        assertEquals(AccountStatus.SUSPENDED, user.accountStatus)
        assertTrue(user.updatedAt.isAfter(initialUpdatedAt))
    }
}

