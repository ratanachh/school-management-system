package com.visor.school.attendance.event;

import com.visor.school.common.events.BaseEvent;
import com.visor.school.attendance.model.AttendanceRecord;
import com.visor.school.attendance.model.AttendanceSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Publisher for attendance-related events to RabbitMQ
 */
@Component
public class AttendanceEventPublisher {
    private static final Logger logger = LoggerFactory.getLogger(AttendanceEventPublisher.class);

    public static final String EXCHANGE_NAME = "school-management.exchange";
    public static final String ATTENDANCE_MARKED_ROUTING_KEY = "attendance.marked";
    public static final String SESSION_DELEGATED_ROUTING_KEY = "attendance.session.delegated";
    public static final String SESSION_COLLECTED_ROUTING_KEY = "attendance.session.collected";
    public static final String SESSION_APPROVED_ROUTING_KEY = "attendance.session.approved";
    public static final String SESSION_REJECTED_ROUTING_KEY = "attendance.session.rejected";

    private final RabbitTemplate rabbitTemplate;
    private final String serviceName;

    public AttendanceEventPublisher(
            RabbitTemplate rabbitTemplate,
            @Value("${spring.application.name}") String serviceName) {
        this.rabbitTemplate = rabbitTemplate;
        this.serviceName = serviceName;
    }

    /**
     * Publish attendance marked event (direct marking)
     */
    public void publishAttendanceMarked(AttendanceRecord record) {
        AttendanceMarkedEvent event = new AttendanceMarkedEvent(
            record.getId(),
            record.getStudentId(),
            record.getClassId(),
            record.getDate(),
            record.getStatus().name(),
            record.getMarkedBy()
        );

        try {
            rabbitTemplate.convertAndSend(EXCHANGE_NAME, ATTENDANCE_MARKED_ROUTING_KEY, event);
            logger.info("Published attendance.marked event for record: {}", record.getId());
        } catch (Exception e) {
            logger.error("Failed to publish attendance.marked event for record: {}", record.getId(), e);
        }
    }

    /**
     * Publish session delegated event
     */
    public void publishSessionDelegated(AttendanceSession session, UUID classLeaderId) {
        SessionDelegatedEvent event = new SessionDelegatedEvent(
            session.getId(),
            session.getClassId(),
            session.getDate(),
            classLeaderId,
            session.getCreatedBy()
        );

        try {
            rabbitTemplate.convertAndSend(EXCHANGE_NAME, SESSION_DELEGATED_ROUTING_KEY, event);
            logger.info("Published attendance.session.delegated event for session: {}", session.getId());
        } catch (Exception e) {
            logger.error("Failed to publish attendance.session.delegated event for session: {}", session.getId(), e);
        }
    }

    /**
     * Publish session collected event
     */
    public void publishSessionCollected(AttendanceSession session) {
        if (session.getDelegatedTo() == null) {
            throw new IllegalStateException("Session must be delegated before collection");
        }

        SessionCollectedEvent event = new SessionCollectedEvent(
            session.getId(),
            session.getClassId(),
            session.getDate(),
            session.getDelegatedTo(),
            session.getCreatedBy()
        );

        try {
            rabbitTemplate.convertAndSend(EXCHANGE_NAME, SESSION_COLLECTED_ROUTING_KEY, event);
            logger.info("Published attendance.session.collected event for session: {}", session.getId());
        } catch (Exception e) {
            logger.error("Failed to publish attendance.session.collected event for session: {}", session.getId(), e);
        }
    }

    /**
     * Publish session approved event
     */
    public void publishSessionApproved(AttendanceSession session, UUID approvedBy) {
        SessionApprovedEvent event = new SessionApprovedEvent(
            session.getId(),
            session.getClassId(),
            session.getDate(),
            approvedBy,
            session.getDelegatedTo()
        );

        try {
            rabbitTemplate.convertAndSend(EXCHANGE_NAME, SESSION_APPROVED_ROUTING_KEY, event);
            logger.info("Published attendance.session.approved event for session: {}", session.getId());
        } catch (Exception e) {
            logger.error("Failed to publish attendance.session.approved event for session: {}", session.getId(), e);
        }
    }

    /**
     * Publish session rejected event
     */
    public void publishSessionRejected(AttendanceSession session, UUID rejectedBy, String reason) {
        SessionRejectedEvent event = new SessionRejectedEvent(
            session.getId(),
            session.getClassId(),
            session.getDate(),
            rejectedBy,
            reason,
            session.getDelegatedTo()
        );

        try {
            rabbitTemplate.convertAndSend(EXCHANGE_NAME, SESSION_REJECTED_ROUTING_KEY, event);
            logger.info("Published attendance.session.rejected event for session: {}", session.getId());
        } catch (Exception e) {
            logger.error("Failed to publish attendance.session.rejected event for session: {}", session.getId(), e);
        }
    }

    // Event classes
    public static class AttendanceMarkedEvent extends BaseEvent {
        private final UUID attendanceRecordId;
        private final UUID studentId;
        private final UUID classId;
        private final LocalDate date;
        private final String status;
        private final UUID markedBy;

