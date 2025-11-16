# Tasks: Class Leader Attendance Delegation

**Input**: Design documents from `/Users/ratana/Documents/developments/web/school-management/specs/001-class-leader-attendance/`
**Prerequisites**: `plan.md`, `spec.md`, `research.md`, `data-model.md`, `contracts/`, `quickstart.md`
**Feature**: Implement session-based attendance delegation with a Keycloak initializer executed during `user-service` startup (idempotent, skip when already provisioned)

**Tests**: Constitution Principle IV requires test-first development with >80% coverage and comprehensive unit + integration tests. Each story phase begins with automated test tasks.

**Checklist Format**: `- [ ] T### [P] [US#] Description with absolute file path`

## Implementation Strategy

- **MVP Scope**: Deliver through Phase 3 (User Story 1) so teachers can delegate sessions end-to-end with Keycloak automatically set up by user-service startup.
- **Incremental Delivery**: Phase 1 → Phase 2 → US1 → US2 → US3 → Academic/Permission Sync → Polish.
- **Key Theme**: Security automation embedded in user-service startup with strict idempotency and observability.

## Dependencies

- **Story Order**: `US1 (P1)` → `US2 (P2)` → `US3 (P3)`.
- **Foundational Order**: Phases 1 & 2 complete before story phases start.
- **Cross-Service Order**: Attendance foundations precede Academic Service permission sync tasks.

## Parallel Execution Opportunities

- Initializer library development (Phase 1) can run alongside database/entity work (Phase 2).
- Academic Service enhancements (Phase 6) can proceed after Phase 2 in parallel with US3 once permission hooks defined.
- Documentation, metrics, and indexing (Phase 7) can run after main story work reaches code complete.

---

## Phase 1: Keycloak Initializer Integration (User Service Startup)

**Goal**: Replace the bash script with a shared Kotlin initializer library invoked automatically during `user-service` startup while remaining reusable for manual runs.

- [X] T001 Update Maven reactor to include `shared/keycloak-initializer-core` by editing `/Users/ratana/Documents/developments/web/school-management/pom.xml`
- [X] T002 Create module definition with dependencies (`keycloak-admin-client`, Spring Boot) in `/Users/ratana/Documents/developments/web/school-management/shared/keycloak-initializer-core/pom.xml`
- [X] T003 Scaffold initializer service classes (`InitializerService.kt`, model definitions) under `/Users/ratana/Documents/developments/web/school-management/shared/keycloak-initializer-core/src/main/kotlin/com/visor/school/keycloak/`
- [X] T004 Implement desired-state detection utilities (realm attribute + diff logic) in `/Users/ratana/Documents/developments/web/school-management/shared/keycloak-initializer-core/src/main/kotlin/com/visor/school/keycloak/detection/InitializerStateEvaluator.kt`
- [X] T005 Add configuration binding objects for admin credentials and retry policy in `/Users/ratana/Documents/developments/web/school-management/shared/keycloak-initializer-core/src/main/kotlin/com/visor/school/keycloak/config/InitializerProperties.kt`
- [X] T006 Register `KeycloakInitializerRunner` (`ApplicationRunner`) inside user-service at `/Users/ratana/Documents/developments/web/school-management/services/user-service/src/main/kotlin/com/visor/school/userservice/bootstrap/KeycloakInitializerRunner.kt`
- [X] T007 Wire initializer configuration and feature toggle in `/Users/ratana/Documents/developments/web/school-management/services/user-service/src/main/kotlin/com/visor/school/userservice/config/SecurityConfig.kt`
- [X] T008 Add bootstrap profile settings (`keycloak.initializer.*`) to `/Users/ratana/Documents/developments/web/school-management/services/user-service/src/main/resources/application-bootstrap.yml`
- [X] T009 Provide manual CLI wrapper using shared library in `/Users/ratana/Documents/developments/web/school-management/shared/keycloak-initializer-core/src/main/kotlin/com/visor/school/keycloak/cli/ManualInitializer.kt`
- [ ] T010 Update setup documentation and script notes in `/Users/ratana/Documents/developments/web/school-management/scripts/setup-keycloak.sh` to reference user-service startup behavior
- [ ] T011 Document startup flow & skip semantics in `/Users/ratana/Documents/developments/web/school-management/shared/keycloak-initializer-core/README.md`
- [ ] T012 [P] Create unit tests for detection logic in `/Users/ratana/Documents/developments/web/school-management/shared/keycloak-initializer-core/src/test/kotlin/com/visor/school/keycloak/detection/InitializerStateEvaluatorTest.kt`
- [ ] T013 [P] Add Testcontainers Keycloak integration test covering first-run + skip paths in `/Users/ratana/Documents/developments/web/school-management/services/user-service/src/test/kotlin/com/visor/school/userservice/bootstrap/KeycloakInitializerIntegrationTest.kt`

