package com.visor.school.academicservice.contract

import com.fasterxml.jackson.databind.ObjectMapper
import com.visor.school.academicservice.controller.AcademicRecordController
import com.visor.school.academicservice.model.AcademicRecord
import com.visor.school.academicservice.model.AcademicStanding
import com.visor.school.academicservice.service.AcademicRecordService
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.math.BigDecimal
import java.util.UUID

@WebMvcTest(AcademicRecordController::class)
class AcademicRecordControllerContractTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper
) {

    @MockBean
    private lateinit var academicRecordService: AcademicRecordService

    private val studentId = UUID.randomUUID().toString()

    @Test
    fun `should return academic record for valid student ID`() {
        // Given
        val academicRecord = AcademicRecord(
            studentId = UUID.fromString(studentId),
            cumulativeGPA = BigDecimal("3.5"),
            creditsEarned = 60,
            academicStanding = AcademicStanding.GOOD_STANDING
        )
        every { academicRecordService.getAcademicRecordByStudentId(studentId) } returns academicRecord

        // When & Then
        mockMvc.perform(get("/api/v1/academic-records/student/{studentId}", studentId))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.studentId").value(studentId))
    }

    @Test
    fun `should return 404 for non-existent student ID`() {
        // Given
        every { academicRecordService.getAcademicRecordByStudentId(studentId) } returns null

        // When & Then
        mockMvc.perform(get("/api/v1/academic-records/student/{studentId}", studentId))
            .andExpect(status().isNotFound)
    }
}
