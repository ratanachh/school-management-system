package com.visor.school.assessment.controller;

import com.visor.school.assessment.model.Assessment;
import com.visor.school.assessment.model.AssessmentType;
import com.visor.school.assessment.service.AssessmentService;
import com.visor.school.common.api.ApiResponse;
import com.visor.school.common.api.Permissions;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Assessment controller
 * Requires TEACHER role with MANAGE_GRADES permission or ADMINISTRATOR role
 */
@RestController
@RequestMapping("/api/v1/assessments")
public class AssessmentController {
    
    private final AssessmentService assessmentService;

    public AssessmentController(AssessmentService assessmentService) {
        this.assessmentService = assessmentService;
    }

    /**
     * Create a new assessment
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRATOR') or (hasRole('TEACHER') and hasAuthority('" + Permissions.MANAGE_GRADES + "'))")
    public ResponseEntity<ApiResponse<AssessmentResponse>> createAssessment(@Valid @RequestBody CreateAssessmentRequest request) {
        Assessment assessment = assessmentService.createAssessment(
            request.getClassId(),
            request.getName(),
            AssessmentType.valueOf(request.getType()),
            request.getTotalPoints(),
            request.getCreatedBy(), // In production, get from JWT token
            request.getDescription(),
            request.getWeight(),
            request.getDueDate() != null ? LocalDate.parse(request.getDueDate()) : null
        );

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(AssessmentResponse.from(assessment)));
    }

    /**
     * Get assessment by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR') or hasRole('TEACHER') or hasRole('STUDENT') or hasRole('PARENT')")
    public ResponseEntity<ApiResponse<AssessmentResponse>> getAssessment(@PathVariable UUID id) {
        Assessment assessment = assessmentService.getAssessment(id);
        if (assessment == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(ApiResponse.success(AssessmentResponse.from(assessment)));
    }

    /**
     * Get assessments by class ID
     */
    @GetMapping("/class/{classId}")
    @PreAuthorize("hasRole('ADMINISTRATOR') or hasRole('TEACHER') or hasRole('STUDENT') or hasRole('PARENT')")
    public ResponseEntity<ApiResponse<List<AssessmentResponse>>> getAssessmentsByClass(@PathVariable UUID classId) {
        List<Assessment> assessments = assessmentService.getAssessmentsByClass(classId);
        List<AssessmentResponse> responses = assessments.stream()
            .map(AssessmentResponse::from)
            .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Publish an assessment
     */
    @PostMapping("/{id}/publish")
    @PreAuthorize("hasRole('ADMINISTRATOR') or (hasRole('TEACHER') and hasAuthority('" + Permissions.MANAGE_GRADES + "'))")
    public ResponseEntity<ApiResponse<AssessmentResponse>> publishAssessment(@PathVariable UUID id) {
        Assessment assessment = assessmentService.publishAssessment(id);
        return ResponseEntity.ok(ApiResponse.success(AssessmentResponse.from(assessment)));
    }
}

class CreateAssessmentRequest {
    @NotNull
    private UUID classId;

    @NotBlank
    private String name;

    @NotNull
    private String type;

    @NotNull
    @Positive
    private BigDecimal totalPoints;

    private String description;
    private BigDecimal weight;
    private String dueDate;

    @NotNull
    private UUID createdBy; // In production, get from JWT token

    // Getters and Setters
    public UUID getClassId() { return classId; }
    public void setClassId(UUID classId) { this.classId = classId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public BigDecimal getTotalPoints() { return totalPoints; }
    public void setTotalPoints(BigDecimal totalPoints) { this.totalPoints = totalPoints; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getWeight() { return weight; }
    public void setWeight(BigDecimal weight) { this.weight = weight; }
    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }
    public UUID getCreatedBy() { return createdBy; }
    public void setCreatedBy(UUID createdBy) { this.createdBy = createdBy; }
}

class AssessmentResponse {
    private final UUID id;
    private final UUID classId;
    private final String name;
    private final String type;
    private final String description;
    private final String totalPoints;
    private final String weight;
    private final String dueDate;
    private final UUID createdBy;
    private final String status;
    private final String createdAt;
    private final String updatedAt;

    public AssessmentResponse(UUID id, UUID classId, String name, String type, String description,
                             String totalPoints, String weight, String dueDate, UUID createdBy,
                             String status, String createdAt, String updatedAt) {
        this.id = id;
        this.classId = classId;
        this.name = name;
        this.type = type;
        this.description = description;
        this.totalPoints = totalPoints;
        this.weight = weight;
        this.dueDate = dueDate;
        this.createdBy = createdBy;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static AssessmentResponse from(Assessment assessment) {
        return new AssessmentResponse(
            assessment.getId(),
            assessment.getClassId(),
            assessment.getName(),
            assessment.getType().name(),
            assessment.getDescription(),
            assessment.getTotalPoints().toString(),
            assessment.getWeight() != null ? assessment.getWeight().toString() : null,
            assessment.getDueDate() != null ? assessment.getDueDate().toString() : null,
            assessment.getCreatedBy(),
            assessment.getStatus().name(),
            assessment.getCreatedAt().toString(),
            assessment.getUpdatedAt().toString()
        );
    }

    // Getters
    public UUID getId() { return id; }
    public UUID getClassId() { return classId; }
    public String getName() { return name; }
    public String getType() { return type; }
    public String getDescription() { return description; }
    public String getTotalPoints() { return totalPoints; }
    public String getWeight() { return weight; }
    public String getDueDate() { return dueDate; }
    public UUID getCreatedBy() { return createdBy; }
    public String getStatus() { return status; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }
}
