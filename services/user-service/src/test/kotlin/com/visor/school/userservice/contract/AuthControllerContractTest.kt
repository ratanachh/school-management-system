package com.visor.school.userservice.contract

import com.fasterxml.jackson.databind.ObjectMapper
import com.visor.school.userservice.controller.AuthController
import com.visor.school.userservice.model.UserRole
import com.visor.school.userservice.service.EmailVerificationService
import com.visor.school.userservice.service.PasswordResetService
import com.visor.school.userservice.service.UserService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(AuthController::class)
class AuthControllerContractTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper
) {

    @MockBean
    private lateinit var userService: UserService

    @MockBean
    private lateinit var emailVerificationService: EmailVerificationService

    @MockBean
    private lateinit var passwordResetService: PasswordResetService

    @Test
    fun `POST /api/v1/auth/register should create user and return 201`() {
        // Given
        val request = mapOf(
            "email" to "test@example.com",
            "firstName" to "John",
            "lastName" to "Doe",
            "role" to "TEACHER",
            "password" to "password123",
            "phoneNumber" to "1234567890"
        )

        // When & Then
        mockMvc.perform(
            post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").exists())
            .andExpect(jsonPath("$.message").exists())
    }

    @Test
    fun `POST /api/v1/auth/verify-email should verify email and return 200`() {
        // Given
        val request = mapOf("token" to "verification-token-123")

        // When & Then
        mockMvc.perform(
            post("/api/v1/auth/verify-email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").exists())
    }

    @Test
    fun `POST /api/v1/auth/login should return login response with token endpoint`() {
        // Given
        val request = mapOf(
            "email" to "test@example.com",
            "password" to "password123"
        )

        // When & Then
        mockMvc.perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.tokenEndpoint").exists())
    }

    @Test
    fun `POST /api/v1/auth/reset-password should initiate password reset and return 200`() {
        // Given
        val request = mapOf("email" to "test@example.com")

        // When & Then
        mockMvc.perform(
            post("/api/v1/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").exists())
    }
}

