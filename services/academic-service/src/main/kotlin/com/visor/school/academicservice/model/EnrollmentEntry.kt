package com.visor.school.academicservice.model

import jakarta.persistence.Embeddable
import java.time.LocalDate

/**
 * Enrollment entry value object for academic record history
 */
@Embeddable
data class EnrollmentEntry(
    val academicYear: String,
    val term: Term,
    val gradeLevel: Int,
    val enrollmentDate: LocalDate,
    val status: EnrollmentStatus
) {
    init {
        require(gradeLevel in 1..12) {
            "Grade level must be between 1 and 12 (K12 system), got: $gradeLevel"
        }
        require(academicYear.isNotBlank()) {
            "Academic year cannot be blank"
        }
    }
}

