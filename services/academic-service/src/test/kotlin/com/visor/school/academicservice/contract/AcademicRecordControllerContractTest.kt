package com.visor.school.academicservice.contract

import com.fasterxml.jackson.databind.ObjectMapper
import com.visor.school.academicservice.controller.AcademicRecordController
import com.visor.school.academicservice.model.AcademicRecord
import com.visor.school.academicservice.model.AcademicStanding
import com.visor.school.academicservice.service.AcademicRecordService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.math.BigDecimal
import java.util.UUID

@WebMvcTest(AcademicRecordController::class)
@AutoConfigureMockMvc(addFilters = false)
class AcademicRecordControllerContractTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper
) {

    @MockBean
    private lateinit var academicRecordService: AcademicRecordService

    private val studentId = UUID.randomUUID()

    @Test
    fun `should return academic record for valid student ID`() {
        // Given
        val academicRecord = AcademicRecord(
            id = UUID.randomUUID(),
            studentId = studentId,
            cumulativeGPA = BigDecimal("3.5"),
            creditsEarned = 60,
            academicStanding = AcademicStanding.GOOD_STANDING
        )
        whenever(academicRecordService.getAcademicRecord(studentId)).thenReturn(academicRecord)

        // When & Then
        mockMvc.perform(get("/api/v1/academic-records/{studentId}", studentId))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.studentId").value(studentId.toString()))
    }

    @Test
    fun `should return 404 for non-existent student ID`() {
        // Given
        whenever(academicRecordService.getAcademicRecord(studentId)).thenReturn(null)

        // When & Then
        mockMvc.perform(get("/api/v1/academic-records/{studentId}", studentId))
            .andExpect(status().isNotFound)
    }
}
