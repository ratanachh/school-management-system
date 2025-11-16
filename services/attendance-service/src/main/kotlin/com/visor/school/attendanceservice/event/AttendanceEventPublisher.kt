package com.visor.school.attendanceservice.event

import com.visor.school.common.events.BaseEvent
import com.visor.school.attendanceservice.model.AttendanceRecord
import com.visor.school.attendanceservice.model.AttendanceSession
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.UUID

/**
 * Publisher for attendance-related events to RabbitMQ
 */
@Component
class AttendanceEventPublisher(
    private val rabbitTemplate: RabbitTemplate,
    @Value("\${spring.application.name}") private val serviceName: String
) {
    private val logger = LoggerFactory.getLogger(AttendanceEventPublisher::class.java)

    companion object {
        const val EXCHANGE_NAME = "school-management.exchange"
        const val ATTENDANCE_MARKED_ROUTING_KEY = "attendance.marked"
        const val SESSION_DELEGATED_ROUTING_KEY = "attendance.session.delegated"
        const val SESSION_COLLECTED_ROUTING_KEY = "attendance.session.collected"
        const val SESSION_APPROVED_ROUTING_KEY = "attendance.session.approved"
        const val SESSION_REJECTED_ROUTING_KEY = "attendance.session.rejected"
    }

    /**
     * Publish attendance marked event (direct marking)
     */
    fun publishAttendanceMarked(record: AttendanceRecord) {
        val event = AttendanceMarkedEvent(
            attendanceRecordId = record.id,
            studentId = record.studentId,
            classId = record.classId,
            date = record.date,
            status = record.status.name,
            markedBy = record.markedBy
        )

        try {
            rabbitTemplate.convertAndSend(EXCHANGE_NAME, ATTENDANCE_MARKED_ROUTING_KEY, event)
            logger.info("Published attendance.marked event for record: ${record.id}")
        } catch (e: Exception) {
            logger.error("Failed to publish attendance.marked event for record: ${record.id}", e)
        }
    }

    /**
     * Publish session delegated event
     */
    fun publishSessionDelegated(session: AttendanceSession, classLeaderId: UUID) {
        val event = SessionDelegatedEvent(
            sessionId = session.id,
            classId = session.classId,
            date = session.date,
            delegatedTo = classLeaderId,
            createdBy = session.createdBy
        )

        try {
            rabbitTemplate.convertAndSend(EXCHANGE_NAME, SESSION_DELEGATED_ROUTING_KEY, event)
            logger.info("Published attendance.session.delegated event for session: ${session.id}")
        } catch (e: Exception) {
            logger.error("Failed to publish attendance.session.delegated event for session: ${session.id}", e)
        }
    }

    /**
     * Publish session collected event
     */
    fun publishSessionCollected(session: AttendanceSession) {
        val event = SessionCollectedEvent(
            sessionId = session.id,
            classId = session.classId,
            date = session.date,
            collectedBy = session.delegatedTo ?: throw IllegalStateException("Session must be delegated before collection"),
            createdBy = session.createdBy
        )

        try {
            rabbitTemplate.convertAndSend(EXCHANGE_NAME, SESSION_COLLECTED_ROUTING_KEY, event)
            logger.info("Published attendance.session.collected event for session: ${session.id}")
        } catch (e: Exception) {
            logger.error("Failed to publish attendance.session.collected event for session: ${session.id}", e)
        }
    }

    /**
     * Publish session approved event
     */
    fun publishSessionApproved(session: AttendanceSession, approvedBy: UUID) {
        val event = SessionApprovedEvent(
            sessionId = session.id,
            classId = session.classId,
            date = session.date,
            approvedBy = approvedBy,
            collectedBy = session.delegatedTo
        )

        try {
            rabbitTemplate.convertAndSend(EXCHANGE_NAME, SESSION_APPROVED_ROUTING_KEY, event)
            logger.info("Published attendance.session.approved event for session: ${session.id}")
        } catch (e: Exception) {
            logger.error("Failed to publish attendance.session.approved event for session: ${session.id}", e)
        }
    }

    /**
     * Publish session rejected event
     */
    fun publishSessionRejected(session: AttendanceSession, rejectedBy: UUID, reason: String) {
        val event = SessionRejectedEvent(
            sessionId = session.id,
            classId = session.classId,
            date = session.date,
            rejectedBy = rejectedBy,
            rejectionReason = reason,
            collectedBy = session.delegatedTo
        )

        try {
            rabbitTemplate.convertAndSend(EXCHANGE_NAME, SESSION_REJECTED_ROUTING_KEY, event)
            logger.info("Published attendance.session.rejected event for session: ${session.id}")
        } catch (e: Exception) {
            logger.error("Failed to publish attendance.session.rejected event for session: ${session.id}", e)
        }
    }
}

/**
 * Attendance Marked Event (Direct Marking)
 */
data class AttendanceMarkedEvent(
    val attendanceRecordId: UUID,
    val studentId: UUID,
    val classId: UUID,
    val date: java.time.LocalDate,
    val status: String,
    val markedBy: UUID?,
    override val eventId: UUID = UUID.randomUUID(),
    override val timestamp: Instant = Instant.now(),
    override val eventType: String = "AttendanceMarkedEvent"
) : BaseEvent() {
    override fun getAggregateId(): UUID = attendanceRecordId
    override fun getAggregateType(): String = "AttendanceRecord"
}

/**
 * Session Delegated Event
 */
data class SessionDelegatedEvent(
    val sessionId: UUID,
    val classId: UUID,
    val date: java.time.LocalDate,
    val delegatedTo: UUID,
    val createdBy: UUID,
    override val eventId: UUID = UUID.randomUUID(),
    override val timestamp: Instant = Instant.now(),
    override val eventType: String = "SessionDelegatedEvent"
) : BaseEvent() {
    override fun getAggregateId(): UUID = sessionId
    override fun getAggregateType(): String = "AttendanceSession"
}

/**
 * Session Collected Event
 */
data class SessionCollectedEvent(
    val sessionId: UUID,
    val classId: UUID,
    val date: java.time.LocalDate,
    val collectedBy: UUID,
    val createdBy: UUID,
    override val eventId: UUID = UUID.randomUUID(),
    override val timestamp: Instant = Instant.now(),
    override val eventType: String = "SessionCollectedEvent"
) : BaseEvent() {
    override fun getAggregateId(): UUID = sessionId
    override fun getAggregateType(): String = "AttendanceSession"
}

/**
 * Session Approved Event
 */
data class SessionApprovedEvent(
    val sessionId: UUID,
    val classId: UUID,
    val date: java.time.LocalDate,
    val approvedBy: UUID,
    val collectedBy: UUID,
    override val eventId: UUID = UUID.randomUUID(),
    override val timestamp: Instant = Instant.now(),
    override val eventType: String = "SessionApprovedEvent"
) : BaseEvent() {
    override fun getAggregateId(): UUID = sessionId
    override fun getAggregateType(): String = "AttendanceSession"
}

/**
 * Session Rejected Event
 */
data class SessionRejectedEvent(
    val sessionId: UUID,
    val classId: UUID,
    val date: java.time.LocalDate,
    val rejectedBy: UUID,
    val rejectionReason: String,
    val collectedBy: UUID,
    override val eventId: UUID = UUID.randomUUID(),
    override val timestamp: Instant = Instant.now(),
    override val eventType: String = "SessionRejectedEvent"
) : BaseEvent() {
    override fun getAggregateId(): UUID = sessionId
    override fun getAggregateType(): String = "AttendanceSession"
}

