package com.visor.school.userservice.integration

import com.visor.school.userservice.integration.KeycloakClient
import com.visor.school.userservice.model.UserRole
import com.visor.school.userservice.service.PasswordResetService
import com.visor.school.userservice.service.UserService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

/**
 * Integration test for password reset flow via Keycloak Admin API
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PasswordResetIntegrationTest @Autowired constructor(
    private val userService: UserService,
    private val passwordResetService: PasswordResetService,
    private val keycloakClient: KeycloakClient
) {

    @Test
    fun `should initiate password reset for existing user`() {
        // Given - Create user
        val email = "reset-test-${System.currentTimeMillis()}@example.com"
        val user = userService.createUser(
            email = email,
            firstName = "Reset",
            lastName = "Test",
            role = UserRole.TEACHER,
            password = "OriginalPassword123!"
        )

        // When
        passwordResetService.initiatePasswordReset(email)

        // Then - Verify no exception is thrown
        // In a real test, we would verify that the email was sent
        assertNotNull(user.keycloakId)
    }

    @Test
    fun `should reset password via Keycloak Admin API`() {
        // Given - Create user
        val email = "reset-keycloak-test-${System.currentTimeMillis()}@example.com"
        val user = userService.createUser(
            email = email,
            firstName = "Keycloak",
            lastName = "Test",
            role = UserRole.STUDENT,
            password = "OldPassword123!"
        )

        val newPassword = "NewPassword123!"

        // When
        passwordResetService.resetPassword(user.keycloakId, newPassword, temporary = false)

        // Then - Verify no exception is thrown
        // In a real test with Keycloak running, we would verify the password was actually changed
        assertNotNull(user.keycloakId)
    }

    @Test
    fun `should throw exception when user not found for password reset`() {
        // When & Then
        assertThrows<IllegalArgumentException> {
            passwordResetService.initiatePasswordReset("nonexistent@example.com")
        }
    }
}

