# Data Model: Class Leader Attendance Delegation

**Date**: 2025-11-09  
**Feature**: Class Leader Attendance Delegation

## Overview

This document defines the data entities for class leader attendance delegation feature. These entities extend the existing Attendance Service and integrate with Academic Service for class leader assignment management. The feature adds session-based workflow for delegation while maintaining backward compatibility with direct attendance marking. A Kotlin-based Keycloak initializer runs during `user-service` startup and manages the security configuration (realm, roles, permissions) required by this workflow, ensuring permissions stay synchronized with entity state while skipping work if Keycloak is already provisioned.

## Entity Definitions

### AttendanceSession (Attendance Service)

Represents an attendance collection session for a specific date and class that can be delegated to class leaders.

**Attributes**:
- `id` (UUID): Unique identifier
- `classId` (UUID): Reference to Class (FK)
- `date` (Date): Attendance date for this session
- `status` (Enum): PENDING, COLLECTED, APPROVED, REJECTED
- `delegatedTo` (UUID): Reference to Student (FK) - Class leader assigned to collect attendance
- `createdBy` (UUID): Reference to Teacher (FK) - Teacher who created the session
- `approvedBy` (UUID, optional): Reference to Teacher (FK) - Teacher who approved the session
- `rejectedBy` (UUID, optional): Reference to Teacher (FK) - Teacher who rejected the session
- `rejectionReason` (String, optional): Reason provided when session is rejected
- `createdAt` (Timestamp): Session creation date
- `collectedAt` (Timestamp, optional): When class leader collected attendance
- `approvedAt` (Timestamp, optional): When teacher approved the session
- `rejectedAt` (Timestamp, optional): When teacher rejected the session
- `updatedAt` (Timestamp): Last update timestamp

**Validation Rules**:
- Class must exist and be active
- Date cannot be future date
- One session per class per date (prevents duplicate sessions)
- DelegatedTo must be a class leader assigned to the class
- Status transitions must follow valid state machine
- If status is REJECTED, rejectionReason is required
- If status is APPROVED, approvedBy must be set
- If status is COLLECTED, collectedAt must be set

**State Transitions**:
- Created → PENDING (when session created and delegated)
- PENDING → COLLECTED (when class leader submits attendance)
- COLLECTED → APPROVED (when teacher approves)
- COLLECTED → REJECTED (when teacher rejects)
- REJECTED → COLLECTED (when class leader resubmits after rejection)

**Relationships**:
- Many-to-one with Class (classId)
- Many-to-one with Student (delegatedTo) - Class leader
- Many-to-one with Teacher (createdBy)
- Many-to-one with Teacher (approvedBy, optional)
- Many-to-one with Teacher (rejectedBy, optional)
- One-to-many with AttendanceRecord (via sessionId)

**Database Schema**:
```sql
CREATE TABLE attendance_sessions (
    id UUID PRIMARY KEY,
    class_id UUID NOT NULL REFERENCES classes(id),
    date DATE NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'COLLECTED', 'APPROVED', 'REJECTED')),
    delegated_to UUID NOT NULL REFERENCES students(id),
    created_by UUID NOT NULL REFERENCES teachers(id),
    approved_by UUID REFERENCES teachers(id),
    rejected_by UUID REFERENCES teachers(id),
    rejection_reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    collected_at TIMESTAMP,
    approved_at TIMESTAMP,
    rejected_at TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(class_id, date)
);
```

---

### AttendanceRecord (Attendance Service) - Extended

Represents a single attendance entry for a student. Extended to support both direct marking and session-based collection.

**Attributes** (existing + new):
- `id` (UUID): Unique identifier
- `studentId` (UUID): Reference to Student (FK)
- `classId` (UUID): Reference to Class (FK)
- `date` (Date): Attendance date
- `status` (Enum): PRESENT, ABSENT, LATE, EXCUSED
- `markedBy` (UUID): Reference to Teacher (FK) - Teacher who marked attendance (direct marking)
- `collectedBy` (UUID, optional): Reference to Student (FK) - Class leader who collected attendance (session-based)
- `sessionId` (UUID, optional): Reference to AttendanceSession (FK) - Session this record belongs to
- `approvedBy` (UUID, optional): Reference to Teacher (FK) - Teacher who approved the session
- `notes` (String, optional): Additional notes or justification
- `markedAt` (Timestamp): When attendance was marked (direct) or collected (session)
- `updatedAt` (Timestamp): Last update timestamp
- `updatedBy` (UUID, optional): Reference to User who updated (if corrected)

