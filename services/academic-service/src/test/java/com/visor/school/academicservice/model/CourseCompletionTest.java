package com.visor.school.academicservice.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CourseCompletionTest {

    @Test
    void shouldCreateCourseCompletionWithRequiredFields() {
        CourseCompletion completion = new CourseCompletion(
                "Mathematics 101",
                "Mathematics",
                9,
                "A",
                3,
                LocalDate.of(2024, 6, 15)
        );

        assertEquals("Mathematics 101", completion.getCourseName());
        assertEquals("Mathematics", completion.getSubject());
        assertEquals(9, completion.getGradeLevel());
        assertEquals("A", completion.getFinalGrade());
        assertEquals(3, completion.getCredits());
        assertEquals(LocalDate.of(2024, 6, 15), completion.getCompletionDate());
    }

    @Test
    void shouldAcceptAllGradeLevels1To12() {
        for (int grade = 1; grade <= 12; grade++) {
            CourseCompletion completion = new CourseCompletion(
                    "Test Course",
                    "Test",
                    grade,
                    "B",
                    3,
                    LocalDate.of(2024, 6, 15)
            );

            assertEquals(grade, completion.getGradeLevel());
        }
    }

    @Test
    void shouldAcceptCommonLetterGrades() {
        List<String> grades = List.of("A", "B", "C", "D", "F", "A+", "A-", "B+", "B-", "C+", "C-", "D+", "D-");

        for (String grade : grades) {
            CourseCompletion completion = new CourseCompletion(
                    "Test Course",
                    "Test",
                    9,
                    grade,
                    3,
                    LocalDate.of(2024, 6, 15)
            );

            assertEquals(grade, completion.getFinalGrade());
        }
    }

    @Test
    void shouldAcceptPositiveCredits() {
        List<Integer> credits = List.of(1, 2, 3, 4, 5, 6);

        for (Integer credit : credits) {
            CourseCompletion completion = new CourseCompletion(
                    "Test Course",
                    "Test",
                    9,
                    "B",
                    credit,
                    LocalDate.of(2024, 6, 15)
            );

            assertEquals(credit, completion.getCredits());
        }
    }
}
