package com.visor.school.academicservice.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDate

class EnrollmentEntryTest {

    @Test
    fun `should create enrollment entry with required fields`() {
        val entry = EnrollmentEntry(
            academicYear = "2024-2025",
            term = Term.FIRST_TERM,
            gradeLevel = 9,
            enrollmentDate = LocalDate.of(2024, 9, 1),
            status = EnrollmentStatus.ENROLLED
        )

        assertEquals("2024-2025", entry.academicYear)
        assertEquals(Term.FIRST_TERM, entry.term)
        assertEquals(9, entry.gradeLevel)
        assertEquals(LocalDate.of(2024, 9, 1), entry.enrollmentDate)
        assertEquals(EnrollmentStatus.ENROLLED, entry.status)
    }

    @Test
    fun `should accept all term values`() {
        val terms = listOf(
            Term.FIRST_TERM,
            Term.SECOND_TERM,
            Term.THIRD_TERM,
            Term.FULL_YEAR
        )

        terms.forEach { term ->
            val entry = EnrollmentEntry(
                academicYear = "2024-2025",
                term = term,
                gradeLevel = 9,
                enrollmentDate = LocalDate.of(2024, 9, 1),
                status = EnrollmentStatus.ENROLLED
            )

            assertEquals(term, entry.term)
        }
    }

    @Test
    fun `should accept all enrollment status values`() {
        val statuses = listOf(
            EnrollmentStatus.ENROLLED,
            EnrollmentStatus.GRADUATED,
            EnrollmentStatus.TRANSFERRED,
            EnrollmentStatus.WITHDRAWN
        )

        statuses.forEach { status ->
            val entry = EnrollmentEntry(
                academicYear = "2024-2025",
                term = Term.FIRST_TERM,
                gradeLevel = 9,
                enrollmentDate = LocalDate.of(2024, 9, 1),
                status = status
            )

            assertEquals(status, entry.status)
        }
    }

    @Test
    fun `should accept grade levels 1-12`() {
        for (grade in 1..12) {
            val entry = EnrollmentEntry(
                academicYear = "2024-2025",
                term = Term.FIRST_TERM,
                gradeLevel = grade,
                enrollmentDate = LocalDate.of(2024, 9, 1),
                status = EnrollmentStatus.ENROLLED
            )

            assertEquals(grade, entry.gradeLevel)
        }
    }
}

