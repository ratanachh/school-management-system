package com.visor.school.userservice.service

import com.visor.school.userservice.integration.KeycloakClient
import com.visor.school.userservice.model.User
import com.visor.school.userservice.model.UserRole
import com.visor.school.userservice.repository.UserRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class PasswordResetServiceTest {

    @Mock
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var keycloakClient: KeycloakClient

    @Mock
    private lateinit var emailService: EmailService

    @InjectMocks
    private lateinit var passwordResetService: PasswordResetService

    private lateinit var testUser: User

    @BeforeEach
    fun setup() {
        passwordResetService = PasswordResetService(userRepository, keycloakClient, emailService)
        testUser = User(
            keycloakId = "keycloak-123",
            email = "test@example.com",
            role = UserRole.TEACHER,
            firstName = "John",
            lastName = "Doe"
        )
    }

    @Test
    fun `should initiate password reset successfully`() {
        // Given
        whenever(userRepository.findByEmail(testUser.email)).thenReturn(Optional.of(testUser))
        doNothing().whenever(emailService).sendPasswordResetEmail(any(), any(), any())

        // When
        passwordResetService.initiatePasswordReset(testUser.email)

        // Then
        verify(userRepository).findByEmail(testUser.email)
        verify(emailService).sendPasswordResetEmail(
            eq(testUser.email),
            eq(testUser.firstName),
            any()
        )
    }

    @Test
    fun `should throw exception when user not found for password reset`() {
        // Given
        whenever(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty())

        // When & Then
        assertThrows<IllegalArgumentException> {
            passwordResetService.initiatePasswordReset("nonexistent@example.com")
        }

        verify(emailService, never()).sendPasswordResetEmail(any(), any(), any())
    }

    @Test
    fun `should reset password via Keycloak`() {
        // Given
        val keycloakId = "keycloak-123"
        val newPassword = "newPassword123"
        doNothing().whenever(keycloakClient).resetPassword(any(), any(), any())

        // When
        passwordResetService.resetPassword(keycloakId, newPassword, temporary = false)

        // Then
        verify(keycloakClient).resetPassword(keycloakId, newPassword, false)
    }
}

