package com.visor.school.assessment.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.UUID

class GradeTest {

    @Test
    fun `should create grade with required fields`() {
        val grade = Grade(
            studentId = UUID.randomUUID(),
            assessmentId = UUID.randomUUID(),
            score = BigDecimal("85.5"),
            totalPoints = BigDecimal("100.0"),
            recordedBy = UUID.randomUUID()
        )

        assertNotNull(grade.id)
        assertEquals(BigDecimal("85.5"), grade.score)
        assertEquals(BigDecimal("100.0"), grade.totalPoints)
        assertEquals(BigDecimal("85.5"), grade.percentage)
        assertNull(grade.letterGrade)
        assertNull(grade.notes)
    }

    @Test
    fun `should calculate percentage correctly`() {
        val grade = Grade(
            studentId = UUID.randomUUID(),
            assessmentId = UUID.randomUUID(),
            score = BigDecimal("75.0"),
            totalPoints = BigDecimal("100.0"),
            recordedBy = UUID.randomUUID()
        )

        assertEquals(BigDecimal("75.0"), grade.percentage)
    }

    @Test
    fun `should calculate percentage for partial points`() {
        val grade = Grade(
            studentId = UUID.randomUUID(),
            assessmentId = UUID.randomUUID(),
            score = BigDecimal("42.5"),
            totalPoints = BigDecimal("50.0"),
            recordedBy = UUID.randomUUID()
        )

        assertEquals(BigDecimal("85.0"), grade.percentage)
    }

    @Test
    fun `should validate score is not negative`() {
        assertThrows<IllegalArgumentException> {
            Grade(
                studentId = UUID.randomUUID(),
                assessmentId = UUID.randomUUID(),
                score = BigDecimal("-10.0"),
                totalPoints = BigDecimal("100.0"),
                recordedBy = UUID.randomUUID()
            )
        }
    }

    @Test
    fun `should validate score does not exceed total points`() {
        assertThrows<IllegalArgumentException> {
            Grade(
                studentId = UUID.randomUUID(),
                assessmentId = UUID.randomUUID(),
                score = BigDecimal("150.0"),
                totalPoints = BigDecimal("100.0"),
                recordedBy = UUID.randomUUID()
            )
        }
    }

    @Test
    fun `should update grade`() {
        val grade = Grade(
            studentId = UUID.randomUUID(),
            assessmentId = UUID.randomUUID(),
            score = BigDecimal("80.0"),
            totalPoints = BigDecimal("100.0"),
            recordedBy = UUID.randomUUID()
        )

        val initialUpdatedAt = grade.updatedAt
        val updatedBy = UUID.randomUUID()
        Thread.sleep(10)

        grade.updateScore(BigDecimal("90.0"), updatedBy)

        assertEquals(BigDecimal("90.0"), grade.score)
        assertEquals(BigDecimal("90.0"), grade.percentage)
        assertEquals(updatedBy, grade.updatedBy)
        assertTrue(grade.updatedAt.isAfter(initialUpdatedAt))
    }

    @Test
    fun `should set letter grade`() {
        val grade = Grade(
            studentId = UUID.randomUUID(),
            assessmentId = UUID.randomUUID(),
            score = BigDecimal("92.0"),
            totalPoints = BigDecimal("100.0"),
            recordedBy = UUID.randomUUID()
        )

        grade.setLetterGrade("A")

        assertEquals("A", grade.letterGrade)
    }

    @Test
    fun `should add notes`() {
        val grade = Grade(
            studentId = UUID.randomUUID(),
            assessmentId = UUID.randomUUID(),
            score = BigDecimal("75.0"),
            totalPoints = BigDecimal("100.0"),
            recordedBy = UUID.randomUUID()
        )

        grade.addNotes("Good effort, needs improvement")

        assertEquals("Good effort, needs improvement", grade.notes)
    }
}

