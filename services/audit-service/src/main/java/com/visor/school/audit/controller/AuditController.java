package com.visor.school.audit.controller;

import com.visor.school.audit.model.AuditAction;
import com.visor.school.audit.model.AuditRecord;
import com.visor.school.audit.service.AuditService;
import com.visor.school.common.api.ApiResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Audit controller
 * Requires ADMINISTRATOR role for viewing audit logs
 */
@RestController
@RequestMapping("/api/v1/audit")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    /**
     * Query audit records
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<List<AuditRecordResponse>>> queryAuditLogs(
        @RequestParam(required = false) UUID userId,
        @RequestParam(required = false) String action,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate startDate,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate endDate
    ) {
        AuditAction auditAction = null;
        if (action != null) {
            try {
                auditAction = AuditAction.valueOf(action);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid action: " + action + ". Must be one of: " + Arrays.toString(AuditAction.values()));
            }
        }

        List<AuditRecord> records = auditService.query(userId, auditAction, startDate, endDate);
        List<AuditRecordResponse> response = records.stream()
            .map(AuditRecordResponse::from)
            .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    public record AuditRecordResponse(
        String id,
        String userId,
        String action,
        String resourceType,
        String resourceId,
        String ipAddress,
        String userAgent,
        Map<String, Object> details,
        boolean success,
        String errorMessage,
        String timestamp
    ) {
        public static AuditRecordResponse from(AuditRecord record) {
            return new AuditRecordResponse(
                record.getId().toString(),
                record.getUserId().toString(),
                record.getAction().name(),
                record.getResourceType(),
                record.getResourceId(),
                record.getIpAddress(),
                record.getUserAgent(),
                record.getDetails(),
                record.isSuccess(),
                record.getErrorMessage(),
                record.getTimestamp().toString()
            );
        }
    }
}
