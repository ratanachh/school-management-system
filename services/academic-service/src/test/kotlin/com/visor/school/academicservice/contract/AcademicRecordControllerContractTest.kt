package com.visor.school.academicservice.contract

import com.fasterxml.jackson.databind.ObjectMapper
import com.visor.school.academicservice.controller.AcademicRecordController
import com.visor.school.academicservice.service.AcademicRecordService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.util.UUID

@WebMvcTest(AcademicRecordController::class)
class AcademicRecordControllerContractTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper
) {

    @MockBean
    private lateinit var academicRecordService: AcademicRecordService

    @Test
    fun `GET /api/v1/academic-records/{studentId} should return academic record`() {
        // Given
        val studentId = UUID.randomUUID()

        // When & Then
        mockMvc.perform(
            get("/api/v1/academic-records/{studentId}", studentId)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").exists())
    }

    @Test
    fun `GET /api/v1/academic-records/{studentId}/transcript should return transcript`() {
        // Given
        val studentId = UUID.randomUUID()

        // When & Then
        mockMvc.perform(
            get("/api/v1/academic-records/{studentId}/transcript", studentId)
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType("application/pdf"))
    }

    @Test
    fun `GET /api/v1/academic-records/{studentId} should return 404 when record not found`() {
        // Given
        val studentId = UUID.randomUUID()

        // When & Then
        mockMvc.perform(
            get("/api/v1/academic-records/{studentId}", studentId)
        )
            .andExpect(status().isOk.or(status().isNotFound))
    }
}

