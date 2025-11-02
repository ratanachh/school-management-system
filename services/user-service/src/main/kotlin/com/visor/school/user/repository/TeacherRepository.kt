package com.visor.school.user.repository

import com.visor.school.persistence.BaseRepository
import com.visor.school.user.domain.model.Teacher
import com.visor.school.user.domain.model.TeacherStatus
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface TeacherRepository : BaseRepository<Teacher> {

    fun findByEmployeeNumber(employeeNumber: String): Optional<Teacher>

    fun existsByEmployeeNumber(employeeNumber: String): Boolean

    @Query("SELECT t FROM Teacher t WHERE t.status = :status")
    fun findByStatus(status: TeacherStatus): List<Teacher>

    @Query("SELECT t FROM Teacher t WHERE t.user.email LIKE %:searchTerm% OR t.employeeNumber LIKE %:searchTerm%")
    fun search(searchTerm: String): List<Teacher>

    @Query("SELECT t FROM Teacher t WHERE t.user.id = :userId")
    fun findByUserId(userId: UUID): Optional<Teacher>
}

