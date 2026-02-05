package com.visor.school.academicservice.integration;

import com.visor.school.academicservice.config.TestConfig;
import com.visor.school.academicservice.model.ClassStatus;
import com.visor.school.academicservice.model.ClassType;
import com.visor.school.academicservice.model.EmploymentStatus;
import com.visor.school.academicservice.model.Teacher;
import com.visor.school.academicservice.model.Term;
import com.visor.school.academicservice.repository.TeacherRepository;
import com.visor.school.academicservice.service.ClassService;
import com.visor.school.academicservice.service.TeacherService;
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
class TeacherAssignmentIntegrationTest {

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private ClassService classService;

    @Autowired
    private TeacherRepository teacherRepository;

    @Test
    void shouldCompleteTeacherCreationAndAssignmentFlow() {
        UUID userId = UUID.randomUUID();
        Teacher teacher = teacherService.createTeacher(userId, List.of("Bachelor's Degree"), List.of("Mathematics", "Physics"), LocalDate.of(2020, 1, 1), "Science");

        assertNotNull(teacher);
        assertNotNull(teacher.getEmployeeId());
        assertEquals(EmploymentStatus.ACTIVE, teacher.getEmploymentStatus());

        com.visor.school.academicservice.model.Class classEntity = classService.createSubjectClass("Mathematics 101", "Mathematics", 10, "2024-2025", Term.FIRST_TERM, null, null, LocalDate.of(2024, 9, 1), null);

        assertNotNull(classEntity);
        assertEquals(ClassType.SUBJECT, classEntity.getClassType());
        assertEquals(10, classEntity.getGradeLevel());

        var savedTeacher = teacherRepository.findById(teacher.getId());
        assertTrue(savedTeacher.isPresent());
    }
}
