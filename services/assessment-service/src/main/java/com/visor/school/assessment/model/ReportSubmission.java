package com.visor.school.assessment.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Report submission entity for class teachers (grades 7-12)
 * Represents the submission of aggregated reports to school administration
 */
@Entity
@Table(name = "report_submissions", indexes = {
    @Index(name = "idx_submission_collection", columnList = "collection_id"),
    @Index(name = "idx_submission_class", columnList = "class_id"),
    @Index(name = "idx_submission_teacher", columnList = "submitted_by"),
    @Index(name = "idx_submission_submitted", columnList = "submitted_at")
})
public class ReportSubmission {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "collection_id", nullable = false)
    private UUID collectionId;

    @Column(name = "submitted_by", nullable = false)
    private UUID submittedBy;

    @Column(name = "class_id", nullable = false)
    private UUID classId;

    @Column(name = "report_data", columnDefinition = "JSONB", nullable = false)
    @Convert(converter = JsonMapConverter.class)
    private Map<String, Object> reportData;

    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt;

    @Column(name = "reviewed_by")
    private UUID reviewedBy;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @Column(name = "review_notes", columnDefinition = "TEXT")
    private String reviewNotes;

    // Default constructor for JPA
    protected ReportSubmission() {
        this.id = UUID.randomUUID();
        this.submittedAt = Instant.now();
    }

    // Constructor with required fields
    public ReportSubmission(UUID collectionId, UUID submittedBy, UUID classId, Map<String, Object> reportData) {
        this();
        this.collectionId = collectionId;
        this.submittedBy = submittedBy;
        this.classId = classId;
        this.reportData = reportData;
        validate();
    }

    private void validate() {
        if (reportData == null || reportData.isEmpty()) {
            throw new IllegalArgumentException("Report data cannot be empty");
        }
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public UUID getCollectionId() {
        return collectionId;
    }

    public UUID getSubmittedBy() {
        return submittedBy;
    }

    public UUID getClassId() {
        return classId;
    }

    public Map<String, Object> getReportData() {
        return reportData;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public UUID getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(UUID reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public Instant getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(Instant reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public String getReviewNotes() {
        return reviewNotes;
    }

    public void setReviewNotes(String reviewNotes) {
        this.reviewNotes = reviewNotes;
    }
}
