package com.visor.school.assessment.event;

import com.visor.school.common.events.BaseEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/**
 * Event publisher for report collection events
 */
@Component
public class ReportEventPublisher {
    private static final String EXCHANGE_NAME = "academic_events";
    
    private final RabbitTemplate rabbitTemplate;

    public ReportEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Publish ReportCollectedEvent when class teacher collects exam results
     */
    public void publishReportCollectedEvent(
            UUID collectionId,
            UUID classId,
            UUID classTeacherId,
            String academicYear,
            String term) {
        ReportCollectedEvent event = new ReportCollectedEvent(
            collectionId,
            classId,
            classTeacherId,
            academicYear,
            term
        );

        rabbitTemplate.convertAndSend(EXCHANGE_NAME, "report.collected", event);
    }

    /**
     * Publish ReportSubmittedEvent when class teacher submits report to school
     */
    public void publishReportSubmittedEvent(
            UUID submissionId,
            UUID collectionId,
            UUID classId,
            UUID classTeacherId) {
        ReportSubmittedEvent event = new ReportSubmittedEvent(
            submissionId,
            collectionId,
            classId,
            classTeacherId
        );

        rabbitTemplate.convertAndSend(EXCHANGE_NAME, "report.submitted", event);
    }
}

/**
 * Report Collected Event
 */
class ReportCollectedEvent extends BaseEvent {
    private final UUID collectionId;
    private final UUID classId;
    private final UUID classTeacherId;
    private final String academicYear;
    private final String term;

    public ReportCollectedEvent(UUID collectionId, UUID classId, UUID classTeacherId,
                               String academicYear, String term) {
        super(UUID.randomUUID(), Instant.now(), "1.0");
        this.collectionId = collectionId;
        this.classId = classId;
        this.classTeacherId = classTeacherId;
        this.academicYear = academicYear;
        this.term = term;
    }

    @Override
    public String getEventType() {
        return "report.collected";
    }

    @Override
    public UUID getAggregateId() {
        return collectionId;
    }

    @Override
    public String getAggregateType() {
        return "ExamResultCollection";
    }

    // Getters
    public UUID getCollectionId() { return collectionId; }
    public UUID getClassId() { return classId; }
    public UUID getClassTeacherId() { return classTeacherId; }
    public String getAcademicYear() { return academicYear; }
    public String getTerm() { return term; }
}

/**
 * Report Submitted Event
 */
class ReportSubmittedEvent extends BaseEvent {
    private final UUID submissionId;
    private final UUID collectionId;
    private final UUID classId;
    private final UUID classTeacherId;

    public ReportSubmittedEvent(UUID submissionId, UUID collectionId, UUID classId, UUID classTeacherId) {
        super(UUID.randomUUID(), Instant.now(), "1.0");
        this.submissionId = submissionId;
        this.collectionId = collectionId;
        this.classId = classId;
        this.classTeacherId = classTeacherId;
    }

    @Override
    public String getEventType() {
        return "report.submitted";
    }

    @Override
    public UUID getAggregateId() {
        return submissionId;
    }

    @Override
    public String getAggregateType() {
        return "ReportSubmission";
    }

    // Getters
    public UUID getSubmissionId() { return submissionId; }
    public UUID getCollectionId() { return collectionId; }
    public UUID getClassId() { return classId; }
    public UUID getClassTeacherId() { return classTeacherId; }
}
