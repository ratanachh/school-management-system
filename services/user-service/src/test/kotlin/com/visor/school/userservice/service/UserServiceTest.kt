package com.visor.school.userservice.service

import com.visor.school.userservice.event.UserEventPublisher
import com.visor.school.userservice.integration.KeycloakClient
import com.visor.school.userservice.integration.KeycloakException
import com.visor.school.userservice.integration.UserAlreadyExistsException
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
import java.util.Optional
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class UserServiceTest {

    @Mock
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var keycloakClient: KeycloakClient

    @Mock
    private lateinit var eventPublisher: UserEventPublisher

    @InjectMocks
    private lateinit var userService: UserService

    private val testKeycloakId = "keycloak-123"
    private val testEmail = "test@example.com"
    private val testPassword = "password123"

    @BeforeEach
    fun setup() {
        userService = UserService(userRepository, keycloakClient, eventPublisher)
    }

    @Test
    fun `should create user successfully with Keycloak integration`() {
        // Given
        whenever(userRepository.existsByEmail(testEmail)).thenReturn(false)
        whenever(keycloakClient.createUser(
            email = testEmail,
            firstName = "John",
            lastName = "Doe",
            password = testPassword,
            emailVerified = false
        )).thenReturn(testKeycloakId)
        whenever(userRepository.save(any())).thenAnswer { it.arguments[0] as User }
        doNothing().whenever(eventPublisher).publishUserCreated(any())

        // When
        val result = userService.createUser(
            email = testEmail,
            firstName = "John",
            lastName = "Doe",
            role = UserRole.TEACHER,
            password = testPassword
        )

        // Then
        assertNotNull(result)
        assertEquals(testKeycloakId, result.keycloakId)
        assertEquals(testEmail, result.email)
        assertEquals(UserRole.TEACHER, result.role)
        verify(keycloakClient).createUser(
            email = testEmail,
            firstName = "John",
            lastName = "Doe",
            password = testPassword,
            emailVerified = false
        )
        verify(userRepository).save(any())
        verify(eventPublisher).publishUserCreated(any())
    }

    @Test
    fun `should throw exception when user already exists`() {
        // Given
        whenever(userRepository.existsByEmail(testEmail)).thenReturn(true)

        // When & Then
        assertThrows<IllegalArgumentException> {
            userService.createUser(
                email = testEmail,
                firstName = "John",
                lastName = "Doe",
                role = UserRole.TEACHER,
                password = testPassword
            )
        }

        verify(keycloakClient, never()).createUser(any(), any(), any(), any(), any())
    }

    @Test
    fun `should throw exception when Keycloak user creation fails`() {
        // Given
        whenever(userRepository.existsByEmail(testEmail)).thenReturn(false)
        whenever(keycloakClient.createUser(any(), any(), any(), any(), any()))
            .thenThrow(KeycloakException("Failed to create user in Keycloak"))

        // When & Then
        assertThrows<KeycloakException> {
            userService.createUser(
                email = testEmail,
                firstName = "John",
                lastName = "Doe",
                role = UserRole.TEACHER,
                password = testPassword
            )
        }

        verify(userRepository, never()).save(any())
    }

    @Test
    fun `should find user by id`() {
        // Given
        val userId = UUID.randomUUID()
        val user = User(
            keycloakId = testKeycloakId,
            email = testEmail,
            role = UserRole.TEACHER,
            firstName = "John",
            lastName = "Doe"
        )
        whenever(userRepository.findById(userId)).thenReturn(Optional.of(user))

        // When
        val result = userService.findById(userId)

        // Then
        assertNotNull(result)
        assertEquals(testEmail, result?.email)
        verify(userRepository).findById(userId)
    }

    @Test
    fun `should return null when user not found`() {
        // Given
        val userId = UUID.randomUUID()
        whenever(userRepository.findById(userId)).thenReturn(Optional.empty())

        // When
        val result = userService.findById(userId)

        // Then
        assertNull(result)
    }

    @Test
    fun `should update last login timestamp`() {
        // Given
        val userId = UUID.randomUUID()
        val user = User(
            keycloakId = testKeycloakId,
            email = testEmail,
            role = UserRole.TEACHER,
            firstName = "John",
            lastName = "Doe"
        )
        whenever(userRepository.findById(userId)).thenReturn(Optional.of(user))
        whenever(userRepository.save(any())).thenAnswer { it.arguments[0] as User }

        // When
        userService.updateLastLogin(userId)

        // Then
        assertNotNull(user.lastLoginAt)
        verify(userRepository).save(user)
    }
}