**Validation Rules**:
- Student must be enrolled in class
- Date cannot be future date
- One attendance record per student per class per date
- Status must be valid enum value
- If status is EXCUSED, notes are recommended
- **NEW**: If sessionId is provided, collectedBy must be set (cannot be null)
- **NEW**: If sessionId is null, markedBy must be set (direct marking)
- **NEW**: If sessionId is provided, markedBy must be null (session-based collection)
- **NEW**: If approvedBy is set, sessionId must be provided and session status must be APPROVED
- **NEW**: If collectedBy is set, sessionId must be provided

**State Transitions**:
- Created → Marked (direct marking)
- Created → Collected (session-based, pending approval)
- Collected → Approved (when teacher approves session)
- Marked → Updated (if corrected, maintains audit trail)
- Collected → Updated (if teacher rejects and class leader corrects)

**Relationships**:
- Many-to-one with Student (studentId)
- Many-to-one with Class (classId)
- Many-to-one with Teacher (markedBy) - Direct marking
- Many-to-one with Student (collectedBy) - Session-based collection
- Many-to-one with AttendanceSession (sessionId, optional)
- Many-to-one with Teacher (approvedBy, optional)

**Database Schema Extension**:
```sql
ALTER TABLE attendance_records 
    ADD COLUMN collected_by UUID REFERENCES students(id),
    ADD COLUMN session_id UUID REFERENCES attendance_sessions(id),
    ADD COLUMN approved_by UUID REFERENCES teachers(id);

-- Constraints
ALTER TABLE attendance_records
    ADD CONSTRAINT check_marking_method CHECK (
        (marked_by IS NOT NULL AND collected_by IS NULL AND session_id IS NULL) OR
        (marked_by IS NULL AND collected_by IS NOT NULL AND session_id IS NOT NULL)
    );
```

---

### StudentClassLeadership (Academic Service)

Represents the assignment of a student to a class leader position for a specific class.

**Attributes**:
- `id` (UUID): Unique identifier
- `studentId` (UUID): Reference to Student (FK)
- `classId` (UUID): Reference to Class (FK)
- `leadershipPosition` (Enum): FIRST_LEADER, SECOND_LEADER, THIRD_LEADER
- `assignedBy` (UUID): Reference to User (FK) - Administrator or teacher who assigned the position
- `assignedAt` (Timestamp): When position was assigned
- `createdAt` (Timestamp): Record creation date
- `updatedAt` (Timestamp): Last update timestamp

**Validation Rules**:
- Student must be enrolled in the class
- Only one student per position per class (one 1st leader, one 2nd leader, one 3rd leader)
- Leadership position must be valid enum value
- Student cannot have multiple positions in the same class
- Assignment must be made by authorized user (administrator or class teacher)

**State Transitions**:
- Created → Active
- Active → Removed (if position reassigned)

**Relationships**:
- Many-to-one with Student (studentId)
- Many-to-one with Class (classId)
- Many-to-one with User (assignedBy)

**Database Schema**:
```sql
CREATE TABLE student_class_leadership (
    id UUID PRIMARY KEY,
    student_id UUID NOT NULL REFERENCES students(id),
    class_id UUID NOT NULL REFERENCES classes(id),
    leadership_position VARCHAR(20) NOT NULL CHECK (leadership_position IN ('FIRST_LEADER', 'SECOND_LEADER', 'THIRD_LEADER')),
    assigned_by UUID NOT NULL REFERENCES users(id),
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(class_id, leadership_position),
    UNIQUE(student_id, class_id)
);
```

---

## Integration Points

### Attendance Service → Academic Service

**REST API Call**: Validate class leader assignment
- Endpoint: `GET /api/v1/classes/{classId}/leaders/{studentId}`
- Purpose: Verify student is assigned as class leader for the class
- Used in: Delegation validation (FR-010, FR-011)

