package com.visor.school.audit.service;

import com.visor.school.audit.model.AuditAction;
import com.visor.school.audit.model.AuditRecord;
import com.visor.school.audit.repository.AuditRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Audit service for logging security-relevant events
 */
@Service
@Transactional
public class AuditService {
    private static final Logger logger = LoggerFactory.getLogger(AuditService.class);
    
    private final AuditRepository auditRepository;

    public AuditService(AuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    /**
     * Log an audit record
     */
    public AuditRecord log(
        UUID userId,
        AuditAction action,
        String resourceType,
        String resourceId,
        String ipAddress,
        String userAgent,
        Map<String, Object> details,
        boolean success,
        String errorMessage
    ) {
        logger.debug("Logging audit record: userId={}, action={}, resourceType={}", userId, action, resourceType);

        AuditRecord record = new AuditRecord(
            userId,
            action,
            resourceType,
            resourceId,
            ipAddress,
            userAgent,
            details,
            success,
            errorMessage
        );

        return auditRepository.save(record);
    }
    
    // Overload for default values if needed, but in Java we can just pass nulls/defaults
    // The Kotlin code had defaults. I will create an overload or just expect callers to pass values.
    // Given the usage in AuditEventConsumer, I'll stick to the full method or maybe a builder? 
    // But for now I'll create an overload with fewer parameters if I see fit, but the main one covers all.
    // The usage in Consumer passes specific named args.
    // Kotlin: log(userId, action, resourceType, resourceId, details...)
    // I will implement a simpler log method if needed, but let's look at the Consumer usage.
    // It uses named arguments. I'll stick to the full method.

    /**
     * Query audit records with filters
     */
    @Transactional(readOnly = true)
    public List<AuditRecord> query(
        UUID userId,
        AuditAction action,
        LocalDate startDate,
        LocalDate endDate
    ) {
        Instant start = (startDate != null) 
            ? startDate.atStartOfDay().toInstant(ZoneOffset.UTC) 
            : Instant.now().minusSeconds(30 * 24 * 60 * 60); // Default: 30 days ago
            
        Instant end = (endDate != null) 
            ? endDate.atTime(23, 59, 59).toInstant(ZoneOffset.UTC) 
            : Instant.now();

        if (userId != null && action != null) {
            return auditRepository.findByUserIdAndActionAndTimestampBetween(userId, action, start, end);
        } else if (userId != null) {
            return auditRepository.findByUserIdAndTimestampBetween(userId, start, end);
        } else if (action != null) {
            return auditRepository.findByActionAndTimestampBetween(action, start, end);
        } else {
            // If no filters, return empty list (should specify at least one filter)
            return Collections.emptyList();
        }
    }

    /**
     * Get audit records by user
     */
    @Transactional(readOnly = true)
    public List<AuditRecord> getByUser(UUID userId) {
        return auditRepository.findByUserId(userId);
    }

    /**
     * Get audit records by action
     */
    @Transactional(readOnly = true)
    public List<AuditRecord> getByAction(AuditAction action) {
        return auditRepository.findByAction(action);
    }
}
