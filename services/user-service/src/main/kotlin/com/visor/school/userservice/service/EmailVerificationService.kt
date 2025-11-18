package com.visor.school.userservice.service

import com.visor.school.userservice.integration.KeycloakClient
import com.visor.school.userservice.model.AccountStatus
import com.visor.school.userservice.model.User
import com.visor.school.userservice.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.SecureRandom
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Email verification service with token generation and validation
 * When email is verified, account status is changed from PENDING to ACTIVE
 */
@Service
@Transactional
class EmailVerificationService(
    private val userRepository: UserRepository,
    private val emailService: EmailService,
    private val keycloakClient: KeycloakClient,
    @Value("\${email.verification.token.expiry.hours:24}") private val tokenExpiryHours: Long
) {
    private val logger = LoggerFactory.getLogger(EmailVerificationService::class.java)
    private val verificationTokens = ConcurrentHashMap<String, VerificationToken>()

    private val random = SecureRandom()

    /**
     * Generate and send verification email
     */
    fun sendVerificationEmail(user: User) {
        logger.info("Generating verification token for user: ${user.id}")

        val token = generateToken()
        val expiresAt = Instant.now().plus(tokenExpiryHours, ChronoUnit.HOURS)

        verificationTokens[token] = VerificationToken(
            userId = user.id!!,
            email = user.email,
            expiresAt = expiresAt
        )

        val verificationUrl = "http://localhost:8080/api/v1/auth/verify-email?token=$token"
        
        emailService.sendVerificationEmail(user.email, user.firstName, verificationUrl)
        
        logger.info("Verification email sent to: ${user.email}")
    }

    /**
     * Verify email with token
     */
    fun verifyEmail(token: String): Boolean {
        logger.info("Verifying email with token")

        val verificationToken = verificationTokens[token]
            ?: throw IllegalArgumentException("Invalid verification token")

        if (verificationToken.expiresAt.isBefore(Instant.now())) {
            verificationTokens.remove(token)
            throw IllegalArgumentException("Verification token has expired")
        }

        val user = userRepository.findById(verificationToken.userId)
            .orElseThrow { IllegalArgumentException("User not found") }

        user.verifyEmail()
        
        // Activate account if it was pending
        if (user.accountStatus == AccountStatus.PENDING) {
            user.updateStatus(AccountStatus.ACTIVE)
            logger.info("Account activated for user: ${user.id} (email verified)")
        }
        
        userRepository.save(user)

        // Update Keycloak email verification status
        keycloakClient.updateEmailVerification(user.keycloakId, true)

        verificationTokens.remove(token)
        logger.info("Email verified and account activated for user: ${user.id}")

        return true
    }

    /**
     * Generate secure random token
     */
    private fun generateToken(): String {
        val bytes = ByteArray(32)
        random.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    /**
     * Clean up expired tokens (should be called periodically)
     */
    fun cleanupExpiredTokens() {
        val now = Instant.now()
        verificationTokens.entries.removeIf { (_, token) -> token.expiresAt.isBefore(now) }
        logger.debug("Cleaned up expired verification tokens")
    }

    private data class VerificationToken(
        val userId: UUID,
        val email: String,
        val expiresAt: Instant
    )
}

