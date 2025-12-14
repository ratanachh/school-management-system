package com.visor.school.academicservice.contract

import com.fasterxml.jackson.databind.ObjectMapper
import com.visor.school.academicservice.controller.StudentController
import com.visor.school.academicservice.model.Student
import com.visor.school.academicservice.model.EnrollmentStatus
import com.visor.school.academicservice.service.StudentService
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDate
import java.util.UUID

@WebMvcTest(StudentController::class)
class StudentControllerContractTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper
) {

    @MockBean
    private lateinit var studentService: StudentService

    @Test
    fun `enroll student should return 201`() {
        // Given
        val request = mapOf(
            "userId" to UUID.randomUUID().toString(),
            "firstName" to "John",
            "lastName" to "Doe",
            "dateOfBirth" to "2010-01-01",
            "gradeLevel" to 5
        )

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
        every { studentService.searchStudents(searchQuery, 1) } returns emptyList<Student>()

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
            userId = UUID.randomUUID().toString(),
            studentId = "",
            firstName = "",
            lastName = "",
            dateOfBirth = LocalDate.now(),
            gradeLevel = 0,
            enrollmentStatus = EnrollmentStatus.ENROLLED
        )
        every { studentService.getStudentByStudentId(studentId.toString()) } returns mockStudent

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
        every { studentService.getStudentsByGrade(gradeLevel, 1) } returns emptyList<Student>()

        // When & Then
        mockMvc.perform(
            get("/api/v1/students/grade/{gradeLevel}", gradeLevel)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray)
    }
}
