package com.visor.school.academicservice.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Course completion value object for academic records
 */
@Embeddable
@Data
@NoArgsConstructor
public class CourseCompletion {
    private String courseName;
    private String subject;
    private int gradeLevel;
    private String finalGrade;
    private int credits;
    private LocalDate completionDate;

    public CourseCompletion(String courseName, String subject, int gradeLevel, String finalGrade, int credits, LocalDate completionDate) {
        if (gradeLevel < 1 || gradeLevel > 12) {
            throw new IllegalArgumentException("Grade level must be between 1 and 12 (K12 system), got: " + gradeLevel);
        }
        if (courseName == null || courseName.isBlank()) {
            throw new IllegalArgumentException("Course name cannot be blank");
        }
        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException("Subject cannot be blank");
        }
        if (finalGrade == null || finalGrade.isBlank()) {
            throw new IllegalArgumentException("Final grade cannot be blank");
        }
        if (credits <= 0) {
            throw new IllegalArgumentException("Credits must be positive, got: " + credits);
        }
        this.courseName = courseName;
        this.subject = subject;
        this.gradeLevel = gradeLevel;
        this.finalGrade = finalGrade;
        this.credits = credits;
        this.completionDate = completionDate;
    }
}
