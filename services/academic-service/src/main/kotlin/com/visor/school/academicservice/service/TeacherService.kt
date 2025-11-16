package com.visor.school.academicservice.service

import com.visor.school.academicservice.model.EmploymentStatus
import com.visor.school.academicservice.model.Teacher
import com.visor.school.academicservice.repository.TeacherRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.UUID

/**
 * Teacher service
 */
@Service
@Transactional
class TeacherService(
    private val teacherRepository: TeacherRepository,
    private val employeeIdGenerator: EmployeeIdGenerator
) {
    private val logger = LoggerFactory.getLogger(TeacherService::class.java)

    /**
     * Create a new teacher
     */
    fun createTeacher(
        userId: UUID,
        qualifications: List<String>,
        subjectSpecializations: List<String>,
        hireDate: LocalDate,
        department: String? = null
    ): Teacher {
        logger.info("Creating teacher for user: $userId")

        require(subjectSpecializations.isNotEmpty()) {
            "Teacher must have at least one subject specialization"
        }

        // Check if teacher with this user ID already exists
        if (teacherRepository.findByUserId(userId).isPresent) {
            throw IllegalArgumentException("Teacher with user ID $userId already exists")
        }

        val employeeId = employeeIdGenerator.generateEmployeeId()

        val teacher = Teacher(
            employeeId = employeeId,
            userId = userId,
            qualifications = qualifications,
            subjectSpecializations = subjectSpecializations,
            hireDate = hireDate,
            department = department,
            employmentStatus = EmploymentStatus.ACTIVE
        )

        val saved = teacherRepository.save(teacher)
        logger.info("Teacher created successfully: ${saved.employeeId}")

        return saved
    }

    /**
     * Get teacher by ID
     */
    @Transactional(readOnly = true)
    fun getTeacherById(id: UUID): Teacher? {
        return teacherRepository.findById(id).orElse(null)
    }

    /**
     * Get teacher by user ID
     */
    @Transactional(readOnly = true)
    fun getTeacherByUserId(userId: UUID): Teacher? {
        return teacherRepository.findByUserId(userId).orElse(null)
    }

    /**
     * Get teacher by employee ID
     */
    @Transactional(readOnly = true)
    fun getTeacherByEmployeeId(employeeId: String): Teacher? {
        return teacherRepository.findByEmployeeId(employeeId).orElse(null)
    }

    /**
     * Get teachers by employment status
     */
    @Transactional(readOnly = true)
    fun getTeachersByStatus(status: EmploymentStatus): List<Teacher> {
        return teacherRepository.findByEmploymentStatus(status)
    }

    /**
     * Get teachers by department
     */
    @Transactional(readOnly = true)
    fun getTeachersByDepartment(department: String): List<Teacher> {
        return teacherRepository.findByDepartment(department)
    }

    /**
     * Update employment status
     */
    fun updateEmploymentStatus(id: UUID, status: EmploymentStatus): Teacher {
        val teacher = teacherRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Teacher not found: $id") }

        teacher.updateEmploymentStatus(status)
        logger.info("Updated employment status for teacher $id to $status")

        return teacherRepository.save(teacher)
    }
}

