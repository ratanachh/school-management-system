package com.visor.school.notification.service

import com.visor.school.notification.model.Notification
import com.visor.school.notification.model.NotificationType
import com.visor.school.notification.repository.NotificationRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

/**
 * Notification service for managing notifications
 */
@Service
@Transactional
class NotificationService(
    private val notificationRepository: NotificationRepository,
    private val notificationDeliveryService: NotificationDeliveryService
) {
    private val logger = LoggerFactory.getLogger(NotificationService::class.java)

    /**
     * Create a new notification
     */
    fun create(
        userId: UUID,
        type: NotificationType,
        title: String,
        message: String,
        metadata: Map<String, Any>? = null
    ): Notification {
        logger.info("Creating notification: type=$type for user=$userId")

        val notification = Notification(
            userId = userId,
            type = type,
            title = title,
            message = message,
            metadata = metadata
        )

        val saved = notificationRepository.save(notification)
        notificationDeliveryService.deliver(saved)

        return saved
    }

    /**
     * Mark notification as read
     */
    fun markAsRead(notificationId: UUID): Notification {
        logger.info("Marking notification as read: $notificationId")

        val notification = notificationRepository.findById(notificationId)
            .orElseThrow { NoSuchElementException("Notification not found: $notificationId") }

        if (!notification.read) {
            notification.read = true
            notification.readAt = Instant.now()
            return notificationRepository.save(notification)
        }

        return notification
    }

    /**
     * Get all notifications for a user
     */
    @Transactional(readOnly = true)
    fun getByUser(userId: UUID): List<Notification> {
        logger.debug("Getting notifications for user: $userId")
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
    }

    /**
     * Get unread notifications for a user
     */
    @Transactional(readOnly = true)
    fun getUnread(userId: UUID): List<Notification> {
        logger.debug("Getting unread notifications for user: $userId")
        return notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId)
    }
}

