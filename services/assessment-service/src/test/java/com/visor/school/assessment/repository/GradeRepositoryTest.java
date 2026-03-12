package com.visor.school.assessment.repository;

import com.visor.school.assessment.model.Grade;
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
class GradeRepositoryTest {

    @Autowired
    private GradeRepository gradeRepository;

    @Test
    void shouldSaveAndFindGradeById() {
        // Given
        Grade grade = new Grade(
            UUID.randomUUID(),
            UUID.randomUUID(),
            new BigDecimal("85.0"),
            new BigDecimal("100.0"),
            UUID.randomUUID()
        );

        // When
        Grade saved = gradeRepository.save(grade);
        Optional<Grade> found = gradeRepository.findById(saved.getId());

        // Then
        assertTrue(found.isPresent());
        assertEquals(0, new BigDecimal("85.0").compareTo(found.get().getScore()));
    }

    @Test
    void shouldFindGradesByStudentId() {
        // Given
        UUID studentId = UUID.randomUUID();
        Grade grade1 = new Grade(
            studentId,
            UUID.randomUUID(),
            new BigDecimal("85.0"),
            new BigDecimal("100.0"),
            UUID.randomUUID()
        );
        Grade grade2 = new Grade(
            studentId,
            UUID.randomUUID(),
            new BigDecimal("90.0"),
            new BigDecimal("100.0"),
            UUID.randomUUID()
        );

        gradeRepository.save(grade1);
        gradeRepository.save(grade2);

        // When
        List<Grade> found = gradeRepository.findByStudentId(studentId);

        // Then
        assertEquals(2, found.size());
    }

    @Test
    void shouldFindGradesByAssessmentId() {
        // Given
        UUID assessmentId = UUID.randomUUID();
        Grade grade1 = new Grade(
            UUID.randomUUID(),
            assessmentId,
            new BigDecimal("85.0"),
            new BigDecimal("100.0"),
            UUID.randomUUID()
        );
        Grade grade2 = new Grade(
            UUID.randomUUID(),
            assessmentId,
            new BigDecimal("90.0"),
            new BigDecimal("100.0"),
            UUID.randomUUID()
        );

        gradeRepository.save(grade1);
        gradeRepository.save(grade2);

        // When
        List<Grade> found = gradeRepository.findByAssessmentId(assessmentId);

        // Then
        assertEquals(2, found.size());
    }

    @Test
    void shouldFindGradeByStudentAndAssessment() {
        // Given
        UUID studentId = UUID.randomUUID();
        UUID assessmentId = UUID.randomUUID();
        Grade grade = new Grade(
            studentId,
            assessmentId,
            new BigDecimal("85.0"),
            new BigDecimal("100.0"),
            UUID.randomUUID()
        );

        gradeRepository.save(grade);

        // When
        Optional<Grade> found = gradeRepository.findByStudentIdAndAssessmentId(studentId, assessmentId);

        // Then
        assertTrue(found.isPresent());
        assertEquals(0, new BigDecimal("85.0").compareTo(found.get().getScore()));
    }
}
