package com.visor.school.userservice.service

import com.visor.school.userservice.model.AccountStatus
import com.visor.school.userservice.model.User
import com.visor.school.userservice.model.UserRole
import com.visor.school.userservice.repository.UserRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import java.time.Instant
import java.util.Optional
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class EmailVerificationServiceTest {

    @Mock
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var emailService: EmailService

    @InjectMocks
    private lateinit var emailVerificationService: EmailVerificationService

    private lateinit var testUser: User

    @BeforeEach
    fun setup() {
        emailVerificationService = EmailVerificationService(
            userRepository = userRepository,
            emailService = emailService,
            tokenExpiryHours = 24
        )
        testUser = User(
            keycloakId = "keycloak-123",
            email = "test@example.com",
            role = UserRole.TEACHER,
            firstName = "John",
            lastName = "Doe",
            emailVerified = false
        )
    }

    @Test
    fun `should send verification email successfully`() {
        // Given
        doNothing().whenever(emailService).sendVerificationEmail(any(), any(), any())

        // When
        emailVerificationService.sendVerificationEmail(testUser)

        // Then
        verify(emailService).sendVerificationEmail(
            eq(testUser.email),
            eq(testUser.firstName),
            any()
        )
    }

    @Test
    fun `should verify email with valid token`() {
        // Given
        emailVerificationService.sendVerificationEmail(testUser)
        val token = emailVerificationService.javaClass.getDeclaredField("verificationTokens")
            .apply { isAccessible = true }
            .get(emailVerificationService) as java.util.Map<*, *>
            .keys.first().toString()

        whenever(userRepository.findById(testUser.id)).thenReturn(Optional.of(testUser))
        whenever(userRepository.save(any())).thenAnswer { it.arguments[0] as User }

        // When
        val result = emailVerificationService.verifyEmail(token)

        // Then
        assertTrue(result)
        assertTrue(testUser.emailVerified)
        verify(userRepository).save(testUser)
    }

    @Test
    fun `should throw exception for invalid token`() {
        // When & Then
        assertThrows<IllegalArgumentException> {
            emailVerificationService.verifyEmail("invalid-token")
        }
    }

    @Test
    fun `should throw exception for expired token`() {
        // Given
        emailVerificationService.sendVerificationEmail(testUser)
        val tokens = emailVerificationService.javaClass.getDeclaredField("verificationTokens")
            .apply { isAccessible = true }
            .get(emailVerificationService) as java.util.Map<String, *>

        // Manually expire the token
        val token = tokens.keys.first()
        val tokenData = tokens[token]
        val tokenClass = tokenData!!::class.java
        val expiresAtField = tokenClass.getDeclaredField("expiresAt")
        expiresAtField.isAccessible = true
        expiresAtField.set(tokenData, Instant.now().minusSeconds(1))

        // When & Then
        assertThrows<IllegalArgumentException> {
            emailVerificationService.verifyEmail(token)
        }
    }

    @Test
    fun `should cleanup expired tokens`() {
        // Given
        emailVerificationService.sendVerificationEmail(testUser)
        val tokens = emailVerificationService.javaClass.getDeclaredField("verificationTokens")
            .apply { isAccessible = true }
            .get(emailVerificationService) as java.util.Map<String, *>

        // Manually expire the token
        val token = tokens.keys.first()
        val tokenData = tokens[token]
        val tokenClass = tokenData!!::class.java
        val expiresAtField = tokenClass.getDeclaredField("expiresAt")
        expiresAtField.isAccessible = true
        expiresAtField.set(tokenData, Instant.now().minusSeconds(1))

        // When
        emailVerificationService.cleanupExpiredTokens()

        // Then
        assertTrue(tokens.isEmpty())
    }
}

