package com.visor.school.academicservice.service;

import com.visor.school.academicservice.event.StudentEventPublisher;
import com.visor.school.academicservice.model.EnrollmentStatus;
import com.visor.school.academicservice.model.Student;
import com.visor.school.academicservice.repository.StudentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private StudentEventPublisher studentEventPublisher;

    @Mock
    private StudentIdGenerator studentIdGenerator;

    @InjectMocks
    private StudentService studentService;

    private final Student testStudent = new Student(
            "S12345",
            UUID.randomUUID(),
            "John",
            "Doe",
            LocalDate.of(2010, 5, 15),
            5,
            EnrollmentStatus.ENROLLED,
            null,
            null
    );

    @Test
    void enrollStudentShouldCreateAndPublishEvent() {
        UUID userId = testStudent.getUserId();
        Student savedStudent = new Student(
                testStudent.getStudentId(),
                userId,
                testStudent.getFirstName(),
                testStudent.getLastName(),
                testStudent.getDateOfBirth(),
                testStudent.getGradeLevel(),
                testStudent.getEnrollmentStatus(),
                null,
                null
        );
        savedStudent.setId(UUID.randomUUID());

        when(studentIdGenerator.generateStudentId()).thenReturn("S12345");
        when(studentRepository.save(any(Student.class))).thenReturn(savedStudent);

        Student enrolledStudent = studentService.enrollStudent(
                userId,
                testStudent.getFirstName(),
                testStudent.getLastName(),
                testStudent.getDateOfBirth(),
                testStudent.getGradeLevel(),
                null,
                null
        );

        assertNotNull(enrolledStudent.getStudentId());
        verify(studentRepository).save(any(Student.class));
        verify(studentEventPublisher).publishStudentEnrolled(enrolledStudent);
    }

    @Test
    void updateStudentShouldSaveAndPublishEvent() {
        UUID studentId = UUID.randomUUID();
        Student existingStudent = new Student(
                testStudent.getStudentId(),
                testStudent.getUserId(),
                testStudent.getFirstName(),
                testStudent.getLastName(),
                testStudent.getDateOfBirth(),
                testStudent.getGradeLevel(),
                testStudent.getEnrollmentStatus(),
                null,
                null
        );
        existingStudent.setId(studentId);

        Student updatedStudent = new Student(
                testStudent.getStudentId(),
                testStudent.getUserId(),
                "Jane",
                testStudent.getLastName(),
                testStudent.getDateOfBirth(),
                testStudent.getGradeLevel(),
                testStudent.getEnrollmentStatus(),
                null,
                null
        );
        updatedStudent.setId(studentId);

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(existingStudent));
        when(studentRepository.save(any(Student.class))).thenReturn(updatedStudent);

        Student result = studentService.updateStudent(studentId, "Jane", null, null, null, null);

        assertEquals("Jane", result.getFirstName());
        verify(studentRepository).save(any(Student.class));
    }

    @Test
    void searchStudentsShouldReturnAPageOfStudents() {
        when(studentRepository.searchByName("John")).thenReturn(List.of(testStudent));

        List<Student> students = studentService.searchStudentsByName("John");

        assertFalse(students.isEmpty());
        assertEquals(1, students.size());
        verify(studentRepository).searchByName("John");
    }

    @Test
    void getStudentsByGradeShouldReturnAList() {
        when(studentRepository.findByGradeLevel(5)).thenReturn(List.of(testStudent));

        List<Student> students = studentService.getStudentsByGradeLevel(5);

        assertFalse(students.isEmpty());
        assertEquals(1, students.size());
        verify(studentRepository).findByGradeLevel(5);
    }
}
