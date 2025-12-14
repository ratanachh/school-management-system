package com.visor.school.academicservice.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

class AcademicRecordTest {

    @Test
    fun `should create academic record with required fields`() {
        val record = AcademicRecord(
            studentId = UUID.randomUUID(),
            cumulativeGPA = BigDecimal("3.4"),
            creditsEarned = 24,
            academicStanding = AcademicStanding.GOOD_STANDING
        )

        assertNotNull(record.id)
        assertNotNull(record.studentId)
        assertEquals(BigDecimal("3.4"), record.cumulativeGPA)
        assertEquals(24, record.creditsEarned)
        assertEquals(AcademicStanding.GOOD_STANDING, record.academicStanding)
        assertNull(record.graduationDate)
    }

    @Test
    fun `should create academic record with enrollment history`() {
        val enrollmentHistory = mutableListOf(
            EnrollmentEntry(
                academicYear = "2023-2024",
                term = Term.FIRST_TERM,
                gradeLevel = 9,
                enrollmentDate = LocalDate.of(2023, 9, 1),
                status = EnrollmentStatus.ENROLLED
            ),
            EnrollmentEntry(
                academicYear = "2024-2025",
                term = Term.FIRST_TERM,
                gradeLevel = 10,
                enrollmentDate = LocalDate.of(2024, 9, 1),
                status = EnrollmentStatus.ENROLLED
            )
        )

        val record = AcademicRecord(
            studentId = UUID.randomUUID(),
            enrollmentHistory = enrollmentHistory,
            cumulativeGPA = BigDecimal("3.5"),
            creditsEarned = 30,
            academicStanding = AcademicStanding.GOOD_STANDING
        )

        assertEquals(2, record.enrollmentHistory.size)
        assertEquals(9, record.enrollmentHistory[0].gradeLevel)
        assertEquals(10, record.enrollmentHistory[1].gradeLevel)
    }

    @Test
    fun `should create academic record with completed courses`() {
        val record = AcademicRecord(
            studentId = UUID.randomUUID(),
            cumulativeGPA = BigDecimal("3.5"),
            creditsEarned = 0,
            academicStanding = AcademicStanding.GOOD_STANDING
        )

        val course1 = CourseCompletion(
            courseName = "Mathematics 101",
            subject = "Mathematics",
            gradeLevel = 9,
            finalGrade = "A",
            credits = 3,
            completionDate = LocalDate.of(2024, 6, 15)
        )
        val course2 = CourseCompletion(
            courseName = "English 101",
            subject = "English",
            gradeLevel = 9,
            finalGrade = "B",
            credits = 3,
            completionDate = LocalDate.of(2024, 6, 15)
        )

        record.completedCourses.add(course1)
        record.completedCourses.add(course2)

        assertEquals(2, record.completedCourses.size)
        assertEquals("Mathematics 101", record.completedCourses[0].courseName)
        assertEquals("English 101", record.completedCourses[1].courseName)
    }

    @Test
    fun `should update academic standing`() {
        val record = AcademicRecord(
            studentId = UUID.randomUUID(),
            cumulativeGPA = BigDecimal("3.4"),
            creditsEarned = 24,
            academicStanding = AcademicStanding.GOOD_STANDING
        )

        record.academicStanding = AcademicStanding.PROBATION

        assertEquals(AcademicStanding.PROBATION, record.academicStanding)
    }

    @Test
    fun `should mark as graduated`() {
        val record = AcademicRecord(
            studentId = UUID.randomUUID(),
            cumulativeGPA = BigDecimal("3.4"),
            creditsEarned = 120,
            academicStanding = AcademicStanding.GOOD_STANDING
        )

        val graduationDate = LocalDate.of(2025, 6, 15)
        record.graduationDate = graduationDate

        assertEquals(graduationDate, record.graduationDate)
    }

    @Test
    fun `should add completed course`() {
        val record = AcademicRecord(
            studentId = UUID.randomUUID(),
            cumulativeGPA = BigDecimal("3.4"),
            creditsEarned = 0,
            academicStanding = AcademicStanding.GOOD_STANDING
        )

        val course = CourseCompletion(
            courseName = "Science 101",
            subject = "Science",
            gradeLevel = 9,
            finalGrade = "A",
            credits = 3,
            completionDate = LocalDate.of(2024, 6, 15)
        )

        record.completedCourses.add(course)

        assertEquals(1, record.completedCourses.size)
        assertEquals("Science 101", record.completedCourses[0].courseName)
    }

    @Test
    fun `should accept all academic standing values`() {
        val standings = listOf(
            AcademicStanding.GOOD_STANDING,
            AcademicStanding.PROBATION,
            AcademicStanding.SUSPENDED,
            AcademicStanding.GRADUATED
        )

        standings.forEach { standing ->
            val record = AcademicRecord(
                studentId = UUID.randomUUID(),
                cumulativeGPA = BigDecimal("3.0"),
                creditsEarned = 0,
                academicStanding = standing
            )

            assertEquals(standing, record.academicStanding)
        }
    }
}
