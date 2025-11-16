package com.visor.school.attendanceservice.model

import jakarta.persistence.*
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

/**
 * Attendance record for a student
 * Supports both direct teacher marking and session-based class leader collection
 */
@Entity
@Table(name = "attendance_records", indexes = [
    Index(name = "idx_attendance_student_class_date", columnList = "student_id,class_id,date", unique = true),
    Index(name = "idx_attendance_class", columnList = "class_id"),
    Index(name = "idx_attendance_date", columnList = "date"),
    Index(name = "idx_attendance_session", columnList = "session_id"),
    Index(name = "idx_attendance_collected_by", columnList = "collected_by")
])
class AttendanceRecord(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),

    @Column(name = "student_id", nullable = false)
    val studentId: UUID,

    @Column(name = "class_id", nullable = false)
    val classId: UUID,

    @Column(name = "date", nullable = false)
    val date: LocalDate,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: AttendanceStatus,

    @Column(name = "marked_by")
    val markedBy: UUID? = null, // Teacher ID (direct marking)

    @Column(name = "collected_by")
    val collectedBy: UUID? = null, // Student ID (class leader, session-based)

    @Column(name = "session_id")
    val sessionId: UUID? = null, // AttendanceSession ID (session-based)

    @Column(name = "approved_by")
    val approvedBy: UUID? = null, // Teacher ID (who approved the session)

    @Column(name = "notes", columnDefinition = "TEXT")
    var notes: String? = null,

    @Column(name = "marked_at", nullable = false)
    val markedAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now(),

    @Version
    @Column(name = "version", nullable = false)
    var version: Long = 0L,

    @Column(name = "updated_by")
    var updatedBy: UUID? = null
) {
    init {
        require(date.isBefore(java.time.LocalDate.now().plusDays(1))) {
            "Attendance date cannot be in the future"
        }

        // Validation: Either direct marking OR session-based collection
        require(
            (markedBy != null && collectedBy == null && sessionId == null) ||
            (markedBy == null && collectedBy != null && sessionId != null)
        ) {
            "Attendance record must be either direct marking (markedBy) or session-based (collectedBy + sessionId)"
        }

        // If session-based, collectedBy must be set
        if (sessionId != null) {
            require(collectedBy != null) {
                "If sessionId is provided, collectedBy must be set"
            }
        }

        // If approvedBy is set, sessionId must be provided
        if (approvedBy != null) {
            require(sessionId != null) {
                "If approvedBy is set, sessionId must be provided"
            }
        }
    }

    fun updateStatus(newStatus: AttendanceStatus, updatedBy: UUID?, notes: String? = null) {
        status = newStatus
        this.updatedBy = updatedBy
        this.notes = notes
        updatedAt = Instant.now()
    }

    fun isDirectMarking(): Boolean = markedBy != null && sessionId == null
    fun isSessionBased(): Boolean = sessionId != null && collectedBy != null
}

