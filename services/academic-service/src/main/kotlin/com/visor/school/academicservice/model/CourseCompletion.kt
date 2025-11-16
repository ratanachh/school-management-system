package com.visor.school.academicservice.model

import jakarta.persistence.Embeddable
import java.time.LocalDate

/**
 * Course completion value object for academic records
 */
@Embeddable
data class CourseCompletion(
    val courseName: String,
    val subject: String,
    val gradeLevel: Int,
    val finalGrade: String,
    val credits: Int,
    val completionDate: LocalDate
) {
    init {
        require(gradeLevel in 1..12) {
            "Grade level must be between 1 and 12 (K12 system), got: $gradeLevel"
        }
        require(courseName.isNotBlank()) {
            "Course name cannot be blank"
        }
        require(subject.isNotBlank()) {
            "Subject cannot be blank"
        }
        require(finalGrade.isNotBlank()) {
            "Final grade cannot be blank"
        }
        require(credits > 0) {
            "Credits must be positive, got: $credits"
        }
    }
}

