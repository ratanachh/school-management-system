package com.visor.school.attendance.controller;

import com.visor.school.attendance.model.AttendanceRecord;
import com.visor.school.attendance.model.AttendanceStatus;
import com.visor.school.attendance.service.AttendanceService;
import com.visor.school.common.api.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Attendance controller for direct marking
 * Requires TEACHER role with MANAGE_ATTENDANCE permission
 */
@RestController
@RequestMapping("/api/v1/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    /**
     * Mark attendance directly (teacher marks without delegation)
     */
    @PostMapping
    @PreAuthorize("hasRole('TEACHER') and hasPermission(null, 'MANAGE_ATTENDANCE')")
    public ResponseEntity<ApiResponse<AttendanceRecordResponse>> markAttendance(
            @Valid @RequestBody MarkAttendanceRequest request) {
        AttendanceRecord record = attendanceService.markAttendanceDirectly(
            request.getStudentId(),
            request.getClassId(),
            request.getDate(),
            request.getStatus(),
            request.getMarkedBy(), // From JWT token in production
            request.getNotes()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(AttendanceRecordResponse.from(record), "Attendance marked successfully"));
    }

    /**
     * Get attendance records for a class
     */
    @GetMapping("/class/{classId}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<List<AttendanceRecordResponse>>> getAttendanceByClass(
            @PathVariable UUID classId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<AttendanceRecord> records = attendanceService.getAttendanceByClass(classId, date);
        List<AttendanceRecordResponse> responses = records.stream()
            .map(AttendanceRecordResponse::from)
            .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Get attendance records for a student
     */
    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMINISTRATOR') or hasRole('PARENT')")
    public ResponseEntity<ApiResponse<List<AttendanceRecordResponse>>> getAttendanceByStudent(
            @PathVariable UUID studentId,
            @RequestParam(required = false) UUID classId) {
        List<AttendanceRecord> records = attendanceService.getAttendanceByStudent(studentId, classId);
        List<AttendanceRecordResponse> responses = records.stream()
            .map(AttendanceRecordResponse::from)
            .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    // Request/Response DTOs
    public static class MarkAttendanceRequest {
        @NotNull
        private UUID studentId;

        @NotNull
        private UUID classId;

        @NotNull
        private LocalDate date;

        @NotNull
        private AttendanceStatus status;

        @NotNull
        private UUID markedBy; // Teacher ID (from JWT token in production)

        private String notes;

        // Getters and Setters
        public UUID getStudentId() {
            return studentId;
        }

        public void setStudentId(UUID studentId) {
            this.studentId = studentId;
        }

        public UUID getClassId() {
            return classId;
        }

        public void setClassId(UUID classId) {
            this.classId = classId;
        }

        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }

        public AttendanceStatus getStatus() {
            return status;
        }

        public void setStatus(AttendanceStatus status) {
            this.status = status;
        }

        public UUID getMarkedBy() {
            return markedBy;
        }

        public void setMarkedBy(UUID markedBy) {
            this.markedBy = markedBy;
        }

        public String getNotes() {
            return notes;
        }

        public void setNotes(String notes) {
            this.notes = notes;
        }
    }

    public static class AttendanceRecordResponse {
        private final UUID id;
        private final UUID studentId;
        private final UUID classId;
        private final LocalDate date;
        private final String status;
        private final UUID markedBy;
        private final UUID collectedBy;
        private final UUID sessionId;
        private final UUID approvedBy;
        private final String notes;

        public AttendanceRecordResponse(UUID id, UUID studentId, UUID classId, LocalDate date,
                                       String status, UUID markedBy, UUID collectedBy,
                                       UUID sessionId, UUID approvedBy, String notes) {
            this.id = id;
            this.studentId = studentId;
            this.classId = classId;
            this.date = date;
            this.status = status;
            this.markedBy = markedBy;
            this.collectedBy = collectedBy;
            this.sessionId = sessionId;
            this.approvedBy = approvedBy;
            this.notes = notes;
        }

        public static AttendanceRecordResponse from(AttendanceRecord record) {
            return new AttendanceRecordResponse(
                record.getId(),
                record.getStudentId(),
                record.getClassId(),
                record.getDate(),
                record.getStatus().name(),
                record.getMarkedBy(),
                record.getCollectedBy(),
                record.getSessionId(),
                record.getApprovedBy(),
                record.getNotes()
            );
        }

        // Getters
        public UUID getId() {
            return id;
        }

        public UUID getStudentId() {
            return studentId;
        }

        public UUID getClassId() {
            return classId;
        }

        public LocalDate getDate() {
            return date;
        }

        public String getStatus() {
            return status;
        }

        public UUID getMarkedBy() {
            return markedBy;
        }

        public UUID getCollectedBy() {
            return collectedBy;
        }

        public UUID getSessionId() {
            return sessionId;
        }

        public UUID getApprovedBy() {
            return approvedBy;
        }

        public String getNotes() {
            return notes;
        }
    }
}
