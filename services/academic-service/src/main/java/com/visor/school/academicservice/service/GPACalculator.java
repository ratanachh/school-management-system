package com.visor.school.academicservice.service;

import com.visor.school.academicservice.model.CourseCompletion;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * GPA calculation service
 * Converts letter grades to grade points and calculates GPA
 */
@Service
public class GPACalculator {

    /**
     * Calculate GPA from a list of course completions
     */
    public BigDecimal calculateGPA(List<CourseCompletion> courses) {
        if (courses.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalPoints = BigDecimal.ZERO;
        int totalCredits = 0;

        for (CourseCompletion course : courses) {
            BigDecimal gradePoints = letterGradeToPoints(course.getFinalGrade());
            totalPoints = totalPoints.add(gradePoints.multiply(BigDecimal.valueOf(course.getCredits())));
            totalCredits += course.getCredits();
        }

        if (totalCredits > 0) {
            return totalPoints.divide(BigDecimal.valueOf(totalCredits), 2, RoundingMode.HALF_UP);
        } else {
            return BigDecimal.ZERO;
        }
    }

    /**
     * Convert letter grade to grade points (4.0 scale)
     */
    private BigDecimal letterGradeToPoints(String grade) {
        return switch (grade.toUpperCase()) {
            case "A+", "A" -> new BigDecimal("4.0");
            case "A-" -> new BigDecimal("3.7");
            case "B+" -> new BigDecimal("3.3");
            case "B" -> new BigDecimal("3.0");
            case "B-" -> new BigDecimal("2.7");
            case "C+" -> new BigDecimal("2.3");
            case "C" -> new BigDecimal("2.0");
            case "C-" -> new BigDecimal("1.7");
            case "D+" -> new BigDecimal("1.3");
            case "D" -> new BigDecimal("1.0");
            case "D-" -> new BigDecimal("0.7");
            case "F" -> new BigDecimal("0.0");
            default -> BigDecimal.ZERO;
        };
    }

    /**
     * Calculate cumulative GPA from all completed courses
     */
    public BigDecimal calculateCumulativeGPA(List<CourseCompletion> allCourses) {
        return calculateGPA(allCourses);
    }

    /**
     * Calculate current term GPA from courses in a specific term
     */
    public BigDecimal calculateCurrentTermGPA(List<CourseCompletion> courses) {
        return calculateGPA(courses);
    }
}
