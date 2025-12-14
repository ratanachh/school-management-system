package com.visor.school.assessment.event

import com.visor.school.common.events.BaseEvent
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.UUID

/**
 * Event publisher for report collection events
 */
@Component
class ReportEventPublisher(
    private val rabbitTemplate: RabbitTemplate
) {
    private val exchangeName = "academic_events"

    /**
     * Publish ReportCollectedEvent when class teacher collects exam results
     */
    fun publishReportCollectedEvent(
        collectionId: UUID,
        classId: UUID,
        classTeacherId: UUID,
        academicYear: String,
        term: String
    ) {
        val event = ReportCollectedEvent(
            collectionId = collectionId,
            classId = classId,
            classTeacherId = classTeacherId,
            academicYear = academicYear,
            term = term
        )

        rabbitTemplate.convertAndSend(exchangeName, "report.collected", event)
    }

    /**
     * Publish ReportSubmittedEvent when class teacher submits report to school
     */
    fun publishReportSubmittedEvent(
        submissionId: UUID,
        collectionId: UUID,
        classId: UUID,
        classTeacherId: UUID
    ) {
        val event = ReportSubmittedEvent(
            submissionId = submissionId,
            collectionId = collectionId,
            classId = classId,
            classTeacherId = classTeacherId
        )

        rabbitTemplate.convertAndSend(exchangeName, "report.submitted", event)
    }
}

/**
 * Report Collected Event
 */
data class ReportCollectedEvent(
    val collectionId: UUID,
    val classId: UUID,
    val classTeacherId: UUID,
    val academicYear: String,
    val term: String,
    val eventIdOverride: UUID = UUID.randomUUID(),
    val eventTimestamp: Instant = Instant.now(),
    val eventVersion: String = "1.0"
) : BaseEvent(eventIdOverride, eventTimestamp, eventVersion) {
    override val eventType: String = "report.collected"
    override fun getAggregateId(): UUID = collectionId
    override fun getAggregateType(): String = "ExamResultCollection"
}

/**
 * Report Submitted Event
 */
data class ReportSubmittedEvent(
    val submissionId: UUID,
    val collectionId: UUID,
    val classId: UUID,
    val classTeacherId: UUID,
    val eventIdOverride: UUID = UUID.randomUUID(),
    val eventTimestamp: Instant = Instant.now(),
    val eventVersion: String = "1.0"
) : BaseEvent(eventIdOverride, eventTimestamp, eventVersion) {
    override val eventType: String = "report.submitted"
    override fun getAggregateId(): UUID = submissionId
    override fun getAggregateType(): String = "ReportSubmission"
}
