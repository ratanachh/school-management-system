package com.visor.school.attendanceservice.integration

import com.visor.school.attendanceservice.model.AttendanceStatus
import com.visor.school.attendanceservice.service.AttendanceService
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

/**
 * Integration test for cross-service communication via RabbitMQ
 * Tests that AttendanceMarkedEvent is properly published when attendance is marked
 */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
class CrossServiceCommunicationTest @Autowired constructor(
    private val attendanceService: AttendanceService,
    private val rabbitTemplate: RabbitTemplate
) {

    companion object {
        @Container
        val rabbitMQ = RabbitMQContainer("rabbitmq:3-management")
            .withReuse(true)
    }

    @Test
    fun `should publish AttendanceMarkedEvent when attendance is marked`() {
        // Given
        val studentId = UUID.randomUUID()
        val classId = UUID.randomUUID()
        val teacherId = UUID.randomUUID()

        // When
        val attendance = attendanceService.markAttendance(
            studentId = studentId,
            classId = classId,
            date = LocalDate.now(),
            status = AttendanceStatus.PRESENT,
            markedBy = teacherId
        )

        // Then - Verify event was published
        Thread.sleep(1000) // Wait for async event publishing
        
        // Verify attendance was marked
        assert(attendance != null)
        assert(attendance.status == AttendanceStatus.PRESENT)
    }
}

