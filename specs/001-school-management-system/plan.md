# Implementation Plan: School Management System

**Branch**: `001-school-management-system` | **Date**: 2025-01-27 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/001-school-management-system/spec.md`

## Summary

A comprehensive, microservices-based K12 school management system (grades 1-12) built with Kotlin 2.2.20 and Spring Boot 3.5.6. The system provides complete functionality for managing students, teachers, academic records, attendance (with class leader delegation), assessments, and administrative tasks. Features include:

- **K12 Education Model**: Differentiated class models for grades 1-6 (homeroom classes) and grades 7-12 (subject classes with class teacher/coordinator)
- **Class Leader Attendance Delegation**: Teachers can mark attendance directly OR delegate to class leaders (1st, 2nd, 3rd leader) for each session, with teacher verification and approval workflow
- **Permission-Based Access Control**: Fine-grained permissions beyond roles (Keycloak roles + custom permissions)
- **Event-Driven Architecture**: RabbitMQ for asynchronous communication between 16 microservices
- **Centralized Authentication**: Keycloak OAuth2/JWT with permission-based access control
- **Comprehensive Search**: Elasticsearch 8.11.0 for full-text search
- **File Storage**: MinIO for object storage

## Technical Context

**Language/Version**: Kotlin 2.2.20, OpenJDK 25, Spring Boot 3.5.6  
**Primary Dependencies**: Spring Cloud (Config Server, Discovery Server, API Gateway), Spring Security, Spring Data JPA, Flyway, RabbitMQ, Elasticsearch, Keycloak  
**Storage**: PostgreSQL 17 (database per service pattern)  
**Testing**: JUnit 5, Mockito, Testcontainers, Spring Cloud Contract (TDD approach, >80% coverage, 100+ unit tests per service)  
**Target Platform**: Linux server (Docker containers, Kubernetes-ready)  
**Project Type**: Microservices (multiple independent services with shared platform infrastructure)  
**Education Level**: K12 (Kindergarten through Grade 12) - supports grades 1 through 12  
**Class Model**: 
- **Grades 1-6**: Homeroom classes with single homeroom teacher per class
- **Grades 7-12**: Subject classes with designated class teacher/coordinator (one of the subject teachers) who can collect exam results and reports
**Access Control**: Role-based with fine-grained permissions (Keycloak roles + custom permissions)  
**Performance Goals**: 
- Support 500 concurrent users without degradation (SC-006)
- Search results in under 2 seconds (SC-014)
- Attendance reports: 5 seconds (class), 30 seconds (school-wide) (SC-008)
- Gradebook views load in under 3 seconds (SC-010)
- Academic transcripts generate in under 10 seconds (SC-009)  
**Constraints**: 
- Each service must have >80% test coverage with 100+ unit tests (Constitution IV)
- All services must use Keycloak OAuth2/JWT authentication with permission-based access control (Constitution I)
- All inter-service communication via REST APIs or RabbitMQ events (Constitution II, III)
- All configuration via encrypted Config Server (Constitution V)
- Services must be independently deployable with versioning (Constitution II)
- Grade levels restricted to 1-12 (K12 system)
- Homeroom classes only for grades 1-6, one homeroom teacher per class
- Class teacher/coordinator must be one of the subject teachers for grades 7-12  
**Scale/Scope**: 
- 16 microservices (3 platform services, 10 business services, 4 infrastructure services)
- 500 concurrent users (administrators, teachers, students, parents)
- K12 education system (grades 1-12)
- Multiple subject classes per grade, homeroom classes for grades 1-6
- Full-text search across students, teachers, classes
- Real-time notifications for grade postings, attendance, account events

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

Verify compliance with School Management System Constitution principles:

- **I. Security & Authentication**: ✅ **COMPLIANT** - Keycloak OAuth2/JWT integration specified in API contracts with permission-based access control. All endpoints require Bearer token authentication. Email verification and password reset flows defined in User Service API. User entity uses `keycloakId` field - password management delegated to Keycloak. Permission entity and Permission-Role mapping defined in data model. Keycloak supports fine-grained permissions through realm roles and custom permissions. All endpoints will validate JWT tokens and enforce RBAC and permission-based access control through Keycloak.

- **II. Microservices Architecture**: ✅ **COMPLIANT** - System consists of 16 independent microservices. Each service will have its own PostgreSQL database schema. Services will register with Eureka Discovery Server and be accessible via API Gateway. Service versioning will follow semantic versioning (MAJOR.MINOR.PATCH). Base package name: `com.visor.school` with service-specific sub-packages.

- **III. Event-Driven Communication**: ✅ **COMPLIANT** - System uses RabbitMQ for asynchronous communication. Critical events (user creation, student enrollment, grade submissions, attendance marking, attendance session approval, class teacher report collection) will be published as events. Event schemas will be versioned and documented. Services consuming events will implement idempotent processing.

- **IV. Test-First Development**: ✅ **COMPLIANT** - All services must achieve >80% code coverage with 100+ unit tests. TDD approach will be followed. Integration tests will verify service contracts, database interactions, and event publishing/consumption. Contract tests will validate API endpoints.

- **V. Configuration Management**: ✅ **COMPLIANT** - All sensitive configuration (database credentials, Keycloak OAuth secrets, email service credentials) will be encrypted and managed via Config Server. Environment-specific configurations will be isolated. Secrets will be retrieved from secure storage at runtime.

- **VI. Comprehensive Documentation**: ✅ **COMPLIANT** - All services will include OpenAPI/Swagger documentation. Service architecture diagrams, deployment guides, and event schema documentation will be created. Documentation will be updated with each feature release. K12-specific entities (homeroom classes, class teachers, permissions) and workflows (class leader attendance delegation, class teacher report collection) will be documented.

**Violations**: None - All constitution principles are satisfied. Permission-based access control extends Keycloak RBAC and is compliant with Constitution Principle I.

## Project Structure

```
school-management/
├── platform/
│   ├── config-server/          # Centralized configuration (Port 8888)
│   ├── discovery-server/       # Eureka Discovery Server (Port 8761)
│   └── api-gateway/            # API Gateway with routing and security (Port 8080)
├── services/
│   ├── user-service/           # User management, authentication, permissions (Port 8001)
│   ├── academic-service/       # Students, teachers, classes, academic records (Port 8002)
│   ├── attendance-service/     # Attendance tracking, sessions, class leader delegation (Port 8003)
│   ├── academic-assessment-service/  # Assessments, grades, class teacher reports (Port 8004)
│   ├── timetable-service/      # Class schedules (Port 8005)
│   ├── notification-service/   # Event notifications (Port 8006)
│   ├── payment-service/        # Payment processing (Port 8007)
│   ├── reporting-service/      # Reports and analytics (Port 8008)
│   ├── cms-service/            # Content management (Port 8009)
│   ├── file-service/           # File storage (MinIO integration) (Port 8010)
│   ├── search-service/         # Full-text search (Elasticsearch) (Port 8011)
│   ├── audit-service/          # Audit logging (Port 8012)
│   ├── integration-service/    # External integrations (Port 8013)
│   └── workflow-service/       # Approval processes (Port 8014)
├── docker/
│   └── docker-compose.yml      # Infrastructure services (PostgreSQL, RabbitMQ, Keycloak, etc.)
└── scripts/
    └── setup.sh                # Project initialization script
