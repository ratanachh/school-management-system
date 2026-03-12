package com.visor.school.assessment.event;

import com.visor.school.assessment.model.Grade;
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
 * Publisher for grade-related events to RabbitMQ
 */
@Component
public class GradeEventPublisher {
    private static final Logger logger = LoggerFactory.getLogger(GradeEventPublisher.class);
    
    public static final String EXCHANGE_NAME = "school-management.exchange";
    public static final String GRADE_RECORDED_ROUTING_KEY = "assessment.grade.recorded";
    public static final String GRADE_UPDATED_ROUTING_KEY = "assessment.grade.updated";
    
    private final RabbitTemplate rabbitTemplate;
    private final String serviceName;

    public GradeEventPublisher(RabbitTemplate rabbitTemplate, 
                              @Value("${spring.application.name}") String serviceName) {
        this.rabbitTemplate = rabbitTemplate;
        this.serviceName = serviceName;
    }

    /**
     * Publish grade recorded event
     */
    public void publishGradeRecorded(Grade grade) {
        GradeRecordedEvent event = new GradeRecordedEvent(
            grade.getId(),
            grade.getStudentId(),
            grade.getAssessmentId(),
            grade.getScore(),
            grade.getTotalPoints(),
            grade.getPercentage(),
            grade.getLetterGrade(),
            grade.getRecordedBy()
        );

        try {
            rabbitTemplate.convertAndSend(EXCHANGE_NAME, GRADE_RECORDED_ROUTING_KEY, event);
            logger.info("Published assessment.grade.recorded event for grade: {}", grade.getId());
        } catch (Exception e) {
            logger.error("Failed to publish assessment.grade.recorded event for grade: {}", grade.getId(), e);
        }
    }

    /**
     * Publish grade updated event
     */
    public void publishGradeUpdated(Grade grade) {
        UUID updatedBy = grade.getUpdatedBy() != null ? grade.getUpdatedBy() : grade.getRecordedBy();
        GradeUpdatedEvent event = new GradeUpdatedEvent(
            grade.getId(),
            grade.getStudentId(),
            grade.getAssessmentId(),
            grade.getScore(),
            grade.getPercentage(),
            grade.getLetterGrade(),
            updatedBy
        );

        try {
            rabbitTemplate.convertAndSend(EXCHANGE_NAME, GRADE_UPDATED_ROUTING_KEY, event);
            logger.info("Published assessment.grade.updated event for grade: {}", grade.getId());
        } catch (Exception e) {
            logger.error("Failed to publish assessment.grade.updated event for grade: {}", grade.getId(), e);
        }
    }
}

/**
 * Grade Recorded Event
 */
class GradeRecordedEvent extends BaseEvent {
    private final UUID gradeId;
    private final UUID studentId;
    private final UUID assessmentId;
    private final BigDecimal score;
    private final BigDecimal totalPoints;
    private final BigDecimal percentage;
    private final String letterGrade;
    private final UUID recordedBy;

    public GradeRecordedEvent(UUID gradeId, UUID studentId, UUID assessmentId, 
                             BigDecimal score, BigDecimal totalPoints, BigDecimal percentage,
                             String letterGrade, UUID recordedBy) {
        super(UUID.randomUUID(), Instant.now(), "1.0");
        this.gradeId = gradeId;
        this.studentId = studentId;
        this.assessmentId = assessmentId;
        this.score = score;
        this.totalPoints = totalPoints;
        this.percentage = percentage;
        this.letterGrade = letterGrade;
        this.recordedBy = recordedBy;
    }

    @Override
    public String getEventType() {
        return "assessment.grade.recorded";
    }

    @Override
    public UUID getAggregateId() {
        return gradeId;
    }

    @Override
    public String getAggregateType() {
        return "Grade";
    }

    // Getters
    public UUID getGradeId() { return gradeId; }
    public UUID getStudentId() { return studentId; }
    public UUID getAssessmentId() { return assessmentId; }
    public BigDecimal getScore() { return score; }
    public BigDecimal getTotalPoints() { return totalPoints; }
    public BigDecimal getPercentage() { return percentage; }
    public String getLetterGrade() { return letterGrade; }
    public UUID getRecordedBy() { return recordedBy; }
}

/**
 * Grade Updated Event
 */
class GradeUpdatedEvent extends BaseEvent {
    private final UUID gradeId;
    private final UUID studentId;
    private final UUID assessmentId;
    private final BigDecimal score;
    private final BigDecimal percentage;
    private final String letterGrade;
    private final UUID updatedBy;

    public GradeUpdatedEvent(UUID gradeId, UUID studentId, UUID assessmentId,
                            BigDecimal score, BigDecimal percentage, String letterGrade, UUID updatedBy) {
        super(UUID.randomUUID(), Instant.now(), "1.0");
        this.gradeId = gradeId;
        this.studentId = studentId;
        this.assessmentId = assessmentId;
        this.score = score;
        this.percentage = percentage;
        this.letterGrade = letterGrade;
        this.updatedBy = updatedBy;
    }

    @Override
    public String getEventType() {
        return "assessment.grade.updated";
    }

    @Override
    public UUID getAggregateId() {
        return gradeId;
    }

    @Override
    public String getAggregateType() {
        return "Grade";
    }

    // Getters
    public UUID getGradeId() { return gradeId; }
    public UUID getStudentId() { return studentId; }
    public UUID getAssessmentId() { return assessmentId; }
    public BigDecimal getScore() { return score; }
    public BigDecimal getPercentage() { return percentage; }
    public String getLetterGrade() { return letterGrade; }
    public UUID getUpdatedBy() { return updatedBy; }
}
