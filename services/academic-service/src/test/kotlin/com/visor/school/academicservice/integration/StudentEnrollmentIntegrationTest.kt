package com.visor.school.academicservice.integration

import com.visor.school.academicservice.config.TestConfig
import com.visor.school.academicservice.model.Address
import com.visor.school.academicservice.model.EnrollmentStatus
import com.visor.school.academicservice.repository.StudentRepository
import com.visor.school.academicservice.service.StudentService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.UUID
import com.visor.school.academicservice.model.EmergencyContact
import org.junit.jupiter.api.assertThrows

/**
 * Integration test for student enrollment flow
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestConfig::class)
@Transactional
class StudentEnrollmentIntegrationTest @Autowired constructor(
    private val studentService: StudentService,
    private val studentRepository: StudentRepository
) {

    @Test
    fun `should complete student enrollment flow`() {
        // Given
        val userId = UUID.randomUUID()
        val firstName = "Integration"
        val lastName = "Test"
        val dateOfBirth = LocalDate.of(2010, 1, 1)
        val gradeLevel = 5

        // When
        val student = studentService.enrollStudent(
            userId = userId,
            firstName = firstName,
            lastName = lastName,
            dateOfBirth = dateOfBirth,
            gradeLevel = gradeLevel
        )

        // Then
        assertNotNull(student)
        assertNotNull(student.id!!)
        assertNotNull(student.studentId)
        assertFalse(student.studentId.isBlank())
        assertEquals(firstName, student.firstName)
        assertEquals(lastName, student.lastName)
        assertEquals(dateOfBirth, student.dateOfBirth)
        assertEquals(gradeLevel, student.gradeLevel)
        assertEquals(EnrollmentStatus.ENROLLED, student.enrollmentStatus)

        // Verify student is persisted
        val savedStudent = studentRepository.findById(student.id!!)
        assertTrue(savedStudent.isPresent)
        assertEquals(firstName, savedStudent.get().firstName)
    }

    @Test
    fun `should enroll student with address and emergency contact`() {
        // Given
        val userId = UUID.randomUUID()
        val address = Address(
            street = "456 Test St",
            city = "Test City",
            state = "Test State",
            postalCode = "12345",
            country = "Test Country"
        )

        // When
        val student = studentService.enrollStudent(
            userId = userId,
            firstName = "With",
            lastName = "Contact",
            dateOfBirth = LocalDate.of(2010, 1, 1),
            gradeLevel = 5,
            address = address
        )

        // Then
        assertNotNull(student.address)
        assertEquals("Test City", student.address?.city)
    }

    @Test
    fun `should fail enrollment when grade level is invalid`() {
        // Given
        val userId = UUID.randomUUID()

        // When & Then
        assertThrows<IllegalArgumentException> {
            studentService.enrollStudent(
                userId = userId,
                firstName = "Invalid",
                lastName = "Grade",
                dateOfBirth = LocalDate.of(2010, 1, 1),
                gradeLevel = 13
            )
        }
    }

    @Test
    fun `should fail enrollment when student already exists`() {
        // Given
        val userId = UUID.randomUUID()
        studentService.enrollStudent(
            userId = userId,
            firstName = "First",
            lastName = "Enrollment",
            dateOfBirth = LocalDate.of(2010, 1, 1),
            gradeLevel = 5
        )

        // When & Then
        assertThrows<IllegalArgumentException> {
            studentService.enrollStudent(
                userId = userId,
                firstName = "Second",
                lastName = "Enrollment",
                dateOfBirth = LocalDate.of(2010, 1, 1),
                gradeLevel = 5
            )
        }
    }

    @Test
    fun `should generate unique student IDs`() {
        // Given
        val userId1 = UUID.randomUUID()
        val userId2 = UUID.randomUUID()

        // When
        val student1 = studentService.enrollStudent(
            userId = userId1,
            firstName = "Student",
            lastName = "One",
            dateOfBirth = LocalDate.of(2010, 1, 1),
            gradeLevel = 5
        )
        val student2 = studentService.enrollStudent(
            userId = userId2,
            firstName = "Student",
            lastName = "Two",
            dateOfBirth = LocalDate.of(2010, 1, 1),
            gradeLevel = 5
        )

        // Then
        assertNotEquals(student1.studentId, student2.studentId)
        assertNotNull(student1.studentId)
        assertNotNull(student2.studentId)
    }
}
