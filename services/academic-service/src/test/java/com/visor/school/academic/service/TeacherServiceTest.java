package com.visor.school.academic.service;

import com.visor.school.academic.model.EmploymentStatus;
import com.visor.school.academic.model.Teacher;
import com.visor.school.academic.repository.TeacherRepository;
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
class TeacherServiceTest {

    @Mock
    private TeacherRepository teacherRepository;

    @Mock
    private EmployeeIdGenerator employeeIdGenerator;

    @InjectMocks
    private TeacherService teacherService;

    private final Teacher testTeacher = new Teacher(
            "T54321",
            UUID.randomUUID(),
            List.of("PhD in Physics"),
            List.of("Physics", "Quantum Mechanics"),
            LocalDate.of(2018, 8, 15),
            "Science",
            EmploymentStatus.ACTIVE
    );

    @Test
    void createTeacherShouldSaveAndReturnTeacher() {
        UUID userId = testTeacher.getUserId();
        Teacher savedTeacher = new Teacher(
                "T54321",
                userId,
                testTeacher.getQualifications(),
                testTeacher.getSubjectSpecializations(),
                testTeacher.getHireDate(),
                testTeacher.getDepartment(),
                testTeacher.getEmploymentStatus()
        );
        savedTeacher.setId(UUID.randomUUID());

        when(employeeIdGenerator.generateEmployeeId()).thenReturn("T54321");
        when(teacherRepository.findByUserId(any())).thenReturn(Optional.empty());
        when(teacherRepository.save(any(Teacher.class))).thenReturn(savedTeacher);

        Teacher createdTeacher = teacherService.createTeacher(
                userId,
                testTeacher.getQualifications(),
                testTeacher.getSubjectSpecializations(),
                testTeacher.getHireDate(),
                testTeacher.getDepartment()
        );

        assertNotNull(createdTeacher.getId());
        assertNotNull(createdTeacher.getEmployeeId());
        verify(teacherRepository).save(any(Teacher.class));
    }

    @Test
    void getTeacherByIdShouldReturnTeacher() {
        UUID teacherId = UUID.randomUUID();
        when(teacherRepository.findById(teacherId)).thenReturn(Optional.of(testTeacher));

        Teacher foundTeacher = teacherService.getTeacherById(teacherId);

        assertEquals(testTeacher, foundTeacher);
        verify(teacherRepository).findById(teacherId);
    }

    @Test
    void getTeachersByStatusShouldReturnAList() {
        when(teacherRepository.findByEmploymentStatus(EmploymentStatus.ACTIVE))
                .thenReturn(List.of(testTeacher));

        List<Teacher> teachers = teacherService.getTeachersByStatus(EmploymentStatus.ACTIVE);

        assertFalse(teachers.isEmpty());
        assertEquals(1, teachers.size());
        verify(teacherRepository).findByEmploymentStatus(EmploymentStatus.ACTIVE);
    }

    @Test
    void getTeachersByDepartmentShouldReturnAList() {
        when(teacherRepository.findByDepartment("Science"))
                .thenReturn(List.of(testTeacher));

        List<Teacher> teachers = teacherService.getTeachersByDepartment("Science");

        assertFalse(teachers.isEmpty());
        assertEquals(1, teachers.size());
        verify(teacherRepository).findByDepartment("Science");
    }
}
