package com.visor.school.assessment.service;

import com.visor.school.assessment.model.Assessment;
import com.visor.school.assessment.model.Grade;
import com.visor.school.assessment.repository.AssessmentRepository;
import com.visor.school.assessment.repository.GradeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Gradebook service for viewing class and student gradebooks
 */
@Service
@Transactional(readOnly = true)
public class GradebookService {
    
    private final AssessmentRepository assessmentRepository;
    private final GradeRepository gradeRepository;
    private final GradeCalculator gradeCalculator;

    public GradebookService(AssessmentRepository assessmentRepository,
                           GradeRepository gradeRepository,
                           GradeCalculator gradeCalculator) {
        this.assessmentRepository = assessmentRepository;
        this.gradeRepository = gradeRepository;
        this.gradeCalculator = gradeCalculator;
    }

    /**
     * Get class gradebook with all assessments and student grades
     */
    public ClassGradebook getClassGradebook(UUID classId) {
        List<Assessment> assessments = assessmentRepository.findByClassId(classId);
        List<UUID> assessmentIds = assessments.stream()
            .map(Assessment::getId)
            .collect(Collectors.toList());

        Map<UUID, List<Grade>> studentGrades = new HashMap<>();

        // Get all grades for assessments in this class
        for (UUID assessmentId : assessmentIds) {
            List<Grade> grades = gradeRepository.findByAssessmentId(assessmentId);
            for (Grade grade : grades) {
                studentGrades.computeIfAbsent(grade.getStudentId(), k -> new ArrayList<>()).add(grade);
            }
        }

        // Calculate averages for each student
        Map<UUID, BigDecimal> studentAverages = studentGrades.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> gradeCalculator.calculateAverage(entry.getValue())
            ));

        return new ClassGradebook(classId, assessments, studentGrades, studentAverages);
    }

    /**
     * Get student gradebook with all assessments and grades
     */
    public StudentGradebook getStudentGradebook(UUID studentId) {
        List<Grade> grades = gradeRepository.findByStudentId(studentId);
        BigDecimal average = gradeCalculator.calculateAverage(grades);

        return new StudentGradebook(studentId, grades, average);
    }
}
