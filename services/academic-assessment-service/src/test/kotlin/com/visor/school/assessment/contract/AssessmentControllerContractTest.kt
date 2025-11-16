package com.visor.school.assessment.contract

import com.fasterxml.jackson.databind.ObjectMapper
import com.visor.school.assessment.controller.AssessmentController
import com.visor.school.assessment.service.AssessmentService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.util.UUID

@WebMvcTest(AssessmentController::class)
class AssessmentControllerContractTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper
) {

    @MockBean
    private lateinit var assessmentService: AssessmentService

    @Test
    fun `POST /api/v1/assessments should create assessment`() {
        // Given
        val request = mapOf(
            "classId" to UUID.randomUUID().toString(),
            "name" to "Midterm Exam",
            "type" to "EXAM",
            "totalPoints" to 100.0
        )

        // When & Then
        mockMvc.perform(
            post("/api/v1/assessments")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk.or(status().isCreated))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").exists())
    }

    @Test
    fun `GET /api/v1/assessments/{id} should return assessment`() {
        // Given
        val assessmentId = UUID.randomUUID()

        // When & Then
        mockMvc.perform(
            get("/api/v1/assessments/{id}", assessmentId)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").exists())
    }
}

