package com.visor.school.assessment.service

import com.visor.school.assessment.event.GradeEventPublisher
import com.visor.school.assessment.model.AssessmentStatus
import com.visor.school.assessment.model.Grade
import com.visor.school.assessment.repository.AssessmentRepository
import com.visor.school.assessment.repository.GradeRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.util.UUID

/**
 * Grade service for recording and managing student grades
 */
@Service
@Transactional
class GradeService(
    private val gradeRepository: GradeRepository,
    private val assessmentRepository: AssessmentRepository,
    private val gradeCalculator: GradeCalculator,
    private val letterGradeConverter: LetterGradeConverter,
    private val gradeEventPublisher: GradeEventPublisher
) {
    private val logger = LoggerFactory.getLogger(GradeService::class.java)

    /**
     * Record a grade for a student on an assessment
     */
    fun recordGrade(
        studentId: UUID,
        assessmentId: UUID,
        score: BigDecimal,
        recordedBy: UUID,
        notes: String? = null
    ): Grade {
        logger.info("Recording grade for student: $studentId, assessment: $assessmentId, score: $score")

        // Validate assessment exists and is published
        val assessment = assessmentRepository.findById(assessmentId)
            .orElseThrow { IllegalArgumentException("Assessment not found: $assessmentId") }

        require(assessment.status == AssessmentStatus.PUBLISHED) {
            "Assessment must be published to record grades, current status: ${assessment.status}"
        }

        // Check if grade already exists
        val existingGrade = gradeRepository.findByStudentIdAndAssessmentId(studentId, assessmentId)

        val grade = if (existingGrade.isPresent) {
            // Update existing grade
            val grade = existingGrade.get()
            grade.updateScore(score, recordedBy)
            if (notes != null) {
                grade.addNotes(notes)
            }
            grade
        } else {
            // Create new grade
            Grade(
                studentId = studentId,
                assessmentId = assessmentId,
                score = score,
                totalPoints = assessment.totalPoints,
                recordedBy = recordedBy,
                notes = notes
            ).apply {
                // Calculate and set letter grade
                val letterGrade = letterGradeConverter.convert(percentage)
                assignLetterGrade(letterGrade)
            }
        }

        val saved = gradeRepository.save(grade)
        logger.info("Grade recorded: ${saved.id}")

        // Publish event
        gradeEventPublisher.publishGradeRecorded(saved)

        return saved
    }

    /**
     * Update an existing grade
     */
    fun updateGrade(
        studentId: UUID,
        assessmentId: UUID,
        newScore: BigDecimal,
        updatedBy: UUID,
        notes: String? = null
    ): Grade {
        logger.info("Updating grade for student: $studentId, assessment: $assessmentId, new score: $newScore")

        val grade = gradeRepository.findByStudentIdAndAssessmentId(studentId, assessmentId)
            .orElseThrow { IllegalArgumentException("Grade not found for student: $studentId, assessment: $assessmentId") }

        grade.updateScore(newScore, updatedBy)

        // Update letter grade
        val letterGrade = letterGradeConverter.convert(grade.percentage)
        grade.assignLetterGrade(letterGrade)

        if (notes != null) {
            grade.addNotes(notes)
        }

        val saved = gradeRepository.save(grade)

        // Publish event
        gradeEventPublisher.publishGradeUpdated(saved)

        return saved
    }

    /**
     * Get grades for a student
     */
    @Transactional(readOnly = true)
    fun getGradesByStudent(studentId: UUID): List<Grade> {
        return gradeRepository.findByStudentId(studentId)
    }

    /**
     * Get grades for an assessment
     */
    @Transactional(readOnly = true)
    fun getGradesByAssessment(assessmentId: UUID): List<Grade> {
        return gradeRepository.findByAssessmentId(assessmentId)
    }

    /**
     * Calculate average grade for a student
     */
    @Transactional(readOnly = true)
    fun calculateAverageGrade(studentId: UUID): BigDecimal {
        val grades = gradeRepository.findByStudentId(studentId)
        return gradeCalculator.calculateAverage(grades)
    }
}

