# Data Model: School Management System

**Date**: 2025-01-27  
**Feature**: School Management System

## Overview

This document defines the core data entities and their relationships for the school management system. Each entity is managed by a specific microservice, with relationships maintained via events and service references.

## Entity Definitions

### User (User Service)

Represents a person with system access (super administrator, administrator, teacher, student, parent).

**Attributes**:
- `id` (UUID): Unique identifier
- `keycloakId` (String): Keycloak user ID (unique, required) - Links to Keycloak identity
- `email` (String): Email address (unique, required)
- `role` (Enum): SUPER_ADMIN, ADMINISTRATOR, TEACHER, STUDENT, PARENT
- `firstName` (String): First name
- `lastName` (String): Last name
- `phoneNumber` (String, optional): Contact phone
- `emailVerified` (Boolean): Email verification status
- `accountStatus` (Enum): ACTIVE, INACTIVE, SUSPENDED, DELETED
- `createdAt` (Timestamp): Account creation date
- `updatedAt` (Timestamp): Last update date
- `lastLoginAt` (Timestamp, optional): Last login timestamp

**Validation Rules**:
- Keycloak ID must be unique and valid (provided by Keycloak)
- Email must be valid format and unique
- Password management is handled by Keycloak (not stored in User entity)
- Role must be valid enum value (SUPER_ADMIN, ADMINISTRATOR, TEACHER, STUDENT, PARENT)
- First name and last name are required
- Role hierarchy: SUPER_ADMIN can manage ADMINISTRATOR users; ADMINISTRATOR cannot manage other ADMINISTRATOR or SUPER_ADMIN users

**State Transitions**:
- Created → Email Verification Pending → Active
- Active → Suspended (admin action)
- Active → Inactive (user action)
- Any status → Deleted (soft delete)

**Keycloak Integration**:
- User creation flow: 
  1. Create user in Keycloak first (via Keycloak Admin API)
  2. Receive `keycloakId` from Keycloak response
  3. Create User entity in User Service with `keycloakId` reference
- Password management: All password operations (set, change, reset) handled by Keycloak
- Authentication: Login happens through Keycloak token endpoint, JWT tokens issued by Keycloak
- User lookup: Can find user by `keycloakId` or by `email` (email must match Keycloak)

**Relationships**:
- One-to-many with Student (if role is PARENT)
- One-to-one with Teacher (if role is TEACHER)
- One-to-one with Student (if role is STUDENT)
- Many-to-many with Permission (via UserPermission)

---

### Permission (User Service)

Represents a fine-grained permission that can be assigned to users or roles. Permissions are stored in Keycloak and included in JWT tokens.

**Attributes**:
- `id` (UUID): Unique identifier
- `permissionKey` (String): Unique permission identifier (e.g., "COLLECT_EXAM_RESULTS", "SUBMIT_REPORTS")
- `name` (String): Human-readable permission name
- `description` (String): Permission description
- `category` (String): Permission category (e.g., "ACADEMIC", "ADMINISTRATIVE", "REPORTING")
- `createdAt` (Timestamp): Record creation date
- `updatedAt` (Timestamp): Last update date

**Validation Rules**:
- Permission key must be unique and uppercase (e.g., "COLLECT_EXAM_RESULTS")
- Name and description are required
- Category must be valid

**Keycloak Integration**:
- Permissions are stored in Keycloak as custom attributes or realm roles
- JWT tokens include permissions in `permissions` claim
- Permission checks performed using Spring Security `@PreAuthorize` with `hasPermission()`

**Permission Examples**:
- `COLLECT_EXAM_RESULTS`: Class teachers (grades 7-12) can collect exam results from subject teachers
- `SUBMIT_REPORTS`: Class teachers can submit aggregated reports to school administration
- `MANAGE_HOMEROOM`: Homeroom teachers (grades 1-6) can manage their homeroom class
- `VIEW_ALL_STUDENTS`: Administrators can view all student records
- `MANAGE_GRADES`: Teachers can record and modify grades
- `VIEW_ATTENDANCE_REPORTS`: Teachers can view attendance reports
- `MANAGE_ADMINISTRATORS`: Super administrators can create, update, and manage administrator accounts

**Relationships**:
- Many-to-many with User (via UserPermission)
- Many-to-many with Role (via RolePermission)

---

### UserPermission (User Service)

Junction table linking users to permissions. Permissions can also be assigned via roles.

