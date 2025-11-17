package com.visor.school.userservice.event

import com.visor.school.common.events.BaseEvent
import com.visor.school.userservice.model.User
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.UUID

/**
 * Publisher for user-related events to RabbitMQ
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
    }

    /**
     * Publish user created event
     */
    fun publishUserCreated(user: User) {
        val event = UserCreatedEvent(
            userId = user.id!!,
            email = user.email,
            role = user.role.name,
            firstName = user.firstName,
            lastName = user.lastName,
            keycloakId = user.keycloakId
        )

        try {
            rabbitTemplate.convertAndSend(EXCHANGE_NAME, USER_CREATED_ROUTING_KEY, event)
            logger.info("Published user.created event for user: ${user.id}")
        } catch (e: Exception) {
            logger.error("Failed to publish user.created event for user: ${user.id}", e)
            // In production, consider using a dead letter queue or retry mechanism
        }
    }

    /**
     * Publish user updated event
     */
    fun publishUserUpdated(user: User) {
        val event = UserUpdatedEvent(
            userId = user.id!!,
            email = user.email,
            role = user.role.name,
            accountStatus = user.accountStatus.name
        )

        try {
            rabbitTemplate.convertAndSend(EXCHANGE_NAME, USER_UPDATED_ROUTING_KEY, event)
            logger.info("Published user.updated event for user: ${user.id}")
        } catch (e: Exception) {
            logger.error("Failed to publish user.updated event for user: ${user.id}", e)
        }
    }

    /**
     * Publish email verified event
     */
    fun publishEmailVerified(user: User) {
        val event = EmailVerifiedEvent(
            userId = user.id!!,
            email = user.email
        )

        try {
            rabbitTemplate.convertAndSend(EXCHANGE_NAME, USER_EMAIL_VERIFIED_ROUTING_KEY, event)
            logger.info("Published user.email.verified event for user: ${user.id}")
        } catch (e: Exception) {
            logger.error("Failed to publish user.email.verified event for user: ${user.id}", e)
        }
    }
}

/**
 * User Created Event
 */
data class UserCreatedEvent(
    val userId: UUID,
    val email: String,
    val role: String,
    val firstName: String,
    val lastName: String,
    val keycloakId: String
) : BaseEvent(), java.io.Serializable {
    override fun getAggregateId(): UUID = userId
    override fun getAggregateType(): String = "User"
}

/**
 * User Updated Event
 */
data class UserUpdatedEvent(
    val userId: UUID,
    val email: String,
    val role: String,
    val accountStatus: String
) : BaseEvent(), java.io.Serializable {
    override fun getAggregateId(): UUID = userId
    override fun getAggregateType(): String = "User"
}

/**
 * Email Verified Event
 */
data class EmailVerifiedEvent(
    val userId: UUID,
    val email: String
) : BaseEvent(), java.io.Serializable {
    override fun getAggregateId(): UUID = userId
    override fun getAggregateType(): String = "User"
}