        public AttendanceMarkedEvent(UUID attendanceRecordId, UUID studentId, UUID classId,
                                    LocalDate date, String status, UUID markedBy) {
            super(UUID.randomUUID(), Instant.now(), "1.0", "AttendanceMarkedEvent");
            this.attendanceRecordId = attendanceRecordId;
            this.studentId = studentId;
            this.classId = classId;
            this.date = date;
            this.status = status;
            this.markedBy = markedBy;
        }

        @Override
        public UUID getAggregateId() {
            return attendanceRecordId;
        }

        @Override
        public String getAggregateType() {
            return "AttendanceRecord";
        }

        public UUID getAttendanceRecordId() {
            return attendanceRecordId;
        }

        public UUID getStudentId() {
            return studentId;
        }

        public UUID getClassId() {
            return classId;
        }

        public LocalDate getDate() {
            return date;
        }

        public String getStatus() {
            return status;
        }

        public UUID getMarkedBy() {
            return markedBy;
        }
    }

    public static class SessionDelegatedEvent extends BaseEvent {
        private final UUID sessionId;
        private final UUID classId;
        private final LocalDate date;
        private final UUID delegatedTo;
        private final UUID createdBy;

        public SessionDelegatedEvent(UUID sessionId, UUID classId, LocalDate date,
                                     UUID delegatedTo, UUID createdBy) {
            super(UUID.randomUUID(), Instant.now(), "1.0", "SessionDelegatedEvent");
            this.sessionId = sessionId;
            this.classId = classId;
            this.date = date;
            this.delegatedTo = delegatedTo;
            this.createdBy = createdBy;
        }

        @Override
        public UUID getAggregateId() {
            return sessionId;
        }

        @Override
        public String getAggregateType() {
            return "AttendanceSession";
        }

        public UUID getSessionId() {
            return sessionId;
        }

        public UUID getClassId() {
            return classId;
        }

        public LocalDate getDate() {
            return date;
        }

        public UUID getDelegatedTo() {
            return delegatedTo;
        }

        public UUID getCreatedBy() {
            return createdBy;
        }
    }

    public static class SessionCollectedEvent extends BaseEvent {
        private final UUID sessionId;
        private final UUID classId;
        private final LocalDate date;
        private final UUID collectedBy;
        private final UUID createdBy;

        public SessionCollectedEvent(UUID sessionId, UUID classId, LocalDate date,
                                     UUID collectedBy, UUID createdBy) {
            super(UUID.randomUUID(), Instant.now(), "1.0", "SessionCollectedEvent");
            this.sessionId = sessionId;
            this.classId = classId;
            this.date = date;
            this.collectedBy = collectedBy;
            this.createdBy = createdBy;
        }

        @Override
        public UUID getAggregateId() {
            return sessionId;
        }

        @Override
        public String getAggregateType() {
            return "AttendanceSession";
        }

        public UUID getSessionId() {
            return sessionId;
        }

        public UUID getClassId() {
            return classId;
        }

        public LocalDate getDate() {
            return date;
        }

        public UUID getCollectedBy() {
            return collectedBy;
        }

        public UUID getCreatedBy() {
            return createdBy;
        }
    }

    public static class SessionApprovedEvent extends BaseEvent {
        private final UUID sessionId;
        private final UUID classId;
        private final LocalDate date;
        private final UUID approvedBy;
        private final UUID collectedBy;

        public SessionApprovedEvent(UUID sessionId, UUID classId, LocalDate date,
                                   UUID approvedBy, UUID collectedBy) {
            super(UUID.randomUUID(), Instant.now(), "1.0", "SessionApprovedEvent");
            this.sessionId = sessionId;
            this.classId = classId;
            this.date = date;
            this.approvedBy = approvedBy;
            this.collectedBy = collectedBy;
        }

        @Override
        public UUID getAggregateId() {
            return sessionId;
        }

        @Override
        public String getAggregateType() {
            return "AttendanceSession";
        }

        public UUID getSessionId() {
            return sessionId;
        }

        public UUID getClassId() {
            return classId;
        }

        public LocalDate getDate() {
            return date;
        }

        public UUID getApprovedBy() {
            return approvedBy;
        }

        public UUID getCollectedBy() {
            return collectedBy;
        }
    }

    public static class SessionRejectedEvent extends BaseEvent {
        private final UUID sessionId;
        private final UUID classId;
        private final LocalDate date;
        private final UUID rejectedBy;
        private final String rejectionReason;
        private final UUID collectedBy;

        public SessionRejectedEvent(UUID sessionId, UUID classId, LocalDate date,
                                   UUID rejectedBy, String rejectionReason, UUID collectedBy) {
            super(UUID.randomUUID(), Instant.now(), "1.0", "SessionRejectedEvent");
            this.sessionId = sessionId;
            this.classId = classId;
            this.date = date;
            this.rejectedBy = rejectedBy;
            this.rejectionReason = rejectionReason;
            this.collectedBy = collectedBy;
        }

        @Override
        public UUID getAggregateId() {
            return sessionId;
        }

        @Override
        public String getAggregateType() {
            return "AttendanceSession";
        }

        public UUID getSessionId() {
            return sessionId;
        }

        public UUID getClassId() {
            return classId;
        }

        public LocalDate getDate() {
            return date;
        }

        public UUID getRejectedBy() {
            return rejectedBy;
        }

        public String getRejectionReason() {
            return rejectionReason;
        }

        public UUID getCollectedBy() {
            return collectedBy;
        }
    }
}
