package com.visor.school.userservice.dto

import com.visor.school.userservice.model.User
import com.visor.school.userservice.model.UserRole
import java.util.UUID

// Response DTOs
data class UserResponse(
    val id: UUID,
    val email: String,
    val firstName: String,
    val lastName: String,
    val role: UserRole,
    val emailVerified: Boolean,
    val accountStatus: String
) {
    companion object {
        fun from(user: User): UserResponse {
            return UserResponse(
                id = user.id!!,
                email = user.email,
                firstName = user.firstName,
                lastName = user.lastName,
                role = user.role,
                emailVerified = user.emailVerified,
                accountStatus = user.accountStatus.name
            )
        }
    }
}