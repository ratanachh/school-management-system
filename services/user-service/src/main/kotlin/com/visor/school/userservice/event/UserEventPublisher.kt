package com.visor.school.userservice.event

import com.visor.school.common.events.BaseEvent
import com.visor.school.userservice.model.User
import org.slf4j.LoggerFactory
import org.springframework.amqp.AmqpException
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.UUID
import kotlin.math.min
import kotlin.math.pow

/**
 * Publisher for user-related events to RabbitMQ
 * Implements retry logic with exponential backoff for failed event publishing
 */
@Component
class UserEventPublisher(
    private val rabbitTemplate: RabbitTemplate,
    @Value("\${spring.application.name}") private val serviceName: String
) {
    private val logger = LoggerFactory.getLogger(UserEventPublisher::class.java)

    companion object {
        const val EXCHANGE_NAME = "school-management.exchange"
        const val USER_CREATED_ROUTING_KEY = "user.created"
        const val USER_UPDATED_ROUTING_KEY = "user.updated"
        const val USER_EMAIL_VERIFIED_ROUTING_KEY = "user.email.verified"
        
        // Retry configuration constants
        private const val MAX_RETRY_ATTEMPTS = 3
        private const val INITIAL_RETRY_DELAY_MS = 100L
        private const val MAX_RETRY_DELAY_MS = 2000L
        private const val RETRY_MULTIPLIER = 2.0
    }

    /**
     * Publish user created event with retry logic
     * Uses exponential backoff for transient failures
     * Does not throw exceptions to avoid blocking user creation
     */
    fun publishUserCreated(user: User) {
        val event = UserCreatedEvent(
            userId = user.id!!,
            email = user.email,
            roles = user.roles.map { it.name }.toSet(),
            firstName = user.firstName,
            lastName = user.lastName,
            keycloakId = user.keycloakId
        )

        publishWithRetry(
            event = event,
            routingKey = USER_CREATED_ROUTING_KEY,
            eventType = "user.created",
            userId = user.id!!
        )
    }
    
    /**
     * Publish event with retry logic and exponential backoff
     * @param event The event to publish
     * @param routingKey The routing key for the event
     * @param eventType Human-readable event type for logging
     * @param userId User ID for logging context
     */
    private fun publishWithRetry(
        event: Any,
        routingKey: String,
        eventType: String,
        userId: UUID
    ) {
        var attempt = 0
        var lastException: Exception? = null
        
        while (attempt < MAX_RETRY_ATTEMPTS) {
            try {
                rabbitTemplate.convertAndSend(EXCHANGE_NAME, routingKey, event)
                logger.info("Published $eventType event for user: $userId")
                return // Success - exit retry loop
            } catch (e: AmqpException) {
                lastException = e
                attempt++
                
                if (attempt < MAX_RETRY_ATTEMPTS) {
                    val delay = calculateRetryDelay(attempt)
                    logger.warn(
                        "Failed to publish $eventType event for user: $userId (attempt $attempt/$MAX_RETRY_ATTEMPTS). " +
                        "Retrying in ${delay}ms. Error: ${e.message}"
                    )
                    Thread.sleep(delay)
                } else {
                    logger.error(
                        "Failed to publish $eventType event for user: $userId after $MAX_RETRY_ATTEMPTS attempts. " +
                        "Event will not be published. Consider implementing a dead letter queue or event store for later retry.",
                        e
                    )
                }
            } catch (e: Exception) {
                lastException = e
                attempt++
                
                // For non-AMQP exceptions, log and don't retry
                logger.error(
                    "Unexpected error publishing $eventType event for user: $userId. " +
                    "Error type: ${e.javaClass.simpleName}, Message: ${e.message}",
                    e
                )
                return // Don't retry for unexpected exceptions
            }
        }
        
        // All retries exhausted - log final error
        if (lastException != null) {
            logger.error(
                "Failed to publish $eventType event for user: $userId after all retry attempts. " +
                "This is a non-critical failure and does not affect user creation. " +
                "Consider implementing an event store or dead letter queue for failed events.",
                lastException
            )
        }
    }
    
    /**
     * Calculate retry delay with exponential backoff
     * @param attempt Current attempt number (1-based)
     * @return Delay in milliseconds
     */
    private fun calculateRetryDelay(attempt: Int): Long {
        val delay = (INITIAL_RETRY_DELAY_MS * RETRY_MULTIPLIER.pow(attempt - 1)).toLong()
        return min(delay, MAX_RETRY_DELAY_MS)
    }

    /**
     * Publish user updated event with retry logic
     */
    fun publishUserUpdated(user: User) {
        val event = UserUpdatedEvent(
            userId = user.id!!,
            email = user.email,
            roles = user.roles.map { it.name }.toSet(),
            accountStatus = user.accountStatus.name
        )

        publishWithRetry(
            event = event,
            routingKey = USER_UPDATED_ROUTING_KEY,
            eventType = "user.updated",
            userId = user.id!!
        )
    }

    /**
     * Publish email verified event with retry logic
     */
    fun publishEmailVerified(user: User) {
        val event = EmailVerifiedEvent(
            userId = user.id!!,
            email = user.email
        )

        publishWithRetry(
            event = event,
            routingKey = USER_EMAIL_VERIFIED_ROUTING_KEY,
            eventType = "user.email.verified",
            userId = user.id!!
        )
    }
}

/**
 * User Created Event
 */
data class UserCreatedEvent(
    val userId: UUID,
    val email: String,
    val roles: Set<String>,
    val firstName: String,
    val lastName: String,
    val keycloakId: String
) : BaseEvent(
    UUID.randomUUID(),
    Instant.now(),
    "1.0",
    "UserCreatedEvent"
) {
    override fun getAggregateId(): UUID = userId
    override fun getAggregateType(): String = "User"
}

/**
 * User Updated Event
 */
data class UserUpdatedEvent(
    val userId: UUID,
    val email: String,
    val roles: Set<String>,
    val accountStatus: String
) : BaseEvent(
    UUID.randomUUID(),
    Instant.now(),
    "1.0",
    "UserUpdatedEvent"
) {
    override fun getAggregateId(): UUID = userId
    override fun getAggregateType(): String = "User"
}

/**
 * Email Verified Event
 */
data class EmailVerifiedEvent(
    val userId: UUID,
    val email: String
) : BaseEvent(
    UUID.randomUUID(),
    Instant.now(),
    "1.0",
    "EmailVerifiedEvent"
) {
    override fun getAggregateId(): UUID = userId
    override fun getAggregateType(): String = "User"
}
