package com.visor.school.assessment.repository;

import com.visor.school.assessment.model.Assessment;
import com.visor.school.assessment.model.AssessmentStatus;
import com.visor.school.assessment.model.AssessmentType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class AssessmentRepositoryTest {

    @Autowired
    private AssessmentRepository assessmentRepository;

    @Test
    void shouldSaveAndFindAssessmentById() {
        // Given
        Assessment assessment = new Assessment(
            UUID.randomUUID(),
            "Test Assessment",
            AssessmentType.TEST,
            new BigDecimal("100.0"),
            UUID.randomUUID()
        );

        // When
        Assessment saved = assessmentRepository.save(assessment);
        Optional<Assessment> found = assessmentRepository.findById(saved.getId());

        // Then
        assertTrue(found.isPresent());
        assertEquals("Test Assessment", found.get().getName());
    }

    @Test
    void shouldFindAssessmentsByClassId() {
        // Given
        UUID classId = UUID.randomUUID();
        Assessment assessment1 = new Assessment(
            classId,
            "Test 1",
            AssessmentType.TEST,
            new BigDecimal("100.0"),
            UUID.randomUUID()
        );
        Assessment assessment2 = new Assessment(
            classId,
            "Test 2",
            AssessmentType.QUIZ,
            new BigDecimal("50.0"),
            UUID.randomUUID()
        );

        assessmentRepository.save(assessment1);
        assessmentRepository.save(assessment2);

        // When
        List<Assessment> found = assessmentRepository.findByClassId(classId);

        // Then
        assertEquals(2, found.size());
    }

    @Test
    void shouldFindAssessmentsByStatus() {
        // Given
        Assessment assessment1 = new Assessment(
            UUID.randomUUID(),
            "Draft Test",
            AssessmentType.TEST,
            new BigDecimal("100.0"),
            UUID.randomUUID()
        );
        Assessment assessment2 = new Assessment(
            UUID.randomUUID(),
            "Published Test",
            AssessmentType.TEST,
            new BigDecimal("100.0"),
            UUID.randomUUID()
        );
        assessment2.publish();

        assessmentRepository.save(assessment1);
        assessmentRepository.save(assessment2);

        // When
        List<Assessment> published = assessmentRepository.findByStatus(AssessmentStatus.PUBLISHED);

        // Then
        assertTrue(published.stream().anyMatch(a -> a.getName().equals("Published Test")));
        assertFalse(published.stream().anyMatch(a -> a.getName().equals("Draft Test")));
    }
}
