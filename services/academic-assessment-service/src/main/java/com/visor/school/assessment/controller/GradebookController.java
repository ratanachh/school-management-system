package com.visor.school.assessment.controller;

import com.visor.school.assessment.service.ClassGradebook;
import com.visor.school.assessment.service.GradebookService;
import com.visor.school.assessment.service.StudentGradebook;
import com.visor.school.common.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Gradebook controller
 * Accessible by ADMINISTRATOR, TEACHER, STUDENT, and PARENT roles
 */
@RestController
@RequestMapping("/api/v1/gradebooks")
public class GradebookController {
    
    private final GradebookService gradebookService;

    public GradebookController(GradebookService gradebookService) {
        this.gradebookService = gradebookService;
    }

    /**
     * Get class gradebook with all assessments and student grades
     */
    @GetMapping("/class/{classId}")
    @PreAuthorize("hasRole('ADMINISTRATOR') or hasRole('TEACHER') or hasRole('STUDENT') or hasRole('PARENT')")
    public ResponseEntity<ApiResponse<ClassGradebookResponse>> getClassGradebook(@PathVariable UUID classId) {
        ClassGradebook gradebook = gradebookService.getClassGradebook(classId);
        return ResponseEntity.ok(ApiResponse.success(ClassGradebookResponse.from(gradebook)));
    }

    /**
     * Get student gradebook with all assessments and grades
     */
    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasRole('ADMINISTRATOR') or hasRole('TEACHER') or hasRole('STUDENT') or hasRole('PARENT')")
    public ResponseEntity<ApiResponse<StudentGradebookResponse>> getStudentGradebook(@PathVariable UUID studentId) {
        StudentGradebook gradebook = gradebookService.getStudentGradebook(studentId);
        return ResponseEntity.ok(ApiResponse.success(StudentGradebookResponse.from(gradebook)));
    }
}

class ClassGradebookResponse {
    private final UUID classId;
    private final List<Map<String, Object>> assessments;
    private final Map<String, List<Map<String, Object>>> studentGrades;
    private final Map<String, Object> studentAverages;

    public ClassGradebookResponse(UUID classId, List<Map<String, Object>> assessments,
                                 Map<String, List<Map<String, Object>>> studentGrades,
                                 Map<String, Object> studentAverages) {
        this.classId = classId;
        this.assessments = assessments;
        this.studentGrades = studentGrades;
        this.studentAverages = studentAverages;
    }

    public static ClassGradebookResponse from(ClassGradebook gradebook) {
        List<Map<String, Object>> assessments = gradebook.getAssessments().stream()
            .map(assessment -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", assessment.getId().toString());
                map.put("name", assessment.getName());
                map.put("type", assessment.getType().name());
                map.put("totalPoints", assessment.getTotalPoints().toString());
                map.put("status", assessment.getStatus().name());
                return map;
            })
            .collect(Collectors.toList());

        Map<String, List<Map<String, Object>>> studentGrades = new HashMap<>();
        for (Map.Entry<UUID, List<com.visor.school.assessment.model.Grade>> entry : gradebook.getStudentGrades().entrySet()) {
            List<Map<String, Object>> gradesList = entry.getValue().stream()
                .map(grade -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", grade.getId().toString());
                    map.put("assessmentId", grade.getAssessmentId().toString());
                    map.put("score", grade.getScore());
                    map.put("percentage", grade.getPercentage());
                    map.put("letterGrade", grade.getLetterGrade() != null ? grade.getLetterGrade() : "");
                    return map;
                })
                .collect(Collectors.toList());
            studentGrades.put(entry.getKey().toString(), gradesList);
        }

        Map<String, Object> studentAverages = new HashMap<>();
        for (Map.Entry<UUID, java.math.BigDecimal> entry : gradebook.getStudentAverages().entrySet()) {
            studentAverages.put(entry.getKey().toString(), entry.getValue());
        }

        return new ClassGradebookResponse(gradebook.getClassId(), assessments, studentGrades, studentAverages);
    }

    // Getters
    public UUID getClassId() { return classId; }
    public List<Map<String, Object>> getAssessments() { return assessments; }
    public Map<String, List<Map<String, Object>>> getStudentGrades() { return studentGrades; }
    public Map<String, Object> getStudentAverages() { return studentAverages; }
}

class StudentGradebookResponse {
    private final UUID studentId;
    private final List<Map<String, Object>> grades;
    private final Object average;

    public StudentGradebookResponse(UUID studentId, List<Map<String, Object>> grades, Object average) {
        this.studentId = studentId;
        this.grades = grades;
        this.average = average;
    }

    public static StudentGradebookResponse from(StudentGradebook gradebook) {
        List<Map<String, Object>> grades = gradebook.getGrades().stream()
            .map(grade -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", grade.getId().toString());
                map.put("assessmentId", grade.getAssessmentId().toString());
                map.put("score", grade.getScore());
                map.put("totalPoints", grade.getTotalPoints());
                map.put("percentage", grade.getPercentage());
                map.put("letterGrade", grade.getLetterGrade() != null ? grade.getLetterGrade() : "");
                map.put("recordedAt", grade.getRecordedAt());
                return map;
            })
            .collect(Collectors.toList());

        return new StudentGradebookResponse(gradebook.getStudentId(), grades, gradebook.getAverage());
    }

    // Getters
    public UUID getStudentId() { return studentId; }
    public List<Map<String, Object>> getGrades() { return grades; }
    public Object getAverage() { return average; }
}
