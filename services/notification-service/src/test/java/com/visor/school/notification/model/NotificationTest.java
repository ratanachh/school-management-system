package com.visor.school.notification.model;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class NotificationTest {

    @Test
    void shouldCreateNotificationWithRequiredFields() {
        Notification notification = new Notification(
            UUID.randomUUID(),
            NotificationType.GRADE_POSTED,
            "New Grade Posted",
            "A new grade has been posted for your assessment",
            null
        );

        assertNotNull(notification.getId());
        assertNotNull(notification.getUserId());
        assertEquals(NotificationType.GRADE_POSTED, notification.getType());
        assertEquals("New Grade Posted", notification.getTitle());
        assertEquals("A new grade has been posted for your assessment", notification.getMessage());
        assertFalse(notification.isRead());
        assertNotNull(notification.getCreatedAt());
    }

    @Test
    void shouldCreateNotificationWithAllFields() {
        Notification notification = new Notification(
            UUID.randomUUID(),
            NotificationType.ATTENDANCE_MARKED,
            "Attendance Marked",
            "Your attendance has been marked for today",
            Map.of("classId", "CLASS-001", "date", "2024-01-15")
        );

        assertEquals(NotificationType.ATTENDANCE_MARKED, notification.getType());
        assertNotNull(notification.getMetadata());
        assertEquals("CLASS-001", notification.getMetadata().get("classId"));
    }

    @Test
    void shouldAcceptAllNotificationTypes() {
        List<NotificationType> types = List.of(
            NotificationType.GRADE_POSTED,
            NotificationType.ATTENDANCE_MARKED,
            NotificationType.ACCOUNT_CREATED,
            NotificationType.ATTENDANCE_SESSION_APPROVED,
            NotificationType.ATTENDANCE_SESSION_REJECTED,
            NotificationType.ASSESSMENT_CREATED
        );

        types.forEach(type -> {
            Notification notification = new Notification(
                UUID.randomUUID(),
                type,
                "Test Notification",
                "Test message",
                null
            );

            assertEquals(type, notification.getType());
        });
    }

    @Test
    void shouldMarkNotificationAsRead() {
        Notification notification = new Notification(
            UUID.randomUUID(),
            NotificationType.GRADE_POSTED,
            "New Grade",
            "Your grade has been posted",
            null
        );

        assertFalse(notification.isRead());

        notification.setRead(true);
        notification.setReadAt(Instant.now());

        assertTrue(notification.isRead());
        assertNotNull(notification.getReadAt());
    }
}
