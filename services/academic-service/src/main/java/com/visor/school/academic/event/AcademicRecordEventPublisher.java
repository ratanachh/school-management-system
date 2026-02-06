package com.visor.school.academic.event;

import com.visor.school.academic.model.AcademicRecord;
import com.visor.school.common.events.BaseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Publisher for academic record-related events to RabbitMQ
 */
@Component
public class AcademicRecordEventPublisher {
    private static final Logger logger = LoggerFactory.getLogger(AcademicRecordEventPublisher.class);
    private static final String EXCHANGE_NAME = "school-management.exchange";
    private static final String ACADEMIC_RECORD_UPDATED_ROUTING_KEY = "academic.record.updated";

    private final RabbitTemplate rabbitTemplate;
    private final String serviceName;

    public AcademicRecordEventPublisher(
            RabbitTemplate rabbitTemplate,
            @Value("${spring.application.name}") String serviceName
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.serviceName = serviceName;
    }

    /**
     * Publish academic record updated event
     */
    public void publishAcademicRecordUpdated(AcademicRecord record) {
        AcademicRecordUpdatedEvent event = new AcademicRecordUpdatedEvent(
                record.getStudentId(),
                record.getId(),
                record.getCurrentGPA(),
                record.getCumulativeGPA(),
                record.getCreditsEarned(),
                record.getCreditsRequired(),
                record.getAcademicStanding().name()
        );

        try {
            rabbitTemplate.convertAndSend(EXCHANGE_NAME, ACADEMIC_RECORD_UPDATED_ROUTING_KEY, event);
            logger.info("Published academic.record.updated event for student: {}", record.getStudentId());
        } catch (Exception e) {
            logger.error("Failed to publish academic.record.updated event for student: {}", record.getStudentId(), e);
        }
    }
}

/**
 * Academic Record Updated Event
 */
class AcademicRecordUpdatedEvent extends BaseEvent {
    private final UUID studentId;
    private final UUID academicRecordId;
    private final BigDecimal currentGPA;
    private final BigDecimal cumulativeGPA;
    private final int creditsEarned;
    private final int creditsRequired;
    private final String academicStanding;

    public AcademicRecordUpdatedEvent(
            UUID studentId,
            UUID academicRecordId,
            BigDecimal currentGPA,
            BigDecimal cumulativeGPA,
            int creditsEarned,
            int creditsRequired,
            String academicStanding
    ) {
        super(UUID.randomUUID(), Instant.now(), "1.0", "AcademicRecordUpdatedEvent");
        this.studentId = studentId;
        this.academicRecordId = academicRecordId;
        this.currentGPA = currentGPA;
        this.cumulativeGPA = cumulativeGPA;
        this.creditsEarned = creditsEarned;
        this.creditsRequired = creditsRequired;
        this.academicStanding = academicStanding;
    }

    @Override
    public UUID getAggregateId() {
        return studentId;
    }

    @Override
    public String getAggregateType() {
        return "AcademicRecord";
    }

    public UUID getStudentId() {
        return studentId;
    }

    public UUID getAcademicRecordId() {
        return academicRecordId;
    }

    public BigDecimal getCurrentGPA() {
        return currentGPA;
    }

    public BigDecimal getCumulativeGPA() {
        return cumulativeGPA;
    }

    public int getCreditsEarned() {
        return creditsEarned;
    }

    public int getCreditsRequired() {
        return creditsRequired;
    }

    public String getAcademicStanding() {
        return academicStanding;
    }
}
