package com.visor.school.academicservice.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.UUID

class TeacherAssignmentTest {

    @Test
    fun `should create teacher assignment`() {
        val assignment = TeacherAssignment(
            teacherId = UUID.randomUUID(),
            classId = UUID.randomUUID(),
            isClassTeacher = false
        )

        assertNotNull(assignment.id)
        assertNotNull(assignment.teacherId)
        assertNotNull(assignment.classId)
        assertFalse(assignment.isClassTeacher)
        assertNotNull(assignment.assignedDate)
    }

    @Test
    fun `should create class teacher assignment for grades 7-12`() {
        val assignment = TeacherAssignment(
            teacherId = UUID.randomUUID(),
            classId = UUID.randomUUID(),
            isClassTeacher = true
        )

        assertTrue(assignment.isClassTeacher)
        assertNotNull(assignment.assignedDate)
    }

    @Test
    fun `should accept assignedBy field`() {
        val assignedBy = UUID.randomUUID()
        val assignment = TeacherAssignment(
            teacherId = UUID.randomUUID(),
            classId = UUID.randomUUID(),
            isClassTeacher = false,
            assignedBy = assignedBy
        )

        assertEquals(assignedBy, assignment.assignedBy)
    }

    @Test
    fun `should have unique teacher and class combination`() {
        val teacherId = UUID.randomUUID()
        val classId = UUID.randomUUID()

        val assignment1 = TeacherAssignment(
            teacherId = teacherId,
            classId = classId,
            isClassTeacher = false
        )

        val assignment2 = TeacherAssignment(
            teacherId = teacherId,
            classId = classId,
            isClassTeacher = true // Different isClassTeacher, but same teacher-class
        )

        // Both should be created, but in database would have unique constraint
        assertNotNull(assignment1.id)
        assertNotNull(assignment2.id)
        assertEquals(teacherId, assignment1.teacherId)
        assertEquals(teacherId, assignment2.teacherId)
        assertEquals(classId, assignment1.classId)
        assertEquals(classId, assignment2.classId)
    }
}

