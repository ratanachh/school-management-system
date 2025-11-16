package com.visor.school.attendanceservice.controller

import com.visor.school.attendanceservice.service.AttendanceService
import com.visor.school.attendanceservice.service.AttendanceReport
import com.visor.school.common.api.ApiResponse
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.util.UUID

/**
 * Attendance report controller
 * Requires TEACHER role or ADMINISTRATOR role
 */
@RestController
@RequestMapping("/api/v1/reports")
class AttendanceReportController(
    private val attendanceService: AttendanceService
) {

    /**
     * Generate attendance report for a class
     */
    @GetMapping("/class/{classId}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMINISTRATOR')")
    fun generateClassReport(
        @PathVariable classId: UUID,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate
    ): ResponseEntity<ApiResponse<AttendanceReportResponse>> {
        val report = attendanceService.generateClassReport(classId, startDate, endDate)
        return ResponseEntity.ok(ApiResponse.success(AttendanceReportResponse.from(report)))
    }
}

data class AttendanceReportResponse(
    val classId: UUID,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val totalDays: Long,
    val attendanceRate: Double,
    val total: Int,
    val present: Int,
    val absent: Int,
    val late: Int,
    val excused: Int
) {
    companion object {
        fun from(report: AttendanceReport): AttendanceReportResponse {
            return AttendanceReportResponse(
                classId = report.classId,
                startDate = report.startDate,
                endDate = report.endDate,
                totalDays = report.totalDays,
                attendanceRate = report.attendanceRate.rate,
                total = report.attendanceRate.total,
                present = report.attendanceRate.present,
                absent = report.attendanceRate.absent,
                late = report.attendanceRate.late,
                excused = report.attendanceRate.excused
            )
        }
    }
}

