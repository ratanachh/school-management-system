package com.visor.school.academicservice.repository

import com.visor.school.academicservice.model.EnrollmentStatus
import com.visor.school.academicservice.model.Student
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

@Repository
interface StudentRepository : JpaRepository<Student, UUID> {
    fun findByStudentId(studentId: String): Optional<Student>
    fun findByUserId(userId: UUID): Optional<Student>
    fun findByGradeLevel(gradeLevel: Int): List<Student>
    fun findByEnrollmentStatus(status: EnrollmentStatus): List<Student>
    
    @Query("SELECT s FROM Student s WHERE s.firstName LIKE %:name% OR s.lastName LIKE %:name%")
    fun searchByName(@Param("name") name: String): List<Student>
    
    @Query("SELECT s FROM Student s WHERE s.gradeLevel = :gradeLevel AND s.enrollmentStatus = :status")
    fun findByGradeLevelAndStatus(@Param("gradeLevel") gradeLevel: Int, @Param("status") status: EnrollmentStatus): List<Student>
}
