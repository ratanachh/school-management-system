package com.visor.school.academicservice.repository

import com.visor.school.academicservice.model.EnrollmentStatus
import com.visor.school.academicservice.model.Student
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate
import java.util.UUID

@DataJpaTest
@ActiveProfiles("test")
class StudentRepositoryTest @Autowired constructor(
    private val entityManager: TestEntityManager,
    private val studentRepository: StudentRepository
) {

    private lateinit var testStudent: Student

    @BeforeEach
    fun setup() {
        testStudent = Student(
            studentId = "STU-TEST-001",
            userId = UUID.randomUUID(),
            firstName = "Test",
            lastName = "Student",
            dateOfBirth = LocalDate.of(2010, 1, 1),
            gradeLevel = 5,
            enrollmentStatus = EnrollmentStatus.ENROLLED
        )
        entityManager.persistAndFlush(testStudent)
    }

    @Test
    fun `should find student by studentId`() {
        val found = studentRepository.findByStudentId("STU-TEST-001")

        assertTrue(found.isPresent)
        assertEquals("STU-TEST-001", found.get().studentId)
        assertEquals("Test", found.get().firstName)
    }

    @Test
    fun `should find student by userId`() {
        val found = studentRepository.findByUserId(testStudent.userId)

        assertTrue(found.isPresent)
        assertEquals(testStudent.userId, found.get().userId)
        assertEquals("STU-TEST-001", found.get().studentId)
    }

    @Test
    fun `should find students by grade level`() {
        val student2 = Student(
            studentId = "STU-TEST-002",
            userId = UUID.randomUUID(),
            firstName = "Grade",
            lastName = "Five",
            dateOfBirth = LocalDate.of(2010, 6, 1),
            gradeLevel = 5,
            enrollmentStatus = EnrollmentStatus.ENROLLED
        )
        entityManager.persistAndFlush(student2)

        val students = studentRepository.findByGradeLevel(5)

        assertTrue(students.size >= 2)
        assertTrue(students.all { it.gradeLevel == 5 })
    }

    @Test
    fun `should find students by enrollment status`() {
        val graduatedStudent = Student(
            studentId = "STU-TEST-003",
            userId = UUID.randomUUID(),
            firstName = "Graduated",
            lastName = "Student",
            dateOfBirth = LocalDate.of(2006, 1, 1),
            gradeLevel = 12,
            enrollmentStatus = EnrollmentStatus.GRADUATED
        )
        entityManager.persistAndFlush(graduatedStudent)

        val graduated = studentRepository.findByEnrollmentStatus(EnrollmentStatus.GRADUATED)

        assertTrue(graduated.isNotEmpty())
        assertTrue(graduated.all { it.enrollmentStatus == EnrollmentStatus.GRADUATED })
    }

    @Test
    fun `should return empty when student not found by studentId`() {
        val found = studentRepository.findByStudentId("NONEXISTENT")

        assertFalse(found.isPresent)
    }

    @Test
    fun `should return empty when student not found by userId`() {
        val found = studentRepository.findByUserId(UUID.randomUUID())

        assertFalse(found.isPresent)
    }
}

