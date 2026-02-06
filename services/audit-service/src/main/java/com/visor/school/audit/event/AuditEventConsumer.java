package com.visor.school.audit.event;

import com.visor.school.audit.model.AuditAction;
import com.visor.school.audit.service.AuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/**
 * Event consumer for logging security events to audit service
 */
@Component
public class AuditEventConsumer {
    private static final Logger logger = LoggerFactory.getLogger(AuditEventConsumer.class);
    
    private final AuditService auditService;

    public AuditEventConsumer(AuditService auditService) {
        this.auditService = auditService;
    }

    /**
     * Handle UserCreatedEvent - Log user creation
     */
    @RabbitListener(queues = "user_created_queue")
    public void handleUserCreated(Map<String, Object> event) {
        UUID userId = UUID.fromString(event.get("userId").toString());
        String role = event.get("role").toString();

        logger.info("Received UserCreatedEvent for audit: {}", userId);

        try {
            auditService.log(
                userId,
                AuditAction.DATA_CREATION,
                "User",
                userId.toString(),
                null,
                null,
                Map.of(
                    "email", event.get("email").toString(),
                    "role", role,
                    "keycloakId", event.get("keycloakId").toString()
                ),
                true,
                null
            );
        } catch (Exception e) {
            logger.error("Failed to log audit record for UserCreatedEvent: {}", userId, e);
        }
    }

    /**
     * Handle authentication failures (if published as events)
     */
    @RabbitListener(queues = "authentication_failure_queue")
    public void handleAuthenticationFailure(Map<String, Object> event) {
        String email = event.getOrDefault("email", "unknown").toString();
        String ipAddress = event.getOrDefault("ipAddress", "").toString();
        String userAgent = event.getOrDefault("userAgent", "").toString();
        String reason = event.getOrDefault("reason", "Invalid credentials").toString();

        logger.info("Received AuthenticationFailureEvent for audit: {}", email);

        try {
            // Try to get userId if available, otherwise use a system user
            UUID userId;
            if (event.get("userId") != null) {
                userId = UUID.fromString(event.get("userId").toString());
            } else {
                userId = UUID.fromString("00000000-0000-0000-0000-000000000000"); // System user
            }

            auditService.log(
                userId,
                AuditAction.ACCESS_ATTEMPT,
                "Authentication",
                null,
                ipAddress,
                userAgent,
                Map.of(
                    "email", email,
                    "reason", reason
                ),
                false,
                reason
            );
        } catch (Exception e) {
            logger.error("Failed to log audit record for AuthenticationFailureEvent: {}", email, e);
        }
    }

    /**
     * Handle data modification events
     * Listens to events that indicate data modifications
     */
    @RabbitListener(queues = "data_modification_queue")
    public void handleDataModification(Map<String, Object> event) {
        UUID userId = UUID.fromString(event.get("userId").toString());
        String resourceType = event.getOrDefault("resourceType", "Unknown").toString();
        String resourceId = event.getOrDefault("resourceId", "").toString();

        logger.info("Received DataModificationEvent for audit: {}/{}", resourceType, resourceId);
        
        String ipAddress = event.getOrDefault("ipAddress", null) != null ? event.get("ipAddress").toString() : null;
        String userAgent = event.getOrDefault("userAgent", null) != null ? event.get("userAgent").toString() : null;
        
        // Handling details - ensuring it's castable to Map if possible, or wrapping it
        Map<String, Object> details = null;
        Object detailsObj = event.get("details");
        if (detailsObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) detailsObj;
            details = map;
        }

        try {
            auditService.log(
                userId,
                AuditAction.DATA_MODIFICATION,
                resourceType,
                resourceId,
                ipAddress,
                userAgent,
                details,
                true,
                null
            );
        } catch (Exception e) {
            logger.error("Failed to log audit record for DataModificationEvent: {}/{}", resourceType, resourceId, e);
        }
    }
}
