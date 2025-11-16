package com.visor.school.userservice.service

import com.visor.school.userservice.integration.KeycloakClient
import com.visor.school.userservice.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Password reset service that delegates to Keycloak Admin API
 */
@Service
@Transactional
class PasswordResetService(
    private val userRepository: UserRepository,
    private val keycloakClient: KeycloakClient,
    private val emailService: EmailService
) {
    private val logger = LoggerFactory.getLogger(PasswordResetService::class.java)

    /**
     * Initiate password reset flow
     * Generates reset token and sends email (Keycloak handles the actual reset)
     */
    fun initiatePasswordReset(email: String) {
        logger.info("Initiating password reset for: $email")

        val user = userRepository.findByEmail(email)
            .orElseThrow { IllegalArgumentException("User not found with email: $email") }

        // Generate reset token (in production, this would be handled by Keycloak)
        val resetToken = generateResetToken()
        
        // Store reset token (in production, use a proper token store)
        // For now, we'll use Keycloak's built-in password reset flow
        
        // Send password reset email
        val resetUrl = "http://localhost:8080/auth/realms/school-management/account/reset-password?token=$resetToken"
        emailService.sendPasswordResetEmail(user.email, user.firstName, resetUrl)

        logger.info("Password reset email sent to: $email")
    }

    /**
     * Reset password using Keycloak Admin API
     */
    fun resetPassword(keycloakId: String, newPassword: String, temporary: Boolean = true) {
        logger.info("Resetting password for Keycloak user: $keycloakId")

        keycloakClient.resetPassword(keycloakId, newPassword, temporary)

        logger.info("Password reset completed for Keycloak user: $keycloakId")
    }

    /**
     * Generate reset token (simplified - in production, Keycloak handles this)
     */
    private fun generateResetToken(): String {
        // In production, this would integrate with Keycloak's password reset flow
        return java.util.UUID.randomUUID().toString()
    }
}

