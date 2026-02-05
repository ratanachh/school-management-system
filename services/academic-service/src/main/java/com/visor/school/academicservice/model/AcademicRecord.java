package com.visor.school.academicservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Academic record representing a student's complete academic history
 */
@Entity
@Table(name = "academic_records", indexes = {
    @Index(name = "idx_academic_records_student", columnList = "student_id", unique = true),
    @Index(name = "idx_academic_records_standing", columnList = "academic_standing")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AcademicRecord {
    private static final BigDecimal MAX_GPA = new BigDecimal("4.0");

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "student_id", nullable = false, unique = true)
    private UUID studentId;

    @ElementCollection
    @CollectionTable(name = "enrollment_history", joinColumns = @JoinColumn(name = "academic_record_id"))
    private List<EnrollmentEntry> enrollmentHistory = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "course_completions", joinColumns = @JoinColumn(name = "academic_record_id"))
    private List<CourseCompletion> completedCourses = new ArrayList<>();

    @Column(name = "current_gpa", nullable = false, precision = 3, scale = 2)
    private BigDecimal currentGPA = BigDecimal.ZERO;

    @Column(name = "cumulative_gpa", nullable = false, precision = 3, scale = 2)
    private BigDecimal cumulativeGPA = BigDecimal.ZERO;

    @Column(name = "credits_earned", nullable = false)
    private int creditsEarned = 0;

    @Column(name = "credits_required", nullable = false)
    private int creditsRequired = 120;

    @Enumerated(EnumType.STRING)
    @Column(name = "academic_standing", nullable = false)
    private AcademicStanding academicStanding = AcademicStanding.GOOD_STANDING;

    @Column(name = "graduation_date")
    private LocalDate graduationDate;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public AcademicRecord(UUID studentId) {
        this.studentId = studentId;
        validateGPA(BigDecimal.ZERO);
        validateCredits(0, 120);
    }

    public AcademicRecord(UUID studentId, BigDecimal currentGPA, BigDecimal cumulativeGPA, int creditsEarned, AcademicStanding academicStanding) {
        validateGPA(currentGPA);
        validateGPA(cumulativeGPA);
        validateCredits(creditsEarned, 120);
        
        this.studentId = studentId;
        this.currentGPA = currentGPA;
        this.cumulativeGPA = cumulativeGPA;
        this.creditsEarned = creditsEarned;
        this.academicStanding = academicStanding;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public AcademicRecord(UUID studentId, BigDecimal currentGPA, BigDecimal cumulativeGPA, int creditsEarned, int creditsRequired, AcademicStanding academicStanding) {
        validateGPA(currentGPA);
        validateGPA(cumulativeGPA);
        validateCredits(creditsEarned, creditsRequired);
        
        this.studentId = studentId;
        this.currentGPA = currentGPA;
        this.cumulativeGPA = cumulativeGPA;
        this.creditsEarned = creditsEarned;
        this.creditsRequired = creditsRequired;
        this.academicStanding = academicStanding;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    private void validateGPA(BigDecimal gpa) {
        if (gpa.compareTo(BigDecimal.ZERO) < 0 || gpa.compareTo(MAX_GPA) > 0) {
            throw new IllegalArgumentException("GPA must be between 0.0 and 4.0, got: " + gpa);
        }
    }

    private void validateCredits(int earned, int required) {
        if (earned < 0) {
            throw new IllegalArgumentException("Credits earned cannot be negative, got: " + earned);
        }
        if (required <= 0) {
            throw new IllegalArgumentException("Credits required must be positive, got: " + required);
        }
    }

    public void updateGPA(BigDecimal current, BigDecimal cumulative) {
        validateGPA(current);
        validateGPA(cumulative);
        this.currentGPA = current;
        this.cumulativeGPA = cumulative;
        this.updatedAt = Instant.now();
    }

    public void updateAcademicStanding(AcademicStanding standing) {
        this.academicStanding = standing;
        this.updatedAt = Instant.now();
    }

    public void markAsGraduated(LocalDate date) {
        this.academicStanding = AcademicStanding.GRADUATED;
        this.graduationDate = date;
        this.updatedAt = Instant.now();
    }

    public void addCompletedCourse(CourseCompletion course) {
        completedCourses.add(course);
        creditsEarned += course.getCredits();
        updatedAt = Instant.now();
    }

    public void addEnrollmentEntry(EnrollmentEntry entry) {
        enrollmentHistory.add(entry);
        updatedAt = Instant.now();
    }
}
