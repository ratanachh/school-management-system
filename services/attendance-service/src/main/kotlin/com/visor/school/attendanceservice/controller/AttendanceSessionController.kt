package com.visor.school.attendanceservice.controller

import com.visor.school.attendanceservice.model.AttendanceSession
import com.visor.school.attendanceservice.model.AttendanceStatus
import com.visor.school.attendanceservice.service.AttendanceService
import com.visor.school.attendanceservice.service.AttendanceEntry
import com.visor.school.common.api.ApiResponse
import com.visor.school.common.api.Permissions
import jakarta.validation.Valid
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.util.UUID

/**
 * Attendance session controller for delegation workflow
 * Requires TEACHER role with MANAGE_ATTENDANCE permission for session management
 * Requires COLLECT_ATTENDANCE permission for class leader collection
 */
@RestController
@RequestMapping("/api/v1/attendance/sessions")
class AttendanceSessionController(
    private val attendanceService: AttendanceService
) {

    /**
     * Create attendance session
     */
    @PostMapping
    @PreAuthorize("hasRole('TEACHER') and hasAuthority('${Permissions.COLLECT_ATTENDANCE}')")
    fun createSession(@Valid @RequestBody request: CreateSessionRequest): ResponseEntity<ApiResponse<AttendanceSessionResponse>> {
        val session = attendanceService.createSession(
            classId = request.classId,
            date = request.date,
            createdBy = request.createdBy // From JWT token in production
        )

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(AttendanceSessionResponse.from(session), "Attendance session created successfully"))
    }

    /**
     * Delegate session to class leader
     */
    @PostMapping("/{sessionId}/delegate")
    @PreAuthorize("hasRole('TEACHER') and hasAuthority('${Permissions.COLLECT_ATTENDANCE}')")
    fun delegateToClassLeader(
        @PathVariable sessionId: UUID,
        @Valid @RequestBody request: DelegateSessionRequest
    ): ResponseEntity<ApiResponse<AttendanceSessionResponse>> {
        val session = attendanceService.delegateToClassLeader(
            sessionId = sessionId,
            classLeaderId = request.classLeaderId,
            classId = request.classId
        )

        return ResponseEntity.ok(ApiResponse.success(AttendanceSessionResponse.from(session), "Session delegated successfully"))
    }

    /**
     * Collect attendance by class leader
     */
    @PostMapping("/{sessionId}/collect")
    @PreAuthorize("hasAuthority('${Permissions.COLLECT_ATTENDANCE}')")
    fun collectAttendance(
        @PathVariable sessionId: UUID,
        @Valid @RequestBody request: CollectAttendanceRequest
    ): ResponseEntity<ApiResponse<AttendanceSessionResponse>> {
        val entries = request.attendanceEntries.map {
            AttendanceEntry(
                studentId = it.studentId,
                status = it.status,
                notes = it.notes
            )
        }

        val session = attendanceService.collectAttendanceByClassLeader(
            sessionId = sessionId,
            classLeaderId = request.classLeaderId, // From JWT token in production
            attendanceEntries = entries
        )

        return ResponseEntity.ok(ApiResponse.success(AttendanceSessionResponse.from(session), "Attendance collected successfully"))
    }

    /**
     * Approve attendance session
     */
    @PostMapping("/{sessionId}/approve")
    @PreAuthorize("hasRole('TEACHER') and hasAuthority('${Permissions.APPROVE_ATTENDANCE}')")
    fun approveSession(
        @PathVariable sessionId: UUID,
        @Valid @RequestBody request: ApproveSessionRequest
    ): ResponseEntity<ApiResponse<AttendanceSessionResponse>> {
        val session = attendanceService.approveSession(
            sessionId = sessionId,
            teacherId = request.teacherId // From JWT token in production
        )

        return ResponseEntity.ok(ApiResponse.success(AttendanceSessionResponse.from(session), "Session approved successfully"))
    }

    /**
     * Reject attendance session
     */
    @PostMapping("/{sessionId}/reject")
    @PreAuthorize("hasRole('TEACHER') and hasAuthority('${Permissions.APPROVE_ATTENDANCE}')")
    fun rejectSession(
        @PathVariable sessionId: UUID,
        @Valid @RequestBody request: RejectSessionRequest
    ): ResponseEntity<ApiResponse<AttendanceSessionResponse>> {
        val session = attendanceService.rejectSession(
            sessionId = sessionId,
            teacherId = request.teacherId, // From JWT token in production
            reason = request.reason
        )

        return ResponseEntity.ok(ApiResponse.success(AttendanceSessionResponse.from(session), "Session rejected"))
    }

    /**
     * Get session by ID
     */
    @GetMapping("/{sessionId}")
    @PreAuthorize("hasRole('TEACHER') or hasPermission(null, 'COLLECT_ATTENDANCE')")
    fun getSession(@PathVariable sessionId: UUID): ResponseEntity<ApiResponse<AttendanceSessionResponse>> {
        val session = attendanceService.getSessionById(sessionId)
            ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(ApiResponse.success(AttendanceSessionResponse.from(session)))
    }

    /**
     * Get sessions for a class
     */
    @GetMapping("/class/{classId}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMINISTRATOR')")
    fun getSessionsByClass(@PathVariable classId: UUID): ResponseEntity<ApiResponse<List<AttendanceSessionResponse>>> {
        val sessions = attendanceService.getSessionsByClass(classId)
        return ResponseEntity.ok(ApiResponse.success(sessions.map { AttendanceSessionResponse.from(it) }))
    }
}

data class CreateSessionRequest(
    @field:NotNull
    val classId: UUID,

    @field:NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    val date: LocalDate,

    @field:NotNull
    val createdBy: UUID // Teacher ID (from JWT token in production)
)

data class DelegateSessionRequest(
    @field:NotNull
    val classId: UUID,

    @field:NotNull
    val classLeaderId: UUID
)

data class CollectAttendanceRequest(
    @field:NotNull
    val classLeaderId: UUID, // From JWT token in production

    @field:NotEmpty
    val attendanceEntries: List<AttendanceEntryRequest>
)

data class AttendanceEntryRequest(
    @field:NotNull
    val studentId: UUID,

    @field:NotNull
    val status: AttendanceStatus,

    val notes: String? = null
)

data class ApproveSessionRequest(
    @field:NotNull
    val teacherId: UUID // From JWT token in production
)

data class RejectSessionRequest(
    @field:NotNull
    val teacherId: UUID, // From JWT token in production

    @field:NotNull
    val reason: String
)

data class AttendanceSessionResponse(
    val id: UUID,
    val classId: UUID,
    val date: LocalDate,
    val status: String,
    val delegatedTo: UUID?,
    val createdBy: UUID,
    val approvedBy: UUID?,
    val rejectedBy: UUID?,
    val rejectionReason: String?,
    val createdAt: java.time.Instant,
    val collectedAt: java.time.Instant?,
    val approvedAt: java.time.Instant?,
    val rejectedAt: java.time.Instant?
) {
    companion object {
        fun from(session: AttendanceSession): AttendanceSessionResponse {
            return AttendanceSessionResponse(
                id = session.id,
                classId = session.classId,
                date = session.date,
                status = session.status.name,
                delegatedTo = session.delegatedTo,
                createdBy = session.createdBy,
                approvedBy = session.approvedBy,
                rejectedBy = session.rejectedBy,
                rejectionReason = session.rejectionReason,
                createdAt = session.createdAt,
                collectedAt = session.collectedAt,
                approvedAt = session.approvedAt,
                rejectedAt = session.rejectedAt
            )
        }
    }
}

