package com.visor.school.assessment.model

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

/**
 * Assessment entity representing an evaluation activity
 */
@Entity
@Table(name = "assessments", indexes = [
    Index(name = "idx_assessments_class", columnList = "class_id"),
    Index(name = "idx_assessments_created_by", columnList = "created_by"),
    Index(name = "idx_assessments_status", columnList = "status")
])
class Assessment(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),

    @Column(name = "class_id", nullable = false)
    val classId: UUID,

    @Column(name = "name", nullable = false)
    val name: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    val type: AssessmentType,

    @Column(name = "description", columnDefinition = "TEXT")
    val description: String? = null,

    @Column(name = "total_points", nullable = false, precision = 10, scale = 2)
    val totalPoints: BigDecimal,

    @Column(name = "weight", precision = 5, scale = 2)
    val weight: BigDecimal? = null,

    @Column(name = "due_date")
    val dueDate: LocalDate? = null,

    @Column(name = "created_by", nullable = false)
    val createdBy: UUID,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: AssessmentStatus = AssessmentStatus.DRAFT,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now(),

    @Version
    @Column(name = "version", nullable = false)
    var version: Long = 0L
) {
    init {
        require(name.isNotBlank()) {
            "Assessment name cannot be blank"
        }
        require(totalPoints > BigDecimal.ZERO) {
            "Total points must be positive, got: $totalPoints"
        }
        weight?.let {
            require(it >= BigDecimal.ZERO && it <= BigDecimal("100.0")) {
                "Weight must be between 0 and 100, got: $it"
            }
        }
        dueDate?.let {
            require(it.isAfter(LocalDate.now().minusDays(1)) || it.isEqual(LocalDate.now())) {
                "Due date cannot be in the past"
            }
        }
    }

    fun publish() {
        require(status == AssessmentStatus.DRAFT) {
            "Only draft assessments can be published, current status: $status"
        }
        status = AssessmentStatus.PUBLISHED
        updatedAt = Instant.now()
    }

    fun markAsGrading() {
        require(status == AssessmentStatus.PUBLISHED) {
            "Only published assessments can be marked as grading, current status: $status"
        }
        status = AssessmentStatus.GRADING
        updatedAt = Instant.now()
    }

    fun complete() {
        require(status == AssessmentStatus.GRADING) {
            "Only assessments in grading status can be completed, current status: $status"
        }
        status = AssessmentStatus.COMPLETED
        updatedAt = Instant.now()
    }
}

