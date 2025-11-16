package com.visor.school.academicservice.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import java.util.UUID

class StudentTest {

    @Test
    fun `should create student with valid grade level`() {
        val student = Student(
            studentId = "STU-2025-001",
            userId = UUID.randomUUID(),
            firstName = "John",
            lastName = "Doe",
            dateOfBirth = LocalDate.of(2010, 1, 1),
            gradeLevel = 5
        )

        assertNotNull(student.id)
        assertEquals("STU-2025-001", student.studentId)
        assertEquals(5, student.gradeLevel)
        assertEquals(EnrollmentStatus.ENROLLED, student.enrollmentStatus)
        assertNotNull(student.enrolledAt)
    }

    @Test
    fun `should accept grade level 1`() {
        val student = Student(
            studentId = "STU-2025-002",
            userId = UUID.randomUUID(),
            firstName = "Jane",
            lastName = "Smith",
            dateOfBirth = LocalDate.of(2018, 1, 1),
            gradeLevel = 1
        )

        assertEquals(1, student.gradeLevel)
    }

    @Test
    fun `should accept grade level 12`() {
        val student = Student(
            studentId = "STU-2025-003",
            userId = UUID.randomUUID(),
            firstName = "Bob",
            lastName = "Johnson",
            dateOfBirth = LocalDate.of(2006, 1, 1),
            gradeLevel = 12
        )

        assertEquals(12, student.gradeLevel)
    }

    @Test
    fun `should throw exception for grade level below 1`() {
        assertThrows<IllegalArgumentException> {
            Student(
                studentId = "STU-2025-004",
                userId = UUID.randomUUID(),
                firstName = "Invalid",
                lastName = "Grade",
                dateOfBirth = LocalDate.of(2020, 1, 1),
                gradeLevel = 0
            )
        }
    }

    @Test
    fun `should throw exception for grade level above 12`() {
        assertThrows<IllegalArgumentException> {
            Student(
                studentId = "STU-2025-005",
                userId = UUID.randomUUID(),
                firstName = "Invalid",
                lastName = "Grade",
                dateOfBirth = LocalDate.of(2000, 1, 1),
                gradeLevel = 13
            )
        }
    }

    @Test
    fun `should promote student to next grade`() {
        val student = Student(
            studentId = "STU-2025-006",
            userId = UUID.randomUUID(),
            firstName = "Promote",
            lastName = "Test",
            dateOfBirth = LocalDate.of(2010, 1, 1),
            gradeLevel = 5
        )

        val initialUpdatedAt = student.updatedAt
        Thread.sleep(10)

        student.promoteToNextGrade()

        assertEquals(6, student.gradeLevel)
        assertTrue(student.updatedAt.isAfter(initialUpdatedAt))
    }

    @Test
    fun `should throw exception when promoting grade 12 student`() {
        val student = Student(
            studentId = "STU-2025-007",
            userId = UUID.randomUUID(),
            firstName = "Senior",
            lastName = "Student",
            dateOfBirth = LocalDate.of(2006, 1, 1),
            gradeLevel = 12
        )

        assertThrows<IllegalArgumentException> {
            student.promoteToNextGrade()
        }
    }

    @Test
    fun `should update enrollment status`() {
        val student = Student(
            studentId = "STU-2025-008",
            userId = UUID.randomUUID(),
            firstName = "Status",
            lastName = "Test",
            dateOfBirth = LocalDate.of(2010, 1, 1),
            gradeLevel = 5
        )

        assertEquals(EnrollmentStatus.ENROLLED, student.enrollmentStatus)
        val initialUpdatedAt = student.updatedAt

        Thread.sleep(10)
        student.updateEnrollmentStatus(EnrollmentStatus.GRADUATED)

        assertEquals(EnrollmentStatus.GRADUATED, student.enrollmentStatus)
        assertTrue(student.updatedAt.isAfter(initialUpdatedAt))
    }

    @Test
    fun `should create student with address and emergency contact`() {
        val address = Address(
            street = "123 Main St",
            city = "Springfield",
            state = "IL",
            postalCode = "62701",
            country = "USA"
        )

        val emergencyContact = EmergencyContact(
            name = "Jane Doe",
            relationship = "Mother",
            phoneNumber = "555-1234",
            email = "jane@example.com"
        )

        val student = Student(
            studentId = "STU-2025-009",
            userId = UUID.randomUUID(),
            firstName = "With",
            lastName = "Contact",
            dateOfBirth = LocalDate.of(2010, 1, 1),
            gradeLevel = 5,
            address = address,
            emergencyContact = emergencyContact
        )

        assertNotNull(student.address)
        assertEquals("Springfield", student.address?.city)
        assertNotNull(student.emergencyContact)
        assertEquals("Jane Doe", student.emergencyContact?.name)
    }
}

