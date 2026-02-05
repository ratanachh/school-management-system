package com.visor.school.academicservice.contract

import com.fasterxml.jackson.databind.ObjectMapper
import com.visor.school.academicservice.controller.TeacherController
import com.visor.school.academicservice.model.EmploymentStatus
import com.visor.school.academicservice.model.Teacher
import com.visor.school.academicservice.service.TeacherService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDate
import java.util.UUID

@WebMvcTest(TeacherController::class)
@AutoConfigureMockMvc(addFilters = false)
class TeacherControllerContractTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper
) {

    @MockBean
    private lateinit var teacherService: TeacherService

    @Test
    fun `create teacher should return 201`() {
        // Given
        val userId = UUID.randomUUID()
        val request = mapOf(
            "userId" to userId.toString(),
            "qualifications" to listOf("Bachelor's Degree"),
            "subjectSpecializations" to listOf("Mathematics", "Physics"),
            "hireDate" to "2020-01-01",
            "department" to "Science"
        )
        val mockTeacher = Teacher(
            id = UUID.randomUUID(),
            userId = userId,
            employeeId = "T12345",
            qualifications = listOf("Bachelor's Degree"),
            subjectSpecializations = listOf("Mathematics", "Physics"),
            hireDate = LocalDate.of(2020, 1, 1),
            employmentStatus = EmploymentStatus.ACTIVE,
            department = "Science"
        )
        whenever(teacherService.createTeacher(any(), any(), any(), any(), any())).thenReturn(mockTeacher)

        // When & Then
        mockMvc.perform(
            post("/api/v1/teachers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").exists())
            .andExpect(jsonPath("$.message").exists())
    }

    @Test
    fun `get teacher by id should return teacher`() {
        // Given
        val teacherId = UUID.randomUUID()
        val mockTeacher = Teacher(
            id = teacherId,
            userId = UUID.randomUUID(),
            employeeId = "T12345",
            qualifications = listOf("Bachelor's Degree"),
            subjectSpecializations = listOf("Mathematics"),
            hireDate = LocalDate.now(),
            employmentStatus = EmploymentStatus.ACTIVE,
            department = "Science"
        )
        whenever(teacherService.getTeacherById(teacherId)).thenReturn(mockTeacher)

        // When & Then
        mockMvc.perform(
            get("/api/v1/teachers/{id}", teacherId)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
    }

    @Test
    fun `get teachers by status should return teachers`() {
        // Given
        val status = EmploymentStatus.ACTIVE
        whenever(teacherService.getTeachersByStatus(status)).thenReturn(emptyList())

        // When & Then
        mockMvc.perform(
            get("/api/v1/teachers/status/{status}", status.name)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray)
    }

    @Test
    fun `get teachers by department should return teachers`() {
        // Given
        val department = "Science"
        whenever(teacherService.getTeachersByDepartment(department)).thenReturn(emptyList())

        // When & Then
        mockMvc.perform(
            get("/api/v1/teachers/department/{department}", department)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray)
    }

    @Test
    fun `update employment status should update status`() {
        // Given
        val teacherId = UUID.randomUUID()
        val request = mapOf("status" to EmploymentStatus.ON_LEAVE.name)
        val mockTeacher = Teacher(
            id = teacherId,
            userId = UUID.randomUUID(),
            employeeId = "T12345",
            qualifications = listOf("Bachelor's Degree"),
            subjectSpecializations = listOf("Mathematics"),
            hireDate = LocalDate.of(2020, 1, 1),
            employmentStatus = EmploymentStatus.ON_LEAVE,
            department = "Science"
        )
        whenever(teacherService.updateEmploymentStatus(any(), any())).thenReturn(mockTeacher)

        // When & Then
        mockMvc.perform(
            patch("/api/v1/teachers/{id}/status", teacherId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").exists())
    }

    @Test
    fun `create teacher without specializations should reject`() {
        // Given
        val request = mapOf(
            "userId" to UUID.randomUUID().toString(),
            "qualifications" to emptyList<String>(),
            "subjectSpecializations" to emptyList<String>(), // Invalid: must have at least one
            "hireDate" to "2020-01-01"
        )

        // When & Then
        mockMvc.perform(
            post("/api/v1/teachers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    // Note: Teacher assignment endpoint is handled by ClassController, not TeacherController
    // This test is removed as the endpoint doesn't exist in TeacherController
}
