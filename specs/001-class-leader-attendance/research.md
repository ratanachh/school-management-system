# Research & Technical Decisions: Class Leader Attendance Delegation

**Date**: 2025-11-09  
**Feature**: Class Leader Attendance Delegation  
**Purpose**: Document research findings and technical decisions for implementation planning

## Integration Decisions

### Decision: Extend Existing Attendance Service (Not New Service)

**Rationale**:
- Feature is an extension of attendance tracking functionality, not a separate domain
- Reduces deployment complexity and service count
- Maintains data consistency within single service boundary
- Reuses existing attendance infrastructure (database, repositories, services)
- Aligns with microservices principle of cohesive service boundaries

**Architectural Decision**:
- Add new entities (AttendanceSession) to Attendance Service
- Extend existing AttendanceRecord entity with session-based workflow fields
- Add new service classes (AttendanceSessionService, AttendanceDelegationService)
- Extend existing AttendanceService with direct marking capability
- New endpoints in AttendanceSessionController
- Extend existing AttendanceController for direct marking

**Alternatives Considered**:
- New dedicated service: Unnecessary complexity, tight coupling with attendance data
- Separate session service: Would require cross-service transactions, violates service boundaries

### Decision: Permission-Based Access Control for Class Leaders

**Rationale**:
- Class leaders are students, not teachers - need specific permission to collect attendance
- Follows existing School Management System permission model (COLLECT_ATTENDANCE permission)
- Integrates with existing Keycloak infrastructure
- Enables fine-grained access control beyond basic roles

**Architectural Decision**:
- Class leaders require COLLECT_ATTENDANCE permission assigned through Keycloak
- Permission checked at API endpoints using Spring Security permission evaluators
- Permission assigned when student is designated as class leader (1st, 2nd, or 3rd leader)
- Teachers require MANAGE_ATTENDANCE permission to delegate and approve sessions
- Permission validation prevents unauthorized access attempts

**Alternatives Considered**:
- Role-based only: Insufficient granularity - all students would have same access
- Custom permission system: Duplicates Keycloak functionality

### Decision: Event-Driven Notification Integration

**Rationale**:
- Follows existing School Management System event-driven architecture
- Decouples notification delivery from attendance service
- Enables asynchronous notification processing
- Supports multiple notification channels (email, in-app, SMS)

**Architectural Decision**:
- Publish SessionDelegatedEvent when teacher delegates to class leader
- Publish SessionCollectedEvent when class leader submits for approval
- Publish SessionApprovedEvent when teacher approves session
- Publish SessionRejectedEvent when teacher rejects session
- Events consumed by Notification Service for user notifications
- Event schemas versioned and documented

**Alternatives Considered**:
- Synchronous notification: Would block attendance operations, poor performance
- Direct notification service calls: Tight coupling, violates service boundaries

### Decision: Session-Based Workflow Model

**Rationale**:
- Sessions represent attendance collection periods requiring teacher approval
- Enables delegation workflow distinct from direct marking
- Supports state management (pending, collected, approved, rejected)
- Maintains audit trail of session state changes

**Architectural Decision**:
- AttendanceSession entity with status enum (PENDING, COLLECTED, APPROVED, REJECTED)
- AttendanceRecord extended with sessionId (optional - null for direct marking)
- AttendanceRecord extended with collectedBy (Student FK, optional - for class leader collection)
- AttendanceRecord extended with approvedBy (Teacher FK, optional - for approved sessions)
- Direct marking bypasses session workflow (sessionId, collectedBy, approvedBy remain null)
- Session workflow: Create → Delegate → Collect → Approve/Reject

**Alternatives Considered**:
- Status-only workflow: Insufficient state tracking, no delegation history
- Separate delegation table: Over-normalization, complicates queries

### Decision: Class Leader Assignment Management

**Rationale**:
- Class leaders are assigned to specific classes (not global)
- Multiple class leader positions per class (1st, 2nd, 3rd leader)
- Assignment validation needed before delegation
- Assignment management is academic domain concern

**Architectural Decision**:
- StudentClassLeadership entity in Academic Service (not Attendance Service)
- Junction table: studentId, classId, leadershipPosition (FIRST_LEADER, SECOND_LEADER, THIRD_LEADER)
- Validation: Only one student per position per class (one 1st leader, one 2nd leader, one 3rd leader)
- Attendance Service validates assignments via REST API call to Academic Service
- Assignment changes trigger permission updates in Keycloak

**Alternatives Considered**:
- Assignment in Attendance Service: Violates service boundaries (academic domain)
- Global class leader roles: Doesn't reflect class-specific assignments

### Decision: Kotlin-Based Keycloak Initialization (Embedded in User Service Startup)

**Rationale**:
- Need fully automated, repeatable Keycloak provisioning without manual scripts
- Running during `user-service` startup guarantees Keycloak stays synchronized before any user authentication occurs
- Startup hook enables coordinated retries and health gating when Keycloak is temporarily unavailable

**Architectural Decision**:
- Create reusable library module `shared/keycloak-initializer-core` containing provisioning logic and idempotency checks
- `user-service` registers an `ApplicationRunner` (`KeycloakInitializerRunner`) that executes after Spring context loads but before readiness probe reports healthy
- Runner queries Keycloak via admin client to detect whether realm, client, roles, and composite mappings already exist
- If configuration matches desired state, initializer logs "already initialized" and exits quickly (<1s)
- If configuration is missing or drifted, initializer applies the diff and records audit events
- Initialization failures prevent `user-service` readiness (fail-fast) while employing exponential backoff for transient Keycloak connectivity issues

**Alternatives Considered**:
- Separate CLI executed manually/CI: risk of drift and missed environments; new requirement mandates automatic run
- Embedding logic in attendance-service: user authentication bootstrap belongs near identity provider entry point (user-service), so this placement keeps concerns aligned

### Decision: Keycloak Initializer Testing Strategy

**Rationale**:
- Need confidence that provisioning logic remains idempotent across restarts & already-initialized environments
- Must validate detection branch (skip when already configured)

**Architectural Decision**:
- Use Testcontainers `quay.io/keycloak/keycloak:24.0.0` for integration tests covering first-run provisioning and subsequent startup skip path
- Provide contract tests verifying detection logic by pre-creating realm/roles before runner executes
- Add metrics (e.g., `keycloak.initializer.runs` with labels `status=created|skipped`) exported via Micrometer for observability

**Alternatives Considered**:
- Mock admin client: insufficient to validate skip path and compatibility across Keycloak releases
- On-demand staging server: introduces external dependency and complicates CI automation

### Decision: Initializer Invocation Model

**Rationale**:
- Requirement dictates automatic execution when user-service starts; must avoid re-running expensive operations on every boot if Keycloak already configured

**Architectural Decision**:
- `user-service` ApplicationRunner invokes initializer library exactly once per startup
- Runner caches success in Keycloak (via custom realm attribute `sso.system.initialized=true`) and optionally writes to Config Server state; detection checks this attribute plus resource existence
- Provide feature toggle (`keycloak.initializer.enabled`) to disable in non-admin environments or tests
- Keep CLI entry point available by creating a thin wrapper that depends on the same library, allowing manual re-provisioning when necessary

**Alternatives Considered**:
- Scheduled task inside user-service: complicates readiness and detection semantics
- External orchestrator: adds operational burden and still needs skip logic

## Conclusion

All clarifications from the implementation plan are resolved. Embedding the initializer in user-service startup with strong idempotency guarantees satisfies the new requirement while aligning with constitution principles. Existing decisions regarding service boundaries, permissions, events, and workflow remain valid and provide the baseline for implementation.