**Attributes**:
- `id` (UUID): Unique identifier
- `userId` (UUID): Reference to User (FK)
- `permissionId` (UUID): Reference to Permission (FK)
- `grantedBy` (UUID): Reference to User (FK) who granted the permission
- `grantedAt` (Timestamp): When permission was granted
- `createdAt` (Timestamp): Record creation date

**Relationships**:
- Many-to-one with User (userId)
- Many-to-one with Permission (permissionId)
- Many-to-one with User (grantedBy)

---

### Student (Academic Service / User Service)

Represents an enrolled student in the system.

**Attributes**:
- `id` (UUID): Unique identifier
- `userId` (UUID): Reference to User entity (FK)
- `studentId` (String): Unique student identifier (e.g., "STU-2025-001")
- `dateOfBirth` (Date): Date of birth
- `gradeLevel` (String): Current grade level (e.g., "Grade 1", "Grade 12")
- `enrollmentDate` (Date): Date of enrollment
- `enrollmentStatus` (Enum): ACTIVE, GRADUATED, TRANSFERRED, WITHDRAWN
- `address` (Address): Street, city, state, zip, country
- `emergencyContact` (Contact): Name, phone, relationship
- `parentIds` (List<UUID>): References to parent User entities
- `createdAt` (Timestamp): Record creation date
- `updatedAt` (Timestamp): Last update date

**Validation Rules**:
- Student ID must be unique
- Date of birth must be valid date
- Grade level must be valid for school
- At least one parent must be linked
- Enrollment date cannot be future date

**State Transitions**:
- Enrollment Pending → Active
- Active → Transferred (with transfer date)
- Active → Graduated (with graduation date)
- Active → Withdrawn (with withdrawal date)

**Relationships**:
- Many-to-one with User (userId)
- Many-to-many with Class (via enrollments)
- Many-to-many with Parent (via parentIds)
- One-to-many with AttendanceRecord
- One-to-many with Grade
- One-to-one with AcademicRecord

---

### Teacher (Academic Service / User Service)

Represents a teaching staff member.

**Attributes**:
- `id` (UUID): Unique identifier
- `userId` (UUID): Reference to User entity (FK)
- `employeeId` (String): Unique employee identifier
- `qualifications` (List<String>): Teaching qualifications/certifications
- `subjectSpecializations` (List<String>): Subjects teacher can teach
- `hireDate` (Date): Employment start date
- `employmentStatus` (Enum): ACTIVE, ON_LEAVE, TERMINATED, RETIRED
- `department` (String, optional): Department assignment
- `createdAt` (Timestamp): Record creation date
- `updatedAt` (Timestamp): Last update date

**Validation Rules**:
- Employee ID must be unique
- At least one subject specialization required
- Hire date cannot be future date

**State Transitions**:
- Hired → Active
- Active → On Leave (with leave dates)
- Active → Terminated (with termination date)
- Active → Retired (with retirement date)

**Relationships**:
- Many-to-one with User (userId)
- Many-to-many with Class (via TeacherAssignment)
- One-to-many with TeacherAssignment (class assignments)
- One-to-many with AttendanceRecord (marks attendance)
- One-to-many with Assessment (creates assessments)
- One-to-many with Grade (records grades)

---

### TeacherAssignment (Academic Service)

Represents the assignment of a teacher to a class. For grades 7-12, one teacher can be designated as class teacher/coordinator.

**Attributes**:
- `id` (UUID): Unique identifier
- `teacherId` (UUID): Reference to Teacher (FK)
- `classId` (UUID): Reference to Class (FK)
- `isClassTeacher` (Boolean): True if this teacher is designated as class teacher/coordinator (grades 7-12 only)
- `assignedDate` (Date): Date when assignment was made
- `assignedBy` (UUID): Reference to User (FK) who made the assignment (typically administrator)
- `createdAt` (Timestamp): Record creation date
- `updatedAt` (Timestamp): Last update date

**Validation Rules**:
- Teacher must be active
- Class must be active
- For grades 7-12: Only one teacher per class can have `isClassTeacher = true`
- For grades 1-6: `isClassTeacher` must be false (homeroom teacher is separate)
- Teacher must be assigned to subject classes only (not homeroom classes)
- Class teacher (isClassTeacher = true) must have `COLLECT_EXAM_RESULTS` and `SUBMIT_REPORTS` permissions

**Relationships**:
- Many-to-one with Teacher (teacherId)
- Many-to-one with Class (classId)
- Many-to-one with User (assignedBy)

---

### Class (Academic Service)

Represents a group of students and teachers. Supports two class types: Homeroom classes (grades 1-6) and Subject classes (all grades).

