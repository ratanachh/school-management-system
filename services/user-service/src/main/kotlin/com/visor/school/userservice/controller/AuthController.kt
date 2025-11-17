package com.visor.school.userservice.controller

import com.visor.school.common.api.ApiResponse
import com.visor.school.userservice.dto.LoginRequest
import com.visor.school.userservice.dto.LoginResponse
import com.visor.school.userservice.dto.PasswordResetRequest
import com.visor.school.userservice.dto.RegisterRequest
import com.visor.school.userservice.dto.UserResponse
import com.visor.school.userservice.dto.VerifyEmailRequest
import com.visor.school.userservice.service.EmailVerificationService
import com.visor.school.userservice.service.PasswordResetService
import com.visor.school.userservice.service.UserService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

/**
 * Authentication controller
 * Handles user registration, email verification, login (redirects to Keycloak), and password reset
 */
@RestController
@RequestMapping("/v1/auth")
class AuthController(
    private val userService: UserService,
    private val emailVerificationService: EmailVerificationService,
    private val passwordResetService: PasswordResetService
) {

    /**
     * Register a new user
     * Flow: Creates Keycloak user first → receives keycloakId → creates User entity
     * Authorization: SUPER_ADMIN or MANAGE_ADMINISTRATORS permission required for ADMINISTRATOR role
     */
    @PostMapping("/register")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasPermission(null, 'MANAGE_ADMINISTRATORS') or #request.role != T(com.visor.school.userservice.model.UserRole).ADMINISTRATOR and #request.role != T(com.visor.school.userservice.model.UserRole).SUPER_ADMIN")
    fun register(@Valid @RequestBody request: RegisterRequest): ResponseEntity<ApiResponse<UserResponse>> {
        val user = userService.createUser(
            email = request.email,
            firstName = request.firstName,
            lastName = request.lastName,
            role = request.role,
            password = request.password,
            phoneNumber = request.phoneNumber
        )

        // Send verification email
        emailVerificationService.sendVerificationEmail(user)

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(UserResponse.from(user), "User created successfully. Verification email sent."))
    }

    /**
     * Verify email with token
     */
    @PostMapping("/verify-email")
    fun verifyEmail(@Valid @RequestBody request: VerifyEmailRequest): ResponseEntity<ApiResponse<Map<String, String>>> {
        emailVerificationService.verifyEmail(request.token)

        return ResponseEntity.ok(
            ApiResponse.success(
                mapOf("message" to "Email verified successfully"),
                "Email verified successfully"
            )
        )
    }

    /**
     * Login - Authenticates user with Keycloak and returns JWT tokens
     */
    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<ApiResponse<LoginResponse>> {
        val user = userService.findByEmail(request.email)
            ?: throw IllegalArgumentException("User not found")

        // Authenticate with Keycloak and get tokens
        val loginResponse = userService.authenticateUser(request.email, request.password)

        // Update last login
        userService.updateLastLogin(user.id!!)

        return ResponseEntity.ok(
            ApiResponse.success(
                loginResponse,
                "Login successful"
            )
        )
    }

    /**
     * Request password reset
     */
    @PostMapping("/reset-password")
    fun requestPasswordReset(@Valid @RequestBody request: PasswordResetRequest): ResponseEntity<ApiResponse<Map<String, String>>> {
        passwordResetService.initiatePasswordReset(request.email)

        return ResponseEntity.ok(
            ApiResponse.success(
                mapOf("message" to "Password reset email sent"),
                "Password reset email sent successfully"
            )
        )
    }
}

