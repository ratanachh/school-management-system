package com.visor.school.notification.service

import com.visor.school.notification.model.Notification
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/**
 * Notification delivery service for handling notification delivery channels
 * (e.g., in-app, email, SMS, push notifications)
 */
@Service
class NotificationDeliveryService {
    private val logger = LoggerFactory.getLogger(NotificationDeliveryService::class.java)

    /**
     * Deliver notification to user
     * Currently supports in-app notifications only
     * Future: Add email, SMS, push notification support
     */
    fun deliver(notification: Notification) {
        logger.info("Delivering notification: ${notification.id} to user: ${notification.userId}")

        // In-app notification is already stored in database
        // Future implementations:
        // - Send email notification if user has email preferences enabled
        // - Send SMS notification if user has SMS preferences enabled
        // - Send push notification if user has push notifications enabled

        logger.debug("Notification delivered: ${notification.id}")
    }
}

