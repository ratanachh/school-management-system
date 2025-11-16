package com.visor.school.userservice.model

/**
 * User roles in the School Management System
 * Role hierarchy: SUPER_ADMIN > ADMINISTRATOR > TEACHER/STUDENT/PARENT
 */
enum class UserRole {
    SUPER_ADMIN,
    ADMINISTRATOR,
    TEACHER,
    STUDENT,
    PARENT
}

