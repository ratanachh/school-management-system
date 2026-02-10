package com.visor.school.userservice.integration;

import com.visor.school.userservice.model.User;
import com.visor.school.userservice.model.UserRole;
import com.visor.school.userservice.repository.UserRepository;
import com.visor.school.userservice.service.SecurityContextService;
import com.visor.school.userservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserPermissionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SecurityContextService securityContextService;
    
    @MockBean
    private UserService userService;

    @MockBean
    private UserRepository userRepository;

    private UUID studentId = UUID.randomUUID();

    @BeforeEach
    void setup() throws Exception {
        // Default behavior: security checks fail
        when(securityContextService.isCurrentUserId(any())).thenReturn(false);
        when(securityContextService.canManageAdministrators()).thenReturn(false);
        when(userService.canManageUser(any())).thenReturn(false);
        
        // Mock finding user
        Set<UserRole> roles = new HashSet<>();
        roles.add(UserRole.STUDENT);
        
        User studentUser = new User(
            "student-k-id",
            "student@example.com",
            roles,
            "Student",
            "Test"
        );
        
        // Set ID using reflection
        Field idField = User.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(studentUser, studentId);
        
        when(userService.findById(studentId)).thenReturn(studentUser);
        when(userService.updateUser(any(), any(), any(), any(), any())).thenReturn(studentUser);
    }

    @Test
    @WithMockUser(username = "student", roles = {"STUDENT"})
    void shouldAllowStudentToUpdateOwnProfile() throws Exception {
        // Given: Security context matches user ID
        when(securityContextService.isCurrentUserId(studentId)).thenReturn(true);

        String json = "{\"firstName\": \"Updated\"}";

        // When & Then
        mockMvc.perform(put("/v1/users/" + studentId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "other", roles = {"STUDENT"})
    void shouldDenyStudentUpdatingAnotherProfile() throws Exception {
        // Given: Security context mismatch
        when(securityContextService.isCurrentUserId(studentId)).thenReturn(false);

        String json = "{\"firstName\": \"Hacker\"}";

        // When & Then
        mockMvc.perform(put("/v1/users/" + studentId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "student", roles = {"STUDENT"})
    void shouldAllowStudentToViewOwnProfile() throws Exception {
        // Given
        when(securityContextService.isCurrentUserId(studentId)).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/v1/users/" + studentId))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "other", roles = {"STUDENT"})
    void shouldDenyStudentViewingAnotherProfile() throws Exception {
        // Given
        when(securityContextService.isCurrentUserId(studentId)).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/v1/users/" + studentId))
            .andExpect(status().isForbidden());
    }
    
    @Test
    @WithMockUser(username = "admin", roles = {"ADMINISTRATOR"})
    void shouldAllowAdminToViewStudentProfile() throws Exception {
         // Given
         when(userService.canManageUser(studentId)).thenReturn(true);

         // When & Then
         mockMvc.perform(get("/v1/users/" + studentId))
             .andExpect(status().isOk());
    }
}
