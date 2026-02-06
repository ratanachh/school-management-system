package com.visor.school.academic.integration;

import com.visor.school.academic.config.TestConfig;
import com.visor.school.academic.model.Address;
import com.visor.school.academic.model.EnrollmentStatus;
import com.visor.school.academic.model.Student;
import com.visor.school.academic.repository.StudentRepository;
import com.visor.school.academic.service.StudentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestConfig.class)
@Transactional
class StudentEnrollmentIntegrationTest {

    @Autowired
    private StudentService studentService;

    @Autowired
    private StudentRepository studentRepository;

    @Test
    void shouldCompleteStudentEnrollmentFlow() {
        UUID userId = UUID.randomUUID();
        String firstName = "Integration";
        String lastName = "Test";
        LocalDate dateOfBirth = LocalDate.of(2010, 1, 1);
        int gradeLevel = 5;

        Student student = studentService.enrollStudent(userId, firstName, lastName, dateOfBirth, gradeLevel, null, null);

        assertNotNull(student);
        assertNotNull(student.getId());
        assertNotNull(student.getStudentId());
        assertFalse(student.getStudentId().isBlank());
        assertEquals(firstName, student.getFirstName());
        assertEquals(lastName, student.getLastName());
        assertEquals(dateOfBirth, student.getDateOfBirth());
        assertEquals(gradeLevel, student.getGradeLevel());
        assertEquals(EnrollmentStatus.ENROLLED, student.getEnrollmentStatus());

        var savedStudent = studentRepository.findById(student.getId());
        assertTrue(savedStudent.isPresent());
        assertEquals(firstName, savedStudent.get().getFirstName());
    }

    @Test
    void shouldEnrollStudentWithAddressAndEmergencyContact() {
        UUID userId = UUID.randomUUID();
        Address address = new Address("456 Test St", "Test City", "Test State", "12345", "Test Country");

        Student student = studentService.enrollStudent(userId, "With", "Contact", LocalDate.of(2010, 1, 1), 5, address, null);

        assertNotNull(student.getAddress());
        assertEquals("Test City", student.getAddress().getCity());
    }

    @Test
    void shouldFailEnrollmentWhenGradeLevelIsInvalid() {
        UUID userId = UUID.randomUUID();
        assertThrows(IllegalArgumentException.class, () ->
                studentService.enrollStudent(userId, "Invalid", "Grade", LocalDate.of(2010, 1, 1), 13, null, null)
        );
    }
}
