package com.visor.school.assessment.service

import com.visor.school.assessment.model.Assessment
import com.visor.school.assessment.model.AssessmentType
import com.visor.school.assessment.repository.AssessmentRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

/**
 * Assessment service for managing evaluation activities
 */
@Service
@Transactional
class AssessmentService(
    private val assessmentRepository: AssessmentRepository
) {
    private val logger = LoggerFactory.getLogger(AssessmentService::class.java)

    /**
     * Create a new assessment
     */
    fun createAssessment(
        classId: UUID,
        name: String,
        type: AssessmentType,
        totalPoints: BigDecimal,
        createdBy: UUID,
        description: String? = null,
        weight: BigDecimal? = null,
        dueDate: LocalDate? = null
    ): Assessment {
        logger.info("Creating assessment: $name for class: $classId")

        val assessment = Assessment(
            classId = classId,
            name = name,
            type = type,
            description = description,
            totalPoints = totalPoints,
            weight = weight,
            dueDate = dueDate,
            createdBy = createdBy
        )

        val saved = assessmentRepository.save(assessment)
        logger.info("Assessment created: ${saved.id}")
        return saved
    }

    /**
     * Get assessment by ID
     */
    @Transactional(readOnly = true)
    fun getAssessment(assessmentId: UUID): Assessment? {
        return assessmentRepository.findById(assessmentId).orElse(null)
    }

    /**
     * Get assessments by class ID
     */
    @Transactional(readOnly = true)
    fun getAssessmentsByClass(classId: UUID): List<Assessment> {
        return assessmentRepository.findByClassId(classId)
    }

    /**
     * Publish an assessment (make it available for grade entry)
     */
    fun publishAssessment(assessmentId: UUID): Assessment {
        val assessment = assessmentRepository.findById(assessmentId)
            .orElseThrow { IllegalArgumentException("Assessment not found: $assessmentId") }

        assessment.publish()
        val saved = assessmentRepository.save(assessment)

        logger.info("Assessment published: $assessmentId")
        return saved
    }

    /**
     * Mark assessment as grading
     */
    fun markAsGrading(assessmentId: UUID): Assessment {
        val assessment = assessmentRepository.findById(assessmentId)
            .orElseThrow { IllegalArgumentException("Assessment not found: $assessmentId") }

        assessment.markAsGrading()
        return assessmentRepository.save(assessment)
    }

    /**
     * Complete an assessment
     */
    fun completeAssessment(assessmentId: UUID): Assessment {
        val assessment = assessmentRepository.findById(assessmentId)
            .orElseThrow { IllegalArgumentException("Assessment not found: $assessmentId") }

        assessment.complete()
        return assessmentRepository.save(assessment)
    }
}

