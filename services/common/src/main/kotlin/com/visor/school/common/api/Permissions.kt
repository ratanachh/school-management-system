package com.visor.school.common.api

/**
 * Permission constants used across services
 * Aligns with Keycloak permissions
 */
object Permissions {
    // Attendance Permissions
    const val COLLECT_ATTENDANCE = "COLLECT_ATTENDANCE"
    const val APPROVE_ATTENDANCE = "APPROVE_ATTENDANCE"
    
    // Grade Permissions
    const val MANAGE_GRADES = "MANAGE_GRADES"
    const val VIEW_GRADES = "VIEW_GRADES"
    
    // Report Permissions
    const val COLLECT_EXAM_RESULTS = "COLLECT_EXAM_RESULTS"
    const val SUBMIT_REPORTS = "SUBMIT_REPORTS"
    
    // User Management Permissions
    const val MANAGE_USERS = "MANAGE_USERS"
    const val VIEW_USERS = "VIEW_USERS"
    
    // Student Management Permissions
    const val MANAGE_STUDENTS = "MANAGE_STUDENTS"
    const val VIEW_STUDENTS = "VIEW_STUDENTS"
    
    // Teacher Management Permissions
    const val MANAGE_TEACHERS = "MANAGE_TEACHERS"
    const val VIEW_TEACHERS = "VIEW_TEACHERS"
}

