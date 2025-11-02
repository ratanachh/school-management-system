package com.visor.school.user.service

import com.visor.school.common.exception.ConflictException
import com.visor.school.user.domain.model.Teacher
import com.visor.school.user.domain.model.TeacherStatus
import com.visor.school.user.repository.TeacherRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class TeacherService(
    private val teacherRepository: TeacherRepository
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional(readOnly = true)
    fun findById(id: UUID): Teacher {
        logger.debug("Finding teacher by id: {}", id)
        return teacherRepository.findByIdOrThrow(id)
    }

    @Transactional(readOnly = true)
    fun findByEmployeeNumber(employeeNumber: String): Teacher? {
        logger.debug("Finding teacher by number: {}", employeeNumber)
        return teacherRepository.findByEmployeeNumber(employeeNumber).orElse(null)
    }

    @Transactional(readOnly = true)
    fun findAll(page: Int, size: Int): Page<Teacher> {
        logger.debug("Finding all teachers, page: {}, size: {}", page, size)
        val pageRequest = PageRequest.of(page, size.coerceAtMost(20))
        return teacherRepository.findAll(pageRequest)
    }

    @Transactional(readOnly = true)
    fun search(searchTerm: String): List<Teacher> {
        logger.debug("Searching teachers with term: {}", searchTerm)
        return teacherRepository.search(searchTerm)
    }

    @Transactional
    fun create(teacher: Teacher): Teacher {
        logger.info("Creating teacher with number: {}", teacher.employeeNumber)
        
        // Check if employee number already exists
        if (teacherRepository.existsByEmployeeNumber(teacher.employeeNumber)) {
            throw ConflictException("Teacher with number ${teacher.employeeNumber} already exists", "Teacher")
        }

        val savedTeacher = teacherRepository.save(teacher)
        logger.info("Teacher created successfully with id: {}", savedTeacher.id)
        return savedTeacher
    }

    @Transactional
    fun update(id: UUID, updatedTeacher: Teacher): Teacher {
        logger.info("Updating teacher with id: {}", id)
        val existingTeacher = teacherRepository.findByIdOrThrow(id)
        
        // Update fields
        updatedTeacher.employeeNumber?.let { existingTeacher.employeeNumber = it }
        updatedTeacher.dateOfBirth?.let { existingTeacher.dateOfBirth = it }
        updatedTeacher.gender?.let { existingTeacher.gender = it }
        updatedTeacher.address?.let { existingTeacher.address = it }
        updatedTeacher.phone?.let { existingTeacher.phone = it }
        updatedTeacher.qualifications?.let { existingTeacher.qualifications = it }
        updatedTeacher.specialization?.let { existingTeacher.specialization = it }
        updatedTeacher.status?.let { existingTeacher.status = it }
        updatedTeacher.photoUrl?.let { existingTeacher.photoUrl = it }
        
        val savedTeacher = teacherRepository.save(existingTeacher)
        logger.info("Teacher updated successfully with id: {}", id)
        return savedTeacher
    }

    @Transactional
    fun delete(id: UUID) {
        logger.info("Deleting teacher with id: {}", id)
        val teacher = teacherRepository.findByIdOrThrow(id)
        teacher.status = TeacherStatus.TERMINATED
        teacherRepository.save(teacher)
        logger.info("Teacher deleted successfully with id: {}", id)
    }
}