---

## Phase 2: Foundational Domain & Security Setup

**Goal**: Establish database schema, entities, repositories, shared security utilities, and event models required by all stories.

### Database & Entities

- [ ] T014 Create Flyway migration for `attendance_sessions` table at `/Users/ratana/Documents/developments/web/school-management/services/attendance-service/src/main/resources/db/migration/V901__create_attendance_sessions.sql`
- [ ] T015 Extend `attendance_records` with session fields via `/Users/ratana/Documents/developments/web/school-management/services/attendance-service/src/main/resources/db/migration/V902__extend_attendance_records.sql`
- [ ] T016 Create `student_class_leadership` table migration at `/Users/ratana/Documents/developments/web/school-management/services/academic-service/src/main/resources/db/migration/V701__create_student_class_leadership.sql`
- [ ] T017 Implement `AttendanceSession.kt` and `AttendanceSessionStatus.kt` in `/Users/ratana/Documents/developments/web/school-management/services/attendance-service/src/main/kotlin/com/visor/school/attendanceservice/model/`
- [ ] T018 Extend `AttendanceRecord.kt` for session-aware fields at `/Users/ratana/Documents/developments/web/school-management/services/attendance-service/src/main/kotlin/com/visor/school/attendanceservice/model/AttendanceRecord.kt`
- [ ] T019 Create `StudentClassLeadership.kt` and `LeadershipPosition.kt` in `/Users/ratana/Documents/developments/web/school-management/services/academic-service/src/main/kotlin/com/visor/school/academicservice/model/`

### Repository & Client Layer

- [ ] T020 Create `AttendanceSessionRepository.kt` with required queries at `/Users/ratana/Documents/developments/web/school-management/services/attendance-service/src/main/kotlin/com/visor/school/attendanceservice/repository/AttendanceSessionRepository.kt`
- [ ] T021 Extend `AttendanceRepository.kt` for session lookups at `/Users/ratana/Documents/developments/web/school-management/services/attendance-service/src/main/kotlin/com/visor/school/attendanceservice/repository/AttendanceRepository.kt`
- [ ] T022 Create `StudentClassLeadershipRepository.kt` in `/Users/ratana/Documents/developments/web/school-management/services/academic-service/src/main/kotlin/com/visor/school/academicservice/repository/`
- [ ] T023 [P] Implement resilient REST client for academic leader validation (`AcademicServiceClient.kt`) at `/Users/ratana/Documents/developments/web/school-management/services/attendance-service/src/main/kotlin/com/visor/school/attendanceservice/client/`

### Security & Permissions

- [ ] T024 [P] Add permission constants + helpers in `/Users/ratana/Documents/developments/web/school-management/services/common/src/main/kotlin/com/visor/school/common/security/Permissions.kt`
- [ ] T025 [P] Implement `AttendancePermissionEvaluator.kt` in `/Users/ratana/Documents/developments/web/school-management/services/attendance-service/src/main/kotlin/com/visor/school/attendanceservice/security/`
- [ ] T026 [P] Update Spring Security configuration to wire evaluators in `/Users/ratana/Documents/developments/web/school-management/services/attendance-service/src/main/kotlin/com/visor/school/attendanceservice/config/SecurityConfig.kt`
- [ ] T027 [P] Add Micrometer counters for initializer runs (`status=created|skipped`) in `/Users/ratana/Documents/developments/web/school-management/services/user-service/src/main/kotlin/com/visor/school/userservice/metrics/KeycloakInitializerMetrics.kt`

### Event Schema & Contracts

- [ ] T028 [P] Define session lifecycle events (`SessionDelegated`, `SessionCollected`, `SessionApproved`, `SessionRejected`) in `/Users/ratana/Documents/developments/web/school-management/services/attendance-service/src/main/kotlin/com/visor/school/attendanceservice/event/`
- [ ] T029 [P] Update event contracts in `/Users/ratana/Documents/developments/web/school-management/specs/001-class-leader-attendance/contracts/attendance-session-events.yaml`

---

## Phase 3: User Story 1 – Teacher Delegates Attendance (Priority P1)

