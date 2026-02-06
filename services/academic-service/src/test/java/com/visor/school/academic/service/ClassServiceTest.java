package com.visor.school.academic.service;

import com.visor.school.academic.model.ClassStatus;
import com.visor.school.academic.model.ClassType;
import com.visor.school.academic.model.EmploymentStatus;
import com.visor.school.academic.model.Teacher;
import com.visor.school.academic.model.TeacherAssignment;
import com.visor.school.academic.model.Term;
import com.visor.school.academic.repository.ClassRepository;
import com.visor.school.academic.repository.TeacherAssignmentRepository;
import com.visor.school.academic.repository.TeacherRepository;
import org.junit.jupiter.api.BeforeEach;
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
class ClassServiceTest {

    @Mock
    private ClassRepository classRepository;

    @Mock
    private TeacherRepository teacherRepository;

    @Mock
    private TeacherAssignmentRepository teacherAssignmentRepository;

    @InjectMocks
    private ClassService classService;

    private UUID testTeacherId;
    private UUID testClassId;

    @BeforeEach
    void setup() {
        testTeacherId = UUID.randomUUID();
        testClassId = UUID.randomUUID();
    }

    @Test
    void shouldCreateHomeroomClassForGrades1To6() {
        Teacher teacher = new Teacher("EMP-001", testTeacherId, List.of(), List.of("General"), LocalDate.of(2020, 1, 1), null, EmploymentStatus.ACTIVE);
        teacher.setId(testTeacherId);

        when(teacherRepository.findById(testTeacherId)).thenReturn(Optional.of(teacher));
        when(classRepository.findByAcademicYearAndTermAndTypeAndGrade(any(), any(), any(), anyInt())).thenReturn(List.of());
        when(classRepository.save(any())).thenAnswer(invocation -> invocation.getArguments()[0]);

        com.visor.school.academic.model.Class result = classService.createHomeroomClass("Grade 3 Homeroom", 3, testTeacherId, "2024-2025", Term.FIRST_TERM, null, null, LocalDate.of(2024, 9, 1), null);

        assertNotNull(result);
        assertEquals(ClassType.HOMEROOM, result.getClassType());
        assertEquals(3, result.getGradeLevel());
        assertEquals(testTeacherId, result.getHomeroomTeacherId());
        assertNull(result.getSubject());
        verify(classRepository).save(any());
    }

