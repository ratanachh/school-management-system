package com.visor.school.attendanceservice.controller

import com.visor.school.attendanceservice.model.AttendanceRecord
import com.visor.school.attendanceservice.model.AttendanceStatus
import com.visor.school.attendanceservice.service.AttendanceService
import com.visor.school.common.api.ApiResponse
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.util.UUID

/**
 * Attendance controller for direct marking
 * Requires TEACHER role with MANAGE_ATTENDANCE permission
 */
@RestController
@RequestMapping("/api/v1/attendance")
class AttendanceController(
    private val attendanceService: AttendanceService
) {

    /**
     * Mark attendance directly (teacher marks without delegation)
     */
    @PostMapping
    @PreAuthorize("hasRole('TEACHER') and hasPermission(null, 'MANAGE_ATTENDANCE')")
    fun markAttendance(@Valid @RequestBody request: MarkAttendanceRequest): ResponseEntity<ApiResponse<AttendanceRecordResponse>> {
        val record = attendanceService.markAttendanceDirectly(
            studentId = request.studentId,
            classId = request.classId,
            date = request.date,
            status = request.status,
            markedBy = request.markedBy, // From JWT token in production
            notes = request.notes
        )

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(AttendanceRecordResponse.from(record), "Attendance marked successfully"))
    }

    /**
     * Get attendance records for a class
     */
    @GetMapping("/class/{classId}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMINISTRATOR')")
    fun getAttendanceByClass(
        @PathVariable classId: UUID,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate?
    ): ResponseEntity<ApiResponse<List<AttendanceRecordResponse>>> {
        val records = attendanceService.getAttendanceByClass(classId, date)
        return ResponseEntity.ok(ApiResponse.success(records.map { AttendanceRecordResponse.from(it) }))
    }

    /**
     * Get attendance records for a student
     */
    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMINISTRATOR') or hasRole('PARENT')")
    fun getAttendanceByStudent(
        @PathVariable studentId: UUID,
        @RequestParam(required = false) classId: UUID?
    ): ResponseEntity<ApiResponse<List<AttendanceRecordResponse>>> {
        val records = attendanceService.getAttendanceByStudent(studentId, classId)
        return ResponseEntity.ok(ApiResponse.success(records.map { AttendanceRecordResponse.from(it) }))
    }
}

data class MarkAttendanceRequest(
    @field:NotNull
    val studentId: UUID,

    @field:NotNull
    val classId: UUID,

    @field:NotNull
    val date: LocalDate,

    @field:NotNull
    val status: AttendanceStatus,

    @field:NotNull
    val markedBy: UUID, // Teacher ID (from JWT token in production)

    val notes: String? = null
)

data class AttendanceRecordResponse(
    val id: UUID,
    val studentId: UUID,
    val classId: UUID,
    val date: LocalDate,
    val status: String,
    val markedBy: UUID?,
    val collectedBy: UUID?,
    val sessionId: UUID?,
    val approvedBy: UUID?,
    val notes: String?
) {
    companion object {
        fun from(record: AttendanceRecord): AttendanceRecordResponse {
            return AttendanceRecordResponse(
                id = record.id,
                studentId = record.studentId,
                classId = record.classId,
                date = record.date,
                status = record.status.name,
                markedBy = record.markedBy,
                collectedBy = record.collectedBy,
                sessionId = record.sessionId,
                approvedBy = record.approvedBy,
                notes = record.notes
            )
        }
    }
}

