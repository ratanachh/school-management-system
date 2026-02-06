package com.visor.school.notification.controller;

import com.visor.school.common.api.ApiResponse;
import com.visor.school.notification.model.Notification;
import com.visor.school.notification.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Notification controller
 * Accessible by all authenticated users for their own notifications
 */
@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Get notifications for a user
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getNotifications(
        @RequestParam UUID userId,
        @RequestParam(required = false, defaultValue = "false") boolean unreadOnly
    ) {
        List<Notification> notifications;
        if (unreadOnly) {
            notifications = notificationService.getUnread(userId);
        } else {
            notifications = notificationService.getByUser(userId);
        }

        List<NotificationResponse> response = notifications.stream()
            .map(NotificationResponse::from)
            .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Mark notification as read
     */
    @PutMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(
        @PathVariable UUID id
    ) {
        Notification notification = notificationService.markAsRead(id);
        NotificationResponse response = NotificationResponse.from(notification);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    public record NotificationResponse(
        String id,
        String userId,
        String type,
        String title,
        String message,
        Map<String, Object> metadata,
        boolean read,
        String readAt,
        String createdAt
    ) {
        public static NotificationResponse from(Notification notification) {
            return new NotificationResponse(
                notification.getId().toString(),
                notification.getUserId().toString(),
                notification.getType().name(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getMetadata(),
                notification.isRead(),
                notification.getReadAt() != null ? notification.getReadAt().toString() : null,
                notification.getCreatedAt().toString()
            );
        }
    }
}
