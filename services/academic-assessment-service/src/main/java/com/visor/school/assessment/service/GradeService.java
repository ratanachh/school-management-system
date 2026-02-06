package com.visor.school.assessment.service;

import com.visor.school.assessment.event.GradeEventPublisher;
import com.visor.school.assessment.model.Assessment;
import com.visor.school.assessment.model.AssessmentStatus;
import com.visor.school.assessment.model.Grade;
import com.visor.school.assessment.repository.AssessmentRepository;
import com.visor.school.assessment.repository.GradeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Grade service for recording and managing student grades
 */
@Service
@Transactional
public class GradeService {
    private static final Logger logger = LoggerFactory.getLogger(GradeService.class);
    
    private final GradeRepository gradeRepository;
    private final AssessmentRepository assessmentRepository;
    private final GradeCalculator gradeCalculator;
    private final LetterGradeConverter letterGradeConverter;
    private final GradeEventPublisher gradeEventPublisher;

    public GradeService(GradeRepository gradeRepository,
                       AssessmentRepository assessmentRepository,
                       GradeCalculator gradeCalculator,
                       LetterGradeConverter letterGradeConverter,
                       GradeEventPublisher gradeEventPublisher) {
        this.gradeRepository = gradeRepository;
        this.assessmentRepository = assessmentRepository;
        this.gradeCalculator = gradeCalculator;
        this.letterGradeConverter = letterGradeConverter;
        this.gradeEventPublisher = gradeEventPublisher;
    }

    /**
     * Record a grade for a student on an assessment
     */
    public Grade recordGrade(
            UUID studentId,
            UUID assessmentId,
            BigDecimal score,
            UUID recordedBy,
            String notes) {
        logger.info("Recording grade for student: {}, assessment: {}, score: {}", studentId, assessmentId, score);

        // Validate assessment exists and is published
        Assessment assessment = assessmentRepository.findById(assessmentId)
            .orElseThrow(() -> new IllegalArgumentException("Assessment not found: " + assessmentId));

        if (assessment.getStatus() != AssessmentStatus.PUBLISHED) {
            throw new IllegalArgumentException(
                "Assessment must be published to record grades, current status: " + assessment.getStatus());
        }

        // Check if grade already exists
        Optional<Grade> existingGrade = gradeRepository.findByStudentIdAndAssessmentId(studentId, assessmentId);

        Grade grade;
        if (existingGrade.isPresent()) {
            // Update existing grade
            grade = existingGrade.get();
            grade.updateScore(score, recordedBy);
            if (notes != null) {
                grade.addNotes(notes);
            }
        } else {
            // Create new grade
            grade = new Grade(studentId, assessmentId, score, assessment.getTotalPoints(), recordedBy);
            if (notes != null) {
                grade.setNotes(notes);
            }
            // Calculate and set letter grade
            String letterGrade = letterGradeConverter.convert(grade.getPercentage());
            grade.assignLetterGrade(letterGrade);
        }

        Grade saved = gradeRepository.save(grade);
        logger.info("Grade recorded: {}", saved.getId());

        // Publish event
        gradeEventPublisher.publishGradeRecorded(saved);

        return saved;
    }

    /**
     * Update an existing grade
     */
    public Grade updateGrade(
            UUID studentId,
            UUID assessmentId,
            BigDecimal newScore,
            UUID updatedBy,
            String notes) {
        logger.info("Updating grade for student: {}, assessment: {}, new score: {}", studentId, assessmentId, newScore);

        Grade grade = gradeRepository.findByStudentIdAndAssessmentId(studentId, assessmentId)
            .orElseThrow(() -> new IllegalArgumentException(
                "Grade not found for student: " + studentId + ", assessment: " + assessmentId));

        grade.updateScore(newScore, updatedBy);

        // Update letter grade
        String letterGrade = letterGradeConverter.convert(grade.getPercentage());
        grade.assignLetterGrade(letterGrade);

        if (notes != null) {
            grade.addNotes(notes);
        }

        Grade saved = gradeRepository.save(grade);

        // Publish event
        gradeEventPublisher.publishGradeUpdated(saved);

        return saved;
    }

    /**
     * Get grades for a student
     */
    @Transactional(readOnly = true)
    public List<Grade> getGradesByStudent(UUID studentId) {
        return gradeRepository.findByStudentId(studentId);
    }

    /**
     * Get grades for an assessment
     */
    @Transactional(readOnly = true)
    public List<Grade> getGradesByAssessment(UUID assessmentId) {
        return gradeRepository.findByAssessmentId(assessmentId);
    }

    /**
     * Calculate average grade for a student
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateAverageGrade(UUID studentId) {
        List<Grade> grades = gradeRepository.findByStudentId(studentId);
        return gradeCalculator.calculateAverage(grades);
    }
}
