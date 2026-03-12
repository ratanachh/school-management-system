package com.visor.school.assessment.controller;

import com.visor.school.assessment.model.Grade;
import com.visor.school.assessment.service.GradeService;
import com.visor.school.common.api.ApiResponse;
import com.visor.school.common.api.Permissions;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Grade controller
 * Requires TEACHER role with MANAGE_GRADES permission or ADMINISTRATOR role
 */
@RestController
@RequestMapping("/api/v1/grades")
public class GradeController {
    
    private final GradeService gradeService;

    public GradeController(GradeService gradeService) {
        this.gradeService = gradeService;
    }

    /**
     * Record a grade for a student on an assessment
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRATOR') or (hasRole('TEACHER') and hasAuthority('" + Permissions.MANAGE_GRADES + "'))")
    public ResponseEntity<ApiResponse<GradeResponse>> recordGrade(@Valid @RequestBody RecordGradeRequest request) {
        Grade grade = gradeService.recordGrade(
            request.getStudentId(),
            request.getAssessmentId(),
            request.getScore(),
            request.getRecordedBy(), // In production, get from JWT token
            request.getNotes()
        );

        return ResponseEntity.ok(ApiResponse.success(GradeResponse.from(grade)));
    }

    /**
     * Update an existing grade
     */
    @PutMapping
    @PreAuthorize("hasRole('ADMINISTRATOR') or (hasRole('TEACHER') and hasAuthority('" + Permissions.MANAGE_GRADES + "'))")
    public ResponseEntity<ApiResponse<GradeResponse>> updateGrade(@Valid @RequestBody UpdateGradeRequest request) {
        Grade grade = gradeService.updateGrade(
            request.getStudentId(),
            request.getAssessmentId(),
            request.getNewScore(),
            request.getUpdatedBy(), // In production, get from JWT token
            request.getNotes()
        );

        return ResponseEntity.ok(ApiResponse.success(GradeResponse.from(grade)));
    }

    /**
     * Get grades for a student
     */
    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasRole('ADMINISTRATOR') or hasRole('TEACHER') or hasRole('STUDENT') or hasRole('PARENT')")
    public ResponseEntity<ApiResponse<List<GradeResponse>>> getGradesByStudent(@PathVariable UUID studentId) {
        List<Grade> grades = gradeService.getGradesByStudent(studentId);
        List<GradeResponse> responses = grades.stream()
            .map(GradeResponse::from)
            .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Get grades for an assessment
     */
    @GetMapping("/assessment/{assessmentId}")
    @PreAuthorize("hasRole('ADMINISTRATOR') or hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<List<GradeResponse>>> getGradesByAssessment(@PathVariable UUID assessmentId) {
        List<Grade> grades = gradeService.getGradesByAssessment(assessmentId);
        List<GradeResponse> responses = grades.stream()
            .map(GradeResponse::from)
            .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}

class RecordGradeRequest {
    @NotNull
    private UUID studentId;

    @NotNull
    private UUID assessmentId;

    @NotNull
    @PositiveOrZero
    private BigDecimal score;

    private String notes;

    @NotNull
    private UUID recordedBy; // In production, get from JWT token

    // Getters and Setters
    public UUID getStudentId() { return studentId; }
    public void setStudentId(UUID studentId) { this.studentId = studentId; }
    public UUID getAssessmentId() { return assessmentId; }
    public void setAssessmentId(UUID assessmentId) { this.assessmentId = assessmentId; }
    public BigDecimal getScore() { return score; }
    public void setScore(BigDecimal score) { this.score = score; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public UUID getRecordedBy() { return recordedBy; }
    public void setRecordedBy(UUID recordedBy) { this.recordedBy = recordedBy; }
}

class UpdateGradeRequest {
    @NotNull
    private UUID studentId;

    @NotNull
    private UUID assessmentId;

    @NotNull
    @PositiveOrZero
    private BigDecimal newScore;

    private String notes;

    @NotNull
    private UUID updatedBy; // In production, get from JWT token

    // Getters and Setters
    public UUID getStudentId() { return studentId; }
    public void setStudentId(UUID studentId) { this.studentId = studentId; }
    public UUID getAssessmentId() { return assessmentId; }
    public void setAssessmentId(UUID assessmentId) { this.assessmentId = assessmentId; }
    public BigDecimal getNewScore() { return newScore; }
    public void setNewScore(BigDecimal newScore) { this.newScore = newScore; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public UUID getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(UUID updatedBy) { this.updatedBy = updatedBy; }
}

class GradeResponse {
    private final UUID id;
    private final UUID studentId;
    private final UUID assessmentId;
    private final String score;
    private final String totalPoints;
    private final String percentage;
    private final String letterGrade;
    private final UUID recordedBy;
    private final String recordedAt;
    private final String updatedAt;
    private final String updatedBy;
    private final String notes;

    public GradeResponse(UUID id, UUID studentId, UUID assessmentId, String score, String totalPoints,
                        String percentage, String letterGrade, UUID recordedBy, String recordedAt,
                        String updatedAt, String updatedBy, String notes) {
        this.id = id;
        this.studentId = studentId;
        this.assessmentId = assessmentId;
        this.score = score;
        this.totalPoints = totalPoints;
        this.percentage = percentage;
        this.letterGrade = letterGrade;
        this.recordedBy = recordedBy;
        this.recordedAt = recordedAt;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
        this.notes = notes;
    }

    public static GradeResponse from(Grade grade) {
        return new GradeResponse(
            grade.getId(),
            grade.getStudentId(),
            grade.getAssessmentId(),
            grade.getScore().toString(),
            grade.getTotalPoints().toString(),
            grade.getPercentage().toString(),
            grade.getLetterGrade(),
            grade.getRecordedBy(),
            grade.getRecordedAt().toString(),
            grade.getUpdatedAt().toString(),
            grade.getUpdatedBy() != null ? grade.getUpdatedBy().toString() : null,
            grade.getNotes()
        );
    }

    // Getters
    public UUID getId() { return id; }
    public UUID getStudentId() { return studentId; }
    public UUID getAssessmentId() { return assessmentId; }
    public String getScore() { return score; }
    public String getTotalPoints() { return totalPoints; }
    public String getPercentage() { return percentage; }
    public String getLetterGrade() { return letterGrade; }
    public UUID getRecordedBy() { return recordedBy; }
    public String getRecordedAt() { return recordedAt; }
    public String getUpdatedAt() { return updatedAt; }
    public String getUpdatedBy() { return updatedBy; }
    public String getNotes() { return notes; }
}