**Attributes**:
- `id` (UUID): Unique identifier
- `className` (String): Class name (e.g., "Grade 3 Homeroom", "Mathematics 101")
- `classType` (Enum): HOMEROOM (grades 1-6), SUBJECT (all grades) - Required
- `subject` (String, optional): Subject name (e.g., "Mathematics") - Required for SUBJECT type, null for HOMEROOM
- `gradeLevel` (String): Grade level (e.g., "Grade 1", "Grade 12") - Must be between 1 and 12 (K12)
- `teacherId` (UUID, optional): Reference to Teacher (FK) - For subject classes, references subject teacher
- `homeroomTeacherId` (UUID, optional): Reference to Teacher (FK) - For homeroom classes (grades 1-6), references homeroom teacher
- `classTeacherId` (UUID, optional): Reference to Teacher (FK) - For grades 7-12, references designated class teacher/coordinator
- `academicYear` (String): Academic year (e.g., "2024-2025")
- `term` (Enum): FIRST_TERM, SECOND_TERM, THIRD_TERM, FULL_YEAR
- `schedule` (Schedule): Days of week, start time, end time, room
- `maxCapacity` (Integer): Maximum number of students
- `currentEnrollment` (Integer): Current number of enrolled students
- `status` (Enum): SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED
- `startDate` (Date): Class start date
- `endDate` (Date): Class end date
- `createdAt` (Timestamp): Record creation date
- `updatedAt` (Timestamp): Last update date

**Validation Rules**:
- Class name must be unique within academic year and term
- Grade level must be between 1 and 12 (K12 system)
- **For HOMEROOM classes (grades 1-6)**:
  - `classType` must be HOMEROOM
  - `gradeLevel` must be between 1 and 6
  - `homeroomTeacherId` is required
  - `subject` must be null
  - Only one homeroom class per grade level per academic year
  - `teacherId` and `classTeacherId` must be null
- **For SUBJECT classes (all grades)**:
  - `classType` must be SUBJECT
  - `subject` is required
  - `teacherId` is required (subject teacher)
  - `homeroomTeacherId` must be null
  - **For grades 7-12**: `classTeacherId` optional (designated class teacher/coordinator)
  - **For grades 1-6**: `classTeacherId` must be null
- Teacher must be active
- Start date must be before end date
- Current enrollment cannot exceed max capacity
- Class teacher (grades 7-12) must be one of the subject teachers assigned to that class

**State Transitions**:
- Created → Scheduled
- Scheduled → In Progress (on start date)
- In Progress → Completed (on end date)
- Any status → Cancelled (admin action)

**K12-Specific Rules**:
- **Grades 1-6**: Homeroom classes have one homeroom teacher responsible for all activities
- **Grades 7-12**: Subject classes have subject teachers, with one designated as class teacher/coordinator who can collect exam results and reports

**Relationships**:
- Many-to-one with Teacher (teacherId) - Subject teacher for subject classes
- Many-to-one with Teacher (homeroomTeacherId) - Homeroom teacher for homeroom classes (grades 1-6)
- Many-to-one with Teacher (classTeacherId) - Class teacher/coordinator for grades 7-12 (derived from TeacherAssignment with isClassTeacher=true)
- Many-to-many with Student (via enrollments)
- One-to-many with TeacherAssignment (teacher assignments to this class)
- One-to-many with AttendanceRecord
- One-to-many with Assessment
- One-to-many with Grade (via assessments)

---

### AttendanceRecord (Attendance Service)

Represents a single attendance entry for a student on a specific date.

**Attributes**:
- `id` (UUID): Unique identifier
- `studentId` (UUID): Reference to Student (FK)
- `classId` (UUID): Reference to Class (FK)
- `date` (Date): Attendance date
- `status` (Enum): PRESENT, ABSENT, LATE, EXCUSED
- `markedBy` (UUID): Reference to Teacher (FK) who marked attendance
- `notes` (String, optional): Additional notes or justification
- `markedAt` (Timestamp): When attendance was marked
- `updatedAt` (Timestamp): Last update timestamp
- `updatedBy` (UUID, optional): Reference to User who updated (if corrected)

**Validation Rules**:
- Student must be enrolled in class
- Date cannot be future date
- One attendance record per student per class per date
- Status must be valid enum value
- If status is EXCUSED, notes are recommended

**State Transitions**:
- Created → Marked
- Marked → Updated (if corrected, maintains audit trail)

**Relationships**:
- Many-to-one with Student (studentId)
- Many-to-one with Class (classId)
- Many-to-one with Teacher (markedBy)

