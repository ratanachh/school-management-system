package com.visor.school.assessment.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Exam result collection entity for class teachers (grades 7-12)
 * Represents the collection of exam results from subject teachers for a class
 */
@Entity
@Table(name = "exam_result_collections", indexes = {
    @Index(name = "idx_collection_class", columnList = "class_id"),
    @Index(name = "idx_collection_teacher", columnList = "collected_by"),
    @Index(name = "idx_collection_year_term", columnList = "academic_year,term"),
    @Index(name = "idx_collection_status", columnList = "status")
})
public class ExamResultCollection {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "class_id", nullable = false)
    private UUID classId;

    @Column(name = "collected_by", nullable = false)
    private UUID collectedBy;

    @Column(name = "academic_year", nullable = false)
    private String academicYear;

    @Column(name = "term", nullable = false)
    private String term;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ExamResultCollectionStatus status;

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @Column(name = "metadata", columnDefinition = "JSONB")
    @Convert(converter = JsonMapConverter.class)
    private Map<String, Object> metadata;

    @Column(name = "collected_at", nullable = false)
    private Instant collectedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    // Default constructor for JPA
    protected ExamResultCollection() {
        this.id = UUID.randomUUID();
        this.status = ExamResultCollectionStatus.COLLECTING;
        this.collectedAt = Instant.now();
    }

    // Constructor with required fields
    public ExamResultCollection(UUID classId, UUID collectedBy, String academicYear, String term) {
        this();
        this.classId = classId;
        this.collectedBy = collectedBy;
        this.academicYear = academicYear;
        this.term = term;
        validate();
    }

    // Full constructor
    public ExamResultCollection(UUID classId, UUID collectedBy, String academicYear, String term, 
                               Map<String, Object> metadata) {
        this(classId, collectedBy, academicYear, term);
        this.metadata = metadata;
    }

    private void validate() {
        if (academicYear == null || academicYear.isBlank()) {
            throw new IllegalArgumentException("Academic year cannot be blank");
        }
        if (term == null || term.isBlank()) {
            throw new IllegalArgumentException("Term cannot be blank");
        }
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public UUID getClassId() {
        return classId;
    }

    public UUID getCollectedBy() {
        return collectedBy;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public String getTerm() {
        return term;
    }

    public ExamResultCollectionStatus getStatus() {
        return status;
    }

    public void setStatus(ExamResultCollectionStatus status) {
        this.status = status;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public Instant getCollectedAt() {
        return collectedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(Instant submittedAt) {
        this.submittedAt = submittedAt;
    }
}
