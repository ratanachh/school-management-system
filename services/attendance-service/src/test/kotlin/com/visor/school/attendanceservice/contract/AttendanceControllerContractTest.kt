package com.visor.school.attendanceservice.contract

import com.fasterxml.jackson.databind.ObjectMapper
import com.visor.school.attendanceservice.controller.AttendanceController
import com.visor.school.attendanceservice.controller.AttendanceSessionController
import com.visor.school.attendanceservice.model.AttendanceSessionStatus
import com.visor.school.attendanceservice.model.AttendanceStatus
import com.visor.school.attendanceservice.service.AttendanceService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDate
import java.util.UUID

@WebMvcTest(controllers = [AttendanceController::class, AttendanceSessionController::class])
class AttendanceControllerContractTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper
) {

    @MockitoBean
    private lateinit var attendanceService: AttendanceService

    @Test
    fun `POST /api/v1/attendance should mark attendance directly and return 201`() {
        // Given
        val request = mapOf(
            "studentId" to UUID.randomUUID().toString(),
            "classId" to UUID.randomUUID().toString(),
            "date" to LocalDate.now().toString(),
            "status" to AttendanceStatus.PRESENT.name,
            "markedBy" to UUID.randomUUID().toString()
        )

        // When & Then
        mockMvc.perform(
            post("/api/v1/attendance")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").exists())
            .andExpect(jsonPath("$.message").exists())
    }

    @Test
    fun `POST /api/v1/attendance/sessions should create attendance session and return 201`() {
        // Given
        val request = mapOf(
            "classId" to UUID.randomUUID().toString(),
            "date" to LocalDate.now().toString()
        )

        // When & Then
        mockMvc.perform(
            post("/api/v1/attendance/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").exists())
    }

    @Test
    fun `POST /api/v1/attendance/sessions/{sessionId}/delegate should delegate to class leader and return 200`() {
        // Given
        val sessionId = UUID.randomUUID()
        val request = mapOf(
            "classLeaderId" to UUID.randomUUID().toString(),
            "classId" to UUID.randomUUID().toString()
        )

        // When & Then
        mockMvc.perform(
            post("/api/v1/attendance/sessions/{sessionId}/delegate", sessionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").exists())
    }

    @Test
    fun `POST /api/v1/attendance/sessions/{sessionId}/collect should collect attendance and return 200`() {
        // Given
        val sessionId = UUID.randomUUID()
        val request = mapOf(
            "classLeaderId" to UUID.randomUUID().toString(),
            "attendanceEntries" to listOf(
                mapOf(
                    "studentId" to UUID.randomUUID().toString(),
                    "status" to AttendanceStatus.PRESENT.name,
                    "notes" to null
                )
            )
        )

        // When & Then
        mockMvc.perform(
            post("/api/v1/attendance/sessions/{sessionId}/collect", sessionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").exists())
    }

    @Test
    fun `POST /api/v1/attendance/sessions/{sessionId}/approve should approve session and return 200`() {
        // Given
        val sessionId = UUID.randomUUID()
        val request = mapOf<String, String>()

        // When & Then
        mockMvc.perform(
            post("/api/v1/attendance/sessions/{sessionId}/approve", sessionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").exists())
    }

    @Test
    fun `GET /api/v1/attendance/class/{classId} should return attendance records`() {
        // Given
        val classId = UUID.randomUUID()

        // When & Then
        mockMvc.perform(
            get("/api/v1/attendance/class/{classId}", classId)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray)
    }

    @Test
    fun `GET /api/v1/attendance/student/{studentId} should return student attendance records`() {
        // Given
        val studentId = UUID.randomUUID()

        // When & Then
        mockMvc.perform(
            get("/api/v1/attendance/student/{studentId}", studentId)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray)
    }

    @Test
    fun `GET /api/v1/reports/class/{classId} should return attendance report`() {
        // Given
        val classId = UUID.randomUUID()

        // When & Then
        mockMvc.perform(
            get("/api/v1/reports/class/{classId}", classId)
                .param("startDate", LocalDate.now().minusDays(7).toString())
                .param("endDate", LocalDate.now().toString())
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").exists())
    }
}

