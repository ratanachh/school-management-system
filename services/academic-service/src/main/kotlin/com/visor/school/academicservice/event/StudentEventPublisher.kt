package com.visor.school.academicservice.event

import com.visor.school.common.events.BaseEvent
import com.visor.school.academicservice.model.Student
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.UUID

/**
 * Publisher for student-related events to RabbitMQ
 */
@Component
class StudentEventPublisher(
    private val rabbitTemplate: RabbitTemplate,
    @Value("\${spring.application.name}") private val serviceName: String
) {
    private val logger = LoggerFactory.getLogger(StudentEventPublisher::class.java)

    companion object {
        const val EXCHANGE_NAME = "school-management.exchange"
        const val STUDENT_ENROLLED_ROUTING_KEY = "academic.student.enrolled"
        const val STUDENT_UPDATED_ROUTING_KEY = "academic.student.updated"
    }

    /**
     * Publish student enrolled event
     */
    fun publishStudentEnrolled(student: Student) {
        val event = StudentEnrolledEvent(
            studentId = student.id!!,
            userId = student.userId,
            studentIdNumber = student.studentId,
            firstName = student.firstName,
            lastName = student.lastName,
            gradeLevel = student.gradeLevel
        )

        try {
            rabbitTemplate.convertAndSend(EXCHANGE_NAME, STUDENT_ENROLLED_ROUTING_KEY, event)
            logger.info("Published student.enrolled event for student: ${student.id}")
        } catch (e: Exception) {
            logger.error("Failed to publish student.enrolled event for student: ${student.id}", e)
        }
    }

    /**
     * Publish student updated event
     */
    fun publishStudentUpdated(student: Student) {
        val event = StudentUpdatedEvent(
            studentId = student.id!!,
            userId = student.userId,
            gradeLevel = student.gradeLevel,
            enrollmentStatus = student.enrollmentStatus.name
        )

        try {
            rabbitTemplate.convertAndSend(EXCHANGE_NAME, STUDENT_UPDATED_ROUTING_KEY, event)
            logger.info("Published student.updated event for student: ${student.id}")
        } catch (e: Exception) {
            logger.error("Failed to publish student.updated event for student: ${student.id}", e)
        }
    }
}

/**
 * Student Enrolled Event
 */
data class StudentEnrolledEvent(
    val studentId: UUID,
    val userId: UUID,
    val studentIdNumber: String,
    val firstName: String,
    val lastName: String,
    val gradeLevel: Int
) : BaseEvent() {
    override val eventType: String = "StudentEnrolledEvent"
    override fun getAggregateId(): UUID = studentId
    override fun getAggregateType(): String = "Student"
}

/**
 * Student Updated Event
 */
data class StudentUpdatedEvent(
    val studentId: UUID,
    val userId: UUID,
    val gradeLevel: Int,
    val enrollmentStatus: String
) : BaseEvent() {
    override val eventType: String = "StudentUpdatedEvent"
    override fun getAggregateId(): UUID = studentId
    override fun getAggregateType(): String = "Student"
}
