package com.visor.school.academicservice.integration;
import com.visor.school.academicservice.config.TestConfig;
import com.visor.school.academicservice.model.ClassStatus;
import com.visor.school.academicservice.model.ClassType;
import com.visor.school.academicservice.model.EmploymentStatus;
import com.visor.school.academicservice.model.Teacher;
import com.visor.school.academicservice.model.Term;
import com.visor.school.academicservice.repository.ClassRepository;
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
class HomeroomClassIntegrationTest {
    @Autowired private TeacherService teacherService;
    @Autowired private ClassService classService;
    @Autowired private ClassRepository classRepository;
    @Test
    void shouldCreateHomeroomClassForGrade1() {
        UUID userId = UUID.randomUUID();
        Teacher teacher = teacherService.createTeacher(userId, List.of("Cert"), List.of("General"), LocalDate.of(2020, 1, 1), "");
        com.visor.school.academicservice.model.Class homeroomClass = classService.createHomeroomClass("Grade 1 Homeroom", 1, teacher.getId(), "2024-2025", Term.FIRST_TERM, null, null, LocalDate.of(2024, 9, 1), null);
        assertNotNull(homeroomClass);
        assertEquals(ClassType.HOMEROOM, homeroomClass.getClassType());
        assertEquals(1, homeroomClass.getGradeLevel());
        var saved = classRepository.findById(homeroomClass.getId());
        assertTrue(saved.isPresent());
    }
}
