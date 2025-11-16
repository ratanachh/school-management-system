package com.visor.school.userservice.repository

import com.visor.school.userservice.model.User
import com.visor.school.userservice.model.UserPermission
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface UserPermissionRepository : JpaRepository<UserPermission, UUID> {
    fun findByUser(user: User): List<UserPermission>
    fun findByUserId(userId: UUID): List<UserPermission>
    fun existsByUserIdAndPermissionId(userId: UUID, permissionId: UUID): Boolean
    fun deleteByUserIdAndPermissionId(userId: UUID, permissionId: UUID)
}

