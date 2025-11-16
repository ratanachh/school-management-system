package com.visor.school.userservice.dto

import com.visor.school.userservice.model.UserRole
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

// Request DTOs
data class RegisterRequest(
    @field:Email
    @field:NotBlank
    val email: String,

    @field:NotBlank
    val firstName: String,

    @field:NotBlank
    val lastName: String,

    val role: UserRole,

    @field:NotBlank
    val password: String,

    val phoneNumber: String? = null
)