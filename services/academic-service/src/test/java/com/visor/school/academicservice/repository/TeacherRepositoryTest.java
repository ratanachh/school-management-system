package com.visor.school.academicservice.repository;

import com.visor.school.academicservice.model.EmploymentStatus;
import com.visor.school.academicservice.model.Teacher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class TeacherRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TeacherRepository teacherRepository;

    private Teacher testTeacher;

    @BeforeEach
    void setup() {
        testTeacher = new Teacher("EMP-TEST-001", UUID.randomUUID(), List.of(), List.of("Mathematics", "Physics"), LocalDate.of(2020, 1, 1), null, EmploymentStatus.ACTIVE);
        entityManager.persistAndFlush(testTeacher);
    }

    @Test
    void shouldFindTeacherByEmployeeId() {
        Optional<Teacher> found = teacherRepository.findByEmployeeId("EMP-TEST-001");
        assertTrue(found.isPresent());
        assertEquals("EMP-TEST-001", found.get().getEmployeeId());
        assertEquals(2, found.get().getSubjectSpecializations().size());
    }

    @Test
    void shouldFindTeacherByUserId() {
        Optional<Teacher> found = teacherRepository.findByUserId(testTeacher.getUserId());
        assertTrue(found.isPresent());
        assertEquals(testTeacher.getUserId(), found.get().getUserId());
        assertEquals("EMP-TEST-001", found.get().getEmployeeId());
    }

    @Test
    void shouldFindTeachersByEmploymentStatus() {
        Teacher onLeaveTeacher = new Teacher("EMP-TEST-002", UUID.randomUUID(), List.of(), List.of("English"), LocalDate.of(2020, 1, 1), null, EmploymentStatus.ON_LEAVE);
        entityManager.persistAndFlush(onLeaveTeacher);

        List<Teacher> activeTeachers = teacherRepository.findByEmploymentStatus(EmploymentStatus.ACTIVE);
        List<Teacher> onLeaveTeachers = teacherRepository.findByEmploymentStatus(EmploymentStatus.ON_LEAVE);

        assertFalse(activeTeachers.isEmpty());
        assertTrue(activeTeachers.stream().allMatch(t -> t.getEmploymentStatus() == EmploymentStatus.ACTIVE));
        assertFalse(onLeaveTeachers.isEmpty());
        assertTrue(onLeaveTeachers.stream().allMatch(t -> t.getEmploymentStatus() == EmploymentStatus.ON_LEAVE));
    }

    @Test
    void shouldFindTeachersByDepartment() {
        Teacher scienceTeacher = new Teacher("EMP-TEST-003", UUID.randomUUID(), List.of(), List.of("Chemistry"), LocalDate.of(2020, 1, 1), "Science", EmploymentStatus.ACTIVE);
        entityManager.persistAndFlush(scienceTeacher);

        List<Teacher> scienceTeachers = teacherRepository.findByDepartment("Science");
        assertFalse(scienceTeachers.isEmpty());
        assertTrue(scienceTeachers.stream().allMatch(t -> "Science".equals(t.getDepartment())));
    }

    @Test
    void shouldReturnEmptyWhenTeacherNotFoundByEmployeeId() {
        Optional<Teacher> found = teacherRepository.findByEmployeeId("NONEXISTENT");
        assertFalse(found.isPresent());
    }

    @Test
    void shouldReturnEmptyWhenTeacherNotFoundByUserId() {
        Optional<Teacher> found = teacherRepository.findByUserId(UUID.randomUUID());
        assertFalse(found.isPresent());
    }
}
