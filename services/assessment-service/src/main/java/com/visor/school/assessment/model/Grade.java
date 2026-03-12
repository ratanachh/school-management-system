package com.visor.school.assessment.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;

/**
 * Grade entity representing a student's score on an assessment
 */
@Entity
@Table(name = "grades", indexes = {
    @Index(name = "idx_grades_student", columnList = "student_id"),
    @Index(name = "idx_grades_assessment", columnList = "assessment_id"),
    @Index(name = "idx_grades_student_assessment", columnList = "student_id,assessment_id", unique = true),
    @Index(name = "idx_grades_recorded_by", columnList = "recorded_by")
})
public class Grade {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "assessment_id", nullable = false)
    private UUID assessmentId;

    @Column(name = "score", nullable = false, precision = 10, scale = 2)
    private BigDecimal score;

    @Column(name = "total_points", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPoints;

    @Column(name = "percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal percentage;

    @Column(name = "letter_grade", length = 10)
    private String letterGrade;

    @Column(name = "recorded_by", nullable = false)
    private UUID recordedBy;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // Default constructor for JPA
    protected Grade() {
        this.id = UUID.randomUUID();
        this.recordedAt = Instant.now();
        this.updatedAt = Instant.now();
        this.version = 0L;
    }

    // Constructor with required fields
    public Grade(UUID studentId, UUID assessmentId, BigDecimal score, BigDecimal totalPoints, UUID recordedBy) {
        this();
        this.studentId = studentId;
        this.assessmentId = assessmentId;
        this.score = score;
        this.totalPoints = totalPoints;
        this.recordedBy = recordedBy;
        this.percentage = calculatePercentage(score, totalPoints);
        validate();
    }

    private void validate() {
        if (score.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Score cannot be negative, got: " + score);
        }
        if (score.compareTo(totalPoints) > 0) {
            throw new IllegalArgumentException("Score cannot exceed total points (" + totalPoints + "), got: " + score);
        }
        if (totalPoints.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Total points must be positive, got: " + totalPoints);
        }
    }

    public static BigDecimal calculatePercentage(BigDecimal score, BigDecimal totalPoints) {
        if (totalPoints.compareTo(BigDecimal.ZERO) > 0) {
            return score.divide(totalPoints, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100.0"))
                    .setScale(2, RoundingMode.HALF_UP);
        } else {
            return BigDecimal.ZERO;
        }
    }

    public void updateScore(BigDecimal newScore, UUID updatedBy) {
        if (newScore.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Score cannot be negative, got: " + newScore);
        }
        if (newScore.compareTo(totalPoints) > 0) {
            throw new IllegalArgumentException("Score cannot exceed total points (" + totalPoints + "), got: " + newScore);
        }
        this.score = newScore;
        this.percentage = calculatePercentage(newScore, totalPoints);
        this.updatedBy = updatedBy;
        this.updatedAt = Instant.now();
    }

    public void assignLetterGrade(String grade) {
        this.letterGrade = grade;
        this.updatedAt = Instant.now();
    }

    public void addNotes(String notes) {
        this.notes = notes;
        this.updatedAt = Instant.now();
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public UUID getStudentId() {
        return studentId;
    }

    public UUID getAssessmentId() {
        return assessmentId;
    }

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }

    public BigDecimal getTotalPoints() {
        return totalPoints;
    }

    public BigDecimal getPercentage() {
        return percentage;
    }

    public void setPercentage(BigDecimal percentage) {
        this.percentage = percentage;
    }

    public String getLetterGrade() {
        return letterGrade;
    }

    public void setLetterGrade(String letterGrade) {
        this.letterGrade = letterGrade;
    }

    public UUID getRecordedBy() {
        return recordedBy;
    }

    public Instant getRecordedAt() {
        return recordedAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = Instant.now();
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public UUID getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(UUID updatedBy) {
        this.updatedBy = updatedBy;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
