package com.visor.school.academicservice.service

import com.visor.school.academicservice.model.CourseCompletion
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * GPA calculation service
 * Converts letter grades to grade points and calculates GPA
 */
@Service
class GPACalculator {

    /**
     * Calculate GPA from a list of course completions
     */
    fun calculateGPA(courses: List<CourseCompletion>): BigDecimal {
        if (courses.isEmpty()) {
            return BigDecimal.ZERO
        }

        var totalPoints = BigDecimal.ZERO
        var totalCredits = 0

        courses.forEach { course ->
            val gradePoints = letterGradeToPoints(course.finalGrade)
            totalPoints += gradePoints.multiply(BigDecimal(course.credits))
            totalCredits += course.credits
        }

        return if (totalCredits > 0) {
            totalPoints.divide(BigDecimal(totalCredits), 2, RoundingMode.HALF_UP)
        } else {
            BigDecimal.ZERO
        }
    }

    /**
     * Convert letter grade to grade points (4.0 scale)
     */
    private fun letterGradeToPoints(grade: String): BigDecimal {
        return when (grade.uppercase()) {
            "A+", "A" -> BigDecimal("4.0")
            "A-" -> BigDecimal("3.7")
            "B+" -> BigDecimal("3.3")
            "B" -> BigDecimal("3.0")
            "B-" -> BigDecimal("2.7")
            "C+" -> BigDecimal("2.3")
            "C" -> BigDecimal("2.0")
            "C-" -> BigDecimal("1.7")
            "D+" -> BigDecimal("1.3")
            "D" -> BigDecimal("1.0")
            "D-" -> BigDecimal("0.7")
            "F" -> BigDecimal("0.0")
            else -> BigDecimal.ZERO
        }
    }

    /**
     * Calculate cumulative GPA from all completed courses
     */
    fun calculateCumulativeGPA(allCourses: List<CourseCompletion>): BigDecimal {
        return calculateGPA(allCourses)
    }

    /**
     * Calculate current term GPA from courses in a specific term
     */
    fun calculateCurrentTermGPA(courses: List<CourseCompletion>): BigDecimal {
        return calculateGPA(courses)
    }
}

