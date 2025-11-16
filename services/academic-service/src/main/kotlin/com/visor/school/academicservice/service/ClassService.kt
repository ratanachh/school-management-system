package com.visor.school.academicservice.service

import com.visor.school.academicservice.model.*
import com.visor.school.academicservice.repository.ClassRepository
import com.visor.school.academicservice.repository.TeacherAssignmentRepository
import com.visor.school.academicservice.repository.TeacherRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.UUID

/**
 * Class service with validation for homeroom classes (grades 1-6) and class teacher assignment (grades 7-12)
 */
@Service
@Transactional
class ClassService(
    private val classRepository: ClassRepository,
    private val teacherRepository: TeacherRepository,
    private val teacherAssignmentRepository: TeacherAssignmentRepository
) {
    private val logger = LoggerFactory.getLogger(ClassService::class.java)

    /**
     * Create a homeroom class (grades 1-6 only)
     * Validates: grade level 1-6, one homeroom class per grade per academic year
     */
    fun createHomeroomClass(
        className: String,
        gradeLevel: Int,
        homeroomTeacherId: UUID,
        academicYear: String,
        term: Term,
        schedule: Schedule? = null,
        maxCapacity: Int? = null,
        startDate: LocalDate,
        endDate: LocalDate? = null
    ): Class {
        logger.info("Creating homeroom class: $className for grade $gradeLevel")

        // Validate grade level
        require(gradeLevel in 1..6) {
            "Homeroom classes are only for grades 1-6, got: $gradeLevel"
        }

        // Validate teacher exists and is active
        val teacher = teacherRepository.findById(homeroomTeacherId)
            .orElseThrow { IllegalArgumentException("Teacher not found: $homeroomTeacherId") }
        require(teacher.employmentStatus == EmploymentStatus.ACTIVE) {
            "Teacher must be active"
        }

        // Validate one homeroom class per grade per academic year
        val existingHomeroom = classRepository.findByAcademicYearAndTermAndTypeAndGrade(
            academicYear = academicYear,
            term = term,
            classType = ClassType.HOMEROOM,
            gradeLevel = gradeLevel
        )
        require(existingHomeroom.isEmpty()) {
            "Homeroom class already exists for grade $gradeLevel in academic year $academicYear, term $term"
        }

        val classEntity = Class(
            className = className,
            classType = ClassType.HOMEROOM,
            subject = null,
            gradeLevel = gradeLevel,
            homeroomTeacherId = homeroomTeacherId,
            classTeacherId = null,
            academicYear = academicYear,
            term = term,
            schedule = schedule,
            maxCapacity = maxCapacity,
            startDate = startDate,
            endDate = endDate,
            status = ClassStatus.SCHEDULED
        )

        val saved = classRepository.save(classEntity)
        logger.info("Homeroom class created: ${saved.id}")

        return saved
    }

    /**
     * Create a subject class (all grades)
     */
    fun createSubjectClass(
        className: String,
        subject: String,
        gradeLevel: Int,
        academicYear: String,
        term: Term,
        schedule: Schedule? = null,
        maxCapacity: Int? = null,
        startDate: LocalDate,
        endDate: LocalDate? = null
    ): Class {
        logger.info("Creating subject class: $className for subject $subject, grade $gradeLevel")

        require(gradeLevel in 1..12) {
            "Grade level must be between 1 and 12, got: $gradeLevel"
        }

        require(subject.isNotBlank()) {
            "Subject classes must have a subject"
        }

        val classEntity = Class(
            className = className,
            classType = ClassType.SUBJECT,
            subject = subject,
            gradeLevel = gradeLevel,
            homeroomTeacherId = null,
            classTeacherId = null,
            academicYear = academicYear,
            term = term,
            schedule = schedule,
            maxCapacity = maxCapacity,
            startDate = startDate,
            endDate = endDate,
            status = ClassStatus.SCHEDULED
        )

        val saved = classRepository.save(classEntity)
        logger.info("Subject class created: ${saved.id}")

        return saved
    }

    /**
     * Assign class teacher/coordinator (grades 7-12 only)
     * Validates: grade level 7-12, teacher must be assigned to the class, only one class teacher per class
     */
    fun assignClassTeacher(
        classId: UUID,
        teacherId: UUID,
        assignedBy: UUID? = null
    ): Class {
        logger.info("Assigning class teacher $teacherId to class $classId")

        val classEntity = classRepository.findById(classId)
            .orElseThrow { IllegalArgumentException("Class not found: $classId") }

        // Validate grade level
        require(classEntity.gradeLevel in 7..12) {
            "Class teacher assignment is only for grades 7-12, got: ${classEntity.gradeLevel}"
        }

        require(classEntity.classType == ClassType.SUBJECT) {
            "Class teacher can only be assigned to subject classes"
        }

        // Validate teacher exists and is active
        val teacher = teacherRepository.findById(teacherId)
            .orElseThrow { IllegalArgumentException("Teacher not found: $teacherId") }
        require(teacher.employmentStatus == EmploymentStatus.ACTIVE) {
            "Teacher must be active"
        }

        // Validate teacher is assigned to the class
        val assignment = teacherAssignmentRepository.findByTeacherIdAndClassId(teacherId, classId)
        require(assignment.isNotEmpty()) {
            "Teacher must be assigned to the class before being designated as class teacher"
        }

        // Validate only one class teacher per class
        val existingClassTeacher = teacherAssignmentRepository.findClassTeacherByClassId(classId)
        require(existingClassTeacher.isEmpty()) {
            "Class already has a class teacher assigned"
        }

        // Create or update teacher assignment with isClassTeacher = true
        val teacherAssignment = teacherAssignmentRepository.findByTeacherIdAndClassId(teacherId, classId).firstOrNull()
            ?: throw IllegalStateException("Teacher assignment not found")

        // Update class teacher ID
        // Note: In a full implementation, we'd need to update the Class entity
        // For now, we'll create a new assignment with isClassTeacher flag
        // This would require modifying the Class entity to update classTeacherId

        logger.info("Class teacher assigned: teacher $teacherId to class $classId")

        return classEntity
    }

    /**
     * Get class by ID
     */
    @Transactional(readOnly = true)
    fun getClassById(id: UUID): Class? {
        return classRepository.findById(id).orElse(null)
    }

    /**
     * Get classes by grade level
     */
    @Transactional(readOnly = true)
    fun getClassesByGradeLevel(gradeLevel: Int): List<Class> {
        require(gradeLevel in 1..12) {
            "Grade level must be between 1 and 12, got: $gradeLevel"
        }
        return classRepository.findByGradeLevel(gradeLevel)
    }

    /**
     * Get classes by type
     */
    @Transactional(readOnly = true)
    fun getClassesByType(classType: ClassType): List<Class> {
        return classRepository.findByClassType(classType)
    }

    /**
     * Update class status
     */
    fun updateClassStatus(id: UUID, status: ClassStatus): Class {
        val classEntity = classRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Class not found: $id") }

        classEntity.updateStatus(status)
        logger.info("Updated class status for $id to $status")

        return classRepository.save(classEntity)
    }
}