```

**Package Naming**: All services use base package `com.visor.school` with service-specific sub-packages:
- Platform services: `com.visor.school.{service}` (e.g., `com.visor.school.configserver`)
- Business services: `com.visor.school.{service}.{module}` (e.g., `com.visor.school.userservice.model`)

**K12-Specific Considerations**:
- **Academic Service**: Handles homeroom classes (grades 1-6), subject classes (grades 7-12), class teacher/coordinator assignment, and class leader assignment (1st, 2nd, 3rd leader positions)
- **User Service**: Handles permission management (Permission entity, UserPermission mapping, Keycloak permission sync)
- **Attendance Service**: Handles attendance sessions, teacher direct marking, class leader delegation workflow, and teacher approval/rejection of attendance sessions
- **Academic Assessment Service**: Handles class teacher report collection workflows (grades 7-12) for collecting exam results and submitting reports to school administration

## Phase 0: Research & Technical Decisions

**Status**: ✅ **COMPLETE** - See [research.md](research.md) for all technical decisions.

**Key Decisions**:
- Kotlin 2.2.20 with Spring Boot 3.5.6 for microservices
- PostgreSQL 17 with database-per-service pattern
- RabbitMQ for event-driven communication
- Keycloak for OAuth2/JWT authentication with permission-based access control
- Elasticsearch 8.11.0 for full-text search
- MinIO for object storage
- K12 education model with differentiated class teacher roles (grades 1-6 vs 7-12)
- Permission-based access control model (roles + fine-grained permissions)
- Class leader attendance delegation with teacher approval workflow

**No further research needed** - All technical choices are well-established patterns with proven implementations.

## Phase 1: Design & Contracts

**Status**: ✅ **COMPLETE** - See [data-model.md](data-model.md) and [contracts/](contracts/) for design artifacts.

**Generated Artifacts**:
- **data-model.md**: Core entities with K12-specific models (homeroom classes, class teachers, permissions, class leaders, attendance sessions)
- **contracts/**: OpenAPI 3.0 specifications for all service APIs
- **quickstart.md**: Setup and testing instructions

**Key Entities**:
- User, Permission, UserPermission (User Service)
- Student, Teacher, Class (with classType, homeroomTeacherId, classTeacherId), TeacherAssignment (Academic Service)
- AttendanceRecord (with collectedBy, sessionId, approvedBy), AttendanceSession (Attendance Service)
- StudentClassLeadership (class leader positions), Assessment, Grade (Academic Assessment Service)

**Note**: Data model will be updated to include AttendanceSession entity and extend AttendanceRecord with session-based workflow fields (collectedBy, sessionId, approvedBy) as per tasks.md requirements.

## Phase 2: Implementation Planning

**Status**: ✅ **COMPLETE** - See [tasks.md](tasks.md) for detailed implementation tasks.

**Implementation Strategy**:
- Test-First Development (TDD) with >80% coverage
- Independent service development and deployment
- Event-driven communication between services
- Permission-based access control at all endpoints
- K12-specific validation and workflows

**Next Steps**:
1. Review and approve design artifacts
2. Begin implementation following tasks.md
3. Update data-model.md with AttendanceSession entity and extended AttendanceRecord fields
4. Update spec.md to include class leader delegation requirements explicitly

## Constitution Check (Post-Design)

✅ **All principles remain compliant** after design phase:

- **I. Security & Authentication**: Permission-based access control implemented through Keycloak with custom permissions. Class leaders and class teachers have specific permissions (COLLECT_ATTENDANCE, COLLECT_EXAM_RESULTS, SUBMIT_REPORTS).
- **II. Microservices Architecture**: All 16 services designed with independent databases and clear boundaries.
- **III. Event-Driven Communication**: Event schemas defined for attendance marking, session approval, class teacher report collection.
- **IV. Test-First Development**: TDD approach with comprehensive test coverage requirements.
- **V. Configuration Management**: Config Server design with encrypted storage.
- **VI. Comprehensive Documentation**: All artifacts generated with K12-specific entities and workflows documented.

**No violations detected** - Ready for implementation.
