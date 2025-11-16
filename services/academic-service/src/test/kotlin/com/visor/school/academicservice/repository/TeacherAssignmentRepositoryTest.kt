package com.visor.school.academicservice.repository

import com.visor.school.academicservice.model.TeacherAssignment
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.test.context.ActiveProfiles
import java.util.UUID

@DataJpaTest
@ActiveProfiles("test")
class TeacherAssignmentRepositoryTest @Autowired constructor(
    private val entityManager: TestEntityManager,
    private val teacherAssignmentRepository: TeacherAssignmentRepository
) {

    private lateinit var testAssignment: TeacherAssignment
    private val testTeacherId = UUID.randomUUID()
    private val testClassId = UUID.randomUUID()

    @BeforeEach
    fun setup() {
        testAssignment = TeacherAssignment(
            teacherId = testTeacherId,
            classId = testClassId,
            isClassTeacher = false
        )
        entityManager.persistAndFlush(testAssignment)
    }

    @Test
    fun `should find assignments by teacherId`() {
        val assignments = teacherAssignmentRepository.findByTeacherId(testTeacherId)

        assertTrue(assignments.isNotEmpty())
        assertTrue(assignments.all { it.teacherId == testTeacherId })
    }

    @Test
    fun `should find assignments by classId`() {
        val assignments = teacherAssignmentRepository.findByClassId(testClassId)

        assertTrue(assignments.isNotEmpty())
        assertTrue(assignments.all { it.classId == testClassId })
    }

    @Test
    fun `should find class teacher assignments`() {
        val classTeacherAssignment = TeacherAssignment(
            teacherId = UUID.randomUUID(),
            classId = testClassId,
            isClassTeacher = true
        )
        entityManager.persistAndFlush(classTeacherAssignment)

        val classTeachers = teacherAssignmentRepository.findClassTeacherByClassId(testClassId)

        assertTrue(classTeachers.isNotEmpty())
        assertTrue(classTeachers.all { it.isClassTeacher && it.classId == testClassId })
    }

    @Test
    fun `should check if assignment exists`() {
        assertTrue(teacherAssignmentRepository.existsByTeacherIdAndClassId(testTeacherId, testClassId))
        assertFalse(teacherAssignmentRepository.existsByTeacherIdAndClassId(UUID.randomUUID(), testClassId))
    }

    @Test
    fun `should find assignment by teacher and class`() {
        val assignments = teacherAssignmentRepository.findByTeacherIdAndClassId(testTeacherId, testClassId)

        assertTrue(assignments.isNotEmpty())
        assertEquals(testTeacherId, assignments.first().teacherId)
        assertEquals(testClassId, assignments.first().classId)
    }
}

