package com.visor.school.attendance.controller;

import com.visor.school.attendance.model.AttendanceSession;
import com.visor.school.attendance.model.AttendanceStatus;
import com.visor.school.attendance.service.AttendanceService;
import com.visor.school.attendance.service.AttendanceService.AttendanceEntry;
import com.visor.school.common.api.ApiResponse;
import com.visor.school.common.api.Permissions;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Attendance session controller for delegation workflow
 * Requires TEACHER role with MANAGE_ATTENDANCE permission for session management
 * Requires COLLECT_ATTENDANCE permission for class leader collection
 */
@RestController
@RequestMapping("/api/v1/attendance/sessions")
public class AttendanceSessionController {

    private final AttendanceService attendanceService;

    public AttendanceSessionController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    /**
     * Create attendance session
     */
    @PostMapping
    @PreAuthorize("hasRole('TEACHER') and hasAuthority('" + Permissions.COLLECT_ATTENDANCE + "')")
    public ResponseEntity<ApiResponse<AttendanceSessionResponse>> createSession(
            @Valid @RequestBody CreateSessionRequest request) {
        AttendanceSession session = attendanceService.createSession(
            request.getClassId(),
            request.getDate(),
            request.getCreatedBy() // From JWT token in production
        );

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(AttendanceSessionResponse.from(session), "Attendance session created successfully"));
    }

    /**
     * Delegate session to class leader
     */
    @PostMapping("/{sessionId}/delegate")
    @PreAuthorize("hasRole('TEACHER') and hasAuthority('" + Permissions.COLLECT_ATTENDANCE + "')")
    public ResponseEntity<ApiResponse<AttendanceSessionResponse>> delegateToClassLeader(
            @PathVariable UUID sessionId,
            @Valid @RequestBody DelegateSessionRequest request) {
        AttendanceSession session = attendanceService.delegateToClassLeader(
            sessionId,
            request.getClassLeaderId(),
            request.getClassId()
        );

        return ResponseEntity.ok(ApiResponse.success(AttendanceSessionResponse.from(session), "Session delegated successfully"));
    }

    /**
     * Collect attendance by class leader
     */
    @PostMapping("/{sessionId}/collect")
    @PreAuthorize("hasAuthority('" + Permissions.COLLECT_ATTENDANCE + "')")
    public ResponseEntity<ApiResponse<AttendanceSessionResponse>> collectAttendance(
            @PathVariable UUID sessionId,
            @Valid @RequestBody CollectAttendanceRequest request) {
        List<AttendanceEntry> entries = request.getAttendanceEntries().stream()
            .map(e -> new AttendanceEntry(e.getStudentId(), e.getStatus(), e.getNotes()))
            .collect(Collectors.toList());

        AttendanceSession session = attendanceService.collectAttendanceByClassLeader(
            sessionId,
            request.getClassLeaderId(), // From JWT token in production
            entries
        );

        return ResponseEntity.ok(ApiResponse.success(AttendanceSessionResponse.from(session), "Attendance collected successfully"));
    }

    /**
     * Approve attendance session
     */
    @PostMapping("/{sessionId}/approve")
    @PreAuthorize("hasRole('TEACHER') and hasAuthority('" + Permissions.APPROVE_ATTENDANCE + "')")
    public ResponseEntity<ApiResponse<AttendanceSessionResponse>> approveSession(
            @PathVariable UUID sessionId,
            @Valid @RequestBody ApproveSessionRequest request) {
        AttendanceSession session = attendanceService.approveSession(
            sessionId,
            request.getTeacherId() // From JWT token in production
        );

        return ResponseEntity.ok(ApiResponse.success(AttendanceSessionResponse.from(session), "Session approved successfully"));
    }

    /**
     * Reject attendance session
     */
    @PostMapping("/{sessionId}/reject")
    @PreAuthorize("hasRole('TEACHER') and hasAuthority('" + Permissions.APPROVE_ATTENDANCE + "')")
    public ResponseEntity<ApiResponse<AttendanceSessionResponse>> rejectSession(
            @PathVariable UUID sessionId,
            @Valid @RequestBody RejectSessionRequest request) {
        AttendanceSession session = attendanceService.rejectSession(
            sessionId,
            request.getTeacherId(), // From JWT token in production
            request.getReason()
        );

        return ResponseEntity.ok(ApiResponse.success(AttendanceSessionResponse.from(session), "Session rejected"));
    }

    /**
     * Get session by ID
     */
    @GetMapping("/{sessionId}")
    @PreAuthorize("hasRole('TEACHER') or hasPermission(null, 'COLLECT_ATTENDANCE')")
    public ResponseEntity<ApiResponse<AttendanceSessionResponse>> getSession(@PathVariable UUID sessionId) {
        AttendanceSession session = attendanceService.getSessionById(sessionId);
        if (session == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(ApiResponse.success(AttendanceSessionResponse.from(session)));
    }

    /**
     * Get sessions for a class
     */
    @GetMapping("/class/{classId}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<List<AttendanceSessionResponse>>> getSessionsByClass(@PathVariable UUID classId) {
        List<AttendanceSession> sessions = attendanceService.getSessionsByClass(classId);
        List<AttendanceSessionResponse> responses = sessions.stream()
            .map(AttendanceSessionResponse::from)
            .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    // Request/Response DTOs
    public static class CreateSessionRequest {
        @NotNull
        private UUID classId;

        @NotNull
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate date;

        @NotNull
        private UUID createdBy; // Teacher ID (from JWT token in production)

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

        public UUID getCreatedBy() {
            return createdBy;
        }

        public void setCreatedBy(UUID createdBy) {
            this.createdBy = createdBy;
        }
    }

    public static class DelegateSessionRequest {
        @NotNull
        private UUID classId;

        @NotNull
        private UUID classLeaderId;

        public UUID getClassId() {
            return classId;
        }

        public void setClassId(UUID classId) {
            this.classId = classId;
        }

        public UUID getClassLeaderId() {
            return classLeaderId;
        }

        public void setClassLeaderId(UUID classLeaderId) {
            this.classLeaderId = classLeaderId;
        }
    }

    public static class CollectAttendanceRequest {
        @NotNull
        private UUID classLeaderId; // From JWT token in production

        @NotEmpty
        private List<AttendanceEntryRequest> attendanceEntries;

        public UUID getClassLeaderId() {
            return classLeaderId;
        }

        public void setClassLeaderId(UUID classLeaderId) {
            this.classLeaderId = classLeaderId;
        }

        public List<AttendanceEntryRequest> getAttendanceEntries() {
            return attendanceEntries;
        }

        public void setAttendanceEntries(List<AttendanceEntryRequest> attendanceEntries) {
            this.attendanceEntries = attendanceEntries;
        }
    }

    public static class AttendanceEntryRequest {
        @NotNull
        private UUID studentId;

        @NotNull
        private AttendanceStatus status;

        private String notes;

        public UUID getStudentId() {
            return studentId;
        }

        public void setStudentId(UUID studentId) {
            this.studentId = studentId;
        }

        public AttendanceStatus getStatus() {
            return status;
        }

        public void setStatus(AttendanceStatus status) {
            this.status = status;
        }

        public String getNotes() {
            return notes;
        }

        public void setNotes(String notes) {
            this.notes = notes;
        }
    }

    public static class ApproveSessionRequest {
        @NotNull
        private UUID teacherId; // From JWT token in production

        public UUID getTeacherId() {
            return teacherId;
        }

        public void setTeacherId(UUID teacherId) {
            this.teacherId = teacherId;
        }
    }

    public static class RejectSessionRequest {
        @NotNull
        private UUID teacherId; // From JWT token in production

        @NotNull
        private String reason;

        public UUID getTeacherId() {
            return teacherId;
        }

        public void setTeacherId(UUID teacherId) {
            this.teacherId = teacherId;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }

    public static class AttendanceSessionResponse {
        private final UUID id;
        private final UUID classId;
        private final LocalDate date;
        private final String status;
        private final UUID delegatedTo;
        private final UUID createdBy;
        private final UUID approvedBy;
        private final UUID rejectedBy;
        private final String rejectionReason;
        private final Instant createdAt;
        private final Instant collectedAt;
        private final Instant approvedAt;
        private final Instant rejectedAt;

        public AttendanceSessionResponse(UUID id, UUID classId, LocalDate date, String status,
                                        UUID delegatedTo, UUID createdBy, UUID approvedBy,
                                        UUID rejectedBy, String rejectionReason, Instant createdAt,
                                        Instant collectedAt, Instant approvedAt, Instant rejectedAt) {
            this.id = id;
            this.classId = classId;
            this.date = date;
            this.status = status;
            this.delegatedTo = delegatedTo;
            this.createdBy = createdBy;
            this.approvedBy = approvedBy;
            this.rejectedBy = rejectedBy;
            this.rejectionReason = rejectionReason;
            this.createdAt = createdAt;
            this.collectedAt = collectedAt;
            this.approvedAt = approvedAt;
            this.rejectedAt = rejectedAt;
        }

        public static AttendanceSessionResponse from(AttendanceSession session) {
            return new AttendanceSessionResponse(
                session.getId(),
                session.getClassId(),
                session.getDate(),
                session.getStatus().name(),
                session.getDelegatedTo(),
                session.getCreatedBy(),
                session.getApprovedBy(),
                session.getRejectedBy(),
                session.getRejectionReason(),
                session.getCreatedAt(),
                session.getCollectedAt(),
                session.getApprovedAt(),
                session.getRejectedAt()
            );
        }

        // Getters
        public UUID getId() {
            return id;
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

        public UUID getDelegatedTo() {
            return delegatedTo;
        }

        public UUID getCreatedBy() {
            return createdBy;
        }

        public UUID getApprovedBy() {
            return approvedBy;
        }

        public UUID getRejectedBy() {
            return rejectedBy;
        }

        public String getRejectionReason() {
            return rejectionReason;
        }

        public Instant getCreatedAt() {
            return createdAt;
        }

        public Instant getCollectedAt() {
            return collectedAt;
        }

        public Instant getApprovedAt() {
            return approvedAt;
        }

        public Instant getRejectedAt() {
            return rejectedAt;
        }
    }
}
