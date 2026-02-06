package com.visor.school.assessment.service;

import com.visor.school.assessment.model.Assessment;
import com.visor.school.assessment.model.AssessmentType;
import com.visor.school.assessment.repository.AssessmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Assessment service for managing evaluation activities
 */
@Service
@Transactional
public class AssessmentService {
    private static final Logger logger = LoggerFactory.getLogger(AssessmentService.class);
    
    private final AssessmentRepository assessmentRepository;

    public AssessmentService(AssessmentRepository assessmentRepository) {
        this.assessmentRepository = assessmentRepository;
    }

    /**
     * Create a new assessment
     */
    public Assessment createAssessment(
            UUID classId,
            String name,
            AssessmentType type,
            BigDecimal totalPoints,
            UUID createdBy,
            String description,
            BigDecimal weight,
            LocalDate dueDate) {
        logger.info("Creating assessment: {} for class: {}", name, classId);

        Assessment assessment = new Assessment(
            classId,
            name,
            type,
            totalPoints,
            createdBy,
            description,
            weight,
            dueDate
        );

        Assessment saved = assessmentRepository.save(assessment);
        logger.info("Assessment created: {}", saved.getId());
        return saved;
    }

    /**
     * Get assessment by ID
     */
    @Transactional(readOnly = true)
    public Assessment getAssessment(UUID assessmentId) {
        return assessmentRepository.findById(assessmentId).orElse(null);
    }

    /**
     * Get assessments by class ID
     */
    @Transactional(readOnly = true)
    public List<Assessment> getAssessmentsByClass(UUID classId) {
        return assessmentRepository.findByClassId(classId);
    }

    /**
     * Publish an assessment (make it available for grade entry)
     */
    public Assessment publishAssessment(UUID assessmentId) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
            .orElseThrow(() -> new IllegalArgumentException("Assessment not found: " + assessmentId));

        assessment.publish();
        Assessment saved = assessmentRepository.save(assessment);

        logger.info("Assessment published: {}", assessmentId);
        return saved;
    }

    /**
     * Mark assessment as grading
     */
    public Assessment markAsGrading(UUID assessmentId) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
            .orElseThrow(() -> new IllegalArgumentException("Assessment not found: " + assessmentId));

        assessment.markAsGrading();
        return assessmentRepository.save(assessment);
    }

    /**
     * Complete an assessment
     */
    public Assessment completeAssessment(UUID assessmentId) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
            .orElseThrow(() -> new IllegalArgumentException("Assessment not found: " + assessmentId));

        assessment.complete();
        return assessmentRepository.save(assessment);
    }
}
