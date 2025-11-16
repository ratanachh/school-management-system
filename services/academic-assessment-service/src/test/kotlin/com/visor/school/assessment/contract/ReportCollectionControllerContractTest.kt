package com.visor.school.assessment.contract

import com.fasterxml.jackson.databind.ObjectMapper
import com.visor.school.assessment.controller.ReportCollectionController
import com.visor.school.assessment.service.ReportCollectionService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.util.UUID

@WebMvcTest(ReportCollectionController::class)
class ReportCollectionControllerContractTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper
) {

    @MockBean
    private lateinit var reportCollectionService: ReportCollectionService

    @Test
    fun `GET /api/v1/classes/{classId}/exam-results should collect exam results`() {
        // Given
        val classId = UUID.randomUUID()

        // When & Then
        mockMvc.perform(
            get("/api/v1/classes/$classId/exam-results")
                .param("academicYear", "2024-2025")
                .param("term", "FIRST_TERM")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").exists())
    }

    @Test
    fun `POST /api/v1/classes/{classId}/reports/submit should submit report`() {
        // Given
        val classId = UUID.randomUUID()
        val requestBody = mapOf(
            "collectionId" to UUID.randomUUID().toString(),
            "reportData" to mapOf("summary" to "Test report")
        )

        // When & Then
        mockMvc.perform(
            post("/api/v1/classes/$classId/reports/submit")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(requestBody))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").exists())
    }
}

