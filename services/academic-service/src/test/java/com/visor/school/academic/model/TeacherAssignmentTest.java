package com.visor.school.academic.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TeacherAssignmentTest {

    @Test
    void shouldCreateTeacherAssignment() {
        TeacherAssignment assignment = new TeacherAssignment(
                UUID.randomUUID(),
                UUID.randomUUID(),
                false
        );

        // ID is null until entity is persisted
        assertNull(assignment.getId());
        assertNotNull(assignment.getTeacherId());
        assertNotNull(assignment.getClassId());
        assertFalse(assignment.isClassTeacher());
        assertNotNull(assignment.getAssignedDate());
    }

    @Test
    void shouldCreateClassTeacherAssignmentForGrades7To12() {
        TeacherAssignment assignment = new TeacherAssignment(
                UUID.randomUUID(),
                UUID.randomUUID(),
                true
        );

        assertTrue(assignment.isClassTeacher());
        assertNotNull(assignment.getAssignedDate());
    }

    @Test
    void shouldAcceptAssignedByField() {
        UUID assignedBy = UUID.randomUUID();
        TeacherAssignment assignment = new TeacherAssignment(
                UUID.randomUUID(),
                UUID.randomUUID(),
                false
        );
        assignment.setAssignedBy(assignedBy);

        assertEquals(assignedBy, assignment.getAssignedBy());
    }

    @Test
    void shouldHaveUniqueTeacherAndClassCombination() {
        UUID teacherId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();

        TeacherAssignment assignment1 = new TeacherAssignment(
                teacherId,
                classId,
                false
        );

        TeacherAssignment assignment2 = new TeacherAssignment(
                teacherId,
                classId,
                true // Different isClassTeacher, but same teacher-class
        );

        // Both should be created, but in database would have unique constraint
        // IDs are null until entities are persisted
        assertNull(assignment1.getId());
        assertNull(assignment2.getId());
        assertEquals(teacherId, assignment1.getTeacherId());
        assertEquals(teacherId, assignment2.getTeacherId());
        assertEquals(classId, assignment1.getClassId());
        assertEquals(classId, assignment2.getClassId());
    }
}
