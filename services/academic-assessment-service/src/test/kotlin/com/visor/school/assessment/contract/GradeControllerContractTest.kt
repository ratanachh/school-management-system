package com.visor.school.assessment.contract

import com.fasterxml.jackson.databind.ObjectMapper
import com.visor.school.assessment.controller.GradeController
import com.visor.school.assessment.service.GradeService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.util.UUID

@WebMvcTest(GradeController::class)
class GradeControllerContractTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper
) {

    @MockBean
    private lateinit var gradeService: GradeService

    @Test
    fun `POST /api/v1/grades should record grade`() {
        // Given
        val request = mapOf(
            "studentId" to UUID.randomUUID().toString(),
            "assessmentId" to UUID.randomUUID().toString(),
            "score" to 85.5
        )

        // When & Then
        mockMvc.perform(
            post("/api/v1/grades")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk.or(status().isCreated))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").exists())
    }

    @Test
    fun `PUT /api/v1/grades should update grade`() {
        // Given
        val request = mapOf(
            "studentId" to UUID.randomUUID().toString(),
            "assessmentId" to UUID.randomUUID().toString(),
            "newScore" to 90.0
        )

        // When & Then
        mockMvc.perform(
            put("/api/v1/grades")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").exists())
    }
}

