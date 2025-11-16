package com.visor.school.assessment.model

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

/**
 * Report submission entity for class teachers (grades 7-12)
 * Represents the submission of aggregated reports to school administration
 */
@Entity
@Table(name = "report_submissions", indexes = [
    Index(name = "idx_submission_collection", columnList = "collection_id"),
    Index(name = "idx_submission_class", columnList = "class_id"),
    Index(name = "idx_submission_teacher", columnList = "submitted_by"),
    Index(name = "idx_submission_submitted", columnList = "submitted_at")
])
class ReportSubmission(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),

    @Column(name = "collection_id", nullable = false)
    val collectionId: UUID,

    @Column(name = "submitted_by", nullable = false)
    val submittedBy: UUID,

    @Column(name = "class_id", nullable = false)
    val classId: UUID,

    @Column(name = "report_data", columnDefinition = "JSONB", nullable = false)
    @Convert(converter = JsonMapConverter::class)
    val reportData: Map<String, Any>,

    @Column(name = "submitted_at", nullable = false)
    val submittedAt: Instant = Instant.now(),

    @Column(name = "reviewed_by")
    var reviewedBy: UUID? = null,

    @Column(name = "reviewed_at")
    var reviewedAt: Instant? = null,

    @Column(name = "review_notes", columnDefinition = "TEXT")
    var reviewNotes: String? = null
) {
    init {
        require(reportData.isNotEmpty()) {
            "Report data cannot be empty"
        }
    }
}

