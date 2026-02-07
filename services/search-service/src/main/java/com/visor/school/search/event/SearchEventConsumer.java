package com.visor.school.search.event;

import com.visor.school.search.model.SearchIndex;
import com.visor.school.search.model.SearchType;
import com.visor.school.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/**
 * Event consumer for updating search index
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class SearchEventConsumer {

    private final SearchService searchService;

    /**
     * Handle UserCreatedEvent - Index user for search
     * Note: UserCreatedEvent is received as a generic map from RabbitMQ
     */
    @RabbitListener(queues = "user_created_queue")
    public void handleUserCreated(Map<String, Object> event) {
        try {
            UUID userId = UUID.fromString(event.get("userId").toString());
            String role = event.get("role").toString();
            String firstName = event.get("firstName").toString();
            String lastName = event.get("lastName").toString();
            String email = event.get("email").toString();
            String keycloakId = event.get("keycloakId").toString();

            log.info("Received UserCreatedEvent: {}", userId);

            SearchType searchType;
            if ("TEACHER".equals(role)) {
                searchType = SearchType.TEACHER;
            } else if ("STUDENT".equals(role)) {
                searchType = SearchType.STUDENT;
            } else {
                return; // Skip indexing for other roles
            }

            SearchIndex searchIndex = SearchIndex.builder()
                    .id(userId)
                    .type(searchType)
                    .title(firstName + " " + lastName)
                    .content(firstName + " " + lastName + " - " + role)
                    .metadata(Map.of(
                            "email", email,
                            "role", role,
                            "keycloakId", keycloakId))
                    .build();

            searchService.index(searchIndex);
            log.info("Indexed user: {} for search", userId);
        } catch (Exception e) {
            log.error("Failed to index user from event: {}", event, e);
        }
    }

    /**
     * Handle StudentEnrolledEvent - Index student for search
     * Note: Event is received as a generic map from RabbitMQ
     */
    @RabbitListener(queues = "student_enrolled_queue")
    public void handleStudentEnrolled(Map<String, Object> event) {
        try {
            UUID studentId = UUID.fromString(event.get("studentId").toString());
            UUID userId = UUID.fromString(event.get("userId").toString());
            String studentIdNumber = event.get("studentIdNumber").toString();
            String firstName = event.get("firstName").toString();
            String lastName = event.get("lastName").toString();
            int gradeLevel = Integer.parseInt(event.get("gradeLevel").toString());

            log.info("Received StudentEnrolledEvent: {}", studentId);

            SearchIndex searchIndex = SearchIndex.builder()
                    .id(studentId)
                    .type(SearchType.STUDENT)
                    .title(firstName + " " + lastName)
                    .content(firstName + " " + lastName + " - Grade " + gradeLevel)
                    .metadata(Map.of(
                            "studentId", studentIdNumber,
                            "gradeLevel", String.valueOf(gradeLevel),
                            "userId", userId.toString()))
                    .build();

            searchService.index(searchIndex);
            log.info("Indexed student: {} for search", studentId);
        } catch (Exception e) {
            log.error("Failed to index student from event: {}", event, e);
        }
    }

    /**
     * Handle TeacherCreatedEvent - Index teacher for search
     * Note: Event is received as a generic map from RabbitMQ
     */
    @RabbitListener(queues = "teacher_assigned_queue")
    public void handleTeacherCreated(Map<String, Object> event) {
        try {
            UUID teacherId = UUID.fromString(event.get("teacherId").toString());
            UUID userId = UUID.fromString(event.get("userId").toString());
            String teacherIdNumber = event.getOrDefault("teacherIdNumber", "").toString();
            String firstName = event.get("firstName").toString();
            String lastName = event.get("lastName").toString();
            String subject = event.getOrDefault("subject", "").toString();

            log.info("Received TeacherCreatedEvent: {}", teacherId);

            SearchIndex searchIndex = SearchIndex.builder()
                    .id(teacherId)
                    .type(SearchType.TEACHER)
                    .title(firstName + " " + lastName)
                    .content(firstName + " " + lastName + " - " + subject)
                    .metadata(Map.of(
                            "teacherId", teacherIdNumber,
                            "subject", subject,
                            "userId", userId.toString()))
                    .build();

            searchService.index(searchIndex);
            log.info("Indexed teacher: {} for search", teacherId);
        } catch (Exception e) {
            log.error("Failed to index teacher from event: {}", event, e);
        }
    }
}
