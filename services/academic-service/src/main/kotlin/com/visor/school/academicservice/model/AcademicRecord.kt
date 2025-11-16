package com.visor.school.academicservice.model

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

/**
 * Academic record representing a student's complete academic history
 */
@Entity
@Table(name = "academic_records", indexes = [
    Index(name = "idx_academic_records_student", columnList = "student_id", unique = true),
    Index(name = "idx_academic_records_standing", columnList = "academic_standing")
])
class AcademicRecord(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),

    @Column(name = "student_id", nullable = false, unique = true)
    val studentId: UUID,

    @ElementCollection
    @CollectionTable(name = "enrollment_history", joinColumns = [JoinColumn(name = "academic_record_id")])
    @Embedded
    val enrollmentHistory: MutableList<EnrollmentEntry> = mutableListOf(),

    @ElementCollection
    @CollectionTable(name = "course_completions", joinColumns = [JoinColumn(name = "academic_record_id")])
    @Embedded
    val completedCourses: MutableList<CourseCompletion> = mutableListOf(),

    @Column(name = "current_gpa", nullable = false, precision = 3, scale = 2)
    var currentGPA: BigDecimal = BigDecimal.ZERO,

    @Column(name = "cumulative_gpa", nullable = false, precision = 3, scale = 2)
    var cumulativeGPA: BigDecimal = BigDecimal.ZERO,

    @Column(name = "credits_earned", nullable = false)
    var creditsEarned: Int = 0,

    @Column(name = "credits_required", nullable = false)
    val creditsRequired: Int = 120,

    @Enumerated(EnumType.STRING)
    @Column(name = "academic_standing", nullable = false)
    var academicStanding: AcademicStanding = AcademicStanding.GOOD_STANDING,

    @Column(name = "graduation_date")
    var graduationDate: LocalDate? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
) {
    init {
        require(currentGPA >= BigDecimal.ZERO && currentGPA <= BigDecimal("4.0")) {
            "Current GPA must be between 0.0 and 4.0, got: $currentGPA"
        }
        require(cumulativeGPA >= BigDecimal.ZERO && cumulativeGPA <= BigDecimal("4.0")) {
            "Cumulative GPA must be between 0.0 and 4.0, got: $cumulativeGPA"
        }
        require(creditsEarned >= 0) {
            "Credits earned cannot be negative, got: $creditsEarned"
        }
        require(creditsRequired > 0) {
            "Credits required must be positive, got: $creditsRequired"
        }
    }

    fun updateGPA(current: BigDecimal, cumulative: BigDecimal) {
        require(current >= BigDecimal.ZERO && current <= BigDecimal("4.0")) {
            "Current GPA must be between 0.0 and 4.0, got: $current"
        }
        require(cumulative >= BigDecimal.ZERO && cumulative <= BigDecimal("4.0")) {
            "Cumulative GPA must be between 0.0 and 4.0, got: $cumulative"
        }
        currentGPA = current
        cumulativeGPA = cumulative
        updatedAt = Instant.now()
    }

    fun updateAcademicStanding(standing: AcademicStanding) {
        academicStanding = standing
        updatedAt = Instant.now()
    }

    fun markAsGraduated(date: LocalDate) {
        academicStanding = AcademicStanding.GRADUATED
        graduationDate = date
        updatedAt = Instant.now()
    }

    fun addCompletedCourse(course: CourseCompletion) {
        completedCourses.add(course)
        creditsEarned += course.credits
        updatedAt = Instant.now()
    }

    fun addEnrollmentEntry(entry: EnrollmentEntry) {
        enrollmentHistory.add(entry)
        updatedAt = Instant.now()
    }
}

