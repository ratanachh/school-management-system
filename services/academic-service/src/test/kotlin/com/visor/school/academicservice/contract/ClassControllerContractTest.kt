package com.visor.school.academicservice.contract

import com.fasterxml.jackson.databind.ObjectMapper
import com.visor.school.academicservice.controller.ClassController
import com.visor.school.academicservice.model.Class
import com.visor.school.academicservice.model.ClassStatus
import com.visor.school.academicservice.model.ClassType
import com.visor.school.academicservice.model.Term
import com.visor.school.academicservice.service.ClassService
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.whenever
import org.junit.jupiter.api.Test
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

@WebMvcTest(ClassController::class)
@AutoConfigureMockMvc(addFilters = false)
class ClassControllerContractTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper
) {

    @MockBean
    private lateinit var classService: ClassService

    @Test
    fun `create homeroom class should return 201`() {
        // Given
        val homeroomTeacherId = UUID.randomUUID()
        val request = mapOf(
            "className" to "Grade 3 Homeroom",
            "gradeLevel" to 3,
            "homeroomTeacherId" to homeroomTeacherId.toString(),
            "academicYear" to "2024-2025",
            "term" to Term.FIRST_TERM.name,
            "startDate" to "2024-09-01"
        )
        val mockClass = Class(
            id = UUID.randomUUID(),
            className = "Grade 3 Homeroom",
            classType = ClassType.HOMEROOM,
            gradeLevel = 3,
            homeroomTeacherId = homeroomTeacherId,
            academicYear = "2024-2025",
            term = Term.FIRST_TERM,
            startDate = LocalDate.of(2024, 9, 1),
            status = ClassStatus.SCHEDULED
        )
        whenever(classService.createHomeroomClass(any(), any(), any(), any(), any(), anyOrNull(), anyOrNull(), any(), anyOrNull())).thenReturn(mockClass)

        // When & Then
        mockMvc.perform(
            post("/api/v1/classes/homeroom")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").exists())
            .andExpect(jsonPath("$.message").exists())
    }

    @Test
    fun `create homeroom class with invalid grade should reject`() {
        // Given
        val request = mapOf(
            "className" to "Invalid Homeroom",
            "gradeLevel" to 7, // Invalid: homeroom only for grades 1-6
            "homeroomTeacherId" to UUID.randomUUID().toString(),
            "academicYear" to "2024-2025",
            "term" to Term.FIRST_TERM.name,
            "startDate" to "2024-09-01"
        )

        // When & Then
        mockMvc.perform(
            post("/api/v1/classes/homeroom")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `create subject class should return 201`() {
        // Given
        val request = mapOf(
            "className" to "Mathematics 101",
            "subject" to "Mathematics",
            "gradeLevel" to 10,
            "academicYear" to "2024-2025",
            "term" to Term.FIRST_TERM.name,
            "startDate" to "2024-09-01"
        )
        val mockClass = Class(
            id = UUID.randomUUID(),
            className = "Mathematics 101",
            classType = ClassType.SUBJECT,
            subject = "Mathematics",
            gradeLevel = 10,
            academicYear = "2024-2025",
            term = Term.FIRST_TERM,
            startDate = LocalDate.of(2024, 9, 1),
            status = ClassStatus.SCHEDULED
        )
        whenever(classService.createSubjectClass(any(), any(), any(), any(), any(), anyOrNull(), anyOrNull(), any(), anyOrNull())).thenReturn(mockClass)

        // When & Then
        mockMvc.perform(
            post("/api/v1/classes/subject")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").exists())
            .andExpect(jsonPath("$.message").exists())
    }

    @Test
    fun `assign class teacher should return 200`() {
        // Given
        val classId = UUID.randomUUID()
        val teacherId = UUID.randomUUID()
        val request = mapOf(
            "teacherId" to teacherId.toString(),
            "assignedBy" to UUID.randomUUID().toString()
        )
        val mockClass = Class(
            id = classId,
            className = "Mathematics 101",
            classType = ClassType.SUBJECT,
            subject = "Mathematics",
            gradeLevel = 10,
            classTeacherId = teacherId,
            academicYear = "2024-2025",
            term = Term.FIRST_TERM,
            startDate = LocalDate.of(2024, 9, 1),
            status = ClassStatus.SCHEDULED
        )
        whenever(classService.assignClassTeacher(any(), any(), any())).thenReturn(mockClass)

        // When & Then
        mockMvc.perform(
            post("/api/v1/classes/{classId}/class-teacher", classId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").exists())
    }

    @Test
    fun `get class by id should return class`() {
        // Given
        val classId = UUID.randomUUID()
        val mockClass = Class(
            id = classId,
            className = "Test Class",
            gradeLevel = 5,
            classType = ClassType.HOMEROOM,
            academicYear = "2024-2025",
            term = Term.FIRST_TERM,
            startDate = LocalDate.now(),
            status = ClassStatus.SCHEDULED

        )
        whenever(classService.getClassById(classId)).thenReturn(mockClass)

        // When & Then
        mockMvc.perform(
            get("/api/v1/classes/{id}", classId)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
    }

    @Test
    fun `get classes by grade should return classes`() {
        // Given
        val gradeLevel = 5
        whenever(classService.getClassesByGradeLevel(gradeLevel)).thenReturn(emptyList())

        // When & Then
        mockMvc.perform(
            get("/api/v1/classes/grade/{gradeLevel}", gradeLevel)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray)
    }

    @Test
    fun `update class status should update status`() {
        // Given
        val classId = UUID.randomUUID()
        val request = mapOf("status" to ClassStatus.IN_PROGRESS.name)
        val mockClass = Class(
            id = classId,
            className = "Test Class",
            classType = ClassType.SUBJECT,
            subject = "Mathematics",
            gradeLevel = 10,
            academicYear = "2024-2025",
            term = Term.FIRST_TERM,
            startDate = LocalDate.of(2024, 9, 1),
            status = ClassStatus.IN_PROGRESS
        )
        whenever(classService.updateClassStatus(any(), any())).thenReturn(mockClass)

        // When & Then
        mockMvc.perform(
            patch("/api/v1/classes/{id}/status", classId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").exists())
    }
}
