<!--
Sync Impact Report:
- Version change: 1.0.0 → 1.1.0
- Modified principles: 
  - I. Security & Authentication: Expanded to explicitly include permission-based access control (extends RBAC)
  - III. Event-Driven Communication: Expanded event examples to include attendance marking, attendance session approval, class teacher report collection
- Added sections: N/A
- Removed sections: N/A
- Templates requiring updates:
  ✅ plan-template.md - No changes required (already aligned with permission-based access control)
  ✅ spec-template.md - No changes required (already aligned)
  ✅ tasks-template.md - No changes required (already aligned)
  ✅ checklist-template.md - No changes required (already aligned)
- Follow-up TODOs: None
-->

# School Management System Constitution

## Core Principles

### I. Security & Authentication (NON-NEGOTIABLE)

All services MUST implement secure OAuth2/JWT authentication via Keycloak. User authentication flows MUST include email verification and password reset capabilities. All endpoints MUST validate JWT tokens before processing requests. Sensitive operations (user management, student/teacher data access, payment processing) MUST enforce role-based access control (RBAC) with fine-grained permissions through Keycloak. Permission-based access control MUST extend RBAC by allowing custom permissions (e.g., COLLECT_ATTENDANCE, COLLECT_EXAM_RESULTS, SUBMIT_REPORTS) to be assigned to users or roles. Authentication failures MUST be logged to the audit service for security monitoring.

**Rationale**: Centralized authentication ensures consistent security posture across all 16 microservices, reduces attack surface, and enables unified user management. Email verification and password reset flows are essential for user trust and account recovery. Permission-based access control provides fine-grained authorization beyond basic roles, enabling domain-specific permissions (such as class leader attendance collection, class teacher report collection) while maintaining RBAC foundation.

### II. Microservices Architecture

All services MUST operate as independent, versioned microservices with clearly defined boundaries. Each service MUST have its own database schema (PostgreSQL) and MUST NOT directly access other services' databases. Service-to-service communication MUST occur via well-defined APIs (REST or message queues). Services MUST register with the Discovery Server (Eureka) and be discoverable via the API Gateway. Service versioning MUST follow semantic versioning (MAJOR.MINOR.PATCH) to manage breaking changes.

**Rationale**: Independent services enable parallel development, independent deployment, fault isolation, and technology stack flexibility. Clear boundaries prevent tight coupling and enable teams to scale development efforts.

### III. Event-Driven Communication

Services MUST use RabbitMQ for asynchronous, event-driven communication. State changes that affect multiple services MUST be published as events to appropriate queues. Services MUST consume events relevant to their domain and MUST be idempotent in event processing. Event schemas MUST be versioned and documented. Critical events (user creation, student enrollment, payment processing, grade submissions, attendance marking, attendance session approval, class teacher report collection) MUST have corresponding event handlers in consuming services.

**Rationale**: Event-driven architecture decouples services, improves system resilience, and enables eventual consistency across distributed services. RabbitMQ provides reliable message delivery and supports complex routing patterns.

### IV. Test-First Development (NON-NEGOTIABLE)

All services MUST achieve >80% code coverage with 100+ unit tests. Tests MUST be written before implementation (TDD). Unit tests MUST cover business logic, service layers, and utility functions. Integration tests MUST verify service contracts, database interactions, and event publishing/consumption. Contract tests MUST validate API endpoint specifications. All tests MUST pass before code is merged to main branch. Test coverage reports MUST be generated and reviewed as part of CI/CD pipeline.

**Rationale**: High test coverage ensures code quality, prevents regressions, and enables confident refactoring. Test-first development catches design issues early and produces more maintainable code.

### V. Configuration Management with Encryption

All sensitive configuration (database credentials, API keys, OAuth secrets) MUST be encrypted at rest and in transit. Configuration MUST be managed centrally via Config Server. Environment-specific configurations (dev, staging, prod) MUST be isolated and MUST NOT contain secrets in plaintext. Secrets MUST be retrieved from secure storage (Keycloak, Vault, or encrypted Config Server) at runtime. Configuration changes MUST be version-controlled and audited.

**Rationale**: Encrypted configuration prevents credential leakage, supports secure deployments across environments, and enables compliance with data protection regulations. Centralized configuration simplifies management and reduces configuration drift.

### VI. Comprehensive Documentation

All services MUST include API documentation (OpenAPI/Swagger), service architecture diagrams, and deployment guides. User-facing features MUST have user documentation. Code MUST include inline documentation for complex business logic. Integration patterns MUST be documented with examples. Event schemas and message contracts MUST be documented. Documentation MUST be updated with each feature release.

**Rationale**: Comprehensive documentation accelerates onboarding, reduces support burden, and ensures knowledge transfer. Well-documented APIs and contracts enable independent service development and integration.

## Development Standards

### Code Quality

- **Language**: Kotlin 2.2.20 with Spring Boot 3.5.6
- **Build Tool**: Maven 3.9.11
- **JDK**: OpenJDK 25 (Temurin recommended)
- **Code Style**: Follow Kotlin official style guide; use ktlint for enforcement
- **Dependencies**: Minimize external dependencies; justify additions; keep versions updated for security
- **Error Handling**: Structured error responses; comprehensive logging with correlation IDs
- **Performance**: Services MUST handle production load; profile and optimize hot paths

### Database & Storage

- **Primary Database**: PostgreSQL 17 for all services
- **Search**: Elasticsearch 8.11.0 for full-text search via Search Service
- **File Storage**: MinIO for object storage via File Service
- **Migrations**: All schema changes MUST be versioned and reversible
- **Backups**: Regular automated backups with point-in-time recovery capability

### Observability

- **Logging**: Structured logging with correlation IDs; centralized via ELK Stack
- **Metrics**: Prometheus metrics exposed by all services; dashboards in Grafana
- **Tracing**: Distributed tracing for request flows across services
- **Health Checks**: All services MUST expose health endpoints for monitoring

### Security Requirements

- **Authentication**: Keycloak OAuth2/JWT for all API requests
- **Authorization**: RBAC with fine-grained permissions enforced at service and endpoint levels
- **Permission Management**: Custom permissions MUST be managed through Keycloak and included in JWT tokens
- **Data Encryption**: TLS 1.3 for all inter-service communication
- **Audit Logging**: All security-relevant events logged to Audit Service
- **Input Validation**: All inputs validated and sanitized
- **SQL Injection Prevention**: Use parameterized queries; ORM with proper escaping
- **XSS Prevention**: Content Security Policy headers; output encoding

## Governance

### Amendment Procedure

Constitution amendments require:
1. Documentation of rationale and impact analysis
2. Review and approval by architecture team
3. Update of affected services and documentation
4. Version bump following semantic versioning (MAJOR.MINOR.PATCH)
5. Communication to all development teams

### Versioning Policy

- **MAJOR**: Backward incompatible principle changes, removal of principles, or fundamental architectural shifts
- **MINOR**: New principles added, existing principles expanded with new requirements
- **PATCH**: Clarifications, typo fixes, non-semantic refinements to existing principles

### Compliance Review

All pull requests MUST verify compliance with applicable constitution principles. Automated checks (linting, tests, coverage) MUST pass. Manual review MUST assess adherence to architectural principles and security requirements. Violations MUST be justified in Complexity Tracking section of implementation plans.

### Enforcement

- Constitution supersedes all other development practices and guidelines
- Deviations require explicit justification and approval
- Non-compliance blocks merge until resolved or exception granted
- Regular architecture reviews assess ongoing compliance

**Version**: 1.1.0 | **Ratified**: 2025-01-27 | **Last Amended**: 2025-01-27
