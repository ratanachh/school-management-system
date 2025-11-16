package com.visor.school.assessment.contract

import com.fasterxml.jackson.databind.ObjectMapper
import com.visor.school.assessment.controller.GradebookController
import com.visor.school.assessment.service.GradebookService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.util.UUID

@WebMvcTest(GradebookController::class)
class GradebookControllerContractTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper
) {

    @MockBean
    private lateinit var gradebookService: GradebookService

    @Test
    fun `GET /api/v1/gradebooks/class/{classId} should return class gradebook`() {
        // Given
        val classId = UUID.randomUUID()

        // When & Then
        mockMvc.perform(
            get("/api/v1/gradebooks/class/{classId}", classId)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").exists())
    }

    @Test
    fun `GET /api/v1/gradebooks/student/{studentId} should return student grades`() {
        // Given
        val studentId = UUID.randomUUID()

        // When & Then
        mockMvc.perform(
            get("/api/v1/gradebooks/student/{studentId}", studentId)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").exists())
    }
}

