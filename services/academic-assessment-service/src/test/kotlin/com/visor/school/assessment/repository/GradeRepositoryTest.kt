package com.visor.school.assessment.repository

import com.visor.school.assessment.model.Grade
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal
import java.util.UUID

@DataJpaTest
@ActiveProfiles("test")
class GradeRepositoryTest @Autowired constructor(
    private val gradeRepository: GradeRepository
) {

    @Test
    fun `should save and find grade by id`() {
        // Given
        val grade = Grade(
            studentId = UUID.randomUUID(),
            assessmentId = UUID.randomUUID(),
            score = BigDecimal("85.0"),
            totalPoints = BigDecimal("100.0"),
            recordedBy = UUID.randomUUID()
        )

        // When
        val saved = gradeRepository.save(grade)
        val found = gradeRepository.findById(saved.id)

        // Then
        assertTrue(found.isPresent)
        assertEquals(BigDecimal("85.0"), found.get().score)
    }

    @Test
    fun `should find grades by student id`() {
        // Given
        val studentId = UUID.randomUUID()
        val grade1 = Grade(
            studentId = studentId,
            assessmentId = UUID.randomUUID(),
            score = BigDecimal("85.0"),
            totalPoints = BigDecimal("100.0"),
            recordedBy = UUID.randomUUID()
        )
        val grade2 = Grade(
            studentId = studentId,
            assessmentId = UUID.randomUUID(),
            score = BigDecimal("90.0"),
            totalPoints = BigDecimal("100.0"),
            recordedBy = UUID.randomUUID()
        )

        gradeRepository.save(grade1)
        gradeRepository.save(grade2)

        // When
        val found = gradeRepository.findByStudentId(studentId)

        // Then
        assertEquals(2, found.size)
    }

    @Test
    fun `should find grades by assessment id`() {
        // Given
        val assessmentId = UUID.randomUUID()
        val grade1 = Grade(
            studentId = UUID.randomUUID(),
            assessmentId = assessmentId,
            score = BigDecimal("85.0"),
            totalPoints = BigDecimal("100.0"),
            recordedBy = UUID.randomUUID()
        )
        val grade2 = Grade(
            studentId = UUID.randomUUID(),
            assessmentId = assessmentId,
            score = BigDecimal("90.0"),
            totalPoints = BigDecimal("100.0"),
            recordedBy = UUID.randomUUID()
        )

        gradeRepository.save(grade1)
        gradeRepository.save(grade2)

        // When
        val found = gradeRepository.findByAssessmentId(assessmentId)

        // Then
        assertEquals(2, found.size)
    }

    @Test
    fun `should find grade by student and assessment`() {
        // Given
        val studentId = UUID.randomUUID()
        val assessmentId = UUID.randomUUID()
        val grade = Grade(
            studentId = studentId,
            assessmentId = assessmentId,
            score = BigDecimal("85.0"),
            totalPoints = BigDecimal("100.0"),
            recordedBy = UUID.randomUUID()
        )

        gradeRepository.save(grade)

        // When
        val found = gradeRepository.findByStudentIdAndAssessmentId(studentId, assessmentId)

        // Then
        assertTrue(found.isPresent)
        assertEquals(BigDecimal("85.0"), found.get().score)
    }
}

