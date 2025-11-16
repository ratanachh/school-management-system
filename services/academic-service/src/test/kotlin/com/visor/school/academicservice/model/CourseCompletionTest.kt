package com.visor.school.academicservice.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDate

class CourseCompletionTest {

    @Test
    fun `should create course completion with required fields`() {
        val completion = CourseCompletion(
            courseName = "Mathematics 101",
            subject = "Mathematics",
            gradeLevel = 9,
            finalGrade = "A",
            credits = 3,
            completionDate = LocalDate.of(2024, 6, 15)
        )

        assertEquals("Mathematics 101", completion.courseName)
        assertEquals("Mathematics", completion.subject)
        assertEquals(9, completion.gradeLevel)
        assertEquals("A", completion.finalGrade)
        assertEquals(3, completion.credits)
        assertEquals(LocalDate.of(2024, 6, 15), completion.completionDate)
    }

    @Test
    fun `should accept all grade levels 1-12`() {
        for (grade in 1..12) {
            val completion = CourseCompletion(
                courseName = "Test Course",
                subject = "Test",
                gradeLevel = grade,
                finalGrade = "B",
                credits = 3,
                completionDate = LocalDate.of(2024, 6, 15)
            )

            assertEquals(grade, completion.gradeLevel)
        }
    }

    @Test
    fun `should accept common letter grades`() {
        val grades = listOf("A", "B", "C", "D", "F", "A+", "A-", "B+", "B-", "C+", "C-", "D+", "D-")

        grades.forEach { grade ->
            val completion = CourseCompletion(
                courseName = "Test Course",
                subject = "Test",
                gradeLevel = 9,
                finalGrade = grade,
                credits = 3,
                completionDate = LocalDate.of(2024, 6, 15)
            )

            assertEquals(grade, completion.finalGrade)
        }
    }

    @Test
    fun `should accept positive credits`() {
        val credits = listOf(1, 2, 3, 4, 5, 6)

        credits.forEach { credit ->
            val completion = CourseCompletion(
                courseName = "Test Course",
                subject = "Test",
                gradeLevel = 9,
                finalGrade = "B",
                credits = credit,
                completionDate = LocalDate.of(2024, 6, 15)
            )

            assertEquals(credit, completion.credits)
        }
    }
}

