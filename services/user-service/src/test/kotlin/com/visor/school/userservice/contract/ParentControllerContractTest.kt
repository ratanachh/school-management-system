package com.visor.school.userservice.contract

import com.fasterxml.jackson.databind.ObjectMapper
import com.visor.school.userservice.controller.ParentController
import com.visor.school.userservice.model.Relationship
import com.visor.school.userservice.service.ParentService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.util.UUID

@WebMvcTest(ParentController::class)
class ParentControllerContractTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper
) {

    @MockBean
    private lateinit var parentService: ParentService

    @Test
    fun `GET /api/v1/parents/{parentId}/students should return parent's children`() {
        // Given
        val parentId = UUID.randomUUID()

        // When & Then
        mockMvc.perform(
            get("/api/v1/parents/{parentId}/students", parentId)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray)
    }

    @Test
    fun `POST /api/v1/parents/{userId}/students/{studentId} should link student to parent`() {
        // Given
        val userId = UUID.randomUUID()
        val studentId = UUID.randomUUID()
        val request = mapOf(
            "relationship" to "MOTHER",
            "isPrimary" to true
        )

        // When & Then
        mockMvc.perform(
            post("/api/v1/parents/{userId}/students/{studentId}", userId, studentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.message").exists())
    }

    @Test
    fun `DELETE /api/v1/parents/{userId}/students/{studentId} should unlink student from parent`() {
        // Given
        val userId = UUID.randomUUID()
        val studentId = UUID.randomUUID()

        // When & Then
        mockMvc.perform(
            delete("/api/v1/parents/{userId}/students/{studentId}", userId, studentId)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.message").exists())
    }
}

