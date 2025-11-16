package com.visor.school.notification.event

import com.visor.school.notification.model.NotificationType
import com.visor.school.notification.service.NotificationService
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component
import java.util.UUID

/**
 * Event consumer for generating notifications from system events
 */
@Component
class NotificationEventConsumer(
    private val notificationService: NotificationService
) {
    private val logger = LoggerFactory.getLogger(NotificationEventConsumer::class.java)

    /**
     * Handle GradeRecordedEvent - Notify student/parent about new grade
     */
    @RabbitListener(queues = ["grade_recorded_queue"])
    fun handleGradeRecorded(event: Map<String, Any>) {
        val studentId = UUID.fromString(event["studentId"].toString())
        val assessmentName = event.getOrDefault("assessmentName", "Assessment").toString()
        val grade = event.getOrDefault("grade", "").toString()

        logger.info("Received GradeRecordedEvent for notification: student=$studentId")

        try {
            // Notify student
            notificationService.create(
                userId = studentId,
                type = NotificationType.GRADE_POSTED,
                title = "New Grade Posted",
                message = "A new grade ($grade) has been posted for $assessmentName",
                metadata = mapOf(
                    "assessmentId" to event.getOrDefault("assessmentId", "").toString(),
                    "gradeId" to event.getOrDefault("gradeId", "").toString(),
                    "grade" to grade
                )
            )

            // TODO: Notify parents if applicable
        } catch (e: Exception) {
            logger.error("Failed to create notification for GradeRecordedEvent: $studentId", e)
        }
    }

    /**
     * Handle AttendanceMarkedEvent - Notify student/parent about attendance
     */
    @RabbitListener(queues = ["attendance_marked_queue"])
    fun handleAttendanceMarked(event: Map<String, Any>) {
        val studentId = UUID.fromString(event["studentId"].toString())
        val status = event.getOrDefault("status", "UNKNOWN").toString()
        val date = event.getOrDefault("date", "").toString()

        logger.info("Received AttendanceMarkedEvent for notification: student=$studentId")

        try {
            notificationService.create(
                userId = studentId,
                type = NotificationType.ATTENDANCE_MARKED,
                title = "Attendance Marked",
                message = "Your attendance for $date has been marked as $status",
                metadata = mapOf(
                    "attendanceRecordId" to event.getOrDefault("attendanceRecordId", "").toString(),
                    "status" to status,
                    "date" to date
                )
            )

            // TODO: Notify parents if applicable
        } catch (e: Exception) {
            logger.error("Failed to create notification for AttendanceMarkedEvent: $studentId", e)
        }
    }

    /**
     * Handle AttendanceSessionApprovedEvent - Notify class leader about approval
     */
    @RabbitListener(queues = ["attendance_session_approved_queue"])
    fun handleAttendanceSessionApproved(event: Map<String, Any>) {
        val classLeaderId = event.getOrDefault("collectedBy", "").toString()
        if (classLeaderId.isBlank()) return

        val sessionId = event.getOrDefault("sessionId", "").toString()
        val date = event.getOrDefault("date", "").toString()

        logger.info("Received AttendanceSessionApprovedEvent for notification: classLeader=$classLeaderId")

        try {
            notificationService.create(
                userId = UUID.fromString(classLeaderId),
                type = NotificationType.ATTENDANCE_SESSION_APPROVED,
                title = "Attendance Session Approved",
                message = "Your attendance collection for $date has been approved by the teacher",
                metadata = mapOf(
                    "sessionId" to sessionId,
                    "date" to date
                )
            )
        } catch (e: Exception) {
            logger.error("Failed to create notification for AttendanceSessionApprovedEvent: $classLeaderId", e)
        }
    }

    /**
     * Handle UserCreatedEvent - Notify user about account creation
     */
    @RabbitListener(queues = ["user_created_queue"])
    fun handleUserCreated(event: Map<String, Any>) {
        val userId = UUID.fromString(event["userId"].toString())
        val email = event["email"].toString()
        val role = event["role"].toString()

        logger.info("Received UserCreatedEvent for notification: user=$userId")

        try {
            notificationService.create(
                userId = userId,
                type = NotificationType.ACCOUNT_CREATED,
                title = "Account Created",
                message = "Your account has been created successfully. Welcome to the school management system!",
                metadata = mapOf(
                    "email" to email,
                    "role" to role
                )
            )
        } catch (e: Exception) {
            logger.error("Failed to create notification for UserCreatedEvent: $userId", e)
        }
    }
}

