package com.visor.school.notification.controller

import com.visor.school.notification.service.NotificationService
import com.visor.school.common.api.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.UUID

/**
 * Notification controller
 * Accessible by all authenticated users for their own notifications
 */
@RestController
@RequestMapping("/api/v1/notifications")
class NotificationController(
    private val notificationService: NotificationService
) {

    /**
     * Get notifications for a user
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    fun getNotifications(
        @RequestParam userId: UUID,
        @RequestParam(required = false, defaultValue = "false") unreadOnly: Boolean
    ): ResponseEntity<ApiResponse<List<NotificationResponse>>> {
        val notifications = if (unreadOnly) {
            notificationService.getUnread(userId)
        } else {
            notificationService.getByUser(userId)
        }

        val response = notifications.map { NotificationResponse.from(it) }

        return ResponseEntity.ok(ApiResponse.success(response))
    }

    /**
     * Mark notification as read
     */
    @PutMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    fun markAsRead(
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<NotificationResponse>> {
        val notification = notificationService.markAsRead(id)
        val response = NotificationResponse.from(notification)

        return ResponseEntity.ok(ApiResponse.success(response))
    }
}

data class NotificationResponse(
    val id: String,
    val userId: String,
    val type: String,
    val title: String,
    val message: String,
    val metadata: Map<String, Any>?,
    val read: Boolean,
    val readAt: String?,
    val createdAt: String
) {
    companion object {
        fun from(notification: com.visor.school.notification.model.Notification): NotificationResponse {
            return NotificationResponse(
                id = notification.id.toString(),
                userId = notification.userId.toString(),
                type = notification.type.name,
                title = notification.title,
                message = notification.message,
                metadata = notification.metadata,
                read = notification.read,
                readAt = notification.readAt?.toString(),
                createdAt = notification.createdAt.toString()
            )
        }
    }
}

