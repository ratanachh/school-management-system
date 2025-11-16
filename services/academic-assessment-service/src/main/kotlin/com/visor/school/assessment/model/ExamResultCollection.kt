package com.visor.school.assessment.model

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

/**
 * Exam result collection entity for class teachers (grades 7-12)
 * Represents the collection of exam results from subject teachers for a class
 */
@Entity
@Table(name = "exam_result_collections", indexes = [
    Index(name = "idx_collection_class", columnList = "class_id"),
    Index(name = "idx_collection_teacher", columnList = "collected_by"),
    Index(name = "idx_collection_year_term", columnList = "academic_year,term"),
    Index(name = "idx_collection_status", columnList = "status")
])
class ExamResultCollection(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),

    @Column(name = "class_id", nullable = false)
    val classId: UUID,

    @Column(name = "collected_by", nullable = false)
    val collectedBy: UUID,

    @Column(name = "academic_year", nullable = false)
    val academicYear: String,

    @Column(name = "term", nullable = false)
    val term: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: ExamResultCollectionStatus = ExamResultCollectionStatus.COLLECTING,

    @Column(name = "summary", columnDefinition = "TEXT")
    var summary: String? = null,

    @Column(name = "metadata", columnDefinition = "JSONB")
    @Convert(converter = JsonMapConverter::class)
    val metadata: Map<String, Any>? = null,

    @Column(name = "collected_at", nullable = false)
    val collectedAt: Instant = Instant.now(),

    @Column(name = "completed_at")
    var completedAt: Instant? = null,

    @Column(name = "submitted_at")
    var submittedAt: Instant? = null
) {
    init {
        require(academicYear.isNotBlank()) {
            "Academic year cannot be blank"
        }
        require(term.isNotBlank()) {
            "Term cannot be blank"
        }
    }
}

