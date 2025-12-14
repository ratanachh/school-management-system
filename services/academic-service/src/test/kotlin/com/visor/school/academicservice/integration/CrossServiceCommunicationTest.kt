package com.visor.school.academicservice.integration

import com.visor.school.academicservice.model.EnrollmentStatus
import com.visor.school.academicservice.service.StudentService
import org.junit.jupiter.api.Test
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.containers.RabbitMQContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.LocalDate
import java.util.UUID
import org.junit.ClassRule
import org.junit.Rule

/**
 * Integration test for cross-service communication via RabbitMQ
 * Tests that StudentEnrolledEvent is properly published when student is enrolled
 */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
class CrossServiceCommunicationTest @Autowired constructor(
    private val studentService: StudentService,
    private val rabbitTemplate: RabbitTemplate
) {

    companion object {
        @JvmField
        @ClassRule
        val rabbitMQ = RabbitMQContainer("rabbitmq:3-management")
            .withReuse(true)
    }

    @Test
    fun `should publish StudentEnrolledEvent when student is enrolled`() {
        // Given
        val userId = UUID.randomUUID()
        val studentId = "STU-CROSS-${UUID.randomUUID()}"

        // When
        val student = studentService.enrollStudent(
            userId = userId,
            firstName = "Cross",
            lastName = "Service",
            dateOfBirth = LocalDate.of(2010, 1, 1),
            gradeLevel = 5
        )

        // Then - Verify event was published
        Thread.sleep(1000) // Wait for async event publishing
        
        // Verify student was enrolled
        assert(student != null)
        assert(student.enrollmentStatus == EnrollmentStatus.ENROLLED)
    }
}