---

### Assessment (Academic Assessment Service)

Represents an evaluation activity (test, quiz, assignment, project).

**Attributes**:
- `id` (UUID): Unique identifier
- `classId` (UUID): Reference to Class (FK)
- `name` (String): Assessment name
- `type` (Enum): TEST, QUIZ, ASSIGNMENT, PROJECT, EXAM, FINAL_EXAM
- `description` (String, optional): Assessment description
- `totalPoints` (Decimal): Maximum possible points
- `weight` (Decimal, optional): Weight in final grade calculation (percentage)
- `dueDate` (Date, optional): Due date for submission
- `createdBy` (UUID): Reference to Teacher (FK)
- `createdAt` (Timestamp): Creation date
- `updatedAt` (Timestamp): Last update date
- `status` (Enum): DRAFT, PUBLISHED, GRADING, COMPLETED

**Validation Rules**:
- Assessment name is required
- Total points must be positive
- Weight must be between 0 and 100 if provided
- Due date cannot be before creation date
- Class must be active

**State Transitions**:
- Created → Draft
- Draft → Published (available for grade entry)
- Published → Grading (grades being recorded)
- Grading → Completed (all grades recorded)

**Relationships**:
- Many-to-one with Class (classId)
- Many-to-one with Teacher (createdBy)
- One-to-many with Grade

---

### Grade (Academic Assessment Service)

Represents a score for a student on an assessment.

**Attributes**:
- `id` (UUID): Unique identifier
- `studentId` (UUID): Reference to Student (FK)
- `assessmentId` (UUID): Reference to Assessment (FK)
- `score` (Decimal): Points earned
- `percentage` (Decimal): Calculated percentage (score / totalPoints * 100)
- `letterGrade` (String, optional): Letter grade (A, B, C, D, F) if applicable
- `recordedBy` (UUID): Reference to Teacher (FK)
- `recordedAt` (Timestamp): When grade was recorded
- `updatedAt` (Timestamp): Last update timestamp
- `updatedBy` (UUID, optional): Reference to User who updated (if modified)
- `notes` (String, optional): Additional notes or comments

**Validation Rules**:
- Score cannot be negative
- Score cannot exceed assessment total points
- Student must be enrolled in class
- Assessment must be published
- Percentage calculated automatically

**State Transitions**:
- Created → Recorded
- Recorded → Updated (if corrected, maintains audit trail)

**Relationships**:
- Many-to-one with Student (studentId)
- Many-to-one with Assessment (assessmentId)
- Many-to-one with Teacher (recordedBy)

---

### AcademicRecord (Academic Service)

Represents a student's complete academic history.

**Attributes**:
- `id` (UUID): Unique identifier
- `studentId` (UUID): Reference to Student (FK)
- `enrollmentHistory` (List<EnrollmentEntry>): Historical enrollment records
- `completedCourses` (List<CourseCompletion>): Completed courses with grades
- `currentGPA` (Decimal): Current grade point average
- `cumulativeGPA` (Decimal): Cumulative GPA across all terms
- `creditsEarned` (Integer): Total credits earned
- `creditsRequired` (Integer): Credits required for graduation
- `academicStanding` (Enum): GOOD_STANDING, PROBATION, SUSPENDED, GRADUATED
- `graduationDate` (Date, optional): Date of graduation if applicable
- `updatedAt` (Timestamp): Last update timestamp

**EnrollmentEntry**:
- `academicYear` (String)
- `term` (Enum)
- `gradeLevel` (String)
- `enrollmentDate` (Date)
- `status` (Enum)

**CourseCompletion**:
- `courseName` (String)
- `subject` (String)
- `gradeLevel` (String)
- `finalGrade` (String)
- `credits` (Integer)
- `completionDate` (Date)

**Validation Rules**:
- GPA calculated from all completed courses
- Credits earned sum of all completed course credits
- Academic standing calculated based on GPA and attendance

**State Transitions**:
- Created → Active
- Active → Graduated (when requirements met)
- Active → Suspended (if academic standing poor)

**Relationships**:
- One-to-one with Student (studentId)
- Aggregates data from Grade, AttendanceRecord, Class entities

---

### Parent (User Service / Academic Service)

Represents a parent or guardian linked to one or more students.

