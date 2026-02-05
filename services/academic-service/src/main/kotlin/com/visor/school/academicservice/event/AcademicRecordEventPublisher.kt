package com.visor.school.academicservice.event

import com.visor.school.academicservice.model.AcademicRecord
import com.visor.school.common.events.BaseEvent
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

/**
 * Publisher for academic record-related events to RabbitMQ
 */
@Component
class AcademicRecordEventPublisher(
    private val rabbitTemplate: RabbitTemplate,
    @Value("\${spring.application.name}") private val serviceName: String
) {
    private val logger = LoggerFactory.getLogger(AcademicRecordEventPublisher::class.java)

    companion object {
        const val EXCHANGE_NAME = "school-management.exchange"
        const val ACADEMIC_RECORD_UPDATED_ROUTING_KEY = "academic.record.updated"
    }

    /**
     * Publish academic record updated event
     */
    fun publishAcademicRecordUpdated(record: AcademicRecord) {
        val event = AcademicRecordUpdatedEvent(
            studentId = record.studentId,
            academicRecordId = record.id!!,
            currentGPA = record.currentGPA,
            cumulativeGPA = record.cumulativeGPA,
            creditsEarned = record.creditsEarned,
            creditsRequired = record.creditsRequired,
            academicStanding = record.academicStanding.name
        )

        try {
            rabbitTemplate.convertAndSend(EXCHANGE_NAME, ACADEMIC_RECORD_UPDATED_ROUTING_KEY, event)
            logger.info("Published academic.record.updated event for student: ${record.studentId}")
        } catch (e: Exception) {
            logger.error("Failed to publish academic.record.updated event for student: ${record.studentId}", e)
        }
    }
}

/**
 * Academic Record Updated Event
 */
data class AcademicRecordUpdatedEvent(
    val studentId: UUID,
    val academicRecordId: UUID,
    val currentGPA: BigDecimal,
    val cumulativeGPA: BigDecimal,
    val creditsEarned: Int,
    val creditsRequired: Int,
    val academicStanding: String
) : BaseEvent() {
    override val eventType: String = "AcademicRecordUpdatedEvent"
    override fun getAggregateId(): UUID = studentId
    override fun getAggregateType(): String = "AcademicRecord"
}
