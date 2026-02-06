package com.visor.school.academic.event;

import com.visor.school.academic.model.Student;
import com.visor.school.common.events.BaseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/**
 * Publisher for student-related events to RabbitMQ
 */
@Component
public class StudentEventPublisher {
    private static final Logger logger = LoggerFactory.getLogger(StudentEventPublisher.class);
    private static final String EXCHANGE_NAME = "school-management.exchange";
    private static final String STUDENT_ENROLLED_ROUTING_KEY = "academic.student.enrolled";
    private static final String STUDENT_UPDATED_ROUTING_KEY = "academic.student.updated";

    private final RabbitTemplate rabbitTemplate;
    private final String serviceName;

    public StudentEventPublisher(
            RabbitTemplate rabbitTemplate,
            @Value("${spring.application.name}") String serviceName
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.serviceName = serviceName;
    }

    /**
     * Publish student enrolled event
     */
    public void publishStudentEnrolled(Student student) {
        StudentEnrolledEvent event = new StudentEnrolledEvent(
                student.getId(),
                student.getUserId(),
                student.getStudentId(),
                student.getFirstName(),
                student.getLastName(),
                student.getGradeLevel()
        );

        try {
            rabbitTemplate.convertAndSend(EXCHANGE_NAME, STUDENT_ENROLLED_ROUTING_KEY, event);
            logger.info("Published student.enrolled event for student: {}", student.getId());
        } catch (Exception e) {
            logger.error("Failed to publish student.enrolled event for student: {}", student.getId(), e);
        }
    }

    /**
     * Publish student updated event
     */
    public void publishStudentUpdated(Student student) {
        StudentUpdatedEvent event = new StudentUpdatedEvent(
                student.getId(),
                student.getUserId(),
                student.getGradeLevel(),
                student.getEnrollmentStatus().name()
        );

        try {
            rabbitTemplate.convertAndSend(EXCHANGE_NAME, STUDENT_UPDATED_ROUTING_KEY, event);
            logger.info("Published student.updated event for student: {}", student.getId());
        } catch (Exception e) {
            logger.error("Failed to publish student.updated event for student: {}", student.getId(), e);
        }
    }
}

/**
 * Student Enrolled Event
 */
class StudentEnrolledEvent extends BaseEvent {
    private final UUID studentId;
    private final UUID userId;
    private final String studentIdNumber;
    private final String firstName;
    private final String lastName;
    private final int gradeLevel;

    public StudentEnrolledEvent(
            UUID studentId,
            UUID userId,
            String studentIdNumber,
            String firstName,
            String lastName,
            int gradeLevel
    ) {
        super(UUID.randomUUID(), Instant.now(), "1.0", "StudentEnrolledEvent");
        this.studentId = studentId;
        this.userId = userId;
        this.studentIdNumber = studentIdNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.gradeLevel = gradeLevel;
    }

    @Override
    public UUID getAggregateId() {
        return studentId;
    }

    @Override
    public String getAggregateType() {
        return "Student";
    }

    public UUID getStudentId() {
        return studentId;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getStudentIdNumber() {
        return studentIdNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public int getGradeLevel() {
        return gradeLevel;
    }
}

/**
 * Student Updated Event
 */
class StudentUpdatedEvent extends BaseEvent {
    private final UUID studentId;
    private final UUID userId;
    private final int gradeLevel;
    private final String enrollmentStatus;

    public StudentUpdatedEvent(
            UUID studentId,
            UUID userId,
            int gradeLevel,
            String enrollmentStatus
    ) {
        super(UUID.randomUUID(), Instant.now(), "1.0", "StudentUpdatedEvent");
        this.studentId = studentId;
        this.userId = userId;
        this.gradeLevel = gradeLevel;
        this.enrollmentStatus = enrollmentStatus;
    }

    @Override
    public UUID getAggregateId() {
        return studentId;
    }

    @Override
    public String getAggregateType() {
        return "Student";
    }

    public UUID getStudentId() {
        return studentId;
    }

    public UUID getUserId() {
        return userId;
    }

    public int getGradeLevel() {
        return gradeLevel;
    }

    public String getEnrollmentStatus() {
        return enrollmentStatus;
    }
}
