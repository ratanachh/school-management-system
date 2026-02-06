package com.visor.school.notification.event;

import com.visor.school.notification.config.RabbitMQConfig;
import com.visor.school.notification.model.NotificationType;
import com.visor.school.notification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/**
 * Event consumer for generating notifications from system events
 */
@Component
public class NotificationEventConsumer {
    private static final Logger logger = LoggerFactory.getLogger(NotificationEventConsumer.class);

    private final NotificationService notificationService;

    public NotificationEventConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Handle GradeRecordedEvent - Notify student/parent about new grade
     */
    @RabbitListener(queues = RabbitMQConfig.GRADE_RECORDED_QUEUE)
    public void handleGradeRecorded(Map<String, Object> event) {
        UUID studentId = UUID.fromString(event.get("studentId").toString());
        String assessmentName = event.getOrDefault("assessmentName", "Assessment").toString();
        String grade = event.getOrDefault("grade", "").toString();

        logger.info("Received GradeRecordedEvent for notification: student={}", studentId);

        try {
            // Notify student
            notificationService.create(
                studentId,
                NotificationType.GRADE_POSTED,
                "New Grade Posted",
                "A new grade (" + grade + ") has been posted for " + assessmentName,
                Map.of(
                    "assessmentId", event.getOrDefault("assessmentId", "").toString(),
                    "gradeId", event.getOrDefault("gradeId", "").toString(),
                    "grade", grade
                )
            );

            // TODO: Notify parents if applicable
        } catch (Exception e) {
            logger.error("Failed to create notification for GradeRecordedEvent: {}", studentId, e);
        }
    }

    /**
     * Handle AttendanceMarkedEvent - Notify student/parent about attendance
     */
    @RabbitListener(queues = RabbitMQConfig.ATTENDANCE_MARKED_QUEUE)
    public void handleAttendanceMarked(Map<String, Object> event) {
        UUID studentId = UUID.fromString(event.get("studentId").toString());
        String status = event.getOrDefault("status", "UNKNOWN").toString();
        String date = event.getOrDefault("date", "").toString();

        logger.info("Received AttendanceMarkedEvent for notification: student={}", studentId);

        try {
            notificationService.create(
                studentId,
                NotificationType.ATTENDANCE_MARKED,
                "Attendance Marked",
                "Your attendance for " + date + " has been marked as " + status,
                Map.of(
                    "attendanceRecordId", event.getOrDefault("attendanceRecordId", "").toString(),
                    "status", status,
                    "date", date
                )
            );

            // TODO: Notify parents if applicable
        } catch (Exception e) {
            logger.error("Failed to create notification for AttendanceMarkedEvent: {}", studentId, e);
        }
    }

    /**
     * Handle AttendanceSessionApprovedEvent - Notify class leader about approval
     */
    @RabbitListener(queues = RabbitMQConfig.ATTENDANCE_SESSION_APPROVED_QUEUE)
    public void handleAttendanceSessionApproved(Map<String, Object> event) {
        String classLeaderIdStr = event.getOrDefault("collectedBy", "").toString();
        if (classLeaderIdStr.isBlank()) return;

        UUID classLeaderId = UUID.fromString(classLeaderIdStr);
        String sessionId = event.getOrDefault("sessionId", "").toString();
        String date = event.getOrDefault("date", "").toString();

        logger.info("Received AttendanceSessionApprovedEvent for notification: classLeader={}", classLeaderId);

        try {
            notificationService.create(
                classLeaderId,
                NotificationType.ATTENDANCE_SESSION_APPROVED,
                "Attendance Session Approved",
                "Your attendance collection for " + date + " has been approved by the teacher",
                Map.of(
                    "sessionId", sessionId,
                    "date", date
                )
            );
        } catch (Exception e) {
            logger.error("Failed to create notification for AttendanceSessionApprovedEvent: {}", classLeaderId, e);
        }
    }

    /**
     * Handle UserCreatedEvent - Notify user about account creation
     */
    @RabbitListener(queues = RabbitMQConfig.USER_CREATED_QUEUE)
    public void handleUserCreated(Map<String, Object> event) {
        UUID userId = UUID.fromString(event.get("userId").toString());
        String email = event.get("email").toString();
        String role = event.get("role").toString();

        logger.info("Received UserCreatedEvent for notification: user={}", userId);

        try {
            notificationService.create(
                userId,
                NotificationType.ACCOUNT_CREATED,
                "Account Created",
                "Your account has been created successfully. Welcome to the school management system!",
                Map.of(
                    "email", email,
                    "role", role
                )
            );
        } catch (Exception e) {
            logger.error("Failed to create notification for UserCreatedEvent: {}", userId, e);
        }
    }
}
