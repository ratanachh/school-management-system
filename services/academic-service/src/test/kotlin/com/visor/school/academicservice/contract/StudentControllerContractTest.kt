package com.visor.school.academicservice.contract

import com.fasterxml.jackson.databind.ObjectMapper
import com.visor.school.academicservice.controller.StudentController
import com.visor.school.academicservice.model.Student
import com.visor.school.academicservice.model.EnrollmentStatus
import com.visor.school.academicservice.service.StudentService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
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

@WebMvcTest(StudentController::class)
@AutoConfigureMockMvc(addFilters = false)
class StudentControllerContractTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper
) {

    @MockBean
    private lateinit var studentService: StudentService

    @Test
    fun `enroll student should return 201`() {
        // Given
        val userId = UUID.randomUUID()
        val request = mapOf(
            "userId" to userId.toString(),
            "firstName" to "John",
            "lastName" to "Doe",
            "dateOfBirth" to "2010-01-01",
            "gradeLevel" to 5
        )
        val mockStudent = Student(
            id = UUID.randomUUID(),
            userId = userId,
            studentId = "S12345",
            firstName = "John",
            lastName = "Doe",
            dateOfBirth = LocalDate.of(2010, 1, 1),
            gradeLevel = 5,
            enrollmentStatus = EnrollmentStatus.ENROLLED
        )
        whenever(studentService.enrollStudent(any(), any(), any(), any(), any(), anyOrNull(), anyOrNull())).thenReturn(mockStudent)

        // When & Then
        mockMvc.perform(
            post("/api/v1/students")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").exists())
            .andExpect(jsonPath("$.message").exists())
    }

    @Test
    fun `search students should return students`() {
        // Given
        val searchQuery = "John"
        whenever(studentService.searchStudentsByName(searchQuery)).thenReturn(emptyList())

        // When & Then
        mockMvc.perform(
            get("/api/v1/students/search")
                .param("name", searchQuery)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray)
    }

    @Test
    fun `get student by id should return student`() {
        // Given
        val studentId = UUID.randomUUID()
        val mockStudent = Student(
            id = studentId,
            userId = UUID.randomUUID(),
            studentId = "S12345",
            firstName = "John",
            lastName = "Doe",
            dateOfBirth = LocalDate.now(),
            gradeLevel = 5,
            enrollmentStatus = EnrollmentStatus.ENROLLED
        )
        whenever(studentService.getStudentById(studentId)).thenReturn(mockStudent)

        // When & Then
        mockMvc.perform(
            get("/api/v1/students/{id}", studentId)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
    }

    @Test
    fun `update student should return 200`() {
        // Given
        val studentId = UUID.randomUUID()
        val request = mapOf(
            "firstName" to "Updated",
            "lastName" to "Name",
            "gradeLevel" to 6
        )
        val mockStudent = Student(
            id = studentId,
            userId = UUID.randomUUID(),
            studentId = "S12345",
            firstName = "Updated",
            lastName = "Name",
            dateOfBirth = LocalDate.of(2010, 1, 1),
            gradeLevel = 6,
            enrollmentStatus = EnrollmentStatus.ENROLLED
        )
        whenever(studentService.updateStudent(any(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())).thenReturn(mockStudent)

        // When & Then
        mockMvc.perform(
            put("/api/v1/students/{id}", studentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").exists())
    }

    @Test
    fun `enroll student with invalid grade should reject`() {
        // Given
        val request = mapOf(
            "userId" to UUID.randomUUID().toString(),
            "firstName" to "Invalid",
            "lastName" to "Grade",
            "dateOfBirth" to "2010-01-01",
            "gradeLevel" to 13 // Invalid: should be 1-12
        )

        // When & Then
        mockMvc.perform(
            post("/api/v1/students")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `get students by grade should return students`() {
        // Given
        val gradeLevel = 5
        whenever(studentService.getStudentsByGradeLevel(gradeLevel)).thenReturn(emptyList())

        // When & Then
        mockMvc.perform(
            get("/api/v1/students/grade/{gradeLevel}", gradeLevel)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray)
    }
}
