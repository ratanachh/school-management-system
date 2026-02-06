package com.visor.school.academic.integration;

import com.visor.school.academic.config.TestConfig;
import com.visor.school.academic.model.Student;
import com.visor.school.academic.service.StudentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestConfig.class)
@Transactional
class StudentSearchIntegrationTest {

    @Autowired
    private StudentService studentService;

    @BeforeEach
    void setup() {
        studentService.enrollStudent(UUID.randomUUID(), "John", "Doe", LocalDate.of(2010, 1, 1), 5, null, null);
        studentService.enrollStudent(UUID.randomUUID(), "Jane", "Smith", LocalDate.of(2010, 6, 1), 5, null, null);
        studentService.enrollStudent(UUID.randomUUID(), "Bob", "Johnson", LocalDate.of(2006, 1, 1), 12, null, null);
    }

    @Test
    void shouldSearchStudentsByName() {
        List<Student> students = studentService.searchStudentsByName("John");
        assertFalse(students.isEmpty());
    }

    @Test
    void shouldGetStudentsByGradeLevel() {
        List<Student> grade5Students = studentService.getStudentsByGradeLevel(5);
        assertTrue(grade5Students.size() >= 2);
        assertTrue(grade5Students.stream().allMatch(s -> s.getGradeLevel() == 5));
    }
}
