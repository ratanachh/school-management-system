package com.visor.school.userservice.integration

import com.visor.school.userservice.model.UserRole
import com.visor.school.userservice.repository.UserRepository
import com.visor.school.userservice.service.UserService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import org.junit.jupiter.api.Disabled

/**
 * Integration test for login flow through Keycloak token endpoint
 * 
 * Note: This test requires Keycloak to be running or Testcontainers
 * TODO: Add Testcontainers support for Keycloak
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Disabled("Requires Keycloak instance or Testcontainers setup")
class LoginIntegrationTest @Autowired constructor(
    private val userService: UserService,
    private val userRepository: UserRepository
) {

    @Test
    fun `should update last login when user logs in`() {
        // Given - Create user
        val email = "login-test-${System.currentTimeMillis()}@example.com"
        val user = userService.createUser(
            email = email,
            firstName = "Login",
            lastName = "Test",
            roles = setOf(UserRole.TEACHER),
            password = "Password123!"
        )

        assertNull(user.lastLoginAt)

        // When - Update last login (simulating login)
        userService.updateLastLogin(user.id!!)

        // Then
        val updatedUser = userRepository.findById(user.id!!)
        assertTrue(updatedUser.isPresent)
        assertNotNull(updatedUser.get().lastLoginAt)
    }

    @Test
    fun `should find user by email for login`() {
        // Given - Create user
        val email = "login-email-test-${System.currentTimeMillis()}@example.com"
        val user = userService.createUser(
            email = email,
            firstName = "Email",
            lastName = "Test",
            roles = setOf(UserRole.STUDENT),
            password = "Password123!"
        )

        // When
        val foundUser = userService.findByEmail(email)

        // Then
        assertNotNull(foundUser)
        assertEquals(email, foundUser?.email)
        assertEquals(user.id, foundUser?.id)
    }
}

