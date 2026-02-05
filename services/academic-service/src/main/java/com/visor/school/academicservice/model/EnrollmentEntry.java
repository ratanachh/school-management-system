package com.visor.school.academicservice.model;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Enrollment entry value object for academic record history
 */
@Embeddable
@Data
@NoArgsConstructor
public class EnrollmentEntry {
    private String academicYear;
    
    @Enumerated(EnumType.STRING)
    private Term term;
    
    private int gradeLevel;
    private LocalDate enrollmentDate;
    
    @Enumerated(EnumType.STRING)
    private EnrollmentStatus status;

    public EnrollmentEntry(String academicYear, Term term, int gradeLevel, LocalDate enrollmentDate, EnrollmentStatus status) {
        if (gradeLevel < 1 || gradeLevel > 12) {
            throw new IllegalArgumentException("Grade level must be between 1 and 12 (K12 system), got: " + gradeLevel);
        }
        if (academicYear == null || academicYear.isBlank()) {
            throw new IllegalArgumentException("Academic year cannot be blank");
        }
        this.academicYear = academicYear;
        this.term = term;
        this.gradeLevel = gradeLevel;
        this.enrollmentDate = enrollmentDate;
        this.status = status;
    }
}