**Goal**: Teachers create sessions, delegate to class leaders, and ensure notifications/audit logging.
**Independent Test**: Teacher creates session and delegates to a valid class leader; invalid assignments are rejected; event + audit generated.

### Tests First

- [ ] T030 [P] [US1] Add `AttendanceSessionServiceTest.kt` covering create/list logic at `/Users/ratana/Documents/developments/web/school-management/services/attendance-service/src/test/kotlin/com/visor/school/attendanceservice/service/`
- [ ] T031 [P] [US1] Add `AttendanceDelegationServiceTest.kt` validating class leader assignment enforcement in same directory
- [ ] T032 [P] [US1] Extend controller tests for create/delegate endpoints in `/Users/ratana/Documents/developments/web/school-management/services/attendance-service/src/test/kotlin/com/visor/school/attendanceservice/controller/AttendanceSessionControllerTest.kt`
- [ ] T033 [P] [US1] Create academic client contract tests at `/Users/ratana/Documents/developments/web/school-management/services/attendance-service/src/test/kotlin/com/visor/school/attendanceservice/client/AcademicServiceClientTest.kt`

### Implementation

- [ ] T034 [US1] Implement session creation/listing in `/Users/ratana/Documents/developments/web/school-management/services/attendance-service/src/main/kotlin/com/visor/school/attendanceservice/service/AttendanceSessionService.kt`
- [ ] T035 [US1] Implement delegation workflow (validation + status management) in `/Users/ratana/Documents/developments/web/school-management/services/attendance-service/src/main/kotlin/com/visor/school/attendanceservice/service/AttendanceDelegationService.kt`
- [ ] T036 [US1] Implement REST endpoints (create, delegate, list, get) in `/Users/ratana/Documents/developments/web/school-management/services/attendance-service/src/main/kotlin/com/visor/school/attendanceservice/controller/AttendanceSessionController.kt`
- [ ] T037 [US1] Publish `SessionDelegatedEvent` via `/Users/ratana/Documents/developments/web/school-management/services/attendance-service/src/main/kotlin/com/visor/school/attendanceservice/event/AttendanceEventPublisher.kt`
- [ ] T038 [US1] Add audit logging hooks for session creation/delegation in `/Users/ratana/Documents/developments/web/school-management/services/attendance-service/src/main/kotlin/com/visor/school/attendanceservice/service/`
- [ ] T039 [P] [US1] Update OpenAPI definitions for session endpoints in `/Users/ratana/Documents/developments/web/school-management/specs/001-class-leader-attendance/contracts/attendance-session-api.yaml`

---

## Phase 4: User Story 2 – Class Leader Collects Attendance (Priority P2)

**Goal**: Delegated class leaders capture attendance and submit sessions for approval.
**Independent Test**: Class leader views delegated session, submits full attendance, status transitions to COLLECTED, event emitted.

### Tests First

- [ ] T040 [P] [US2] Add `AttendanceCollectionServiceTest.kt` with permission enforcement in `/Users/ratana/Documents/developments/web/school-management/services/attendance-service/src/test/kotlin/com/visor/school/attendanceservice/service/`
- [ ] T041 [P] [US2] Extend controller tests for collect endpoint at `/Users/ratana/Documents/developments/web/school-management/services/attendance-service/src/test/kotlin/com/visor/school/attendanceservice/controller/AttendanceSessionControllerTest.kt`
- [ ] T042 [P] [US2] Add permission evaluator tests in `/Users/ratana/Documents/developments/web/school-management/services/attendance-service/src/test/kotlin/com/visor/school/attendanceservice/security/AttendancePermissionEvaluatorTest.kt`

### Implementation

- [ ] T043 [US2] Implement collection + submit logic in `/Users/ratana/Documents/developments/web/school-management/services/attendance-service/src/main/kotlin/com/visor/school/attendanceservice/service/AttendanceCollectionService.kt`
- [ ] T044 [US2] Wire collect endpoint with permission checks in `/Users/ratana/Documents/developments/web/school-management/services/attendance-service/src/main/kotlin/com/visor/school/attendanceservice/controller/AttendanceSessionController.kt`
- [ ] T045 [US2] Publish `SessionCollectedEvent` and notifications via `/Users/ratana/Documents/developments/web/school-management/services/attendance-service/src/main/kotlin/com/visor/school/attendanceservice/event/AttendanceEventPublisher.kt`
- [ ] T046 [US2] Extend audit logging for collection submission in `/Users/ratana/Documents/developments/web/school-management/services/attendance-service/src/main/kotlin/com/visor/school/attendanceservice/service/AttendanceCollectionService.kt`

