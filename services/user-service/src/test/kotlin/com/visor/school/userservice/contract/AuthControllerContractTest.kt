package com.visor.school.userservice.contract

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.visor.school.userservice.controller.AuthController
import com.visor.school.userservice.dto.LoginResponse
import com.visor.school.userservice.model.AccountStatus
import com.visor.school.userservice.model.User
import com.visor.school.userservice.model.UserRole
import com.visor.school.userservice.service.EmailVerificationService
import com.visor.school.userservice.service.PasswordResetService
import com.visor.school.userservice.service.UserService
import java.util.UUID
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.whenever
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

@ExtendWith(MockitoExtension::class)
class AuthControllerContractTest {

    @Mock
    private lateinit var userService: UserService

    @Mock
    private lateinit var emailVerificationService: EmailVerificationService

    @Mock
    private lateinit var passwordResetService: PasswordResetService

    private lateinit var mockMvc: MockMvc
    private val objectMapper: ObjectMapper = jacksonObjectMapper()

    @BeforeEach
    fun setup() {
        val authController = AuthController(userService, emailVerificationService, passwordResetService)
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build()
    }

    @Test
    fun `POST api v1 auth register should create user and return 201`() {
        // Given
        val request = mapOf(
            "email" to "test@example.com",
            "firstName" to "John",
            "lastName" to "Doe",
            "role" to "TEACHER",
            "password" to "password123",
            "phoneNumber" to "1234567890"
        )
        val testUser = User(
            id = UUID.randomUUID(),
            keycloakId = "keycloak-123",
            email = "test@example.com",
            role = UserRole.TEACHER,
            firstName = "John",
            lastName = "Doe",
            phoneNumber = "1234567890"
        )
        whenever(userService.createUser(any(), any(), any(), any(), any(), any())).thenReturn(testUser)
        doNothing().whenever(emailVerificationService).sendVerificationEmail(any())

        // When & Then
        mockMvc.perform(
            post("/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").exists())
            .andExpect(jsonPath("$.message").exists())
    }

    @Test
    fun `POST api v1 auth verify-email should verify email and return 200`() {
        // Given
        val request = mapOf("token" to "verification-token-123")
        whenever(emailVerificationService.verifyEmail(any())).thenReturn(true)

        // When & Then
        mockMvc.perform(
            post("/v1/auth/verify-email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").exists())
    }

    @Test
    fun `POST api v1 auth login should return login response with token endpoint`() {
        // Given
        val request = mapOf(
            "email" to "test@example.com",
            "password" to "password123"
        )
        val testUser = User(
            id = UUID.randomUUID(),
            keycloakId = "keycloak-123",
            email = "test@example.com",
            role = UserRole.TEACHER,
            firstName = "John",
            lastName = "Doe",
            accountStatus = AccountStatus.ACTIVE
        )
        val loginResponse = LoginResponse(
            accessToken = "access-token",
            refreshToken = "refresh-token",
            expiresIn = 300,
            refreshExpiresIn = 1800
        )
        whenever(userService.findByEmail(any())).thenReturn(testUser)
        whenever(userService.authenticateUser(any(), any())).thenReturn(loginResponse)
        doNothing().whenever(userService).updateLastLogin(any())

        // When & Then
        mockMvc.perform(
            post("/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.accessToken").exists())
            .andExpect(jsonPath("$.data.refreshToken").exists())
    }

    @Test
    fun `POST api v1 auth reset-password should initiate password reset and return 200`() {
        // Given
        val request = mapOf("email" to "test@example.com")
        doNothing().whenever(passwordResetService).initiatePasswordReset(any())

        // When & Then
        mockMvc.perform(
            post("/v1/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").exists())
    }

    @Test
    fun `POST v1 auth refresh-token should refresh token and return 200`() {
        // Given
        val request = mapOf("refreshToken" to "refresh-token-123")
        val loginResponse = LoginResponse(
            accessToken = "new-access-token",
            refreshToken = "new-refresh-token",
            expiresIn = 300,
            refreshExpiresIn = 1800
        )
        whenever(userService.refreshToken(any())).thenReturn(loginResponse)

        // When & Then
        mockMvc.perform(
            post("/v1/auth/refresh-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.accessToken").value("new-access-token"))
            .andExpect(jsonPath("$.data.refreshToken").value("new-refresh-token"))
    }

    @Test
    fun `GET v1 auth me should return user profile and 200`() {
        // Given
        val testUser = User(
            id = UUID.randomUUID(),
            keycloakId = "keycloak-123",
            email = "test@example.com",
            role = UserRole.TEACHER,
            firstName = "John",
            lastName = "Doe"
        )
        whenever(userService.getMe()).thenReturn(testUser)

        // When & Then
        mockMvc.perform(
            get("/v1/auth/me")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.email").value("test@example.com"))
            .andExpect(jsonPath("$.data.firstName").value("John"))
            .andExpect(jsonPath("$.data.lastName").value("Doe"))
    }
}
