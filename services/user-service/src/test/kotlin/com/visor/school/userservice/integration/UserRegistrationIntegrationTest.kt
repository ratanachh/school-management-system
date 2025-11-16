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

/**
 * Integration test for user registration flow
 * Tests: Keycloak user creation → User entity creation
 * 
 * Note: This test requires Keycloak to be running or mocked
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserRegistrationIntegrationTest @Autowired constructor(
    private val userService: UserService,
    private val userRepository: UserRepository
) {

    @Test
    fun `should complete user registration flow with Keycloak integration`() {
        // Given
        val email = "integration-test-${System.currentTimeMillis()}@example.com"
        val firstName = "Integration"
        val lastName = "Test"
        val password = "TestPassword123!"

        // When
        val user = userService.createUser(
            email = email,
            firstName = firstName,
            lastName = lastName,
            role = UserRole.STUDENT,
            password = password
        )

        // Then
        assertNotNull(user)
        assertNotNull(user.keycloakId)
        assertFalse(user.keycloakId.isBlank())
        assertEquals(email, user.email)
        assertEquals(firstName, user.firstName)
        assertEquals(lastName, user.lastName)
        assertEquals(UserRole.STUDENT, user.role)

        // Verify user is persisted
        val savedUser = userRepository.findById(user.id)
        assertTrue(savedUser.isPresent)
        assertEquals(email, savedUser.get().email)
    }

    @Test
    fun `should fail registration when email already exists`() {
        // Given
        val email = "duplicate-test-${System.currentTimeMillis()}@example.com"
        userService.createUser(
            email = email,
            firstName = "First",
            lastName = "User",
            role = UserRole.TEACHER,
            password = "Password123!"
        )

        // When & Then
        assertThrows<IllegalArgumentException> {
            userService.createUser(
                email = email,
                firstName = "Second",
                lastName = "User",
                role = UserRole.TEACHER,
                password = "Password123!"
            )
        }
    }
}

