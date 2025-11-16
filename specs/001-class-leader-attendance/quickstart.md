# Quick Start: Class Leader Attendance Delegation

**Feature**: Class Leader Attendance Delegation with Teacher Approval Workflow  
**Branch**: `001-class-leader-attendance`  
**Date**: 2025-11-09

This guide provides step-by-step instructions for setting up and testing the class leader attendance delegation feature.

## Prerequisites

- School Management System infrastructure running (see main project quickstart.md)
- Attendance Service deployed and running
- Academic Service deployed and running (for class leader assignments)
- RabbitMQ running for event publishing
- PostgreSQL 17 database for Attendance Service
- JDK 25 + Maven wrapper available locally
- Keycloak 24.0.0 accessible and configured with admin credentials (stored in Config Server / Vault)

## Setup Steps

### 1. Database Migrations

Run Flyway migrations to add new tables:

```bash
# Navigate to Attendance Service
cd /Users/ratana/Documents/developments/web/school-management/services/attendance-service

# Run migrations
./mvnw flyway:migrate

# Verify tables created
# - attendance_sessions
# - attendance_records (extended with collected_by, session_id, approved_by)
```

### 2. Configure User Service Startup Initializer

The Kotlin initializer now executes automatically when `user-service` starts. It provisions Keycloak on first run and skips if the realm/client/role configuration already matches the blueprint.

```bash
# From repo root, start user-service (local profile)
cd /Users/ratana/Documents/developments/web/school-management

export KEYCLOAK_URL="http://localhost:8070"
export KEYCLOAK_ADMIN="admin"
export KEYCLOAK_ADMIN_PASSWORD="<admin-password>"
export KEYCLOAK_ADMIN_CLIENT_ID="admin-cli"
export KEYCLOAK_REALM="school-management"
export KEYCLOAK_CLIENT_ID="school-management-client"
export KEYCLOAK_INITIALIZER_ENABLED=true

./mvnw -pl services/user-service spring-boot:run -Dspring-boot.run.profiles=local

# Logs include either:
#   INFO ... keycloak-initializer: Applied realm/client/role configuration
# or
#   INFO ... keycloak-initializer: Keycloak already initialized (skip)
```

If Keycloak is unavailable at startup, the initializer retries with exponential backoff before failing the service boot (maintaining security guarantees).

### 3. Optional Manual Re-Provisioning

A CLI wrapper remains available for manual runs (uses the same library logic and skip checks):

```bash
./mvnw -pl shared/keycloak-initializer-core -am exec:java \
  -Dexec.mainClass=com.visor.school.keycloak.cli.ManualInitializer \
  -Dspring.profiles.active=bootstrap
```

### 4. Configure Class Leader Assignments

1. Assign students as class leaders via Academic Service:
   ```bash
   POST /api/v1/classes/{classId}/leaders
   {
     "studentId": "uuid",
     "leadershipPosition": "FIRST_LEADER" | "SECOND_LEADER" | "THIRD_LEADER"
   }
   ```

2. Verify assignment triggers permission update in Keycloak (attendance-service listens for assignment events and grants COLLECT_ATTENDANCE via initializer utilities).

## Testing the Feature

### Test 1: Teacher Creates and Delegates Session

**Prerequisites**: Teacher logged in with MANAGE_ATTENDANCE permission

```bash
# 1. Create attendance session
POST /api/v1/attendance/sessions
Authorization: Bearer <teacher-jwt-token>
Content-Type: application/json

{
  "classId": "class-uuid",
  "date": "2025-01-27"
}

# Expected: 201 Created
# Response includes session ID and status: PENDING

# 2. Delegate to class leader
POST /api/v1/attendance/sessions/{sessionId}/delegate
Authorization: Bearer <teacher-jwt-token>
Content-Type: application/json

{
  "classLeaderId": "student-uuid"
}

# Expected: 200 OK
# Response shows status: PENDING, delegatedTo set
# SessionDelegatedEvent published
```

**Verification**:
- Session created with status PENDING
- Session assigned to class leader
- Notification sent to class leader
- Event published to RabbitMQ

### Test 2: Class Leader Collects Attendance

**Prerequisites**: Class leader logged in with COLLECT_ATTENDANCE permission

```bash
# 1. View pending sessions
GET /api/v1/attendance/sessions?delegatedTo={studentId}&status=PENDING
Authorization: Bearer <class-leader-jwt-token>

# Expected: 200 OK
# Response includes delegated session

# 2. Collect attendance
POST /api/v1/attendance/sessions/{sessionId}/collect
Authorization: Bearer <class-leader-jwt-token>
Content-Type: application/json

{
  "attendanceEntries": [
    {
      "studentId": "student-1-uuid",
      "status": "PRESENT"
    },
    {
      "studentId": "student-2-uuid",
      "status": "ABSENT",
      "notes": "Sick"
    },
    {
      "studentId": "student-3-uuid",
      "status": "LATE"
    }
  ]
}

# Expected: 200 OK
# Response shows status: COLLECTED
# SessionCollectedEvent published
```

