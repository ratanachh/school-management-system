package com.visor.school.assessment.event

import com.visor.school.assessment.model.Grade
import com.visor.school.common.events.BaseEvent
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

/**
 * Publisher for grade-related events to RabbitMQ
 */
@Component
class GradeEventPublisher(
    private val rabbitTemplate: RabbitTemplate,
    @Value("\${spring.application.name}") private val serviceName: String
) {
    private val logger = LoggerFactory.getLogger(GradeEventPublisher::class.java)

    companion object {
        const val EXCHANGE_NAME = "school-management.exchange"
        const val GRADE_RECORDED_ROUTING_KEY = "assessment.grade.recorded"
        const val GRADE_UPDATED_ROUTING_KEY = "assessment.grade.updated"
    }

    /**
     * Publish grade recorded event
     */
    fun publishGradeRecorded(grade: Grade) {
        val event = GradeRecordedEvent(
            gradeId = grade.id,
            studentId = grade.studentId,
            assessmentId = grade.assessmentId,
            score = grade.score,
            totalPoints = grade.totalPoints,
            percentage = grade.percentage,
            letterGrade = grade.letterGrade,
            recordedBy = grade.recordedBy
        )

        try {
            rabbitTemplate.convertAndSend(EXCHANGE_NAME, GRADE_RECORDED_ROUTING_KEY, event)
            logger.info("Published assessment.grade.recorded event for grade: ${grade.id}")
        } catch (e: Exception) {
            logger.error("Failed to publish assessment.grade.recorded event for grade: ${grade.id}", e)
        }
    }

    /**
     * Publish grade updated event
     */
    fun publishGradeUpdated(grade: Grade) {
        val event = GradeUpdatedEvent(
            gradeId = grade.id,
            studentId = grade.studentId,
            assessmentId = grade.assessmentId,
            score = grade.score,
            percentage = grade.percentage,
            letterGrade = grade.letterGrade,
            updatedBy = grade.updatedBy ?: grade.recordedBy
        )

        try {
            rabbitTemplate.convertAndSend(EXCHANGE_NAME, GRADE_UPDATED_ROUTING_KEY, event)
            logger.info("Published assessment.grade.updated event for grade: ${grade.id}")
        } catch (e: Exception) {
            logger.error("Failed to publish assessment.grade.updated event for grade: ${grade.id}", e)
        }
    }
}

/**
 * Grade Recorded Event
 */
data class GradeRecordedEvent(
    val gradeId: UUID,
    val studentId: UUID,
    val assessmentId: UUID,
    val score: BigDecimal,
    val totalPoints: BigDecimal,
    val percentage: BigDecimal,
    val letterGrade: String?,
    val recordedBy: UUID,
    val eventIdOverride: UUID = UUID.randomUUID(),
    val eventTimestamp: Instant = Instant.now(),
    val eventVersion: String = "1.0"
) : BaseEvent(eventIdOverride, eventTimestamp, eventVersion) {
    override fun getAggregateId(): UUID = gradeId
    override fun getAggregateType(): String = "Grade"
}

/**
 * Grade Updated Event
 */
data class GradeUpdatedEvent(
    val gradeId: UUID,
    val studentId: UUID,
    val assessmentId: UUID,
    val score: BigDecimal,
    val percentage: BigDecimal,
    val letterGrade: String?,
    val updatedBy: UUID,
    val eventIdOverride: UUID = UUID.randomUUID(),
    val eventTimestamp: Instant = Instant.now(),
    val eventVersion: String = "1.0"
) : BaseEvent(eventIdOverride, eventTimestamp, eventVersion) {
    override fun getAggregateId(): UUID = gradeId
    override fun getAggregateType(): String = "Grade"
}