**Event Consumption**: StudentClassLeadershipAssignmentEvent
- Purpose: Update permissions when class leader assigned/unassigned
- Action: Grant/revoke COLLECT_ATTENDANCE permission in Keycloak

### Attendance Service → Notification Service

**Event Publishing**: 
- SessionDelegatedEvent → Notify class leader
- SessionCollectedEvent → Notify teacher
- SessionApprovedEvent → Notify class leader
- SessionRejectedEvent → Notify class leader

**Event Schema**: Versioned and documented in contracts/

### Attendance Service → User Service

**Permission Check**: 
- Validate COLLECT_ATTENDANCE permission for class leaders
- Validate MANAGE_ATTENDANCE permission for teachers
- Via Keycloak JWT token claims

---

## Data Flow

### Direct Marking Flow (No Session)
1. Teacher marks attendance directly
2. AttendanceRecord created with markedBy (no sessionId, collectedBy, approvedBy)
3. AttendanceMarkedEvent published
4. Attendance record immediately official

### Delegation Flow (Session-Based)
1. Teacher creates AttendanceSession (status: PENDING)
2. Teacher delegates to class leader (delegatedTo set)
3. SessionDelegatedEvent published
4. Class leader collects attendance (creates AttendanceRecords with sessionId, collectedBy)
5. Class leader submits session (status: COLLECTED)
6. SessionCollectedEvent published
7. Teacher reviews and approves (status: APPROVED, approvedBy set)
8. SessionApprovedEvent published
9. Attendance records become official

---

## Validation Rules Summary

**Session Validation**:
- One session per class per date
- DelegatedTo must be valid class leader for the class
- Date cannot be future

**Attendance Record Validation**:
- Direct marking: markedBy set, collectedBy null, sessionId null
- Session-based: markedBy null, collectedBy set, sessionId set
- One record per student per class per date

**Class Leader Assignment Validation**:
- One student per position per class
- Student must be enrolled in class
- Position must be valid (FIRST_LEADER, SECOND_LEADER, THIRD_LEADER)

## Configuration Artifacts

### Keycloak Provisioning Blueprint (shared/keycloak-initializer-core + user-service bootstrap)

Although not persisted in PostgreSQL, the initializer maintains a declarative configuration describing the security artifacts that must exist in Keycloak for this feature to operate.

**Artifacts Managed**:
- Realm: `school-management` (ensured to exist with authentication policies)
- Client: `school-management-client` (service + frontend access)
- Realm Roles: `SUPER_ADMIN`, `ADMINISTRATOR`, `TEACHER`, `STUDENT`, `PARENT`
- Client Roles (permissions): `COLLECT_ATTENDANCE`, `MANAGE_ATTENDANCE`, `COLLECT_EXAM_RESULTS`, `SUBMIT_REPORTS`, `VIEW_ALL_STUDENTS`, `MANAGE_GRADES`, `MANAGE_HOMEROOM`, `MANAGE_ADMINISTRATORS`
- Composite mappings aligning realm roles to permissions (e.g., TEACHER → MANAGE_ATTENDANCE)
- Realm attribute `sso.system.initialized=true` to mark completion for startup skip logic

**Execution Context**:
- `user-service` registers `KeycloakInitializerRunner` (`ApplicationRunner`) that executes during application startup
- Runner loads declarative blueprint from `shared/keycloak-initializer-core` and compares desired vs actual state
- If state matches and completion flag exists, runner logs "already initialized" and returns immediately
- If drift detected, runner reconciles differences and emits audit events before allowing readiness probes to succeed
- Feature toggle `keycloak.initializer.enabled` allows disabling the runner for specific environments/tests

**Idempotency Rules**:
- Each artifact is created if missing and updated when configuration drifts
- Realm and client attributes (token lifetimes, redirect URIs) are reconciled without destructive resets
- Skip path relies on both realm attribute and resource existence checks to avoid rework
- Execution logs published to the Audit Service to maintain compliance trail (via existing auditing schema)

These configuration guarantees align with the data model by ensuring that class leader assignments always correspond to valid Keycloak permissions while avoiding manual, script-based drift or redundant initialization during service restarts.

