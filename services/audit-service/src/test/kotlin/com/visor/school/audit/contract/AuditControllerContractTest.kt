package com.visor.school.audit.contract

import com.fasterxml.jackson.databind.ObjectMapper
import com.visor.school.audit.controller.AuditController
import com.visor.school.audit.service.AuditService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.util.UUID

@WebMvcTest(AuditController::class)
class AuditControllerContractTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper
) {

    @MockBean
    private lateinit var auditService: AuditService

    @Test
    fun `GET /api/v1/audit should return audit records`() {
        // Given
        val userId = UUID.randomUUID()
        val action = "AUTHENTICATION"

        // When & Then
        mockMvc.perform(
            get("/api/v1/audit")
                .param("userId", userId.toString())
                .param("action", action)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").exists())
    }

    @Test
    fun `GET /api/v1/audit should support date range filtering`() {
        // Given
        val userId = UUID.randomUUID()
        val startDate = "2024-01-01"
        val endDate = "2024-12-31"

        // When & Then
        mockMvc.perform(
            get("/api/v1/audit")
                .param("userId", userId.toString())
                .param("startDate", startDate)
                .param("endDate", endDate)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
    }

    @Test
    fun `GET /api/v1/audit should support action filtering only`() {
        // Given
        val action = "DATA_MODIFICATION"

        // When & Then
        mockMvc.perform(
            get("/api/v1/audit")
                .param("action", action)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
    }
}

