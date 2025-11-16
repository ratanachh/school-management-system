package com.visor.school.userservice.dto

data class LoginResponse(
    val message: String,
    val tokenEndpoint: String
)