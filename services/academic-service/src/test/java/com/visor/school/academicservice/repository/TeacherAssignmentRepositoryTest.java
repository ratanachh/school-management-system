package com.visor.school.academicservice.repository;

import com.visor.school.academicservice.model.TeacherAssignment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class TeacherAssignmentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TeacherAssignmentRepository teacherAssignmentRepository;

    private TeacherAssignment testAssignment;
    private UUID testTeacherId;
    private UUID testClassId;

    @BeforeEach
    void setup() {
        testTeacherId = UUID.randomUUID();
        testClassId = UUID.randomUUID();
        testAssignment = new TeacherAssignment(testTeacherId, testClassId, false);
        entityManager.persistAndFlush(testAssignment);
    }

    @Test
    void shouldFindAssignmentsByTeacherId() {
        List<TeacherAssignment> assignments = teacherAssignmentRepository.findByTeacherId(testTeacherId);
        assertFalse(assignments.isEmpty());
        assertTrue(assignments.stream().allMatch(a -> a.getTeacherId().equals(testTeacherId)));
    }

    @Test
    void shouldFindAssignmentsByClassId() {
        List<TeacherAssignment> assignments = teacherAssignmentRepository.findByClassId(testClassId);
        assertFalse(assignments.isEmpty());
        assertTrue(assignments.stream().allMatch(a -> a.getClassId().equals(testClassId)));
    }

    @Test
    void shouldFindClassTeacherAssignments() {
        TeacherAssignment classTeacherAssignment = new TeacherAssignment(UUID.randomUUID(), testClassId, true);
        entityManager.persistAndFlush(classTeacherAssignment);

        List<TeacherAssignment> classTeachers = teacherAssignmentRepository.findClassTeacherByClassId(testClassId);
        assertFalse(classTeachers.isEmpty());
        assertTrue(classTeachers.stream().allMatch(a -> a.isClassTeacher() && a.getClassId().equals(testClassId)));
    }

    @Test
    void shouldCheckIfAssignmentExists() {
        assertTrue(teacherAssignmentRepository.existsByTeacherIdAndClassId(testTeacherId, testClassId));
        assertFalse(teacherAssignmentRepository.existsByTeacherIdAndClassId(UUID.randomUUID(), testClassId));
    }

    @Test
    void shouldFindAssignmentByTeacherAndClass() {
        List<TeacherAssignment> assignments = teacherAssignmentRepository.findByTeacherIdAndClassId(testTeacherId, testClassId);
        assertFalse(assignments.isEmpty());
        assertEquals(testTeacherId, assignments.get(0).getTeacherId());
        assertEquals(testClassId, assignments.get(0).getClassId());
    }
}