**Attributes**:
- `id` (UUID): Unique identifier
- `userId` (UUID): Reference to User entity (FK)
- `relationship` (Enum): MOTHER, FATHER, GUARDIAN, OTHER
- `primaryContact` (Boolean): Is this the primary contact
- `canPickUp` (Boolean): Can pick up student from school
- `emergencyContact` (Boolean): Emergency contact person
- `linkedStudentIds` (List<UUID>): References to Student entities
- `createdAt` (Timestamp): Record creation date
- `updatedAt` (Timestamp): Last update date

**Validation Rules**:
- At least one student must be linked
- At least one primary contact per student
- Relationship must be valid enum value

**Relationships**:
- Many-to-one with User (userId)
- Many-to-many with Student (via linkedStudentIds)

---

## Cross-Service Data Relationships

### Event-Driven Relationships

Since services maintain independent databases, relationships are maintained via events:

1. **User Created Event** → Academic Service creates Student/Teacher record
2. **Student Enrolled Event** → Search Service indexes student, Notification Service notifies parents
3. **Grade Recorded Event** → Academic Service updates AcademicRecord, Notification Service notifies student/parent
4. **Attendance Marked Event** → Reporting Service updates statistics, Notification Service notifies parents if absent
5. **Class Created Event** → Timetable Service updates schedule, Search Service indexes class

### Data Consistency

- **Strong Consistency**: Within a single service (e.g., Student and User in same service)
- **Eventual Consistency**: Across services (e.g., Search index updated asynchronously)
- **Event Ordering**: Critical events processed in order via RabbitMQ
- **Idempotency**: Event handlers must be idempotent to handle duplicate events

## Database Schema Per Service

### User Service Database
- `users` table
  - `id` (UUID, primary key)
  - `keycloak_id` (String, unique, not null) - Links to Keycloak user identity
  - `email` (String, unique, not null)
  - `role` (Enum)
  - `first_name` (String, not null)
  - `last_name` (String, not null)
  - `phone_number` (String, nullable)
  - `email_verified` (Boolean, default false)
  - `account_status` (Enum)
  - `created_at` (Timestamp)
  - `updated_at` (Timestamp)
  - `last_login_at` (Timestamp, nullable)
  - Note: Password hash NOT stored - managed by Keycloak
- `permissions` table
  - `id` (UUID, primary key)
  - `permission_key` (String, unique, not null) - e.g., "COLLECT_EXAM_RESULTS"
  - `name` (String, not null)
  - `description` (String)
  - `category` (String)
  - `created_at` (Timestamp)
  - `updated_at` (Timestamp)
- `user_permissions` table (junction)
  - `id` (UUID, primary key)
  - `user_id` (UUID, FK to users)
  - `permission_id` (UUID, FK to permissions)
  - `granted_by` (UUID, FK to users)
  - `granted_at` (Timestamp)
  - `created_at` (Timestamp)
- `parents` table (if managed here)
- `role_permissions` table (if custom role-permission mapping beyond Keycloak)

### Academic Service Database
- `students` table
- `teachers` table
- `classes` table
  - `class_type` (Enum): HOMEROOM, SUBJECT
  - `homeroom_teacher_id` (UUID, nullable, FK to teachers) - For grades 1-6
  - `class_teacher_id` (UUID, nullable, FK to teachers) - For grades 7-12
  - `grade_level` (String) - Validated to be 1-12
- `class_enrollments` table (many-to-many)
- `teacher_assignments` table
  - `is_class_teacher` (Boolean) - For grades 7-12
  - `assigned_by` (UUID, FK to users)
- `academic_records` table

### Attendance Service Database
- `attendance_records` table
- `attendance_corrections` table (audit trail)

### Academic Assessment Service Database
- `assessments` table
- `grades` table
- `grade_corrections` table (audit trail)

## Indexing Strategy

### User Service
- Index on `email` (unique lookup)
- Index on `role` (role-based queries)

### Academic Service
- Index on `studentId` (student lookup)
- Index on `classId` (class queries)
- Composite index on `(academicYear, term, gradeLevel)` (class queries)

### Attendance Service
- Composite index on `(studentId, classId, date)` (attendance lookup)
- Index on `date` (date range queries)

### Academic Assessment Service
- Index on `assessmentId` (grade lookup)
- Composite index on `(studentId, assessmentId)` (student grade lookup)
- Index on `classId` (class gradebook queries)

## Data Migration Strategy

- **Flyway** for database migrations
- Versioned migration scripts
- Reversible migrations for rollback
- Separate migrations per service

## Backup and Recovery

- Regular automated backups per service database
- Point-in-time recovery capability
- Backup retention policy (30 days minimum)
- Cross-region backup replication for disaster recovery

