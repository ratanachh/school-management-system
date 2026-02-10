package com.visor.school.userservice.contract;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.visor.school.userservice.controller.AuthController;
import com.visor.school.userservice.dto.LoginResponse;
import com.visor.school.userservice.model.AccountStatus;
import com.visor.school.userservice.model.User;
import com.visor.school.userservice.model.UserRole;
import com.visor.school.userservice.service.EmailVerificationService;
import com.visor.school.userservice.service.PasswordResetService;
import com.visor.school.userservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerContractTest {

    @Mock
    private UserService userService;

    @Mock
    private EmailVerificationService emailVerificationService;

    @Mock
    private PasswordResetService passwordResetService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        AuthController authController = new AuthController(userService, emailVerificationService, passwordResetService);
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    void postApiV1AuthRegisterShouldCreateUserAndReturn201() throws Exception {
        // Given
        Map<String, Object> request = Map.of(
            "email", "test@example.com",
            "firstName", "John",
            "lastName", "Doe",
            "roles", java.util.List.of("TEACHER"),
            "password", "password123",
            "phoneNumber", "1234567890"
        );
        
        Set<UserRole> roles = new HashSet<>();
        roles.add(UserRole.TEACHER);
        
        User testUser = new User(
            "keycloak-123",
            "test@example.com",
            roles,
            "John",
            "Doe"
        );
        testUser.setPhoneNumber("1234567890");
        
        // Set ID using reflection
        Field idField = User.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(testUser, UUID.randomUUID());
        
        when(userService.createUser(any(), any(), any(), any(), any(), any())).thenReturn(testUser);
        doNothing().when(emailVerificationService).sendVerificationEmail(any());

        // When & Then
        mockMvc.perform(
            post("/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").exists())
            .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void postApiV1AuthVerifyEmailShouldVerifyEmailAndReturn200() throws Exception {
        // Given
        Map<String, String> request = Map.of("token", "verification-token-123");
        when(emailVerificationService.verifyEmail(any())).thenReturn(true);

        // When & Then
        mockMvc.perform(
            post("/v1/auth/verify-email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void postApiV1AuthLoginShouldReturnLoginResponseWithTokenEndpoint() throws Exception {
        // Given
        Map<String, String> request = Map.of(
            "email", "test@example.com",
            "password", "password123"
        );
        
        Set<UserRole> roles = new HashSet<>();
        roles.add(UserRole.TEACHER);
        
        User testUser = new User(
            "keycloak-123",
            "test@example.com",
            roles,
            "John",
            "Doe"
        );
        testUser.updateStatus(AccountStatus.ACTIVE);
        
        // Set ID using reflection
        Field idField = User.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(testUser, UUID.randomUUID());
        
        LoginResponse loginResponse = new LoginResponse(
            "access-token",
            "refresh-token",
            300,
            1800,
            "Bearer"
        );
        
        when(userService.findByEmail(any())).thenReturn(testUser);
        when(userService.authenticateUser(any(), any())).thenReturn(loginResponse);
        doNothing().when(userService).updateLastLogin(any());

        // When & Then
        mockMvc.perform(
            post("/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.accessToken").exists())
            .andExpect(jsonPath("$.data.refreshToken").exists());
    }

    @Test
    void postApiV1AuthResetPasswordShouldInitiatePasswordResetAndReturn200() throws Exception {
        // Given
        Map<String, String> request = Map.of("email", "test@example.com");
        doNothing().when(passwordResetService).initiatePasswordReset(any());

        // When & Then
        mockMvc.perform(
            post("/v1/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void postV1AuthRefreshTokenShouldRefreshTokenAndReturn200() throws Exception {
        // Given
        Map<String, String> request = Map.of("refreshToken", "refresh-token-123");
        LoginResponse loginResponse = new LoginResponse(
            "new-access-token",
            "new-refresh-token",
            300,
            1800,
            "Bearer"
        );
        when(userService.refreshToken(any())).thenReturn(loginResponse);

        // When & Then
        mockMvc.perform(
            post("/v1/auth/refresh-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.accessToken").value("new-access-token"))
            .andExpect(jsonPath("$.data.refreshToken").value("new-refresh-token"));
    }

    @Test
    void getV1AuthMeShouldReturnUserProfileAndReturn200() throws Exception {
        // Given
        Set<UserRole> roles = new HashSet<>();
        roles.add(UserRole.TEACHER);
        
        User testUser = new User(
            "keycloak-123",
            "test@example.com",
            roles,
            "John",
            "Doe"
        );
        
        // Set ID using reflection
        Field idField = User.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(testUser, UUID.randomUUID());
        
        when(userService.getMe()).thenReturn(testUser);

        // When & Then
        mockMvc.perform(
            get("/v1/auth/me")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.email").value("test@example.com"))
            .andExpect(jsonPath("$.data.firstName").value("John"))
            .andExpect(jsonPath("$.data.lastName").value("Doe"));
    }
}
