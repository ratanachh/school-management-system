package com.visor.school.attendance.controller;

import com.visor.school.attendance.service.AttendanceService;
import com.visor.school.attendance.service.AttendanceService.AttendanceReport;
import com.visor.school.common.api.ApiResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Attendance report controller
 * Requires TEACHER role or ADMINISTRATOR role
 */
@RestController
@RequestMapping("/api/v1/reports")
public class AttendanceReportController {

    private final AttendanceService attendanceService;

    public AttendanceReportController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    /**
     * Generate attendance report for a class
     */
    @GetMapping("/class/{classId}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<AttendanceReportResponse>> generateClassReport(
            @PathVariable UUID classId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        AttendanceReport report = attendanceService.generateClassReport(classId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(AttendanceReportResponse.from(report)));
    }

    // Response DTO
    public static class AttendanceReportResponse {
        private final UUID classId;
        private final LocalDate startDate;
        private final LocalDate endDate;
        private final long totalDays;
        private final double attendanceRate;
        private final int total;
        private final int present;
        private final int absent;
        private final int late;
        private final int excused;

        public AttendanceReportResponse(UUID classId, LocalDate startDate, LocalDate endDate,
                                       long totalDays, double attendanceRate, int total,
                                       int present, int absent, int late, int excused) {
            this.classId = classId;
            this.startDate = startDate;
            this.endDate = endDate;
            this.totalDays = totalDays;
            this.attendanceRate = attendanceRate;
            this.total = total;
            this.present = present;
            this.absent = absent;
            this.late = late;
            this.excused = excused;
        }

        public static AttendanceReportResponse from(AttendanceReport report) {
            return new AttendanceReportResponse(
                report.getClassId(),
                report.getStartDate(),
                report.getEndDate(),
                report.getTotalDays(),
                report.getAttendanceRate().getRate(),
                report.getAttendanceRate().getTotal(),
                report.getAttendanceRate().getPresent(),
                report.getAttendanceRate().getAbsent(),
                report.getAttendanceRate().getLate(),
                report.getAttendanceRate().getExcused()
            );
        }

        // Getters
        public UUID getClassId() {
            return classId;
        }

        public LocalDate getStartDate() {
            return startDate;
        }

        public LocalDate getEndDate() {
            return endDate;
        }

        public long getTotalDays() {
            return totalDays;
        }

        public double getAttendanceRate() {
            return attendanceRate;
        }

        public int getTotal() {
            return total;
        }

        public int getPresent() {
            return present;
        }

        public int getAbsent() {
            return absent;
        }

        public int getLate() {
            return late;
        }

        public int getExcused() {
            return excused;
        }
    }
}
