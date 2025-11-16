package com.visor.school.userservice.repository

import com.visor.school.userservice.model.Parent
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ParentRepository : JpaRepository<Parent, UUID> {
    fun findByUserId(userId: UUID): kotlin.collections.List<Parent>
    fun findByStudentId(studentId: UUID): kotlin.collections.List<Parent>
    fun existsByUserIdAndStudentId(userId: UUID, studentId: UUID): Boolean
}

