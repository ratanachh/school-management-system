package com.visor.school.assessment.model

import jakarta.persistence.*
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.util.UUID

/**
 * Grade entity representing a student's score on an assessment
 */
@Entity
@Table(name = "grades", indexes = [
    Index(name = "idx_grades_student", columnList = "student_id"),
    Index(name = "idx_grades_assessment", columnList = "assessment_id"),
    Index(name = "idx_grades_student_assessment", columnList = "student_id,assessment_id", unique = true),
    Index(name = "idx_grades_recorded_by", columnList = "recorded_by")
])
class Grade(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),

    @Column(name = "student_id", nullable = false)
    val studentId: UUID,

    @Column(name = "assessment_id", nullable = false)
    val assessmentId: UUID,

    @Column(name = "score", nullable = false, precision = 10, scale = 2)
    var score: BigDecimal,

    @Column(name = "total_points", nullable = false, precision = 10, scale = 2)
    val totalPoints: BigDecimal,

    @Column(name = "percentage", nullable = false, precision = 5, scale = 2)
    var percentage: BigDecimal = calculatePercentage(score, totalPoints),

    @Column(name = "letter_grade", length = 10)
    var letterGrade: String? = null,

    @Column(name = "recorded_by", nullable = false)
    val recordedBy: UUID,

    @Column(name = "recorded_at", nullable = false)
    val recordedAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now(),

    @Version
    @Column(name = "version", nullable = false)
    var version: Long = 0L,

    @Column(name = "updated_by")
    var updatedBy: UUID? = null,

    @Column(name = "notes", columnDefinition = "TEXT")
    var notes: String? = null
) {
    init {
        require(score >= BigDecimal.ZERO) {
            "Score cannot be negative, got: $score"
        }
        require(score <= totalPoints) {
            "Score cannot exceed total points ($totalPoints), got: $score"
        }
        require(totalPoints > BigDecimal.ZERO) {
            "Total points must be positive, got: $totalPoints"
        }
    }

    companion object {
        fun calculatePercentage(score: BigDecimal, totalPoints: BigDecimal): BigDecimal {
            return if (totalPoints > BigDecimal.ZERO) {
                score.divide(totalPoints, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal("100.0"))
                    .setScale(2, RoundingMode.HALF_UP)
            } else {
                BigDecimal.ZERO
            }
        }
    }

    fun updateScore(newScore: BigDecimal, updatedBy: UUID) {
        require(newScore >= BigDecimal.ZERO) {
            "Score cannot be negative, got: $newScore"
        }
        require(newScore <= totalPoints) {
            "Score cannot exceed total points ($totalPoints), got: $newScore"
        }
        score = newScore
        percentage = calculatePercentage(newScore, totalPoints)
        this.updatedBy = updatedBy
        updatedAt = Instant.now()
    }

    fun assignLetterGrade(grade: String) {
        letterGrade = grade
        updatedAt = Instant.now()
    }

    fun addNotes(notes: String) {
        this.notes = notes
        updatedAt = Instant.now()
    }
}

