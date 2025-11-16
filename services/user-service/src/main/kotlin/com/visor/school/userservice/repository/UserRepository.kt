package com.visor.school.userservice.repository

import com.visor.school.userservice.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID
import java.util.Optional

@Repository
interface UserRepository : JpaRepository<User, UUID> {
    fun findByKeycloakId(keycloakId: String): Optional<User>
    fun findByEmail(email: String): Optional<User>
    fun existsByEmail(email: String): Boolean
    fun existsByKeycloakId(keycloakId: String): Boolean
}

