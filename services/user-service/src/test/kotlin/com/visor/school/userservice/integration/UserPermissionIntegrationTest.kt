package com.visor.school.userservice.integration

import com.visor.school.userservice.controller.UpdateUserRequest
import com.visor.school.userservice.model.User
import com.visor.school.userservice.model.UserRole
import com.visor.school.userservice.repository.UserRepository
import com.visor.school.userservice.service.SecurityContextService
import com.visor.school.userservice.service.UserService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserPermissionIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var securityContextService: SecurityContextService
    
    @MockBean
    private lateinit var userService: UserService

    @MockBean
    private lateinit var userRepository: UserRepository

    private val studentId = UUID.randomUUID()

    @BeforeEach
    fun setup() {
        // Default behavior: security checks fail
        whenever(securityContextService.isCurrentUserId(any())).thenReturn(false)
        whenever(securityContextService.canManageAdministrators()).thenReturn(false)
        whenever(userService.canManageUser(any())).thenReturn(false)
        
        // Mock finding user
        val studentUser = User(
            keycloakId = "student-k-id",
            email = "student@example.com",
            firstName = "Student", 
            lastName = "Test",
            roles = mutableSetOf(UserRole.STUDENT)
        )
        studentUser.id = studentId
        
        whenever(userService.findById(studentId)).thenReturn(studentUser)
        whenever(userService.updateUser(any(), any(), any(), any(), any())).thenReturn(studentUser)
    }

    @Test
    @WithMockUser(username = "student", roles = ["STUDENT"])
    fun `should allow student to update own profile`() {
        // Given: Security context matches user ID
        whenever(securityContextService.isCurrentUserId(studentId)).thenReturn(true)

        val json = """{"firstName": "Updated"}"""

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.put("/v1/users/$studentId")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
            .andExpect(status().isOk)
    }

    @Test
    @WithMockUser(username = "other", roles = ["STUDENT"])
    fun `should deny student updating another profile`() {
        // Given: Security context mismatch
        whenever(securityContextService.isCurrentUserId(studentId)).thenReturn(false)

        val json = """{"firstName": "Hacker"}"""

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.put("/v1/users/$studentId")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
            .andExpect(status().isForbidden)
    }

    @Test
    @WithMockUser(username = "student", roles = ["STUDENT"])
    fun `should allow student to view own profile`() {
        // Given
        whenever(securityContextService.isCurrentUserId(studentId)).thenReturn(true)

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.get("/v1/users/$studentId"))
            .andExpect(status().isOk)
    }

    @Test
    @WithMockUser(username = "other", roles = ["STUDENT"])
    fun `should deny student viewing another profile`() {
        // Given
        whenever(securityContextService.isCurrentUserId(studentId)).thenReturn(false)

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.get("/v1/users/$studentId"))
            .andExpect(status().isForbidden)
    }
    
    @Test
    @WithMockUser(username = "admin", roles = ["ADMINISTRATOR"])
    fun `should allow admin to view student profile`() {
         // Given
         whenever(userService.canManageUser(studentId)).thenReturn(true)

         // When & Then
         mockMvc.perform(MockMvcRequestBuilders.get("/v1/users/$studentId"))
             .andExpect(status().isOk)
    }
}
