package com.visor.school.audit.contract

import com.fasterxml.jackson.databind.ObjectMapper
import com.visor.school.audit.controller.AuditController
import com.visor.school.audit.model.AuditAction
import com.visor.school.audit.model.AuditRecord
import com.visor.school.audit.service.AuditService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDate
import java.util.UUID

@WebMvcTest(
    controllers = [AuditController::class],
    excludeAutoConfiguration = [org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration::class]
)
class AuditControllerContractTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper
) {

    @MockBean
    private lateinit var auditService: AuditService

    private val testUserId = UUID.randomUUID()

    @BeforeEach
    fun setup() {
        // Setup default mock responses
        whenever(auditService.query(any(), any(), any(), any())).thenReturn(emptyList())
    }

    @Test
    fun `GET api v1 audit should return audit records`() {
        // Given
        val userId = UUID.randomUUID()
        val action = "AUTHENTICATION"
        val records = listOf(
            AuditRecord(
                userId = userId,
                action = AuditAction.AUTHENTICATION,
                resourceType = "User",
                resourceId = userId.toString(),
                ipAddress = "192.168.1.1",
                userAgent = "Mozilla/5.0"
            )
        )
        whenever(auditService.query(userId, AuditAction.AUTHENTICATION, null, null))
            .thenReturn(records)

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
    fun `GET api v1 audit should support date range filtering`() {
        // Given
        val userId = UUID.randomUUID()
        val startDate = "2024-01-01"
        val endDate = "2024-12-31"
        val records = listOf(
            AuditRecord(
                userId = userId,
                action = AuditAction.AUTHENTICATION,
                resourceType = "User",
                resourceId = userId.toString(),
                ipAddress = "192.168.1.1",
                userAgent = "Mozilla/5.0"
            )
        )
        whenever(auditService.query(userId, null, LocalDate.parse(startDate), LocalDate.parse(endDate)))
            .thenReturn(records)

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
    fun `GET api v1 audit should support action filtering only`() {
        // Given
        val action = "DATA_MODIFICATION"
        val records = listOf(
            AuditRecord(
                userId = testUserId,
                action = AuditAction.DATA_MODIFICATION,
                resourceType = "Student",
                resourceId = UUID.randomUUID().toString(),
                ipAddress = "192.168.1.1",
                userAgent = "Mozilla/5.0"
            )
        )
        whenever(auditService.query(null, AuditAction.DATA_MODIFICATION, null, null))
            .thenReturn(records)

        // When & Then
        mockMvc.perform(
            get("/api/v1/audit")
                .param("action", action)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
    }
}

