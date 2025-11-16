# Tasks: School Management System

**Input**: Design documents from `/specs/001-school-management-system/`
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: Tests are REQUIRED per Constitution Principle IV (Test-First Development, >80% coverage, 100+ unit tests per service)

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- **Microservices**: `platform/` and `services/` directories at repository root
- Each service follows Spring Boot structure: `src/main/kotlin/` and `src/test/kotlin/`
- Infrastructure services in `docker/docker-compose.yml`

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and infrastructure setup

- [X] T001 Create project directory structure (platform/, services/, docker/, scripts/)
- [X] T002 Create docker/docker-compose.yml with PostgreSQL, RabbitMQ, Elasticsearch, Keycloak, MinIO
- [X] T003 [P] Initialize platform/config-server with Spring Boot 3.5.6 and Spring Cloud Config
- [X] T004 [P] Initialize platform/discovery-server with Spring Boot 3.5.6 and Eureka
- [X] T005 [P] Initialize platform/api-gateway with Spring Boot 3.5.6 and Spring Cloud Gateway
- [X] T006 [P] Initialize services/user-service with Spring Boot 3.5.6, Kotlin 2.2.20, Maven
- [X] T007 [P] Initialize services/academic-service with Spring Boot 3.5.6, Kotlin 2.2.20, Maven
- [X] T008 [P] Initialize services/attendance-service with Spring Boot 3.5.6, Kotlin 2.2.20, Maven
- [X] T009 [P] Initialize services/academic-assessment-service with Spring Boot 3.5.6, Kotlin 2.2.20, Maven
- [X] T010 [P] Configure ktlint for Kotlin code formatting in all services
- [X] T011 [P] Configure Maven parent POM with shared dependencies (Spring Boot, Spring Cloud, JUnit 5, Mockito)
- [X] T012 Create scripts/setup.sh for project initialization
- [X] T013 Create scripts/build-all.sh for building all services

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [X] T014 Setup Config Server database schema and encrypted configuration storage in platform/config-server/src/main/resources/application.yml
- [X] T015 [P] Implement Config Server REST endpoints in platform/config-server/src/main/kotlin/com/visor/school/configserver/ConfigServerApplication.kt
- [X] T016 [P] Implement Eureka Discovery Server in platform/discovery-server/src/main/kotlin/com/visor/school/discovery/DiscoveryServerApplication.kt
- [X] T017 [P] Implement API Gateway routing configuration in platform/api-gateway/src/main/resources/application.yml
- [X] T018 [P] Setup Keycloak realm and OAuth2 clients (manual configuration or automation script)
- [X] T019 [P] Create shared Spring Security configuration for Keycloak JWT validation in platform/api-gateway/src/main/kotlin/com/visor/school/gateway/security/
- [X] T020 [P] Create shared event schema library in services/common-events/ for RabbitMQ event definitions
- [X] T021 [P] Setup RabbitMQ exchanges and queues configuration in docker/rabbitmq-config/
- [X] T022 [P] Create shared database migration framework using Flyway in each service's src/main/resources/db/migration/
- [X] T023 [P] Configure Spring Boot Actuator health checks in all services' application.yml
- [X] T024 [P] Setup centralized logging with correlation IDs in all services' logback-spring.xml
- [X] T025 [P] Create shared error handling and response DTOs in services/common-api/
- [X] T026 [P] Configure Prometheus metrics in all services' pom.xml and application.yml
- [X] T027 [P] Create OpenAPI/Swagger configuration in all services using springdoc-openapi
- [X] T305 [P] Create bootstrap.yml for Config Server client configuration in services/user-service/src/main/resources/bootstrap.yml (spring.cloud.config.uri, optional connection, fail-fast settings)
- [X] T306 [P] Create bootstrap.yml for Config Server client configuration in services/academic-service/src/main/resources/bootstrap.yml
- [X] T307 [P] Create bootstrap.yml for Config Server client configuration in services/attendance-service/src/main/resources/bootstrap.yml
- [X] T308 [P] Create bootstrap.yml for Config Server client configuration in services/academic-assessment-service/src/main/resources/bootstrap.yml
- [X] T309 [P] Create bootstrap.yml for Config Server client configuration in services/audit-service/src/main/resources/bootstrap.yml
- [X] T310 [P] Create bootstrap.yml for Config Server client configuration in services/notification-service/src/main/resources/bootstrap.yml
- [X] T311 [P] Create bootstrap.yml for Config Server client configuration in services/search-service/src/main/resources/bootstrap.yml
- [X] T312 Create SQL migration script to populate Config Server with user-service configuration in platform/config-server/src/main/resources/db/migration/V2__insert_user_service_config.sql (APPLICATION='user-service', PROFILE='default', LABEL='master')
- [X] T313 Create SQL migration script to populate Config Server with academic-service configuration in platform/config-server/src/main/resources/db/migration/V3__insert_academic_service_config.sql
- [X] T314 Create SQL migration script to populate Config Server with attendance-service configuration in platform/config-server/src/main/resources/db/migration/V4__insert_attendance_service_config.sql
- [X] T315 Create SQL migration script to populate Config Server with academic-assessment-service configuration in platform/config-server/src/main/resources/db/migration/V5__insert_academic_assessment_service_config.sql
- [X] T316 Create SQL migration script to populate Config Server with audit-service configuration in platform/config-server/src/main/resources/db/migration/V6__insert_audit_service_config.sql
- [X] T317 Create SQL migration script to populate Config Server with notification-service configuration in platform/config-server/src/main/resources/db/migration/V7__insert_notification_service_config.sql
- [X] T318 Create SQL migration script to populate Config Server with search-service configuration in platform/config-server/src/main/resources/db/migration/V8__insert_search_service_config.sql
- [X] T319 [P] Refactor services/user-service/src/main/resources/application.yml to keep only local/bootstrap config (spring.application.name, spring.cloud.config.uri) and move service-specific properties to Config Server
- [X] T320 [P] Refactor services/academic-service/src/main/resources/application.yml to keep only local/bootstrap config
- [X] T321 [P] Refactor services/attendance-service/src/main/resources/application.yml to keep only local/bootstrap config
- [X] T322 [P] Refactor services/academic-assessment-service/src/main/resources/application.yml to keep only local/bootstrap config
- [X] T323 [P] Refactor services/audit-service/src/main/resources/application.yml to keep only local/bootstrap config
- [X] T324 [P] Refactor services/notification-service/src/main/resources/application.yml to keep only local/bootstrap config
- [X] T325 [P] Refactor services/search-service/src/main/resources/application.yml to keep only local/bootstrap config
- [X] T326 Create documentation for Config Server configuration management in platform/config-server/docs/config-management.md (how to add/update configs, environment-specific configs)

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - User Registration and Authentication (Priority: P1) 🎯 MVP

**Goal**: Enable secure user authentication and account management with Keycloak OAuth2/JWT integration

**Independent Test**: Can be fully tested by creating user accounts, verifying email links, signing in, and resetting passwords. Delivers value by enabling secure access to the system and establishing the identity foundation for all other features.

### Tests for User Story 1 ⚠️

> **NOTE: Write these tests FIRST, ensure they FAIL before implementation**

