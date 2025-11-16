package com.visor.school.notification.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class NotificationTest {

    @Test
    fun `should create notification with required fields`() {
        val notification = Notification(
            userId = UUID.randomUUID(),
            type = NotificationType.GRADE_POSTED,
            title = "New Grade Posted",
            message = "A new grade has been posted for your assessment"
        )

        assertNotNull(notification.id)
        assertNotNull(notification.userId)
        assertEquals(NotificationType.GRADE_POSTED, notification.type)
        assertEquals("New Grade Posted", notification.title)
        assertEquals("A new grade has been posted for your assessment", notification.message)
        assertFalse(notification.read)
        assertNotNull(notification.createdAt)
    }

    @Test
    fun `should create notification with all fields`() {
        val notification = Notification(
            userId = UUID.randomUUID(),
            type = NotificationType.ATTENDANCE_MARKED,
            title = "Attendance Marked",
            message = "Your attendance has been marked for today",
            metadata = mapOf("classId" to "CLASS-001", "date" to "2024-01-15")
        )

        assertEquals(NotificationType.ATTENDANCE_MARKED, notification.type)
        assertNotNull(notification.metadata)
        assertEquals("CLASS-001", notification.metadata!!["classId"])
    }

    @Test
    fun `should accept all notification types`() {
        val types = listOf(
            NotificationType.GRADE_POSTED,
            NotificationType.ATTENDANCE_MARKED,
            NotificationType.ACCOUNT_CREATED,
            NotificationType.ATTENDANCE_SESSION_APPROVED,
            NotificationType.ATTENDANCE_SESSION_REJECTED,
            NotificationType.ASSESSMENT_CREATED
        )

        types.forEach { type ->
            val notification = Notification(
                userId = UUID.randomUUID(),
                type = type,
                title = "Test Notification",
                message = "Test message"
            )

            assertEquals(type, notification.type)
        }
    }

    @Test
    fun `should mark notification as read`() {
        val notification = Notification(
            userId = UUID.randomUUID(),
            type = NotificationType.GRADE_POSTED,
            title = "New Grade",
            message = "Your grade has been posted"
        )

        assertFalse(notification.read)

        notification.read = true
        notification.readAt = Instant.now()

        assertTrue(notification.read)
        assertNotNull(notification.readAt)
    }
}

