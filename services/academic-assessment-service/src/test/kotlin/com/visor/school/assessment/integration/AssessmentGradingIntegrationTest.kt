package com.visor.school.assessment.integration

import com.visor.school.assessment.model.Assessment
import com.visor.school.assessment.model.AssessmentStatus
import com.visor.school.assessment.model.AssessmentType
import com.visor.school.assessment.model.Grade
import com.visor.school.assessment.repository.AssessmentRepository
import com.visor.school.assessment.repository.GradeRepository
import com.visor.school.assessment.service.AssessmentService
import com.visor.school.assessment.service.GradeService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.util.UUID

/**
 * Integration test for assessment creation and grading flow
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AssessmentGradingIntegrationTest @Autowired constructor(
    private val assessmentService: AssessmentService,
    private val gradeService: GradeService,
    private val assessmentRepository: AssessmentRepository,
    private val gradeRepository: GradeRepository
) {

    @Test
    fun `should create assessment, publish, and record grades`() {
        // Given
        val classId = UUID.randomUUID()
        val teacherId = UUID.randomUUID()
        val studentId = UUID.randomUUID()

        // When - Create assessment
        val assessment = assessmentService.createAssessment(
            classId = classId,
            name = "Midterm Exam",
            type = AssessmentType.EXAM,
            totalPoints = BigDecimal("100.0"),
            createdBy = teacherId
        )

        // Then
        assertNotNull(assessment.id)
        assertEquals(AssessmentStatus.DRAFT, assessment.status)

        // When - Publish assessment
        val published = assessmentService.publishAssessment(assessment.id)

        // Then
        assertEquals(AssessmentStatus.PUBLISHED, published.status)

        // When - Record grade
        val grade = gradeService.recordGrade(
            studentId = studentId,
            assessmentId = published.id,
            score = BigDecimal("85.0"),
            recordedBy = teacherId
        )

        // Then
        assertNotNull(grade.id)
        assertEquals(BigDecimal("85.0"), grade.score)
        assertEquals(BigDecimal("85.0"), grade.percentage)
    }

    @Test
    fun `should update grade and maintain audit trail`() {
        // Given
        val classId = UUID.randomUUID()
        val teacherId = UUID.randomUUID()
        val studentId = UUID.randomUUID()

        val assessment = assessmentService.createAssessment(
            classId = classId,
            name = "Test",
            type = AssessmentType.TEST,
            totalPoints = BigDecimal("100.0"),
            createdBy = teacherId
        )
        assessmentService.publishAssessment(assessment.id)

        val grade = gradeService.recordGrade(
            studentId = studentId,
            assessmentId = assessment.id,
            score = BigDecimal("80.0"),
            recordedBy = teacherId
        )

        // When - Update grade
        val updatedGrade = gradeService.updateGrade(
            studentId = studentId,
            assessmentId = assessment.id,
            newScore = BigDecimal("90.0"),
            updatedBy = teacherId
        )

        // Then
        assertEquals(BigDecimal("90.0"), updatedGrade.score)
        assertEquals(teacherId, updatedGrade.updatedBy)
        assertNotNull(updatedGrade.updatedAt)
    }
}