---

## Phase 5: User Story 3 – Teacher Approves/Rejects & Direct Marking (Priority P3)

**Goal**: Teachers finalize collected sessions and continue to mark attendance directly when needed.
**Independent Test**: Teacher approves or rejects a collected session with audit trail; direct marking bypasses session workflow safely.

### Tests First

- [ ] T047 [P] [US3] Add `AttendanceApprovalServiceTest.kt` in `/Users/ratana/Documents/developments/web/school-management/services/attendance-service/src/test/kotlin/com/visor/school/attendanceservice/service/`
- [ ] T048 [P] [US3] Add `AttendanceDirectMarkingServiceTest.kt` in same directory
- [ ] T049 [P] [US3] Extend controller tests for approve/reject endpoints in `/Users/ratana/Documents/developments/web/school-management/services/attendance-service/src/test/kotlin/com/visor/school/attendanceservice/controller/AttendanceSessionControllerTest.kt`
- [ ] T050 [P] [US3] Add direct marking controller tests in `/Users/ratana/Documents/developments/web/school-management/services/attendance-service/src/test/kotlin/com/visor/school/attendanceservice/controller/AttendanceControllerTest.kt`

### Implementation

- [ ] T051 [US3] Implement approval/rejection flows in `/Users/ratana/Documents/developments/web/school-management/services/attendance-service/src/main/kotlin/com/visor/school/attendanceservice/service/AttendanceApprovalService.kt`
- [ ] T052 [US3] Implement direct marking service in `/Users/ratana/Documents/developments/web/school-management/services/attendance-service/src/main/kotlin/com/visor/school/attendanceservice/service/AttendanceDirectMarkingService.kt`
- [ ] T053 [US3] Wire approve/reject endpoints in `/Users/ratana/Documents/developments/web/school-management/services/attendance-service/src/main/kotlin/com/visor/school/attendanceservice/controller/AttendanceSessionController.kt`
- [ ] T054 [US3] Wire direct marking endpoint in `/Users/ratana/Documents/developments/web/school-management/services/attendance-service/src/main/kotlin/com/visor/school/attendanceservice/controller/AttendanceController.kt`
- [ ] T055 [US3] Publish approval/rejection/direct-marking events via `/Users/ratana/Documents/developments/web/school-management/services/attendance-service/src/main/kotlin/com/visor/school/attendanceservice/event/AttendanceEventPublisher.kt`
- [ ] T056 [US3] Extend audit logging for approval, rejection, and direct marking in relevant service classes under `/Users/ratana/Documents/developments/web/school-management/services/attendance-service/src/main/kotlin/com/visor/school/attendanceservice/service/`

---

## Phase 6: Academic Service Permission Sync & Keycloak Hooks

**Goal**: Ensure class leader assignments stay synchronized with Keycloak permissions using shared initializer utilities.

- [ ] T057 [P] Implement `StudentClassLeadershipService.kt` CRUD + validation at `/Users/ratana/Documents/developments/web/school-management/services/academic-service/src/main/kotlin/com/visor/school/academicservice/service/StudentClassLeadershipService.kt`
- [ ] T058 [P] Create REST controller endpoints in `/Users/ratana/Documents/developments/web/school-management/services/academic-service/src/main/kotlin/com/visor/school/academicservice/controller/StudentClassLeadershipController.kt`
- [ ] T059 [P] Add unit tests for service/controller logic in `/Users/ratana/Documents/developments/web/school-management/services/academic-service/src/test/kotlin/com/visor/school/academicservice/`
- [ ] T060 [P] Emit leader assignment/removal events in `/Users/ratana/Documents/developments/web/school-management/services/academic-service/src/main/kotlin/com/visor/school/academicservice/event/StudentClassLeadershipEventPublisher.kt`
- [ ] T061 [P] Hook Keycloak permission grant/revoke in `StudentClassLeadershipService.kt` using shared initializer client utilities
- [ ] T062 [P] Expose shared initializer client bean in `/Users/ratana/Documents/developments/web/school-management/services/common/src/main/kotlin/com/visor/school/common/security/KeycloakInitializerClientFactory.kt`
- [ ] T063 [P] Add integration test ensuring Keycloak permissions update when leaders change in `/Users/ratana/Documents/developments/web/school-management/services/academic-service/src/test/kotlin/com/visor/school/academicservice/integration/StudentClassLeadershipIntegrationTest.kt`

---

## Phase 7: Polish, Observability & Documentation

**Goal**: Final integration tests, metrics, documentation, and performance tuning.

