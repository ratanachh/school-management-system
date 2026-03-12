package com.visor.school.assessment.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Assessment entity representing an evaluation activity
 */
@Entity
@Table(name = "assessments", indexes = {
    @Index(name = "idx_assessments_class", columnList = "class_id"),
    @Index(name = "idx_assessments_created_by", columnList = "created_by"),
    @Index(name = "idx_assessments_status", columnList = "status")
})
public class Assessment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "class_id", nullable = false)
    private UUID classId;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private AssessmentType type;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "total_points", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPoints;

    @Column(name = "weight", precision = 5, scale = 2)
    private BigDecimal weight;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AssessmentStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    // Default constructor for JPA
    protected Assessment() {
        this.id = UUID.randomUUID();
        this.status = AssessmentStatus.DRAFT;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        this.version = 0L;
    }

    // Constructor with required fields
    public Assessment(UUID classId, String name, AssessmentType type, BigDecimal totalPoints, UUID createdBy) {
        this();
        this.classId = classId;
        this.name = name;
        this.type = type;
        this.totalPoints = totalPoints;
        this.createdBy = createdBy;
        validate();
    }

    // Full constructor
    public Assessment(UUID classId, String name, AssessmentType type, BigDecimal totalPoints, 
                     UUID createdBy, String description, BigDecimal weight, LocalDate dueDate) {
        this(classId, name, type, totalPoints, createdBy);
        this.description = description;
        this.weight = weight;
        this.dueDate = dueDate;
        validate();
    }

    private void validate() {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Assessment name cannot be blank");
        }
        if (totalPoints == null || totalPoints.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Total points must be positive, got: " + totalPoints);
        }
        if (weight != null) {
            if (weight.compareTo(BigDecimal.ZERO) < 0 || weight.compareTo(new BigDecimal("100.0")) > 0) {
                throw new IllegalArgumentException("Weight must be between 0 and 100, got: " + weight);
            }
        }
        if (dueDate != null) {
            LocalDate yesterday = LocalDate.now().minusDays(1);
            if (dueDate.isBefore(LocalDate.now()) && !dueDate.isEqual(LocalDate.now())) {
                throw new IllegalArgumentException("Due date cannot be in the past");
            }
        }
    }

    public void publish() {
        if (status != AssessmentStatus.DRAFT) {
            throw new IllegalStateException("Only draft assessments can be published, current status: " + status);
        }
        status = AssessmentStatus.PUBLISHED;
        updatedAt = Instant.now();
    }

    public void markAsGrading() {
        if (status != AssessmentStatus.PUBLISHED) {
            throw new IllegalStateException("Only published assessments can be marked as grading, current status: " + status);
        }
        status = AssessmentStatus.GRADING;
        updatedAt = Instant.now();
    }

    public void complete() {
        if (status != AssessmentStatus.GRADING) {
            throw new IllegalStateException("Only assessments in grading status can be completed, current status: " + status);
        }
        status = AssessmentStatus.COMPLETED;
        updatedAt = Instant.now();
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public UUID getClassId() {
        return classId;
    }

    public String getName() {
        return name;
    }

    public AssessmentType getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getTotalPoints() {
        return totalPoints;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public AssessmentStatus getStatus() {
        return status;
    }

    public void setStatus(AssessmentStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
