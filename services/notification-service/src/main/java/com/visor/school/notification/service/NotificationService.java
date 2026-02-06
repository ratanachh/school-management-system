package com.visor.school.notification.service;

import com.visor.school.notification.model.Notification;
import com.visor.school.notification.model.NotificationType;
import com.visor.school.notification.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * Notification service for managing notifications
 */
@Service
@Transactional
public class NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final NotificationDeliveryService notificationDeliveryService;

    public NotificationService(NotificationRepository notificationRepository, NotificationDeliveryService notificationDeliveryService) {
        this.notificationRepository = notificationRepository;
        this.notificationDeliveryService = notificationDeliveryService;
    }

    /**
     * Create a new notification
     */
    public Notification create(
        UUID userId,
        NotificationType type,
        String title,
        String message,
        Map<String, Object> metadata
    ) {
        logger.info("Creating notification: type={} for user={}", type, userId);

        Notification notification = new Notification(
            userId,
            type,
            title,
            message,
            metadata
        );

        Notification saved = notificationRepository.save(notification);
        notificationDeliveryService.deliver(saved);

        return saved;
    }

    /**
     * Mark notification as read
     */
    public Notification markAsRead(UUID notificationId) {
        logger.info("Marking notification as read: {}", notificationId);

        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new NoSuchElementException("Notification not found: " + notificationId));

        if (!notification.isRead()) {
            notification.setRead(true);
            notification.setReadAt(Instant.now());
            return notificationRepository.save(notification);
        }

        return notification;
    }

    /**
     * Get all notifications for a user
     */
    @Transactional(readOnly = true)
    public List<Notification> getByUser(UUID userId) {
        logger.debug("Getting notifications for user: {}", userId);
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * Get unread notifications for a user
     */
    @Transactional(readOnly = true)
    public List<Notification> getUnread(UUID userId) {
        logger.debug("Getting unread notifications for user: {}", userId);
        return notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId);
    }
}
