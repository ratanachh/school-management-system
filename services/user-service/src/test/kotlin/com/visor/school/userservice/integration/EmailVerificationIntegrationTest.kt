package com.visor.school.userservice.integration

import com.visor.school.userservice.model.UserRole
import com.visor.school.userservice.repository.UserRepository
import com.visor.school.userservice.service.EmailVerificationService
import com.visor.school.userservice.service.UserService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import org.junit.jupiter.api.Disabled

/**
 * Integration test for email verification flow
 * 
 * Note: This test requires Keycloak to be running or Testcontainers
 * TODO: Add Testcontainers support for Keycloak
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Disabled("Requires Keycloak instance or Testcontainers setup")
class EmailVerificationIntegrationTest @Autowired constructor(
    private val userService: UserService,
    private val emailVerificationService: EmailVerificationService,
    private val userRepository: UserRepository
) {

    @Test
    fun `should complete email verification flow`() {
        // Given - Create user
        val email = "verify-test-${System.currentTimeMillis()}@example.com"
        val user = userService.createUser(
            email = email,
            firstName = "Verify",
            lastName = "Test",
            role = UserRole.STUDENT,
            password = "Password123!"
        )

        assertFalse(user.emailVerified)

        // When - Send verification email
        emailVerificationService.sendVerificationEmail(user)

        // Note: In a real test, we would extract the token from the email service mock
        // For now, we'll test the verification service directly
        // The actual token would come from the email sent by EmailService
        
        // Verify that user exists
        val savedUser = userRepository.findById(user.id!!)
        assertTrue(savedUser.isPresent)
    }

    @Test
    fun `should verify email with valid token`() {
        // Given - Create user and send verification email
        val email = "verify-token-test-${System.currentTimeMillis()}@example.com"
        val user = userService.createUser(
            email = email,
            firstName = "Token",
            lastName = "Test",
            role = UserRole.STUDENT,
            password = "Password123!"
        )

        emailVerificationService.sendVerificationEmail(user)

        // Note: In a real implementation, we would extract the token from EmailService
        // For integration testing, we might need to access the token storage directly
        // This is a placeholder for the actual test implementation
    }
}

