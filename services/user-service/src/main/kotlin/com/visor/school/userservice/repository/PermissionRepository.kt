package com.visor.school.userservice.repository

import com.visor.school.userservice.model.Permission
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

@Repository
interface PermissionRepository : JpaRepository<Permission, UUID> {
    fun findByPermissionKey(permissionKey: String): Optional<Permission>
    fun existsByPermissionKey(permissionKey: String): Boolean
    fun findByCategory(category: String): List<Permission>
}

