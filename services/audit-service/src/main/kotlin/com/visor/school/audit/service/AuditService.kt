package com.visor.school.audit.service

import com.visor.school.audit.model.AuditAction
import com.visor.school.audit.model.AuditRecord
import com.visor.school.audit.repository.AuditRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.UUID

/**
 * Audit service for logging security-relevant events
 */
@Service
@Transactional
class AuditService(
    private val auditRepository: AuditRepository
) {
    private val logger = LoggerFactory.getLogger(AuditService::class.java)

    /**
     * Log an audit record
     */
    fun log(
        userId: UUID,
        action: AuditAction,
        resourceType: String,
        resourceId: String? = null,
        ipAddress: String? = null,
        userAgent: String? = null,
        details: Map<String, Any>? = null,
        success: Boolean = true,
        errorMessage: String? = null
    ): AuditRecord {
        logger.debug("Logging audit record: userId=$userId, action=$action, resourceType=$resourceType")

        val record = AuditRecord(
            userId = userId,
            action = action,
            resourceType = resourceType,
            resourceId = resourceId,
            ipAddress = ipAddress,
            userAgent = userAgent,
            details = details,
            success = success,
            errorMessage = errorMessage
        )

        return auditRepository.save(record)
    }

    /**
     * Query audit records with filters
     */
    @Transactional(readOnly = true)
    fun query(
        userId: UUID? = null,
        action: AuditAction? = null,
        startDate: LocalDate? = null,
        endDate: LocalDate? = null
    ): List<AuditRecord> {
        val start = startDate?.atStartOfDay()?.toInstant(ZoneOffset.UTC) ?: Instant.now().minusSeconds(30 * 24 * 60 * 60) // Default: 30 days ago
        val end = endDate?.atTime(23, 59, 59)?.toInstant(ZoneOffset.UTC) ?: Instant.now()

        return when {
            userId != null && action != null -> {
                auditRepository.findByUserIdAndActionAndTimestampBetween(userId, action, start, end)
            }
            userId != null -> {
                auditRepository.findByUserIdAndTimestampBetween(userId, start, end)
            }
            action != null -> {
                auditRepository.findByActionAndTimestampBetween(action, start, end)
            }
            else -> {
                // If no filters, return empty list (should specify at least one filter)
                emptyList()
            }
        }
    }

    /**
     * Get audit records by user
     */
    @Transactional(readOnly = true)
    fun getByUser(userId: UUID): List<AuditRecord> {
        return auditRepository.findByUserId(userId)
    }

    /**
     * Get audit records by action
     */
    @Transactional(readOnly = true)
    fun getByAction(action: AuditAction): List<AuditRecord> {
        return auditRepository.findByAction(action)
    }
}