- [X] T028 [P] [US1] Unit test for User model validation (keycloakId required) in services/user-service/src/test/kotlin/com/visor/school/userservice/model/UserTest.kt
- [X] T029 [P] [US1] Unit test for UserRepository with findByKeycloakId and findByEmail in services/user-service/src/test/kotlin/com/visor/school/userservice/repository/UserRepositoryTest.kt
- [X] T030 [P] [US1] Unit test for KeycloakClient (user creation, password reset) in services/user-service/src/test/kotlin/com/visor/school/userservice/integration/KeycloakClientTest.kt
- [X] T031 [P] [US1] Unit test for UserService with Keycloak integration (create Keycloak user first) in services/user-service/src/test/kotlin/com/visor/school/userservice/service/UserServiceTest.kt
- [X] T032 [P] [US1] Unit test for EmailVerificationService in services/user-service/src/test/kotlin/com/visor/school/userservice/service/EmailVerificationServiceTest.kt
- [X] T033 [P] [US1] Unit test for PasswordResetService (delegates to Keycloak) in services/user-service/src/test/kotlin/com/visor/school/userservice/service/PasswordResetServiceTest.kt
- [X] T034 [P] [US1] Contract test for POST /api/v1/auth/register (creates Keycloak user first) in services/user-service/src/test/kotlin/com/visor/school/userservice/contract/AuthControllerContractTest.kt
- [X] T035 [P] [US1] Contract test for POST /api/v1/auth/verify-email in services/user-service/src/test/kotlin/com/visor/school/userservice/contract/AuthControllerContractTest.kt
- [X] T036 [P] [US1] Contract test for POST /api/v1/auth/login (redirects to Keycloak token endpoint) in services/user-service/src/test/kotlin/com/visor/school/userservice/contract/AuthControllerContractTest.kt
- [X] T037 [P] [US1] Contract test for POST /api/v1/auth/reset-password (delegates to Keycloak) in services/user-service/src/test/kotlin/com/visor/school/userservice/contract/AuthControllerContractTest.kt
- [X] T038 [P] [US1] Integration test for user registration flow (Keycloak first → User entity) in services/user-service/src/test/kotlin/com/visor/school/userservice/integration/UserRegistrationIntegrationTest.kt
- [X] T039 [P] [US1] Integration test for email verification flow in services/user-service/src/test/kotlin/com/visor/school/userservice/integration/EmailVerificationIntegrationTest.kt
- [X] T040 [P] [US1] Integration test for login flow through Keycloak token endpoint in services/user-service/src/test/kotlin/com/visor/school/userservice/integration/LoginIntegrationTest.kt
- [X] T041 [P] [US1] Integration test for password reset flow via Keycloak Admin API in services/user-service/src/test/kotlin/com/visor/school/userservice/integration/PasswordResetIntegrationTest.kt

### Implementation for User Story 1

