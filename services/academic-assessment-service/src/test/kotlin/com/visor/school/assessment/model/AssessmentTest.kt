package com.visor.school.assessment.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class AssessmentTest {

    @Test
    fun `should create assessment with required fields`() {
        val assessment = Assessment(
            classId = UUID.randomUUID(),
            name = "Midterm Exam",
            type = AssessmentType.EXAM,
            totalPoints = BigDecimal("100.0"),
            createdBy = UUID.randomUUID()
        )

        assertNotNull(assessment.id)
        assertEquals("Midterm Exam", assessment.name)
        assertEquals(AssessmentType.EXAM, assessment.type)
        assertEquals(BigDecimal("100.0"), assessment.totalPoints)
        assertEquals(AssessmentStatus.DRAFT, assessment.status)
        assertNull(assessment.description)
        assertNull(assessment.weight)
        assertNull(assessment.dueDate)
    }

    @Test
    fun `should create assessment with all fields`() {
        val classId = UUID.randomUUID()
        val teacherId = UUID.randomUUID()
        val dueDate = LocalDate.of(2024, 12, 15)

        val assessment = Assessment(
            classId = classId,
            name = "Final Project",
            type = AssessmentType.PROJECT,
            description = "Comprehensive project",
            totalPoints = BigDecimal("150.0"),
            weight = BigDecimal("30.0"),
            dueDate = dueDate,
            createdBy = teacherId
        )

        assertEquals(classId, assessment.classId)
        assertEquals("Final Project", assessment.name)
        assertEquals(AssessmentType.PROJECT, assessment.type)
        assertEquals("Comprehensive project", assessment.description)
        assertEquals(BigDecimal("150.0"), assessment.totalPoints)
        assertEquals(BigDecimal("30.0"), assessment.weight)
        assertEquals(dueDate, assessment.dueDate)
        assertEquals(teacherId, assessment.createdBy)
        assertEquals(AssessmentStatus.DRAFT, assessment.status)
    }

    @Test
    fun `should validate total points is positive`() {
        assertThrows<IllegalArgumentException> {
            Assessment(
                classId = UUID.randomUUID(),
                name = "Test",
                type = AssessmentType.TEST,
                totalPoints = BigDecimal("-10.0"),
                createdBy = UUID.randomUUID()
            )
        }
    }

    @Test
    fun `should validate weight is between 0 and 100`() {
        assertThrows<IllegalArgumentException> {
            Assessment(
                classId = UUID.randomUUID(),
                name = "Test",
                type = AssessmentType.TEST,
                totalPoints = BigDecimal("100.0"),
                weight = BigDecimal("150.0"),
                createdBy = UUID.randomUUID()
            )
        }
    }

    @Test
    fun `should publish assessment`() {
        val assessment = Assessment(
            classId = UUID.randomUUID(),
            name = "Quiz 1",
            type = AssessmentType.QUIZ,
            totalPoints = BigDecimal("50.0"),
            createdBy = UUID.randomUUID()
        )

        val initialUpdatedAt = assessment.updatedAt
        Thread.sleep(10)

        assessment.publish()

        assertEquals(AssessmentStatus.PUBLISHED, assessment.status)
        assertTrue(assessment.updatedAt.isAfter(initialUpdatedAt))
    }

    @Test
    fun `should mark assessment as grading`() {
        val assessment = Assessment(
            classId = UUID.randomUUID(),
            name = "Test",
            type = AssessmentType.TEST,
            totalPoints = BigDecimal("100.0"),
            createdBy = UUID.randomUUID()
        )
        assessment.publish()

        assessment.markAsGrading()

        assertEquals(AssessmentStatus.GRADING, assessment.status)
    }

    @Test
    fun `should complete assessment`() {
        val assessment = Assessment(
            classId = UUID.randomUUID(),
            name = "Test",
            type = AssessmentType.TEST,
            totalPoints = BigDecimal("100.0"),
            createdBy = UUID.randomUUID()
        )
        assessment.publish()
        assessment.markAsGrading()

        assessment.complete()

        assertEquals(AssessmentStatus.COMPLETED, assessment.status)
    }

    @Test
    fun `should accept all assessment types`() {
        val types = listOf(
            AssessmentType.TEST,
            AssessmentType.QUIZ,
            AssessmentType.ASSIGNMENT,
            AssessmentType.PROJECT,
            AssessmentType.EXAM,
            AssessmentType.FINAL_EXAM
        )

        types.forEach { type ->
            val assessment = Assessment(
                classId = UUID.randomUUID(),
                name = "Test $type",
                type = type,
                totalPoints = BigDecimal("100.0"),
                createdBy = UUID.randomUUID()
            )

            assertEquals(type, assessment.type)
        }
    }
}