- [ ] T064 [P] Add end-to-end integration test covering delegation → collection → approval in `/Users/ratana/Documents/developments/web/school-management/services/attendance-service/src/test/kotlin/com/visor/school/attendanceservice/integration/AttendanceDelegationWorkflowIntegrationTest.kt`
- [ ] T065 [P] Add direct marking integration test in `/Users/ratana/Documents/developments/web/school-management/services/attendance-service/src/test/kotlin/com/visor/school/attendanceservice/integration/AttendanceDirectMarkingIntegrationTest.kt`
- [ ] T066 [P] Add notification consumer tests for new events in `/Users/ratana/Documents/developments/web/school-management/services/notification-service/src/test/kotlin/com/visor/school/notificationservice/event/AttendanceEventConsumerTest.kt`
- [ ] T067 [P] Implement notification handlers (delegated/collected/approved/rejected) at `/Users/ratana/Documents/developments/web/school-management/services/notification-service/src/main/kotlin/com/visor/school/notificationservice/event/AttendanceEventConsumer.kt`
- [ ] T068 [P] Add Prometheus metrics for session lifecycle in `/Users/ratana/Documents/developments/web/school-management/services/attendance-service/src/main/kotlin/com/visor/school/attendanceservice/metrics/AttendanceMetrics.kt`
- [ ] T069 [P] Add database indexes for performance in `/Users/ratana/Documents/developments/web/school-management/services/attendance-service/src/main/resources/db/migration/V903__attendance_session_indexes.sql`
- [ ] T070 [P] Document event schemas + initializer metrics in `/Users/ratana/Documents/developments/web/school-management/services/attendance-service/docs/attendance-session-events.md`
- [ ] T071 [P] Update feature quickstart + security blueprint docs in `/Users/ratana/Documents/developments/web/school-management/specs/001-class-leader-attendance/quickstart.md` and `/Users/ratana/Documents/developments/web/school-management/shared/keycloak-initializer-core/docs/security-blueprint.md`
- [ ] T072 [P] Run ktlint, detekt, unit, and integration suites; publish coverage via `/Users/ratana/Documents/developments/web/school-management/scripts/verify-test-coverage.sh`
- [ ] T073 [P] Update changelog entries in `/Users/ratana/Documents/developments/web/school-management/CHANGELOG.md`
- [ ] T074 Add constitution coverage gate enforcement (fail build if <80% coverage or <100 unit tests) in `/Users/ratana/Documents/developments/web/school-management/scripts/verify-test-coverage.sh`
- [ ] T075 [P] Capture measurable definitions for SC-001–SC-005 and automate performance checks in `/Users/ratana/Documents/developments/web/school-management/docs/performance/attendance-success-criteria.md`
- [ ] T076 [P] Add automated security validation for SC-006 (unauthorized access attempts) in `/Users/ratana/Documents/developments/web/school-management/services/attendance-service/src/test/kotlin/com/visor/school/attendanceservice/security/UnauthorizedAccessTest.kt`
- [ ] T077 [P] Instrument audit latency measurement for SC-007 via `/Users/ratana/Documents/developments/web/school-management/scripts/verify-audit-latency.sh`
- [ ] T078 [P] Define and document edge case handling (leader absent, timeout, multi-leader, enrollment changes) in `/Users/ratana/Documents/developments/web/school-management/specs/001-class-leader-attendance/docs/edge-cases.md`

---

## Summary

- **Total Tasks**: 78
- **Task Count by User Story**:
  - US1 Phase: 6 implementation tasks + 4 test/documentation tasks (10 total)
  - US2 Phase: 4 implementation tasks + 3 test tasks (7 total)
  - US3 Phase: 6 implementation tasks + 4 test tasks (10 total)
- **Parallel Opportunities**: Marked with `[P]` across initializer tests, security utilities, academic permission sync, and polish work.
- **Independent Test Criteria**:
  - **US1**: Teacher can create and delegate sessions; invalid delegates rejected; events + audits captured.
  - **US2**: Delegated class leader collects and submits attendance; session transitions to COLLECTED with notifications.
  - **US3**: Teacher approves/rejects or marks directly; audit log and events confirm outcomes without duplicate sessions.
- **Suggested MVP**: Complete Phases 1–3 (initializer integration + US1) to deliver delegation workflow with automated Keycloak provisioning.

**Format Validation**: All tasks follow `- [ ] T### [P] [US#] Description with absolute path`; user story phases include `[US#]`, other phases omit story labels.

