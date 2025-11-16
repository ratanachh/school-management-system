package com.visor.school.assessment.integration

import com.visor.school.assessment.model.Assessment
import com.visor.school.assessment.model.AssessmentType
import com.visor.school.assessment.model.Grade
import com.visor.school.assessment.repository.AssessmentRepository
import com.visor.school.assessment.repository.GradeRepository
import com.visor.school.assessment.service.AssessmentService
import com.visor.school.assessment.service.GradeService
import com.visor.school.assessment.service.GradebookService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.util.UUID

/**
 * Integration test for gradebook view
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class GradebookIntegrationTest @Autowired constructor(
    private val assessmentService: AssessmentService,
    private val gradeService: GradeService,
    private val gradebookService: GradebookService,
    private val assessmentRepository: AssessmentRepository,
    private val gradeRepository: GradeRepository
) {

    @Test
    fun `should get class gradebook with assessments and grades`() {
        // Given
        val classId = UUID.randomUUID()
        val teacherId = UUID.randomUUID()
        val studentId1 = UUID.randomUUID()
        val studentId2 = UUID.randomUUID()

        val assessment1 = assessmentService.createAssessment(
            classId = classId,
            name = "Test 1",
            type = AssessmentType.TEST,
            totalPoints = BigDecimal("100.0"),
            createdBy = teacherId
        )
        assessmentService.publishAssessment(assessment1.id)

        val assessment2 = assessmentService.createAssessment(
            classId = classId,
            name = "Quiz 1",
            type = AssessmentType.QUIZ,
            totalPoints = BigDecimal("50.0"),
            createdBy = teacherId
        )
        assessmentService.publishAssessment(assessment2.id)

        gradeService.recordGrade(studentId1, assessment1.id, BigDecimal("85.0"), teacherId)
        gradeService.recordGrade(studentId1, assessment2.id, BigDecimal("45.0"), teacherId)
        gradeService.recordGrade(studentId2, assessment1.id, BigDecimal("90.0"), teacherId)

        // When
        val gradebook = gradebookService.getClassGradebook(classId)

        // Then
        assertNotNull(gradebook)
        assertEquals(classId, gradebook.classId)
        assertTrue(gradebook.assessments.size >= 2)
        assertTrue(gradebook.studentGrades.isNotEmpty())
    }

    @Test
    fun `should get student gradebook with all assessments`() {
        // Given
        val classId = UUID.randomUUID()
        val teacherId = UUID.randomUUID()
        val studentId = UUID.randomUUID()

        val assessment1 = assessmentService.createAssessment(
            classId = classId,
            name = "Test 1",
            type = AssessmentType.TEST,
            totalPoints = BigDecimal("100.0"),
            createdBy = teacherId
        )
        assessmentService.publishAssessment(assessment1.id)

        val assessment2 = assessmentService.createAssessment(
            classId = classId,
            name = "Quiz 1",
            type = AssessmentType.QUIZ,
            totalPoints = BigDecimal("50.0"),
            createdBy = teacherId
        )
        assessmentService.publishAssessment(assessment2.id)

        gradeService.recordGrade(studentId, assessment1.id, BigDecimal("85.0"), teacherId)
        gradeService.recordGrade(studentId, assessment2.id, BigDecimal("45.0"), teacherId)

        // When
        val studentGradebook = gradebookService.getStudentGradebook(studentId)

        // Then
        assertNotNull(studentGradebook)
        assertEquals(studentId, studentGradebook.studentId)
        assertTrue(studentGradebook.grades.size >= 2)
    }
}

