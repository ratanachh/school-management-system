package com.visor.school.academicservice.integration

import com.visor.school.academicservice.model.EnrollmentStatus
import com.visor.school.academicservice.repository.StudentRepository
import com.visor.school.academicservice.service.StudentService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.UUID

/**
 * Integration test for student search functionality
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class StudentSearchIntegrationTest @Autowired constructor(
    private val studentService: StudentService,
    private val studentRepository: StudentRepository
) {

    @BeforeEach
    fun setup() {
        // Create test students
        val userId1 = UUID.randomUUID()
        val userId2 = UUID.randomUUID()
        val userId3 = UUID.randomUUID()

        studentService.enrollStudent(
            userId = userId1,
            firstName = "John",
            lastName = "Doe",
            dateOfBirth = LocalDate.of(2010, 1, 1),
            gradeLevel = 5
        )

        studentService.enrollStudent(
            userId = userId2,
            firstName = "Jane",
            lastName = "Smith",
            dateOfBirth = LocalDate.of(2010, 6, 1),
            gradeLevel = 5
        )

        studentService.enrollStudent(
            userId = userId3,
            firstName = "Bob",
            lastName = "Johnson",
            dateOfBirth = LocalDate.of(2006, 1, 1),
            gradeLevel = 12
        )
    }

    @Test
    fun `should search students by first name`() {
        // When
        val results = studentService.searchStudentsByName("John")

        // Then
        assertTrue(results.isNotEmpty())
        assertTrue(results.any { it.firstName.contains("John", ignoreCase = true) })
    }

    @Test
    fun `should search students by last name`() {
        // When
        val results = studentService.searchStudentsByName("Smith")

        // Then
        assertTrue(results.isNotEmpty())
        assertTrue(results.any { it.lastName.contains("Smith", ignoreCase = true) })
    }

    @Test
    fun `should search students by full name`() {
        // When
        val results = studentService.searchStudentsByName("John Doe")

        // Then
        assertTrue(results.isNotEmpty())
        assertTrue(results.any { 
            it.firstName.contains("John", ignoreCase = true) && 
            it.lastName.contains("Doe", ignoreCase = true) 
        })
    }

    @Test
    fun `should return empty list for non-existent student`() {
        // When
        val results = studentService.searchStudentsByName("Nonexistent")

        // Then
        assertTrue(results.isEmpty())
    }

    @Test
    fun `should find students by grade level`() {
        // When
        val grade5Students = studentService.getStudentsByGradeLevel(5)
        val grade12Students = studentService.getStudentsByGradeLevel(12)

        // Then
        assertTrue(grade5Students.size >= 2)
        assertTrue(grade5Students.all { it.gradeLevel == 5 })
        assertTrue(grade12Students.size >= 1)
        assertTrue(grade12Students.all { it.gradeLevel == 12 })
    }

    @Test
    fun `should find students by enrollment status via repository`() {
        // When - Access repository directly for enrollment status search
        val enrolledStudents = studentRepository.findByEnrollmentStatus(EnrollmentStatus.ENROLLED)

        // Then
        assertTrue(enrolledStudents.isNotEmpty())
        assertTrue(enrolledStudents.all { it.enrollmentStatus == EnrollmentStatus.ENROLLED })
    }

    @Test
    fun `should find student by student ID`() {
        // Given
        val userId = UUID.randomUUID()
        val student = studentService.enrollStudent(
            userId = userId,
            firstName = "Find",
            lastName = "ByID",
            dateOfBirth = LocalDate.of(2010, 1, 1),
            gradeLevel = 5
        )

        // When
        val found = studentService.getStudentByStudentId(student.studentId)

        // Then
        assertNotNull(found)
        assertEquals(student.studentId, found?.studentId)
        assertEquals("Find", found?.firstName)
    }
}

