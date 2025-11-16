package com.visor.school.audit.event

import com.visor.school.audit.model.AuditAction
import com.visor.school.audit.service.AuditService
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component
import java.util.UUID

/**
 * Event consumer for logging security events to audit service
 */
@Component
class AuditEventConsumer(
    private val auditService: AuditService
) {
    private val logger = LoggerFactory.getLogger(AuditEventConsumer::class.java)

    /**
     * Handle UserCreatedEvent - Log user creation
     */
    @RabbitListener(queues = ["user_created_queue"])
    fun handleUserCreated(event: Map<String, Any>) {
        val userId = UUID.fromString(event["userId"].toString())
        val role = event["role"].toString()

        logger.info("Received UserCreatedEvent for audit: $userId")

        try {
            auditService.log(
                userId = userId,
                action = AuditAction.DATA_CREATION,
                resourceType = "User",
                resourceId = userId.toString(),
                details = mapOf(
                    "email" to event["email"].toString(),
                    "role" to role,
                    "keycloakId" to event["keycloakId"].toString()
                )
            )
        } catch (e: Exception) {
            logger.error("Failed to log audit record for UserCreatedEvent: $userId", e)
        }
    }

    /**
     * Handle authentication failures (if published as events)
     */
    @RabbitListener(queues = ["authentication_failure_queue"])
    fun handleAuthenticationFailure(event: Map<String, Any>) {
        val email = event.getOrDefault("email", "unknown").toString()
        val ipAddress = event.getOrDefault("ipAddress", "").toString()
        val userAgent = event.getOrDefault("userAgent", "").toString()
        val reason = event.getOrDefault("reason", "Invalid credentials").toString()

        logger.info("Received AuthenticationFailureEvent for audit: $email")

        try {
            // Try to get userId if available, otherwise use a system user
            val userId = event.get("userId")?.let { UUID.fromString(it.toString()) }
                ?: UUID.fromString("00000000-0000-0000-0000-000000000000") // System user

            auditService.log(
                userId = userId,
                action = AuditAction.ACCESS_ATTEMPT,
                resourceType = "Authentication",
                resourceId = null,
                ipAddress = ipAddress,
                userAgent = userAgent,
                details = mapOf(
                    "email" to email,
                    "reason" to reason
                ),
                success = false,
                errorMessage = reason
            )
        } catch (e: Exception) {
            logger.error("Failed to log audit record for AuthenticationFailureEvent: $email", e)
        }
    }

    /**
     * Handle data modification events
     * Listens to events that indicate data modifications
     */
    @RabbitListener(queues = ["data_modification_queue"])
    fun handleDataModification(event: Map<String, Any>) {
        val userId = UUID.fromString(event["userId"].toString())
        val resourceType = event.getOrDefault("resourceType", "Unknown").toString()
        val resourceId = event.getOrDefault("resourceId", "").toString()

        logger.info("Received DataModificationEvent for audit: $resourceType/$resourceId")

        try {
            auditService.log(
                userId = userId,
                action = AuditAction.DATA_MODIFICATION,
                resourceType = resourceType,
                resourceId = resourceId,
                ipAddress = event.getOrDefault("ipAddress", null)?.toString(),
                userAgent = event.getOrDefault("userAgent", null)?.toString(),
                details = event.getOrDefault("details", null) as? Map<String, Any>
            )
        } catch (e: Exception) {
            logger.error("Failed to log audit record for DataModificationEvent: $resourceType/$resourceId", e)
        }
    }
}

