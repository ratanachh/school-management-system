package com.visor.school.academicservice.repository

import com.visor.school.academicservice.model.EmploymentStatus
import com.visor.school.academicservice.model.Teacher
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

@Repository
interface TeacherRepository : JpaRepository<Teacher, UUID> {
    fun findByEmployeeId(employeeId: String): Optional<Teacher>
    fun findByUserId(userId: UUID): Optional<Teacher>
    fun findByEmploymentStatus(status: EmploymentStatus): List<Teacher>
    
    @Query("SELECT t FROM Teacher t WHERE t.department = :department")
    fun findByDepartment(@Param("department") department: String): List<Teacher>
}

