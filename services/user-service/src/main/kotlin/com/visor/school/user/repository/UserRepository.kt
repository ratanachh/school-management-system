package com.visor.school.user.repository

import com.visor.school.persistence.BaseRepository
import com.visor.school.user.domain.model.User
import com.visor.school.user.domain.model.UserStatus
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository : BaseRepository<User> {

    fun findByEmail(email: String): Optional<User>

    fun existsByEmail(email: String): Boolean

    fun findByKeycloakId(keycloakId: String): Optional<User>

    @Query("SELECT u FROM User u WHERE u.status = :status")
    fun findByStatus(status: UserStatus): List<User>

    @Query("SELECT u FROM User u WHERE u.email LIKE %:searchTerm% OR u.firstName LIKE %:searchTerm% OR u.lastName LIKE %:searchTerm%")
    fun search(searchTerm: String): List<User>
}

