package com.visor.school.user.repository

import com.visor.school.persistence.BaseRepository
import com.visor.school.user.domain.model.Student
import com.visor.school.user.domain.model.StudentStatus
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface StudentRepository : BaseRepository<Student> {

    fun findByStudentNumber(studentNumber: String): Optional<Student>

    fun existsByStudentNumber(studentNumber: String): Boolean

    @Query("SELECT s FROM Student s WHERE s.status = :status")
    fun findByStatus(status: StudentStatus): List<Student>

    @Query("SELECT s FROM Student s WHERE s.user.email LIKE %:searchTerm% OR s.studentNumber LIKE %:searchTerm%")
    fun search(searchTerm: String): List<Student>

    @Query("SELECT s FROM Student s WHERE s.user.id = :userId")
    fun findByUserId(userId: UUID): Optional<Student>
}

