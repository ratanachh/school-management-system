package com.visor.school.search.event

import com.visor.school.search.model.SearchIndex
import com.visor.school.search.model.SearchType
import com.visor.school.search.service.SearchService
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component
import java.util.UUID

/**
 * Event consumer for updating search index
 */
@Component
class SearchEventConsumer(
    private val searchService: SearchService
) {
    private val logger = LoggerFactory.getLogger(SearchEventConsumer::class.java)

    /**
     * Handle UserCreatedEvent - Index user for search
     * Note: UserCreatedEvent is received as a generic map from RabbitMQ
     */
    @RabbitListener(queues = ["user_created_queue"])
    fun handleUserCreated(event: Map<String, Any>) {
        val userId = UUID.fromString(event["userId"].toString())
        val role = event["role"].toString()
        val firstName = event["firstName"].toString()
        val lastName = event["lastName"].toString()
        val email = event["email"].toString()
        val keycloakId = event["keycloakId"].toString()

        logger.info("Received UserCreatedEvent: $userId")

        try {
            val searchType = when (role) {
                "TEACHER" -> SearchType.TEACHER
                "STUDENT" -> SearchType.STUDENT
                else -> return // Skip indexing for other roles
            }

            val searchIndex = SearchIndex(
                id = userId,
                type = searchType,
                title = "$firstName $lastName",
                content = "$firstName $lastName - $role",
                metadata = mapOf(
                    "email" to email,
                    "role" to role,
                    "keycloakId" to keycloakId
                )
            )

            searchService.index(searchIndex)
            logger.info("Indexed user: $userId for search")
        } catch (e: Exception) {
            logger.error("Failed to index user: $userId", e)
        }
    }

    /**
     * Handle StudentEnrolledEvent - Index student for search
     * Note: Event is received as a generic map from RabbitMQ
     */
    @RabbitListener(queues = ["student_enrolled_queue"])
    fun handleStudentEnrolled(event: Map<String, Any>) {
        val studentId = UUID.fromString(event["studentId"].toString())
        val userId = UUID.fromString(event["userId"].toString())
        val studentIdNumber = event["studentIdNumber"].toString()
        val firstName = event["firstName"].toString()
        val lastName = event["lastName"].toString()
        val gradeLevel = event["gradeLevel"].toString().toInt()

        logger.info("Received StudentEnrolledEvent: $studentId")

        try {
            val searchIndex = SearchIndex(
                id = studentId,
                type = SearchType.STUDENT,
                title = "$firstName $lastName",
                content = "$firstName $lastName - Grade $gradeLevel",
                metadata = mapOf(
                    "studentId" to studentIdNumber,
                    "gradeLevel" to gradeLevel.toString(),
                    "userId" to userId.toString()
                )
            )

            searchService.index(searchIndex)
            logger.info("Indexed student: $studentId for search")
        } catch (e: Exception) {
            logger.error("Failed to index student: $studentId", e)
        }
    }

    /**
     * Handle TeacherCreatedEvent - Index teacher for search
     * Note: Event is received as a generic map from RabbitMQ
     */
    @RabbitListener(queues = ["teacher_assigned_queue"])
    fun handleTeacherCreated(event: Map<String, Any>) {
        val teacherId = UUID.fromString(event["teacherId"].toString())
        val userId = UUID.fromString(event["userId"].toString())
        val teacherIdNumber = event.getOrDefault("teacherIdNumber", "").toString()
        val firstName = event["firstName"].toString()
        val lastName = event["lastName"].toString()
        val subject = event.getOrDefault("subject", "").toString()

        logger.info("Received TeacherCreatedEvent: $teacherId")

        try {
            val searchIndex = SearchIndex(
                id = teacherId,
                type = SearchType.TEACHER,
                title = "$firstName $lastName",
                content = "$firstName $lastName - $subject",
                metadata = mapOf(
                    "teacherId" to teacherIdNumber,
                    "subject" to subject,
                    "userId" to userId.toString()
                )
            )

            searchService.index(searchIndex)
            logger.info("Indexed teacher: $teacherId for search")
        } catch (e: Exception) {
            logger.error("Failed to index teacher: $teacherId", e)
        }
    }
}

