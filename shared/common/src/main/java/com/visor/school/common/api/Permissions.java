package com.visor.school.common.api;

/**
 * Permission constants used across services.
 * Aligns with Keycloak permissions.
 */
public final class Permissions {

    private Permissions() {
    }

    // Attendance Permissions
    public static final String COLLECT_ATTENDANCE = "COLLECT_ATTENDANCE";
    public static final String APPROVE_ATTENDANCE = "APPROVE_ATTENDANCE";

    // Grade Permissions
    public static final String MANAGE_GRADES = "MANAGE_GRADES";
    public static final String VIEW_GRADES = "VIEW_GRADES";

    // Report Permissions
    public static final String COLLECT_EXAM_RESULTS = "COLLECT_EXAM_RESULTS";
    public static final String SUBMIT_REPORTS = "SUBMIT_REPORTS";

    // User Management Permissions
    public static final String MANAGE_USERS = "MANAGE_USERS";
    public static final String VIEW_USERS = "VIEW_USERS";

    // Student Management Permissions
    public static final String MANAGE_STUDENTS = "MANAGE_STUDENTS";
    public static final String VIEW_STUDENTS = "VIEW_STUDENTS";

    // Teacher Management Permissions
    public static final String MANAGE_TEACHERS = "MANAGE_TEACHERS";
    public static final String VIEW_TEACHERS = "VIEW_TEACHERS";
}
