package com.visor.school.audit.controller

import com.visor.school.audit.model.AuditAction
import com.visor.school.audit.service.AuditService
import com.visor.school.common.api.ApiResponse
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.util.UUID

/**
 * Audit controller
 * Requires ADMINISTRATOR role for viewing audit logs
 */
@RestController
@RequestMapping("/api/v1/audit")
class AuditController(
    private val auditService: AuditService
) {

    /**
     * Query audit records
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    fun queryAuditLogs(
        @RequestParam(required = false) userId: UUID?,
        @RequestParam(required = false) action: String?,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        startDate: LocalDate?,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        endDate: LocalDate?
    ): ResponseEntity<ApiResponse<List<AuditRecordResponse>>> {
        val auditAction = action?.let {
            try {
                AuditAction.valueOf(it)
            } catch (e: IllegalArgumentException) {
                throw IllegalArgumentException("Invalid action: $action. Must be one of: ${AuditAction.values().joinToString()}")
            }
        }

        val records = auditService.query(userId, auditAction, startDate, endDate)
        val response = records.map { AuditRecordResponse.from(it) }

        return ResponseEntity.ok(ApiResponse.success(response))
    }
}

data class AuditRecordResponse(
    val id: String,
    val userId: String,
    val action: String,
    val resourceType: String,
    val resourceId: String?,
    val ipAddress: String?,
    val userAgent: String?,
    val details: Map<String, Any>?,
    val success: Boolean,
    val errorMessage: String?,
    val timestamp: String
) {
    companion object {
        fun from(record: com.visor.school.audit.model.AuditRecord): AuditRecordResponse {
            return AuditRecordResponse(
                id = record.id.toString(),
                userId = record.userId.toString(),
                action = record.action.name,
                resourceType = record.resourceType,
                resourceId = record.resourceId,
                ipAddress = record.ipAddress,
                userAgent = record.userAgent,
                details = record.details,
                success = record.success,
                errorMessage = record.errorMessage,
                timestamp = record.timestamp.toString()
            )
        }
    }
}

