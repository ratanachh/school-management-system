package com.visor.school.notification.service

import com.visor.school.notification.model.Notification
import com.visor.school.notification.model.NotificationType
import com.visor.school.notification.repository.NotificationRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class NotificationServiceTest {

    @Mock
    private lateinit var notificationRepository: NotificationRepository

    @Mock
    private lateinit var notificationDeliveryService: NotificationDeliveryService

    @InjectMocks
    private lateinit var notificationService: NotificationService

    private val testUserId = UUID.randomUUID()

    @BeforeEach
    fun setup() {
        notificationService = NotificationService(notificationRepository, notificationDeliveryService)
    }

    @Test
    fun `should create notification`() {
        // Given
        val notification = Notification(
            userId = testUserId,
            type = NotificationType.GRADE_POSTED,
            title = "New Grade Posted",
            message = "A new grade has been posted"
        )
        whenever(notificationRepository.save(any())).thenReturn(notification)
        doNothing().whenever(notificationDeliveryService).deliver(any())

        // When
        val result = notificationService.create(
            userId = testUserId,
            type = NotificationType.GRADE_POSTED,
            title = "New Grade Posted",
            message = "A new grade has been posted"
        )

        // Then
        assertNotNull(result)
        assertEquals(testUserId, result.userId)
        assertEquals(NotificationType.GRADE_POSTED, result.type)
        verify(notificationRepository).save(any())
        verify(notificationDeliveryService).deliver(any())
    }

    @Test
    fun `should mark notification as read`() {
        // Given
        val notificationId = UUID.randomUUID()
        val notification = Notification(
            id = notificationId,
            userId = testUserId,
            type = NotificationType.GRADE_POSTED,
            title = "New Grade",
            message = "Your grade has been posted",
            read = false
        )
        whenever(notificationRepository.findById(notificationId)).thenReturn(java.util.Optional.of(notification))
        whenever(notificationRepository.save(any())).thenReturn(notification.copy(read = true))

        // When
        val result = notificationService.markAsRead(notificationId)

        // Then
        assertTrue(result.read)
        verify(notificationRepository).findById(notificationId)
        verify(notificationRepository).save(any())
    }

    @Test
    fun `should get notifications by user`() {
        // Given
        val notifications = listOf(
            Notification(
                userId = testUserId,
                type = NotificationType.GRADE_POSTED,
                title = "Grade 1",
                message = "Message 1"
            ),
            Notification(
                userId = testUserId,
                type = NotificationType.ATTENDANCE_MARKED,
                title = "Attendance 1",
                message = "Message 2"
            )
        )
        whenever(notificationRepository.findByUserIdOrderByCreatedAtDesc(testUserId)).thenReturn(notifications)

        // When
        val result = notificationService.getByUser(testUserId)

        // Then
        assertEquals(2, result.size)
        verify(notificationRepository).findByUserIdOrderByCreatedAtDesc(testUserId)
    }

    @Test
    fun `should get unread notifications`() {
        // Given
        val notifications = listOf(
            Notification(
                userId = testUserId,
                type = NotificationType.GRADE_POSTED,
                title = "Unread 1",
                message = "Message 1",
                read = false
            ),
            Notification(
                userId = testUserId,
                type = NotificationType.ATTENDANCE_MARKED,
                title = "Unread 2",
                message = "Message 2",
                read = false
            )
        )
        whenever(notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(testUserId)).thenReturn(notifications)

        // When
        val result = notificationService.getUnread(testUserId)

        // Then
        assertEquals(2, result.size)
        assertTrue(result.all { !it.read })
        verify(notificationRepository).findByUserIdAndReadFalseOrderByCreatedAtDesc(testUserId)
    }
}

