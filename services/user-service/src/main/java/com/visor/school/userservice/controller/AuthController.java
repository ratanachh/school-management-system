package com.visor.school.userservice.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.visor.school.common.api.ApiResponse;
import com.visor.school.userservice.dto.LoginRequest;
import com.visor.school.userservice.dto.LoginResponse;
import com.visor.school.userservice.dto.PasswordResetRequest;
import com.visor.school.userservice.dto.RefreshTokenRequest;
import com.visor.school.userservice.dto.RegisterRequest;
import com.visor.school.userservice.dto.UserResponse;
import com.visor.school.userservice.dto.VerifyEmailRequest;
import com.visor.school.userservice.model.AccountStatus;
import com.visor.school.userservice.model.User;
import com.visor.school.userservice.service.EmailVerificationService;
import com.visor.school.userservice.service.PasswordResetService;
import com.visor.school.userservice.service.UserService;

import jakarta.validation.Valid;

/**
 * Authentication controller
 * Handles user registration, email verification, login (redirects to Keycloak), and password reset
 */
@RestController
@RequestMapping("/v1/auth")
public class AuthController {

    private final UserService userService;
    private final EmailVerificationService emailVerificationService;
    private final PasswordResetService passwordResetService;

    public AuthController(
        UserService userService,
        EmailVerificationService emailVerificationService,
        PasswordResetService passwordResetService
    ) {
        this.userService = userService;
        this.emailVerificationService = emailVerificationService;
        this.passwordResetService = passwordResetService;
    }

    /**
     * Register a new user
     * Flow: Creates Keycloak user first -> receives keycloakId -> creates User entity
     * Authorization: SUPER_ADMIN or MANAGE_ADMINISTRATORS permission required for ADMINISTRATOR role
     */
    @PostMapping("/register")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasAuthority('MANAGE_ADMINISTRATORS') or (!#request.roles().contains(T(com.visor.school.userservice.model.UserRole).ADMINISTRATOR) and !#request.roles().contains(T(com.visor.school.userservice.model.UserRole).SUPER_ADMIN))")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        User user = userService.createUser(
            request.email(),
            request.firstName(),
            request.lastName(),
            request.roles(),
            request.password(),
            request.phoneNumber()
        );

        // Send verification email
        emailVerificationService.sendVerificationEmail(user);

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(
                UserResponse.from(user),
                "User created successfully. Verification email sent. Please verify your account."
            ));
    }

    /**
     * Verify email with token
     * When email is verified, account status is automatically changed from PENDING to ACTIVE
     */
    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<Map<String, String>>> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        emailVerificationService.verifyEmail(request.token());

        return ResponseEntity.ok(
            ApiResponse.success(
                Map.of("message", "Email verified successfully. Your account has been activated and you can now log in."),
                "Email verified successfully. Your account has been activated."
            )
        );
    }

    /**
     * Login - Authenticates user with Keycloak and returns JWT tokens
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        User user = userService.findByEmail(request.email());
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        // Check if account is active
        if (user.getAccountStatus() != AccountStatus.ACTIVE) {
            String msg = switch (user.getAccountStatus()) {
                case PENDING -> "Your account is pending approval. Please wait for administrator approval or verify your email.";
                case INACTIVE -> "Your account is inactive. Please contact administrator.";
                case SUSPENDED -> "Your account has been suspended. Please contact administrator.";
                case DELETED -> "Your account has been deleted. Please contact administrator.";
                default -> "Your account is not active. Please contact administrator.";
            };
            throw new IllegalStateException(msg);
        }

        // Authenticate with Keycloak and get tokens
        LoginResponse loginResponse = userService.authenticateUser(request.email(), request.password());

        // Update last login
        userService.updateLastLogin(user.getId());

        return ResponseEntity.ok(
            ApiResponse.success(
                loginResponse,
                "Login successful"
            )
        );
    }

    /**
     * Request password reset
     */
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Map<String, String>>> requestPasswordReset(@Valid @RequestBody PasswordResetRequest request) {
        passwordResetService.initiatePasswordReset(request.email());

        return ResponseEntity.ok(
            ApiResponse.success(
                Map.of("message", "Password reset email sent"),
                "Password reset email sent successfully"
            )
        );
    }

    /**
     * Refresh access token using a refresh token.
     * Returns a new access token and optionally a new refresh token if rotation is enabled.
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        LoginResponse loginResponse = userService.refreshToken(request.refreshToken());
        return ResponseEntity.ok(
            ApiResponse.success(
                loginResponse,
                "Token refreshed successfully"
            )
        );
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> getMe() {
        User user = userService.getMe();
        if (user == null) {
            throw new IllegalStateException("User not found");
        }

        return ResponseEntity.ok(
            ApiResponse.success(
                UserResponse.from(user),
                "User profile retrieved successfully"
            )
        );
    }
}