**Verification**:
- Attendance records created with sessionId and collectedBy
- Session status changed to COLLECTED
- Notification sent to teacher
- Event published to RabbitMQ

### Test 3: Teacher Approves Session

**Prerequisites**: Teacher logged in, session in COLLECTED status

```bash
# 1. View collected session
GET /api/v1/attendance/sessions/{sessionId}
Authorization: Bearer <teacher-jwt-token>

# Expected: 200 OK
# Response includes all attendance records

# 2. Approve session
POST /api/v1/attendance/sessions/{sessionId}/approve
Authorization: Bearer <teacher-jwt-token>

# Expected: 200 OK
# Response shows status: APPROVED
# SessionApprovedEvent published
```

**Verification**:
- Session status changed to APPROVED
- Attendance records marked with approvedBy
- Notification sent to class leader
- Event published to RabbitMQ
- Attendance records are now official

### Test 4: Teacher Rejects Session

**Prerequisites**: Teacher logged in, session in COLLECTED status

```bash
# 1. Reject session
POST /api/v1/attendance/sessions/{sessionId}/reject
Authorization: Bearer <teacher-jwt-token>
Content-Type: application/json

{
  "reason": "Missing attendance entries for 3 students. Please complete all entries."
}

# Expected: 200 OK
# Response shows status: REJECTED
# SessionRejectedEvent published
```

**Verification**:
- Session status changed to REJECTED
- Rejection reason stored
- Notification sent to class leader with reason
- Class leader can resubmit after corrections

### Test 5: Teacher Marks Attendance Directly

**Prerequisites**: Teacher logged in with MANAGE_ATTENDANCE permission

```bash
# Mark attendance directly (bypasses session workflow)
POST /api/v1/attendance/mark
Authorization: Bearer <teacher-jwt-token>
Content-Type: application/json

{
  "classId": "class-uuid",
  "date": "2025-01-27",
  "attendanceEntries": [
    {
      "studentId": "student-1-uuid",
      "status": "PRESENT"
    },
    {
      "studentId": "student-2-uuid",
      "status": "ABSENT"
    }
  ]
}

# Expected: 201 Created
# Response includes attendance records
# AttendanceMarkedEvent published
```

**Verification**:
- Attendance records created immediately (no session)
- Records have markedBy set (no collectedBy, sessionId, approvedBy)
- Records are immediately official
- Event published to RabbitMQ

### Test 6: Conflict Prevention

**Test**: Prevent both direct marking and delegation for same date+class

```bash
# 1. Create session for date
POST /api/v1/attendance/sessions
{
  "classId": "class-uuid",
  "date": "2025-01-27"
}

# 2. Attempt direct marking for same date+class
POST /api/v1/attendance/mark
{
  "classId": "class-uuid",
  "date": "2025-01-27",
  ...
}

# Expected: 409 Conflict
# Error: "Attendance session already exists for this date and class"
```

## Integration Testing

### End-to-End Workflow Test

1. **Setup**:
   - Create class with 30 students
   - Assign 3 class leaders (1st, 2nd, 3rd leader)
   - Assign teacher to class
   - Grant COLLECT_ATTENDANCE permission to class leaders
   - Grant MANAGE_ATTENDANCE permission to teacher

2. **Delegation Flow**:
   - Teacher creates session → Status: PENDING
   - Teacher delegates to 1st leader → Status: PENDING, delegatedTo set
   - Class leader collects attendance → Status: COLLECTED
   - Teacher approves → Status: APPROVED
   - Verify attendance records are official

3. **Direct Marking Flow**:
   - Teacher marks attendance directly → Records created immediately
   - Verify no session created
   - Verify records are official

4. **Rejection Flow**:
   - Teacher rejects collected session → Status: REJECTED
   - Class leader views rejection reason
   - Class leader resubmits → Status: COLLECTED
   - Teacher approves → Status: APPROVED

## Performance Testing

### Load Test Scenarios

1. **Concurrent Delegations**:
   - 50 teachers delegate sessions simultaneously
   - Verify: All delegations complete in under 30 seconds each

2. **Concurrent Collections**:
   - 50 class leaders collect attendance simultaneously
   - Verify: All collections complete in under 5 minutes each

3. **Concurrent Approvals**:
   - 50 teachers approve sessions simultaneously
   - Verify: All approvals complete in under 2 minutes each

## Troubleshooting

### Common Issues

1. **Permission Denied (403)**:
   - Verify JWT token includes COLLECT_ATTENDANCE permission
   - Check Keycloak permission assignment
   - Verify class leader is assigned to the class

2. **Session Not Found (404)**:
   - Verify session ID is correct
   - Check session exists in database
   - Verify user has permission to view session

3. **Conflict (409)**:
   - Check for duplicate session (same date+class)
   - Verify session is in correct state for operation
   - Check for concurrent modifications

4. **Event Not Published**:
   - Verify RabbitMQ is running
   - Check event publisher configuration
   - Verify event schema matches consumer expectations

## Next Steps

1. Run `/speckit.tasks` to generate implementation tasks
2. Begin implementation following tasks.md
3. Update main project spec (001-school-management-system) to include class leader delegation
4. Integrate with existing Attendance Service codebase

