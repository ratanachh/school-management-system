package com.visor.school.attendanceservice.model

import jakarta.persistence.*
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

/**
 * Attendance session for delegation to class leaders
 * Each session represents attendance collection for a specific date and class
 */
@Entity
@Table(name = "attendance_sessions", indexes = [
    Index(name = "idx_sessions_class_date", columnList = "class_id,date", unique = true),
    Index(name = "idx_sessions_delegated_to", columnList = "delegated_to"),
    Index(name = "idx_sessions_status", columnList = "status"),
    Index(name = "idx_sessions_created_by", columnList = "created_by")
])
class AttendanceSession(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),

    @Column(name = "class_id", nullable = false)
    val classId: UUID,

    @Column(name = "date", nullable = false)
    val date: LocalDate,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: AttendanceSessionStatus = AttendanceSessionStatus.PENDING,

    @Column(name = "delegated_to")
    var delegatedTo: UUID? = null, // Student ID (class leader) - nullable initially, set during delegation

    @Column(name = "created_by", nullable = false)
    val createdBy: UUID, // Teacher ID

    @Column(name = "approved_by")
    var approvedBy: UUID? = null, // Teacher ID

    @Column(name = "rejected_by")
    var rejectedBy: UUID? = null, // Teacher ID

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    var rejectionReason: String? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "collected_at")
    var collectedAt: Instant? = null,

    @Column(name = "approved_at")
    var approvedAt: Instant? = null,

    @Column(name = "rejected_at")
    var rejectedAt: Instant? = null,

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
) {
    init {
        require(date.isBefore(java.time.LocalDate.now().plusDays(1))) {
            "Attendance date cannot be in the future"
        }
    }

    fun markAsCollected() {
        require(status == AttendanceSessionStatus.PENDING || status == AttendanceSessionStatus.REJECTED) {
            "Session must be in PENDING or REJECTED status to be marked as collected"
        }
        require(delegatedTo != null) {
            "Session must be delegated to a class leader before collection"
        }
        status = AttendanceSessionStatus.COLLECTED
        collectedAt = Instant.now()
        updatedAt = Instant.now()
    }

    fun approve(teacherId: UUID) {
        require(status == AttendanceSessionStatus.COLLECTED) {
            "Session must be in COLLECTED status to be approved"
        }
        status = AttendanceSessionStatus.APPROVED
        approvedBy = teacherId
        approvedAt = Instant.now()
        updatedAt = Instant.now()
        rejectionReason = null
        rejectedBy = null
        rejectedAt = null
    }

    fun reject(teacherId: UUID, reason: String) {
        require(status == AttendanceSessionStatus.COLLECTED) {
            "Session must be in COLLECTED status to be rejected"
        }
        require(reason.isNotBlank()) {
            "Rejection reason is required"
        }
        status = AttendanceSessionStatus.REJECTED
        rejectedBy = teacherId
        rejectionReason = reason
        rejectedAt = Instant.now()
        updatedAt = Instant.now()
        // Reset collected timestamp to allow re-collection
        collectedAt = null
    }

    fun resubmit() {
        require(status == AttendanceSessionStatus.REJECTED) {
            "Session must be in REJECTED status to be resubmitted"
        }
        status = AttendanceSessionStatus.COLLECTED
        collectedAt = Instant.now()
        updatedAt = Instant.now()
        rejectionReason = null
        rejectedBy = null
        rejectedAt = null
        // Keep delegatedTo unchanged
    }

    fun delegateTo(classLeaderId: UUID) {
        require(status == AttendanceSessionStatus.PENDING) {
            "Session must be in PENDING status to be delegated"
        }
        require(delegatedTo == null) {
            "Session is already delegated to $delegatedTo"
        }
        delegatedTo = classLeaderId
        updatedAt = Instant.now()
    }
}

