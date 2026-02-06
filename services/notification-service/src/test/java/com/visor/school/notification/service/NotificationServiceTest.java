package com.visor.school.notification.service;

import com.visor.school.notification.model.Notification;
import com.visor.school.notification.model.NotificationType;
import com.visor.school.notification.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationDeliveryService notificationDeliveryService;

    private NotificationService notificationService;

    private final UUID testUserId = UUID.randomUUID();

    @BeforeEach
    void setup() {
        notificationService = new NotificationService(notificationRepository, notificationDeliveryService);
    }

    @Test
    void shouldCreateNotification() {
        // Given
        Notification notification = new Notification(
            testUserId,
            NotificationType.GRADE_POSTED,
            "New Grade Posted",
            "A new grade has been posted",
            null
        );
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        doNothing().when(notificationDeliveryService).deliver(any(Notification.class));

        // When
        Notification result = notificationService.create(
            testUserId,
            NotificationType.GRADE_POSTED,
            "New Grade Posted",
            "A new grade has been posted",
            null
        );

        // Then
        assertNotNull(result);
        assertEquals(testUserId, result.getUserId());
        assertEquals(NotificationType.GRADE_POSTED, result.getType());
        verify(notificationRepository).save(any(Notification.class));
        verify(notificationDeliveryService).deliver(any(Notification.class));
    }

    @Test
    void shouldMarkNotificationAsRead() {
        // Given
        UUID notificationId = UUID.randomUUID();
        Notification notification = new Notification(
            testUserId,
            NotificationType.GRADE_POSTED,
            "New Grade",
            "Your grade has been posted",
            null
        );
        notification.setId(notificationId);
        notification.setRead(false);

        Notification readNotification = new Notification(
            testUserId,
            NotificationType.GRADE_POSTED,
            "New Grade",
            "Your grade has been posted",
            null
        );
        readNotification.setId(notificationId);
        readNotification.setRead(true);

        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(readNotification);

        // When
        Notification result = notificationService.markAsRead(notificationId);

        // Then
        assertTrue(result.isRead());
        verify(notificationRepository).findById(notificationId);
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void shouldGetNotificationsByUser() {
        // Given
        List<Notification> notifications = List.of(
            new Notification(
                testUserId,
                NotificationType.GRADE_POSTED,
                "Grade 1",
                "Message 1",
                null
            ),
            new Notification(
                testUserId,
                NotificationType.ATTENDANCE_MARKED,
                "Attendance 1",
                "Message 2",
                null
            )
        );
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(testUserId)).thenReturn(notifications);

        // When
        List<Notification> result = notificationService.getByUser(testUserId);

        // Then
        assertEquals(2, result.size());
        verify(notificationRepository).findByUserIdOrderByCreatedAtDesc(testUserId);
    }

    @Test
    void shouldGetUnreadNotificationsByUser() {
        // Given
        List<Notification> notifications = List.of(
            new Notification(
                testUserId,
                NotificationType.GRADE_POSTED,
                "Grade 1",
                "Message 1",
                null
            )
        );
        when(notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(testUserId)).thenReturn(notifications);

        // When
        List<Notification> result = notificationService.getUnread(testUserId);

        // Then
        assertEquals(1, result.size());
        verify(notificationRepository).findByUserIdAndReadFalseOrderByCreatedAtDesc(testUserId);
    }
}