- [X] T042 [P] [US1] Create User entity model with keycloakId field (not passwordHash) in services/user-service/src/main/kotlin/com/visor/school/userservice/model/User.kt
- [X] T043 [P] [US1] Create UserRole enum in services/user-service/src/main/kotlin/com/visor/school/userservice/model/UserRole.kt
- [X] T044 [P] [US1] Create AccountStatus enum in services/user-service/src/main/kotlin/com/visor/school/userservice/model/AccountStatus.kt
- [X] T045 [US1] Create Flyway migration for users table with keycloak_id column (not password_hash) in services/user-service/src/main/resources/db/migration/V1__create_users_table.sql
- [X] T046 [US1] Create UserRepository interface with findByKeycloakId and findByEmail methods in services/user-service/src/main/kotlin/com/visor/school/userservice/repository/UserRepository.kt
- [X] T047 [US1] Implement KeycloakClient for user creation, password reset, and user management using Keycloak Admin API in services/user-service/src/main/kotlin/com/visor/school/userservice/integration/KeycloakClient.kt
- [X] T048 [US1] Implement UserService with create method (creates Keycloak user first → receives keycloakId → creates User entity), find, update methods in services/user-service/src/main/kotlin/com/visor/school/userservice/service/UserService.kt
- [X] T049 [US1] Implement EmailVerificationService with token generation and validation in services/user-service/src/main/kotlin/com/visor/school/userservice/service/EmailVerificationService.kt
- [X] T050 [US1] Implement PasswordResetService that delegates password reset to Keycloak Admin API in services/user-service/src/main/kotlin/com/visor/school/userservice/service/PasswordResetService.kt
- [X] T051 [US1] Create AuthController with register (creates Keycloak user first), verify-email, login (redirects to Keycloak token endpoint), reset-password (via Keycloak Admin API) endpoints in services/user-service/src/main/kotlin/com/visor/school/userservice/controller/AuthController.kt
- [X] T052 [US1] Create UserController with get user, update user, search users endpoints in services/user-service/src/main/kotlin/com/visor/school/userservice/controller/UserController.kt
- [X] T053 [US1] Implement email service integration (mock or real) in services/user-service/src/main/kotlin/com/visor/school/userservice/service/EmailService.kt
- [X] T054 [US1] Create UserCreatedEvent publisher in services/user-service/src/main/kotlin/com/visor/school/userservice/event/UserEventPublisher.kt
- [X] T055 [US1] Configure Spring Security with Keycloak JWT validation, resource server configuration, and permission evaluator in services/user-service/src/main/kotlin/com/visor/school/userservice/config/SecurityConfig.kt
- [X] T056 [US1] Configure Keycloak Admin API client properties in services/user-service/src/main/resources/application.yml
- [X] T057 [US1] Add input validation and error handling in services/user-service/src/main/kotlin/com/visor/school/userservice/controller/ValidationAdvice.kt
- [X] T058 [US1] Create Permission entity model in services/user-service/src/main/kotlin/com/visor/school/userservice/model/Permission.kt
- [X] T059 [US1] Create UserPermission junction entity in services/user-service/src/main/kotlin/com/visor/school/userservice/model/UserPermission.kt
- [X] T060 [US1] Create Flyway migration for permissions table in services/user-service/src/main/resources/db/migration/V2__create_permissions_table.sql
- [X] T061 [US1] Create Flyway migration for user_permissions table in services/user-service/src/main/resources/db/migration/V3__create_user_permissions_table.sql
- [X] T062 [US1] Create PermissionRepository interface in services/user-service/src/main/kotlin/com/visor/school/userservice/repository/PermissionRepository.kt
- [X] T063 [US1] Create UserPermissionRepository interface in services/user-service/src/main/kotlin/com/visor/school/userservice/repository/UserPermissionRepository.kt
- [X] T064 [US1] Implement PermissionService with create, assignToUser, assignToRole, getByUser methods in services/user-service/src/main/kotlin/com/visor/school/userservice/service/PermissionService.kt
- [X] T065 [US1] Create PermissionController with create permission, assign permission to user endpoints in services/user-service/src/main/kotlin/com/visor/school/userservice/controller/PermissionController.kt
- [X] T066 [US1] Implement Keycloak permission sync service to sync permissions with Keycloak custom attributes in services/user-service/src/main/kotlin/com/visor/school/userservice/service/KeycloakPermissionSyncService.kt
- [X] T067 [US1] Register User Service with Eureka Discovery in services/user-service/src/main/resources/application.yml
- [X] T068 [US1] Configure API Gateway route for /api/v1/users/** and /api/v1/permissions/** in platform/api-gateway/src/main/resources/application.yml
- [X] T069 [US1] Add logging for authentication operations in services/user-service/src/main/kotlin/com/visor/school/userservice/service/

**Checkpoint**: At this point, User Story 1 should be fully functional and testable independently. Users can register (via Keycloak), verify email, login (through Keycloak), reset passwords (via Keycloak Admin API), and permissions can be assigned to users for fine-grained access control.

---

## Phase 4: User Story 2 - Student Enrollment and Management (Priority: P2)

**Goal**: Enable administrators to enroll and manage student records, with parents able to view their children's information

**Independent Test**: Can be fully tested by enrolling students, searching for students, updating student information, and viewing student profiles. Delivers value by maintaining accurate student records that support all academic activities.

### Tests for User Story 2 ⚠️

> **NOTE: Write these tests FIRST, ensure they FAIL before implementation**

- [X] T070 [P] [US2] Unit test for Student model validation (including gradeLevel 1-12 validation) in services/academic-service/src/test/kotlin/com/visor/school/academicservice/model/StudentTest.kt
- [X] T071 [P] [US2] Unit test for Parent model validation in services/user-service/src/test/kotlin/com/visor/school/userservice/model/ParentTest.kt
- [X] T072 [P] [US2] Unit test for StudentRepository in services/academic-service/src/test/kotlin/com/visor/school/academicservice/repository/StudentRepositoryTest.kt
- [X] T073 [P] [US2] Unit test for StudentService in services/academic-service/src/test/kotlin/com/visor/school/academicservice/service/StudentServiceTest.kt
- [X] T074 [P] [US2] Contract test for POST /api/v1/students in services/academic-service/src/test/kotlin/com/visor/school/academicservice/contract/StudentControllerContractTest.kt
- [X] T075 [P] [US2] Contract test for GET /api/v1/students/search in services/academic-service/src/test/kotlin/com/visor/school/academicservice/contract/StudentControllerContractTest.kt
- [X] T076 [P] [US2] Contract test for GET /api/v1/students/{studentId} in services/academic-service/src/test/kotlin/com/visor/school/academicservice/contract/StudentControllerContractTest.kt
- [X] T077 [P] [US2] Contract test for PUT /api/v1/students/{studentId} in services/academic-service/src/test/kotlin/com/visor/school/academicservice/contract/StudentControllerContractTest.kt
- [X] T078 [P] [US2] Contract test for GET /api/v1/parents/{parentId}/students in services/user-service/src/test/kotlin/com/visor/school/userservice/contract/ParentControllerContractTest.kt
- [X] T079 [P] [US2] Integration test for student enrollment flow in services/academic-service/src/test/kotlin/com/visor/school/academicservice/integration/StudentEnrollmentIntegrationTest.kt
- [X] T080 [P] [US2] Integration test for student search in services/academic-service/src/test/kotlin/com/visor/school/academicservice/integration/StudentSearchIntegrationTest.kt

### Implementation for User Story 2

- [X] T081 [P] [US2] Create Student entity model with gradeLevel validation (1-12) in services/academic-service/src/main/kotlin/com/visor/school/academicservice/model/Student.kt
- [X] T082 [P] [US2] Create EnrollmentStatus enum in services/academic-service/src/main/kotlin/com/visor/school/academicservice/model/EnrollmentStatus.kt
- [X] T083 [P] [US2] Create Address value object in services/academic-service/src/main/kotlin/com/visor/school/academicservice/model/Address.kt
- [X] T084 [P] [US2] Create EmergencyContact value object in services/academic-service/src/main/kotlin/com/visor/school/academicservice/model/EmergencyContact.kt
- [X] T085 [P] [US2] Create Parent entity model in services/user-service/src/main/kotlin/com/visor/school/userservice/model/Parent.kt
- [X] T086 [P] [US2] Create Relationship enum in services/user-service/src/main/kotlin/com/visor/school/userservice/model/Relationship.kt
- [X] T087 [US2] Create Flyway migration for students table with grade_level validation (1-12) in services/academic-service/src/main/resources/db/migration/V1__create_students_table.sql
- [X] T088 [US2] Create Flyway migration for parents table in services/user-service/src/main/resources/db/migration/V4__create_parents_table.sql
- [X] T089 [US2] Create StudentRepository interface in services/academic-service/src/main/kotlin/com/visor/school/academicservice/repository/StudentRepository.kt
- [X] T090 [US2] Create ParentRepository interface in services/user-service/src/main/kotlin/com/visor/school/userservice/repository/ParentRepository.kt
- [X] T091 [US2] Implement StudentService with enroll, search, update, get methods (including gradeLevel 1-12 validation) in services/academic-service/src/main/kotlin/com/visor/school/academicservice/service/StudentService.kt
- [X] T092 [US2] Implement ParentService with link students, get children methods in services/user-service/src/main/kotlin/com/visor/school/userservice/service/ParentService.kt
- [X] T093 [US2] Create StudentController with enroll, search, get, update endpoints in services/academic-service/src/main/kotlin/com/visor/school/academicservice/controller/StudentController.kt
- [X] T094 [US2] Create ParentController with get children endpoint in services/user-service/src/main/kotlin/com/visor/school/userservice/controller/ParentController.kt
- [X] T095 [US2] Create StudentEnrolledEvent publisher in services/academic-service/src/main/kotlin/com/visor/school/academicservice/event/StudentEventPublisher.kt
- [X] T096 [US2] Implement student ID generation service in services/academic-service/src/main/kotlin/com/visor/school/academicservice/service/StudentIdGenerator.kt
- [X] T097 [US2] Add RBAC authorization checks with permission-based access (ADMINISTRATOR role, VIEW_ALL_STUDENTS permission) in services/academic-service/src/main/kotlin/com/visor/school/academicservice/controller/StudentController.kt
- [X] T098 [US2] Register Academic Service with Eureka Discovery in services/academic-service/src/main/resources/application.yml
- [X] T099 [US2] Configure API Gateway route for /api/v1/students/** in platform/api-gateway/src/main/resources/application.yml

**Checkpoint**: At this point, User Stories 1 AND 2 should both work independently. Administrators can enroll students and parents can view their children's information.

---

## Phase 5: User Story 3 - Teacher Management and Assignment (Priority: P3)

**Goal**: Enable administrators to manage teachers and assign them to classes, with teachers able to view their assignments

**Independent Test**: Can be fully tested by adding teachers, assigning them to classes/subjects, and viewing teacher assignments. Delivers value by organizing teaching responsibilities and enabling teachers to access their classes.

### Tests for User Story 3 ⚠️

> **NOTE: Write these tests FIRST, ensure they FAIL before implementation**

- [X] T100 [P] [US3] Unit test for Teacher model validation in services/academic-service/src/test/kotlin/com/visor/school/academicservice/model/TeacherTest.kt
- [X] T101 [P] [US3] Unit test for Class model validation (including classType, homeroomTeacherId, classTeacherId) in services/academic-service/src/test/kotlin/com/visor/school/academicservice/model/ClassTest.kt
- [X] T102 [P] [US3] Unit test for TeacherAssignment model validation (including isClassTeacher flag) in services/academic-service/src/test/kotlin/com/visor/school/academicservice/model/TeacherAssignmentTest.kt
- [X] T103 [P] [US3] Unit test for TeacherRepository in services/academic-service/src/test/kotlin/com/visor/school/academicservice/repository/TeacherRepositoryTest.kt
- [X] T104 [P] [US3] Unit test for ClassRepository in services/academic-service/src/test/kotlin/com/visor/school/academicservice/repository/ClassRepositoryTest.kt
- [X] T105 [P] [US3] Unit test for TeacherAssignmentRepository in services/academic-service/src/test/kotlin/com/visor/school/academicservice/repository/TeacherAssignmentRepositoryTest.kt
- [X] T106 [P] [US3] Unit test for TeacherService in services/academic-service/src/test/kotlin/com/visor/school/academicservice/service/TeacherServiceTest.kt
- [X] T107 [P] [US3] Unit test for ClassService with homeroom and class teacher validation in services/academic-service/src/test/kotlin/com/visor/school/academicservice/service/ClassServiceTest.kt
- [X] T108 [P] [US3] Contract test for POST /api/v1/teachers in services/academic-service/src/test/kotlin/com/visor/school/academicservice/contract/TeacherControllerContractTest.kt
- [X] T109 [P] [US3] Contract test for POST /api/v1/teachers/{teacherId}/assignments in services/academic-service/src/test/kotlin/com/visor/school/academicservice/contract/TeacherControllerContractTest.kt
- [X] T110 [P] [US3] Contract test for POST /api/v1/classes (homeroom and subject classes) in services/academic-service/src/test/kotlin/com/visor/school/academicservice/contract/ClassControllerContractTest.kt
- [X] T111 [P] [US3] Contract test for GET /api/v1/teachers/{teacherId} in services/academic-service/src/test/kotlin/com/visor/school/academicservice/contract/TeacherControllerContractTest.kt
- [X] T112 [P] [US3] Integration test for teacher creation and assignment flow (including class teacher designation) in services/academic-service/src/test/kotlin/com/visor/school/academicservice/integration/TeacherAssignmentIntegrationTest.kt
- [X] T113 [P] [US3] Integration test for homeroom class creation (grades 1-6) in services/academic-service/src/test/kotlin/com/visor/school/academicservice/integration/HomeroomClassIntegrationTest.kt

### Implementation for User Story 3

- [X] T114 [P] [US3] Create Teacher entity model in services/academic-service/src/main/kotlin/com/visor/school/academicservice/model/Teacher.kt
- [X] T115 [P] [US3] Create EmploymentStatus enum in services/academic-service/src/main/kotlin/com/visor/school/academicservice/model/EmploymentStatus.kt
- [X] T116 [P] [US3] Create ClassType enum (HOMEROOM, SUBJECT) in services/academic-service/src/main/kotlin/com/visor/school/academicservice/model/ClassType.kt
- [X] T117 [P] [US3] Create Class entity model with classType, homeroomTeacherId, classTeacherId fields in services/academic-service/src/main/kotlin/com/visor/school/academicservice/model/Class.kt
- [X] T118 [P] [US3] Create ClassStatus enum in services/academic-service/src/main/kotlin/com/visor/school/academicservice/model/ClassStatus.kt
- [X] T119 [P] [US3] Create Term enum in services/academic-service/src/main/kotlin/com/visor/school/academicservice/model/Term.kt
- [X] T120 [P] [US3] Create Schedule value object in services/academic-service/src/main/kotlin/com/visor/school/academicservice/model/Schedule.kt
- [X] T121 [P] [US3] Create TeacherAssignment entity model with isClassTeacher flag in services/academic-service/src/main/kotlin/com/visor/school/academicservice/model/TeacherAssignment.kt
- [X] T122 [US3] Create Flyway migration for teachers table in services/academic-service/src/main/resources/db/migration/V2__create_teachers_table.sql
- [X] T123 [US3] Create Flyway migration for classes table with class_type, homeroom_teacher_id, class_teacher_id columns in services/academic-service/src/main/resources/db/migration/V3__create_classes_table.sql
- [X] T124 [US3] Create Flyway migration for teacher_assignments table with is_class_teacher column in services/academic-service/src/main/resources/db/migration/V4__create_teacher_assignments_table.sql
- [X] T125 [US3] Create TeacherRepository interface in services/academic-service/src/main/kotlin/com/visor/school/academicservice/repository/TeacherRepository.kt
- [X] T126 [US3] Create ClassRepository interface in services/academic-service/src/main/kotlin/com/visor/school/academicservice/repository/ClassRepository.kt
- [X] T127 [US3] Create TeacherAssignmentRepository interface in services/academic-service/src/main/kotlin/com/visor/school/academicservice/repository/TeacherAssignmentRepository.kt
- [X] T128 [US3] Implement TeacherService with create, assign, get methods in services/academic-service/src/main/kotlin/com/visor/school/academicservice/service/TeacherService.kt
- [X] T129 [US3] Implement ClassService with create (homeroom for grades 1-6, subject for all grades), assign homeroom teacher, assign class teacher (grades 7-12) methods in services/academic-service/src/main/kotlin/com/visor/school/academicservice/service/ClassService.kt
- [X] T130 [US3] Create TeacherController with create, assign, get, search endpoints in services/academic-service/src/main/kotlin/com/visor/school/academicservice/controller/TeacherController.kt
- [X] T131 [US3] Create ClassController with create homeroom class, create subject class, assign class teacher endpoints in services/academic-service/src/main/kotlin/com/visor/school/academicservice/controller/ClassController.kt
- [X] T132 [US3] Implement employee ID generation service in services/academic-service/src/main/kotlin/com/visor/school/academicservice/service/EmployeeIdGenerator.kt
- [X] T133 [US3] Add validation for homeroom classes (grades 1-6 only, one per grade) in services/academic-service/src/main/kotlin/com/visor/school/academicservice/service/ClassService.kt
- [X] T134 [US3] Add validation for class teacher assignment (grades 7-12, must be one of subject teachers) in services/academic-service/src/main/kotlin/com/visor/school/academicservice/service/ClassService.kt
- [X] T135 [US3] Add RBAC authorization checks with permission-based access (ADMINISTRATOR role, MANAGE_HOMEROOM permission for homeroom classes) in services/academic-service/src/main/kotlin/com/visor/school/academicservice/controller/

**Checkpoint**: At this point, User Stories 1, 2, AND 3 should all work independently. Teachers can be added and assigned to classes.

---

## Phase 6: User Story 4 - Daily Attendance Tracking with Class Leader Delegation (Priority: P4)

**Goal**: Enable teachers to mark daily attendance directly or delegate to class leaders (1st, 2nd, 3rd leader), verify and approve attendance sessions. Class leaders can collect attendance for their assigned class. Teachers can view attendance history and generate reports.

**Independent Test**: Can be fully tested by teachers marking attendance, delegating to class leaders, class leaders collecting attendance, teachers approving sessions, viewing attendance history, and generating reports. Delivers value by replacing manual attendance tracking and enabling student-led attendance collection with teacher oversight.

### Tests for User Story 4 ⚠️

> **NOTE: Write these tests FIRST, ensure they FAIL before implementation**

- [X] T136 [P] [US4] Unit test for AttendanceRecord model validation (including collectedBy, sessionId) in services/attendance-service/src/test/kotlin/com/visor/school/attendanceservice/model/AttendanceRecordTest.kt
- [X] T137 [P] [US4] Unit test for AttendanceSession model validation in services/attendance-service/src/test/kotlin/com/visor/school/attendanceservice/model/AttendanceSessionTest.kt
- [X] T138 [P] [US4] Unit test for AttendanceSessionStatus enum in services/attendance-service/src/test/kotlin/com/visor/school/attendanceservice/model/AttendanceSessionStatusTest.kt
- [X] T139 [P] [US4] Unit test for AttendanceStatus enum in services/attendance-service/src/test/kotlin/com/visor/school/attendanceservice/model/AttendanceStatusTest.kt
- [X] T140 [P] [US4] Unit test for LeadershipPosition enum (FIRST_LEADER, SECOND_LEADER, THIRD_LEADER) in services/academic-service/src/test/kotlin/com/visor/school/academicservice/model/LeadershipPositionTest.kt
- [X] T141 [P] [US4] Unit test for AttendanceRepository in services/attendance-service/src/test/kotlin/com/visor/school/attendanceservice/repository/AttendanceRepositoryTest.kt
- [X] T142 [P] [US4] Unit test for AttendanceSessionRepository in services/attendance-service/src/test/kotlin/com/visor/school/attendanceservice/repository/AttendanceSessionRepositoryTest.kt
- [X] T143 [P] [US4] Unit test for AttendanceService with delegation and approval methods in services/attendance-service/src/test/kotlin/com/visor/school/attendanceservice/service/AttendanceServiceTest.kt
- [X] T144 [P] [US4] Contract test for POST /api/v1/attendance (direct teacher marking) in services/attendance-service/src/test/kotlin/com/visor/school/attendanceservice/contract/AttendanceControllerContractTest.kt
- [X] T145 [P] [US4] Contract test for POST /api/v1/attendance/sessions (create attendance session) in services/attendance-service/src/test/kotlin/com/visor/school/attendanceservice/contract/AttendanceControllerContractTest.kt
- [X] T146 [P] [US4] Contract test for POST /api/v1/attendance/sessions/{sessionId}/delegate (delegate to class leader) in services/attendance-service/src/test/kotlin/com/visor/school/attendanceservice/contract/AttendanceControllerContractTest.kt
- [X] T147 [P] [US4] Contract test for POST /api/v1/attendance/sessions/{sessionId}/collect (class leader collects) in services/attendance-service/src/test/kotlin/com/visor/school/attendanceservice/contract/AttendanceControllerContractTest.kt
- [X] T148 [P] [US4] Contract test for POST /api/v1/attendance/sessions/{sessionId}/approve (teacher approves) in services/attendance-service/src/test/kotlin/com/visor/school/attendanceservice/contract/AttendanceControllerContractTest.kt
- [X] T149 [P] [US4] Contract test for GET /api/v1/attendance/class/{classId} in services/attendance-service/src/test/kotlin/com/visor/school/attendanceservice/contract/AttendanceControllerContractTest.kt
- [X] T150 [P] [US4] Contract test for GET /api/v1/attendance/student/{studentId} in services/attendance-service/src/test/kotlin/com/visor/school/attendanceservice/contract/AttendanceControllerContractTest.kt
- [X] T151 [P] [US4] Contract test for GET /api/v1/reports/class/{classId} in services/attendance-service/src/test/kotlin/com/visor/school/attendanceservice/contract/AttendanceControllerContractTest.kt
- [X] T152 [P] [US4] Integration test for direct attendance marking flow in services/attendance-service/src/test/kotlin/com/visor/school/attendanceservice/integration/AttendanceMarkingIntegrationTest.kt
- [X] T153 [P] [US4] Integration test for class leader attendance collection and approval flow in services/attendance-service/src/test/kotlin/com/visor/school/attendanceservice/integration/ClassLeaderAttendanceIntegrationTest.kt
- [X] T154 [P] [US4] Integration test for attendance report generation in services/attendance-service/src/test/kotlin/com/visor/school/attendanceservice/integration/AttendanceReportIntegrationTest.kt

### Implementation for User Story 4

- [X] T155 [P] [US4] Create LeadershipPosition enum (FIRST_LEADER, SECOND_LEADER, THIRD_LEADER, NONE) in services/academic-service/src/main/kotlin/com/visor/school/academicservice/model/LeadershipPosition.kt
- [X] T156 [US4] Update Student entity model to add leadershipPosition field (class-specific via StudentClassLeadership junction table) in services/academic-service/src/main/kotlin/com/visor/school/academicservice/model/Student.kt
- [X] T157 [P] [US4] Create StudentClassLeadership junction entity in services/academic-service/src/main/kotlin/com/visor/school/academicservice/model/StudentClassLeadership.kt
- [X] T158 [P] [US4] Create AttendanceSession entity model with status (PENDING, COLLECTED, APPROVED, REJECTED) in services/attendance-service/src/main/kotlin/com/visor/school/attendanceservice/model/AttendanceSession.kt
- [X] T159 [P] [US4] Create AttendanceSessionStatus enum in services/attendance-service/src/main/kotlin/com/visor/school/attendanceservice/model/AttendanceSessionStatus.kt
- [X] T160 [P] [US4] Create AttendanceRecord entity model with collectedBy (Student FK), sessionId (FK), approvedBy (Teacher FK) fields in services/attendance-service/src/main/kotlin/com/visor/school/attendanceservice/model/AttendanceRecord.kt
- [X] T161 [P] [US4] Create AttendanceStatus enum in services/attendance-service/src/main/kotlin/com/visor/school/attendanceservice/model/AttendanceStatus.kt
- [X] T162 [US4] Create Flyway migration for student_class_leadership table in services/academic-service/src/main/resources/db/migration/V5__create_student_class_leadership_table.sql
- [X] T163 [US4] Create Flyway migration for attendance_sessions table in services/attendance-service/src/main/resources/db/migration/V1__create_attendance_sessions_table.sql
- [X] T164 [US4] Create Flyway migration for attendance_records table with collected_by, session_id, approved_by columns in services/attendance-service/src/main/resources/db/migration/V2__create_attendance_records_table.sql
- [X] T165 [US4] Create Flyway migration for attendance_corrections audit table in services/attendance-service/src/main/resources/db/migration/V3__create_attendance_corrections_table.sql
- [X] T166 [US4] Create StudentClassLeadershipRepository interface in services/academic-service/src/main/kotlin/com/visor/school/academicservice/repository/StudentClassLeadershipRepository.kt
- [X] T167 [US4] Create AttendanceRepository interface in services/attendance-service/src/main/kotlin/com/visor/school/attendanceservice/repository/AttendanceRepository.kt
- [X] T168 [US4] Create AttendanceSessionRepository interface in services/attendance-service/src/main/kotlin/com/visor/school/attendanceservice/repository/AttendanceSessionRepository.kt
- [X] T169 [US4] Implement AttendanceService with mark (direct), createSession, delegateToClassLeader, collectByClassLeader, approveSession, rejectSession, get, update, generateReport methods in services/attendance-service/src/main/kotlin/com/visor/school/attendanceservice/service/AttendanceService.kt
- [X] T170 [US4] Implement StudentClassLeadershipService with assignLeader, getLeadersByClass methods in services/academic-service/src/main/kotlin/com/visor/school/academicservice/service/StudentClassLeadershipService.kt
- [X] T171 [US4] Create AttendanceController with mark (direct), get class attendance, get student attendance endpoints in services/attendance-service/src/main/kotlin/com/visor/school/attendanceservice/controller/AttendanceController.kt
- [X] T172 [US4] Create AttendanceSessionController with create session, delegate to class leader, collect (class leader), approve, reject endpoints in services/attendance-service/src/main/kotlin/com/visor/school/attendanceservice/controller/AttendanceSessionController.kt
- [X] T173 [US4] Create AttendanceReportController with class report, school report endpoints in services/attendance-service/src/main/kotlin/com/visor/school/attendanceservice/controller/AttendanceReportController.kt
- [X] T174 [US4] Create StudentClassLeadershipController with assign class leader, get class leaders endpoints in services/academic-service/src/main/kotlin/com/visor/school/academicservice/controller/StudentClassLeadershipController.kt
- [X] T175 [US4] Create AttendanceMarkedEvent publisher in services/attendance-service/src/main/kotlin/com/visor/school/attendanceservice/event/AttendanceEventPublisher.kt
- [X] T176 [US4] Create AttendanceSessionApprovedEvent publisher in services/attendance-service/src/main/kotlin/com/visor/school/attendanceservice/event/AttendanceEventPublisher.kt
- [X] T177 [US4] Implement attendance rate calculation service in services/attendance-service/src/main/kotlin/com/visor/school/attendanceservice/service/AttendanceCalculator.kt
- [X] T178 [US4] Add validation for class leader assignment (only one 1st leader, one 2nd leader, one 3rd leader per class) in services/academic-service/src/main/kotlin/com/visor/school/academicservice/service/StudentClassLeadershipService.kt
- [X] T179 [US4] Add permission-based authorization checks (TEACHER role with MANAGE_ATTENDANCE, class leaders with COLLECT_ATTENDANCE permission) in services/attendance-service/src/main/kotlin/com/visor/school/attendanceservice/controller/
- [X] T180 [US4] Register Attendance Service with Eureka Discovery in services/attendance-service/src/main/resources/application.yml
- [X] T181 [US4] Configure API Gateway route for /api/v1/attendance/** in platform/api-gateway/src/main/resources/application.yml

**Checkpoint**: At this point, User Stories 1, 2, 3, AND 4 should all work independently. Teachers can mark attendance directly or delegate to class leaders. Class leaders can collect attendance, and teachers can verify and approve attendance sessions.

---

## Phase 7: User Story 5 - Academic Record Management (Priority: P5)

**Goal**: Maintain comprehensive academic records and generate official transcripts

**Independent Test**: Can be fully tested by viewing student academic records, generating transcripts, and tracking academic progression. Delivers value by providing comprehensive academic history and official documentation.

### Tests for User Story 5 ⚠️

> **NOTE: Write these tests FIRST, ensure they FAIL before implementation**

- [X] T182 [P] [US5] Unit test for AcademicRecord model validation in services/academic-service/src/test/kotlin/com/visor/school/academicservice/model/AcademicRecordTest.kt
- [X] T183 [P] [US5] Unit test for AcademicRecordService in services/academic-service/src/test/kotlin/com/visor/school/academicservice/service/AcademicRecordServiceTest.kt
- [X] T184 [P] [US5] Contract test for GET /api/v1/academic-records/{studentId} in services/academic-service/src/test/kotlin/com/visor/school/academicservice/contract/AcademicRecordControllerContractTest.kt
- [X] T185 [P] [US5] Contract test for GET /api/v1/academic-records/{studentId}/transcript in services/academic-service/src/test/kotlin/com/visor/school/academicservice/contract/AcademicRecordControllerContractTest.kt
- [X] T186 [P] [US5] Integration test for transcript generation in services/academic-service/src/test/kotlin/com/visor/school/academicservice/integration/TranscriptGenerationIntegrationTest.kt

### Implementation for User Story 5

- [X] T187 [P] [US5] Create AcademicRecord entity model in services/academic-service/src/main/kotlin/com/visor/school/academicservice/model/AcademicRecord.kt
- [X] T188 [P] [US5] Create AcademicStanding enum in services/academic-service/src/main/kotlin/com/visor/school/academicservice/model/AcademicStanding.kt
- [X] T189 [P] [US5] Create EnrollmentEntry value object in services/academic-service/src/main/kotlin/com/visor/school/academicservice/model/EnrollmentEntry.kt
- [X] T190 [P] [US5] Create CourseCompletion value object in services/academic-service/src/main/kotlin/com/visor/school/academicservice/model/CourseCompletion.kt
- [X] T191 [US5] Create Flyway migration for academic_records table in services/academic-service/src/main/resources/db/migration/V6__create_academic_records_table.sql
- [X] T192 [US5] Create AcademicRecordRepository interface in services/academic-service/src/main/kotlin/com/visor/school/academicservice/repository/AcademicRecordRepository.kt
- [X] T193 [US5] Implement AcademicRecordService with get, generateTranscript, updateGPA methods in services/academic-service/src/main/kotlin/com/visor/school/academicservice/service/AcademicRecordService.kt
- [X] T194 [US5] Create AcademicRecordController with get record, get transcript endpoints in services/academic-service/src/main/kotlin/com/visor/school/academicservice/controller/AcademicRecordController.kt
- [X] T195 [US5] Implement GPA calculation service in services/academic-service/src/main/kotlin/com/visor/school/academicservice/service/GPACalculator.kt
- [X] T196 [US5] Implement transcript PDF generation service in services/academic-service/src/main/kotlin/com/visor/school/academicservice/service/TranscriptGenerator.kt
- [X] T197 [US5] Create AcademicRecordUpdatedEvent publisher in services/academic-service/src/main/kotlin/com/visor/school/academicservice/event/AcademicRecordEventPublisher.kt

**Checkpoint**: At this point, User Stories 1-5 should all work independently. Academic records can be viewed and transcripts generated.

---

## Phase 8: User Story 6 - Assessment and Grade Management (Priority: P6)

**Goal**: Enable teachers to create assessments, record grades, and maintain gradebooks

**Independent Test**: Can be fully tested by creating assessments, recording grades, viewing gradebooks, and generating grade reports. Delivers value by streamlining the grading process and providing grade visibility.

### Tests for User Story 6 ⚠️

> **NOTE: Write these tests FIRST, ensure they FAIL before implementation**

- [X] T198 [P] [US6] Unit test for Assessment model validation in services/academic-assessment-service/src/test/kotlin/com/visor/school/assessment/model/AssessmentTest.kt
- [X] T199 [P] [US6] Unit test for Grade model validation in services/academic-assessment-service/src/test/kotlin/com/visor/school/assessment/model/GradeTest.kt
- [X] T200 [P] [US6] Unit test for AssessmentRepository in services/academic-assessment-service/src/test/kotlin/com/visor/school/assessment/repository/AssessmentRepositoryTest.kt
- [X] T201 [P] [US6] Unit test for GradeRepository in services/academic-assessment-service/src/test/kotlin/com/visor/school/assessment/repository/GradeRepositoryTest.kt
- [X] T202 [P] [US6] Unit test for AssessmentService in services/academic-assessment-service/src/test/kotlin/com/visor/school/assessment/service/AssessmentServiceTest.kt
- [X] T203 [P] [US6] Unit test for GradeService in services/academic-assessment-service/src/test/kotlin/com/visor/school/assessment/service/GradeServiceTest.kt
- [X] T204 [P] [US6] Contract test for POST /api/v1/assessments in services/academic-assessment-service/src/test/kotlin/com/visor/school/assessment/contract/AssessmentControllerContractTest.kt
- [X] T205 [P] [US6] Contract test for POST /api/v1/grades in services/academic-assessment-service/src/test/kotlin/com/visor/school/assessment/contract/GradeControllerContractTest.kt
- [X] T206 [P] [US6] Contract test for GET /api/v1/gradebooks/class/{classId} in services/academic-assessment-service/src/test/kotlin/com/visor/school/assessment/contract/GradebookControllerContractTest.kt
- [X] T207 [P] [US6] Contract test for GET /api/v1/gradebooks/student/{studentId} in services/academic-assessment-service/src/test/kotlin/com/visor/school/assessment/contract/GradebookControllerContractTest.kt
- [X] T208 [P] [US6] Integration test for assessment creation and grading flow in services/academic-assessment-service/src/test/kotlin/com/visor/school/assessment/integration/AssessmentGradingIntegrationTest.kt
- [X] T209 [P] [US6] Integration test for gradebook view in services/academic-assessment-service/src/test/kotlin/com/visor/school/assessment/integration/GradebookIntegrationTest.kt

### Implementation for User Story 6

- [X] T210 [P] [US6] Create Assessment entity model in services/academic-assessment-service/src/main/kotlin/com/visor/school/assessment/model/Assessment.kt
- [X] T211 [P] [US6] Create AssessmentType enum in services/academic-assessment-service/src/main/kotlin/com/visor/school/assessment/model/AssessmentType.kt
- [X] T212 [P] [US6] Create AssessmentStatus enum in services/academic-assessment-service/src/main/kotlin/com/visor/school/assessment/model/AssessmentStatus.kt
- [X] T213 [P] [US6] Create Grade entity model in services/academic-assessment-service/src/main/kotlin/com/visor/school/assessment/model/Grade.kt
- [X] T214 [US6] Create Flyway migration for assessments table in services/academic-assessment-service/src/main/resources/db/migration/V1__create_assessments_table.sql
- [X] T215 [US6] Create Flyway migration for grades table in services/academic-assessment-service/src/main/resources/db/migration/V2__create_grades_table.sql
- [X] T216 [US6] Create Flyway migration for grade_corrections audit table in services/academic-assessment-service/src/main/resources/db/migration/V3__create_grade_corrections_table.sql
- [X] T217 [US6] Create AssessmentRepository interface in services/academic-assessment-service/src/main/kotlin/com/visor/school/assessment/repository/AssessmentRepository.kt
- [X] T218 [US6] Create GradeRepository interface in services/academic-assessment-service/src/main/kotlin/com/visor/school/assessment/repository/GradeRepository.kt
- [X] T219 [US6] Implement AssessmentService with create, publish, get methods in services/academic-assessment-service/src/main/kotlin/com/visor/school/assessment/service/AssessmentService.kt
- [X] T220 [US6] Implement GradeService with record, update, calculateAverage methods in services/academic-assessment-service/src/main/kotlin/com/visor/school/assessment/service/GradeService.kt
- [X] T221 [US6] Create AssessmentController with create, get, publish endpoints in services/academic-assessment-service/src/main/kotlin/com/visor/school/assessment/controller/AssessmentController.kt
- [X] T222 [US6] Create GradeController with record, update endpoints in services/academic-assessment-service/src/main/kotlin/com/visor/school/assessment/controller/GradeController.kt
- [X] T223 [US6] Create GradebookController with get class gradebook, get student grades endpoints in services/academic-assessment-service/src/main/kotlin/com/visor/school/assessment/controller/GradebookController.kt
- [X] T224 [US6] Create GradeRecordedEvent publisher in services/academic-assessment-service/src/main/kotlin/com/visor/school/assessment/event/GradeEventPublisher.kt
- [X] T225 [US6] Implement grade average calculation service in services/academic-assessment-service/src/main/kotlin/com/visor/school/assessment/service/GradeCalculator.kt
- [X] T226 [US6] Implement letter grade conversion service in services/academic-assessment-service/src/main/kotlin/com/visor/school/assessment/service/LetterGradeConverter.kt
- [X] T227 [US6] Add RBAC authorization checks with permission-based access (TEACHER role with MANAGE_GRADES permission, ADMINISTRATOR, STUDENT, PARENT roles) in services/academic-assessment-service/src/main/kotlin/com/visor/school/assessment/controller/
- [X] T228 [US6] Register Academic Assessment Service with Eureka Discovery in services/academic-assessment-service/src/main/resources/application.yml
- [X] T229 [US6] Configure API Gateway route for /api/v1/assessments/** and /api/v1/gradebooks/** in platform/api-gateway/src/main/resources/application.yml

**Checkpoint**: All user stories should now be independently functional. Teachers can create assessments, record grades, and maintain gradebooks.

---

## Phase 9: Cross-Cutting Services (FR-026, FR-027, FR-030)

**Purpose**: Implement Search Service, Audit Service, and Notification Service to fulfill cross-cutting functional requirements

### Phase 9A: Search Service (FR-026)

**Goal**: Enable system-wide search across students, teachers, and classes

- [X] T230 [P] Initialize services/search-service with Spring Boot 3.5.6, Kotlin 2.2.20, Maven
- [X] T231 [P] [FR-026] Unit test for SearchService in services/search-service/src/test/kotlin/com/visor/school/search/service/SearchServiceTest.kt
- [X] T232 [P] [FR-026] Unit test for ElasticsearchClient in services/search-service/src/test/kotlin/com/visor/school/search/integration/ElasticsearchClientTest.kt
- [X] T233 [P] [FR-026] Contract test for GET /api/v1/search?q={query}&type={students|teachers|classes} in services/search-service/src/test/kotlin/com/visor/school/search/contract/SearchControllerContractTest.kt
- [X] T234 [P] [FR-026] Create SearchIndex entity for Elasticsearch documents in services/search-service/src/main/kotlin/com/visor/school/search/model/SearchIndex.kt
- [X] T235 [FR-026] Implement ElasticsearchClient for indexing and querying in services/search-service/src/main/kotlin/com/visor/school/search/integration/ElasticsearchClient.kt
- [X] T236 [FR-026] Implement SearchService with index, search, updateIndex, deleteIndex methods in services/search-service/src/main/kotlin/com/visor/school/search/service/SearchService.kt
- [X] T237 [FR-026] Create SearchController with search endpoint (supports students, teachers, classes) in services/search-service/src/main/kotlin/com/visor/school/search/controller/SearchController.kt
- [X] T238 [FR-026] Create event consumers for UserCreatedEvent, StudentEnrolledEvent, TeacherCreatedEvent to update search index in services/search-service/src/main/kotlin/com/visor/school/search/event/SearchEventConsumer.kt
- [X] T239 [FR-026] Register Search Service with Eureka Discovery in services/search-service/src/main/resources/application.yml
- [X] T240 [FR-026] Configure API Gateway route for /api/v1/search/** in platform/api-gateway/src/main/resources/application.yml

### Phase 9B: Audit Service (FR-027)

**Goal**: Log all security-relevant events (authentication, access attempts, data modifications)

- [X] T241 [P] Initialize services/audit-service with Spring Boot 3.5.6, Kotlin 2.2.20, Maven
- [X] T242 [P] [FR-027] Unit test for AuditRecord model in services/audit-service/src/test/kotlin/com/visor/school/audit/model/AuditRecordTest.kt
- [X] T243 [P] [FR-027] Unit test for AuditService in services/audit-service/src/test/kotlin/com/visor/school/audit/service/AuditServiceTest.kt
- [X] T244 [P] [FR-027] Contract test for GET /api/v1/audit?userId={id}&action={action}&startDate={date}&endDate={date} in services/audit-service/src/test/kotlin/com/visor/school/audit/contract/AuditControllerContractTest.kt
- [X] T245 [FR-027] Create AuditRecord entity model in services/audit-service/src/main/kotlin/com/visor/school/audit/model/AuditRecord.kt
- [X] T246 [FR-027] Create AuditAction enum (AUTHENTICATION, ACCESS_ATTEMPT, DATA_MODIFICATION, etc.) in services/audit-service/src/main/kotlin/com/visor/school/audit/model/AuditAction.kt
- [X] T247 [FR-027] Create Flyway migration for audit_records table in services/audit-service/src/main/resources/db/migration/V1__create_audit_records_table.sql
- [X] T248 [FR-027] Create AuditRepository interface in services/audit-service/src/main/kotlin/com/visor/school/audit/repository/AuditRepository.kt
- [X] T249 [FR-027] Implement AuditService with log, query, getByUser, getByAction methods in services/audit-service/src/main/kotlin/com/visor/school/audit/service/AuditService.kt
- [X] T250 [FR-027] Create AuditController with query audit logs endpoint in services/audit-service/src/main/kotlin/com/visor/school/audit/controller/AuditController.kt
- [X] T251 [FR-027] Create event consumers for security events (UserCreatedEvent, authentication failures, data modifications) in services/audit-service/src/main/kotlin/com/visor/school/audit/event/AuditEventConsumer.kt
- [X] T252 [FR-027] Register Audit Service with Eureka Discovery in services/audit-service/src/main/resources/application.yml
- [X] T253 [FR-027] Configure API Gateway route for /api/v1/audit/** in platform/api-gateway/src/main/resources/application.yml

### Phase 9C: Notification Service (FR-030)

**Goal**: Provide notifications for important events (new grades posted, attendance marked, account created)

- [X] T254 [P] Initialize services/notification-service with Spring Boot 3.5.6, Kotlin 2.2.20, Maven
- [X] T255 [P] [FR-030] Unit test for Notification model in services/notification-service/src/test/kotlin/com/visor/school/notification/model/NotificationTest.kt
- [X] T256 [P] [FR-030] Unit test for NotificationService in services/notification-service/src/test/kotlin/com/visor/school/notification/service/NotificationServiceTest.kt
- [X] T257 [P] [FR-030] Contract test for GET /api/v1/notifications?userId={id}&unreadOnly=true in services/notification-service/src/test/kotlin/com/visor/school/notification/contract/NotificationControllerContractTest.kt
- [X] T258 [FR-030] Create Notification entity model in services/notification-service/src/main/kotlin/com/visor/school/notification/model/Notification.kt
- [X] T259 [FR-030] Create NotificationType enum (GRADE_POSTED, ATTENDANCE_MARKED, ACCOUNT_CREATED, ATTENDANCE_SESSION_APPROVED, etc.) in services/notification-service/src/main/kotlin/com/visor/school/notification/model/NotificationType.kt
- [X] T260 [FR-030] Create Flyway migration for notifications table in services/notification-service/src/main/resources/db/migration/V1__create_notifications_table.sql
- [X] T261 [FR-030] Create NotificationRepository interface in services/notification-service/src/main/kotlin/com/visor/school/notification/repository/NotificationRepository.kt
- [X] T262 [FR-030] Implement NotificationService with create, markAsRead, getByUser, getUnread methods in services/notification-service/src/main/kotlin/com/visor/school/notification/service/NotificationService.kt
- [X] T263 [FR-030] Create NotificationController with get notifications, mark as read endpoints in services/notification-service/src/main/kotlin/com/visor/school/notification/controller/NotificationController.kt
- [X] T264 [FR-030] Create event consumers for GradeRecordedEvent, AttendanceMarkedEvent, AttendanceSessionApprovedEvent, UserCreatedEvent to generate notifications in services/notification-service/src/main/kotlin/com/visor/school/notification/event/NotificationEventConsumer.kt
- [X] T265 [FR-030] Implement notification templates and delivery service in services/notification-service/src/main/kotlin/com/visor/school/notification/service/NotificationDeliveryService.kt
- [X] T266 [FR-030] Register Notification Service with Eureka Discovery in services/notification-service/src/main/resources/application.yml
- [X] T267 [FR-030] Configure API Gateway route for /api/v1/notifications/** in platform/api-gateway/src/main/resources/application.yml

**Checkpoint**: Search, Audit, and Notification services are now functional. System-wide search, security event logging, and event notifications are available.

---

## Phase 10: Class Teacher Report Collection (Grades 7-12)

**Purpose**: Enable class teachers/coordinators (grades 7-12) to collect exam results and reports from subject teachers and submit to school administration

**Goal**: Class teachers can collect exam results from all subject teachers for their assigned class and submit aggregated reports to school administration.

- [X] T268 [P] Unit test for ExamResultCollection model in services/academic-assessment-service/src/test/kotlin/com/visor/school/assessment/model/ExamResultCollectionTest.kt
- [X] T269 [P] Unit test for ReportCollectionService in services/academic-assessment-service/src/test/kotlin/com/visor/school/assessment/service/ReportCollectionServiceTest.kt
- [X] T270 [P] Contract test for GET /api/v1/classes/{classId}/exam-results (class teacher collects) in services/academic-assessment-service/src/test/kotlin/com/visor/school/assessment/contract/ReportCollectionControllerContractTest.kt
- [X] T271 [P] Contract test for POST /api/v1/classes/{classId}/reports/submit (class teacher submits report) in services/academic-assessment-service/src/test/kotlin/com/visor/school/assessment/contract/ReportCollectionControllerContractTest.kt
- [X] T272 Create ExamResultCollection entity model in services/academic-assessment-service/src/main/kotlin/com/visor/school/assessment/model/ExamResultCollection.kt
- [X] T273 Create ReportSubmission entity model in services/academic-assessment-service/src/main/kotlin/com/visor/school/assessment/model/ReportSubmission.kt
- [X] T274 Create Flyway migration for exam_result_collections table in services/academic-assessment-service/src/main/resources/db/migration/V4__create_exam_result_collections_table.sql
- [X] T275 Create Flyway migration for report_submissions table in services/academic-assessment-service/src/main/resources/db/migration/V5__create_report_submissions_table.sql
- [X] T276 Create ExamResultCollectionRepository interface in services/academic-assessment-service/src/main/kotlin/com/visor/school/assessment/repository/ExamResultCollectionRepository.kt
- [X] T277 Create ReportSubmissionRepository interface in services/academic-assessment-service/src/main/kotlin/com/visor/school/assessment/repository/ReportSubmissionRepository.kt
- [X] T278 Implement ReportCollectionService with collectExamResults, aggregateReport, submitReport methods in services/academic-assessment-service/src/main/kotlin/com/visor/school/assessment/service/ReportCollectionService.kt
- [X] T279 Create ReportCollectionController with collect exam results, submit report endpoints (requires COLLECT_EXAM_RESULTS and SUBMIT_REPORTS permissions) in services/academic-assessment-service/src/main/kotlin/com/visor/school/assessment/controller/ReportCollectionController.kt
- [X] T280 Create ReportCollectedEvent publisher in services/academic-assessment-service/src/main/kotlin/com/visor/school/assessment/event/ReportEventPublisher.kt
- [X] T281 Add validation: Only class teachers (grades 7-12) with COLLECT_EXAM_RESULTS permission can collect exam results in services/academic-assessment-service/src/main/kotlin/com/visor/school/assessment/service/ReportCollectionService.kt
- [X] T282 Add validation: Only class teachers with SUBMIT_REPORTS permission can submit reports to school in services/academic-assessment-service/src/main/kotlin/com/visor/school/assessment/service/ReportCollectionService.kt
- [X] T283 Configure API Gateway route for /api/v1/classes/{classId}/exam-results/** and /api/v1/classes/{classId}/reports/** in platform/api-gateway/src/main/resources/application.yml

**Checkpoint**: Class teachers (grades 7-12) can now collect exam results from subject teachers and submit aggregated reports to school administration.

---

## Phase 11: Cross-Cutting Concerns & Polish

**Purpose**: Improvements that affect multiple user stories and services

- [X] T284 [P] Add systematic input validation (DTO validation annotations, custom validators) in all services' controller packages
- [X] T285 [P] [FR-028] Add validation error response handling in services/common-api/src/main/kotlin/com/visor/school/common/api/ValidationErrorResponse.kt
- [X] T286 [FR-029] Add optimistic locking (version fields) to all entity models that require concurrent access handling
- [X] T287 [FR-029] Implement transaction management and conflict resolution strategies in service layers
- [ ] T288 [P] Update OpenAPI/Swagger documentation for all services
- [ ] T289 [P] Add comprehensive API documentation in docs/api/
- [ ] T290 [P] Create service architecture diagrams in docs/architecture/
- [ ] T291 [P] Add deployment guides for each service in docs/deployment/
- [ ] T292 [P] Document event schemas in docs/events/
- [X] T293 Code cleanup and refactoring across all services
- [X] T294 Performance optimization (database indexing, query optimization)
- [X] T295 Security hardening (input sanitization, rate limiting, CORS configuration)
- [X] T296 [P] Add integration tests for cross-service communication
- [X] T297 [P] Add end-to-end tests for critical user journeys
- [X] T298 Implement comprehensive error handling and user-friendly error messages
- [ ] T299 [P] Add monitoring dashboards in Grafana
- [ ] T300 [P] Configure alerting rules in Prometheus
- [ ] T301 [P] Add distributed tracing configuration
- [X] T302 Run quickstart.md validation and update if needed
- [X] T303 Verify all services meet >80% test coverage requirement
- [X] T304 Generate test coverage reports for all services

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3-8)**: All depend on Foundational phase completion
  - User stories can then proceed in parallel (if staffed)
  - Or sequentially in priority order (P1 → P2 → P3 → P4 → P5 → P6)
- **Cross-Cutting Services (Phase 9)**: Depends on User Stories 1-6 completion (needs entities and events to consume)
  - Phase 9A (Search Service): Can start after US1, US2, US3 (needs User, Student, Teacher entities)
  - Phase 9B (Audit Service): Can start after US1 (needs authentication events)
  - Phase 9C (Notification Service): Can start after US1, US4, US6 (needs events from those stories)
- **Class Teacher Report Collection (Phase 10)**: Depends on US3 (class teacher assignment) and US6 (grades/assessments)
  - Requires class teacher/coordinator assigned (grades 7-12)
  - Requires grades and assessments to be recorded
- **Cross-Cutting Concerns & Polish (Phase 11)**: Depends on all desired user stories, cross-cutting services, and class teacher workflows being complete

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational (Phase 2) - No dependencies on other stories. BLOCKS all other stories (authentication required).
- **User Story 2 (P2)**: Can start after Foundational (Phase 2) and User Story 1 - Depends on User entity from US1
- **User Story 3 (P3)**: Can start after Foundational (Phase 2) and User Story 1 - Depends on User entity from US1
- **User Story 4 (P4)**: Can start after Foundational (Phase 2), User Story 1, and User Story 2 - Depends on Student and Class entities
- **User Story 5 (P5)**: Can start after Foundational (Phase 2), User Story 1, and User Story 2 - Depends on Student entity
- **User Story 6 (P6)**: Can start after Foundational (Phase 2), User Story 1, User Story 2, and User Story 3 - Depends on Student, Class, and Teacher entities

### Within Each User Story

- Tests (included per Constitution) MUST be written and FAIL before implementation
- Models before repositories
- Repositories before services
- Services before controllers
- Core implementation before event publishing
- Story complete before moving to next priority

### Parallel Opportunities

- All Setup tasks marked [P] can run in parallel (T003-T011)
- All Foundational tasks marked [P] can run in parallel (T015-T027)
- Once Foundational phase completes, user stories can start in parallel (if team capacity allows)
- All tests for a user story marked [P] can run in parallel
- Models within a story marked [P] can run in parallel
- Different user stories can be worked on in parallel by different team members (after dependencies are met)
- Cross-cutting services (Phase 9A, 9B, 9C) can be developed in parallel once their dependencies are met

---

## Parallel Example: User Story 1

```bash
# Launch all unit tests for User Story 1 together:
Task: "Unit test for User model validation in services/user-service/src/test/kotlin/com/visor/school/userservice/model/UserTest.kt"
Task: "Unit test for UserRepository in services/user-service/src/test/kotlin/com/visor/school/userservice/repository/UserRepositoryTest.kt"
Task: "Unit test for UserService in services/user-service/src/test/kotlin/com/visor/school/userservice/service/UserServiceTest.kt"
Task: "Unit test for EmailVerificationService in services/user-service/src/test/kotlin/com/visor/school/userservice/service/EmailVerificationServiceTest.kt"
Task: "Unit test for PasswordResetService in services/user-service/src/test/kotlin/com/visor/school/userservice/service/PasswordResetServiceTest.kt"

# Launch all models for User Story 1 together:
Task: "Create User entity model in services/user-service/src/main/kotlin/com/visor/school/userservice/model/User.kt"
Task: "Create UserRole enum in services/user-service/src/main/kotlin/com/visor/school/userservice/model/UserRole.kt"
Task: "Create AccountStatus enum in services/user-service/src/main/kotlin/com/visor/school/userservice/model/AccountStatus.kt"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational (CRITICAL - blocks all stories)
3. Complete Phase 3: User Story 1 (Authentication & User Management)
4. **STOP and VALIDATE**: Test User Story 1 independently
5. Deploy/demo if ready

### Incremental Delivery

1. Complete Setup + Foundational → Foundation ready
2. Add User Story 1 → Test independently → Deploy/Demo (MVP!)
3. Add User Story 2 → Test independently → Deploy/Demo
4. Add User Story 3 → Test independently → Deploy/Demo
5. Add User Story 4 → Test independently → Deploy/Demo
6. Add User Story 5 → Test independently → Deploy/Demo
7. Add User Story 6 → Test independently → Deploy/Demo
8. Each story adds value without breaking previous stories

### Parallel Team Strategy

With multiple developers:

1. Team completes Setup + Foundational together
2. Once Foundational is done:
   - Developer A: User Story 1 (authentication - blocks others)
   - Once US1 complete:
     - Developer A: User Story 2 (students)
     - Developer B: User Story 3 (teachers)
   - Once US2 and US3 complete:
     - Developer A: User Story 4 (attendance)
     - Developer B: User Story 5 (academic records)
     - Developer C: User Story 6 (assessments & grades)
3. Stories complete and integrate independently

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- Each user story should be independently completable and testable
- Verify tests fail before implementing
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
- All services must achieve >80% test coverage with 100+ unit tests (Constitution Principle IV)
- All inter-service communication via REST APIs or RabbitMQ events (Constitution Principle III)
- All configuration via encrypted Config Server (Constitution Principle V)
- Avoid: vague tasks, same file conflicts, cross-story dependencies that break independence