    @Test
    void shouldThrowExceptionForHomeroomClassWithGradeAbove6() {
        assertThrows(IllegalArgumentException.class, () ->
                classService.createHomeroomClass("Invalid Homeroom", 7, testTeacherId, "2024-2025", Term.FIRST_TERM, null, null, LocalDate.of(2024, 9, 1), null)
        );
        verify(classRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenHomeroomClassAlreadyExistsForGrade() {
        com.visor.school.academic.model.Class existingClass = new com.visor.school.academic.model.Class("Existing Homeroom", ClassType.HOMEROOM, null, 3, UUID.randomUUID(), null, "2024-2025", Term.FIRST_TERM, null, null, LocalDate.of(2024, 9, 1), null, ClassStatus.SCHEDULED);
        Teacher teacher = new Teacher("EMP-001", testTeacherId, List.of(), List.of("Mathematics"), LocalDate.of(2020, 1, 1), null, EmploymentStatus.ACTIVE);
        teacher.setId(testTeacherId);

        when(teacherRepository.findById(testTeacherId)).thenReturn(Optional.of(teacher));
        when(classRepository.findByAcademicYearAndTermAndTypeAndGrade(any(), any(), any(), anyInt())).thenReturn(List.of(existingClass));

        assertThrows(IllegalArgumentException.class, () ->
                classService.createHomeroomClass("Duplicate Homeroom", 3, testTeacherId, "2024-2025", Term.FIRST_TERM, null, null, LocalDate.of(2024, 9, 1), null)
        );
    }

    @Test
    void shouldCreateSubjectClassForAllGrades() {
        when(classRepository.save(any())).thenAnswer(invocation -> invocation.getArguments()[0]);

        com.visor.school.academic.model.Class result = classService.createSubjectClass("Mathematics 101", "Mathematics", 10, "2024-2025", Term.FIRST_TERM, null, null, LocalDate.of(2024, 9, 1), null);

        assertNotNull(result);
        assertEquals(ClassType.SUBJECT, result.getClassType());
        assertEquals("Mathematics", result.getSubject());
        assertEquals(10, result.getGradeLevel());
        assertNull(result.getHomeroomTeacherId());
        verify(classRepository).save(any());
    }

    @Test
    void shouldAssignClassTeacherForGrades7To12() {
        UUID classId = UUID.randomUUID();
        com.visor.school.academic.model.Class classEntity = new com.visor.school.academic.model.Class("Mathematics 101", ClassType.SUBJECT, "Mathematics", 10, null, null, "2024-2025", Term.FIRST_TERM, null, null, LocalDate.of(2024, 9, 1), null, ClassStatus.SCHEDULED);
        classEntity.setId(classId);

        Teacher teacher = new Teacher("EMP-001", testTeacherId, List.of(), List.of("Mathematics"), LocalDate.of(2020, 1, 1), null, EmploymentStatus.ACTIVE);
        teacher.setId(testTeacherId);

        TeacherAssignment assignment = new TeacherAssignment(testTeacherId, classId, false);

        when(classRepository.findById(classId)).thenReturn(Optional.of(classEntity));
        when(teacherRepository.findById(testTeacherId)).thenReturn(Optional.of(teacher));
        when(teacherAssignmentRepository.findByTeacherIdAndClassId(testTeacherId, classId)).thenReturn(List.of(assignment));
        when(teacherAssignmentRepository.findClassTeacherByClassId(classId)).thenReturn(List.of());

        com.visor.school.academic.model.Class result = classService.assignClassTeacher(classId, testTeacherId, null);

        assertNotNull(result);
        verify(classRepository).findById(classId);
    }

    @Test
    void shouldThrowExceptionWhenAssigningClassTeacherToGrades1To6() {
        UUID classId = UUID.randomUUID();
        com.visor.school.academic.model.Class classEntity = new com.visor.school.academic.model.Class("Grade 3 Homeroom", ClassType.HOMEROOM, null, 3, UUID.randomUUID(), null, "2024-2025", Term.FIRST_TERM, null, null, LocalDate.of(2024, 9, 1), null, ClassStatus.SCHEDULED);
        classEntity.setId(classId);

        when(classRepository.findById(classId)).thenReturn(Optional.of(classEntity));

        assertThrows(IllegalArgumentException.class, () ->
                classService.assignClassTeacher(classId, testTeacherId, null)
        );
    }

    @Test
    void shouldThrowExceptionWhenTeacherNotAssignedToClass() {
        UUID classId = UUID.randomUUID();
        com.visor.school.academic.model.Class classEntity = new com.visor.school.academic.model.Class("Mathematics 101", ClassType.SUBJECT, "Mathematics", 10, null, null, "2024-2025", Term.FIRST_TERM, null, null, LocalDate.of(2024, 9, 1), null, ClassStatus.SCHEDULED);
        classEntity.setId(classId);

        Teacher teacher = new Teacher("EMP-001", testTeacherId, List.of(), List.of("Mathematics"), LocalDate.of(2020, 1, 1), null, EmploymentStatus.ACTIVE);
        teacher.setId(testTeacherId);

        when(classRepository.findById(classId)).thenReturn(Optional.of(classEntity));
        when(teacherRepository.findById(testTeacherId)).thenReturn(Optional.of(teacher));
        when(teacherAssignmentRepository.findByTeacherIdAndClassId(testTeacherId, classId)).thenReturn(List.of());

        assertThrows(IllegalArgumentException.class, () ->
                classService.assignClassTeacher(classId, testTeacherId, null)
        );
    }

    @Test
    void shouldUpdateClassStatus() {
        UUID classId = UUID.randomUUID();
        com.visor.school.academic.model.Class classEntity = new com.visor.school.academic.model.Class("Test Class", ClassType.SUBJECT, "Mathematics", 5, null, null, "2024-2025", Term.FIRST_TERM, null, null, LocalDate.of(2024, 9, 1), null, ClassStatus.SCHEDULED);
        classEntity.setId(classId);

        when(classRepository.findById(classId)).thenReturn(Optional.of(classEntity));
        when(classRepository.save(any())).thenAnswer(invocation -> invocation.getArguments()[0]);

        com.visor.school.academic.model.Class result = classService.updateClassStatus(classId, ClassStatus.IN_PROGRESS);

        assertEquals(ClassStatus.IN_PROGRESS, result.getStatus());
        verify(classRepository).save(classEntity);
    }
}
