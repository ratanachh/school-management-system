# Research & Technical Decisions: School Management System

**Date**: 2025-01-27  
**Feature**: School Management System  
**Purpose**: Document research findings and technical decisions for implementation planning

## Technology Stack Decisions

### Decision: Package Naming Convention - com.visor.school

**Rationale**:
- Standard Java/Kotlin package naming convention (reverse domain name)
- Clear organizational structure: `com.visor.school.{service}.{module}`
- Example: `com.visor.school.userservice.model.User`
- Enables clear package boundaries and module organization
- Prevents naming conflicts with other libraries

**Package Structure**:
- Platform services: `com.visor.school.{service}.{module}` (e.g., `com.visor.school.configserver`)
- Business services: `com.visor.school.{service}.{module}` (e.g., `com.visor.school.userservice.model`)
- Shared libraries: `com.visor.school.common.{module}` (e.g., `com.visor.school.common.events`)

### Decision: Kotlin 2.2.20 with Spring Boot 3.5.6

**Rationale**: 
- Kotlin provides null safety, concise syntax, and excellent interoperability with Java ecosystem
- Spring Boot 3.5.6 offers comprehensive microservices support via Spring Cloud
- JDK 25 provides performance improvements and modern language features
- Strong ecosystem with extensive libraries for enterprise applications

**Alternatives Considered**:
- Java 21: More verbose, less modern language features
- Scala: Steeper learning curve, smaller ecosystem
- Go: Different paradigm, less mature microservices frameworks

### Decision: PostgreSQL 17 as Primary Database

**Rationale**:
- ACID compliance essential for academic records, grades, and financial data
- Strong support for JSON columns for flexible schema extensions
- Excellent performance with proper indexing
- Mature ecosystem with robust tooling (Flyway migrations)
- Support for complex queries and transactions

**Alternatives Considered**:
- MongoDB: Less suitable for transactional academic data
- MySQL: PostgreSQL offers better JSON support and advanced features
- Cassandra: Overkill for this use case, eventual consistency not needed

### Decision: RabbitMQ for Event-Driven Communication

**Rationale**:
- Mature message broker with proven reliability
- Supports complex routing patterns (topics, exchanges)
- Excellent Spring AMQP integration
- Supports message persistence and guaranteed delivery
- Enables decoupled, asynchronous communication between services

**Alternatives Considered**:
- Apache Kafka: Better for high-throughput streaming, but more complex setup
- Redis Pub/Sub: Less reliable, no message persistence guarantees
- ActiveMQ: Less mature Spring integration

### Decision: Keycloak for Authentication & Authorization with Permission-Based Access Control

**Rationale**:
- Industry-standard OAuth2/OIDC implementation
- Centralized identity management for all microservices
- Built-in support for user roles, groups, and fine-grained permissions
- Self-hosted solution (no external dependencies)
- Excellent Spring Security integration
- Supports email verification and password reset flows
- **Password Management**: Keycloak handles all password storage, hashing, and validation - User entity stores only `keycloakId` for linking
- **Permission Support**: Keycloak supports both realm roles and custom permissions, enabling fine-grained access control beyond basic roles

**Architectural Decision**:
- User entity includes `keycloakId` field to link with Keycloak identity
- Password hash is NOT stored in User entity - Keycloak manages all password operations
- User creation flow: Create user in Keycloak first → Receive keycloakId → Create User entity with keycloakId reference
- Authentication: All password validation happens in Keycloak, JWT tokens issued by Keycloak
- **Permission Model**: System uses Keycloak roles (Administrator, Teacher, Student, Parent) + custom permissions for fine-grained access control
- Permissions stored in Keycloak and included in JWT tokens
- Permission checks performed at service level using Spring Security permission evaluators

**Alternatives Considered**:
- Auth0: External service dependency, cost concerns
- Okta: Enterprise-focused, higher cost
- Custom JWT solution: Security risks, maintenance burden
- Storing passwordHash in User entity: Security risk, duplicates Keycloak functionality, maintenance burden

### Decision: Elasticsearch 8.11.0 for Search

**Rationale**:
- Full-text search across students, teachers, classes
- Aggregations for reporting and analytics
- Scalable distributed search architecture
- Real-time indexing capabilities
- Strong Spring Data Elasticsearch integration

**Alternatives Considered**:
- PostgreSQL full-text search: Limited capabilities, performance concerns
- Apache Solr: Less modern, smaller ecosystem
- Algolia: External service, cost and dependency concerns

### Decision: MinIO for Object Storage

**Rationale**:
- S3-compatible API for easy migration
- Self-hosted solution (no external dependencies)
- Supports file uploads, documents, images
- Good performance for file operations
- Docker-friendly deployment

**Alternatives Considered**:
- AWS S3: External dependency, cost concerns
- Local file system: Scalability and backup challenges
- Ceph: More complex setup and maintenance

## Architecture Patterns

### Decision: Microservices Architecture with 16 Services

**Rationale**:
- Independent deployment and scaling of services
- Clear domain boundaries (user, academic, attendance, etc.)
- Team autonomy and parallel development
- Fault isolation (failure in one service doesn't cascade)
- Technology stack flexibility per service

**Service Boundaries**:
- **User Service**: User management, profiles, authentication flows
- **Academic Service**: Curriculum, classes, grade levels
- **Attendance Service**: Daily attendance tracking
- **Academic Assessment Service**: Grades, assessments, gradebooks
- **Other Services**: Follow domain-driven design principles

**Alternatives Considered**:
- Monolithic architecture: Scaling and maintenance challenges
- Fewer services: Less independent deployment capability
- More services: Increased operational complexity

### Decision: Event-Driven Communication for Cross-Service Updates

**Rationale**:
- Decouples services (no direct dependencies)
- Enables eventual consistency across services
- Improves system resilience (services can handle failures independently)
- Supports audit trails and event sourcing patterns
- Scales better than synchronous REST calls

**Event Types**:
- User created/updated → Notify other services
- Student enrolled → Update search index, notify parents
- Grade recorded → Update academic records, notify students/parents
- Attendance marked → Update reports, notify parents

**Alternatives Considered**:
- Synchronous REST calls: Tight coupling, cascading failures
- Database replication: Complex, tightly coupled
- Message queue per service pair: Too many queues, complexity

### Decision: API Gateway Pattern

**Rationale**:
- Single entry point for all client requests
- Centralized authentication and authorization
- Request routing to appropriate services
- Rate limiting and throttling
- Request/response transformation
- Reduces client complexity

**Alternatives Considered**:
- Direct service access: Client complexity, security concerns
- Service mesh: More complex, overkill for this scale

### Decision: Service Discovery with Eureka

**Rationale**:
- Automatic service registration and discovery
- Health checking and load balancing
- Service instance management
- Spring Cloud integration
- Simplifies service-to-service communication

**Alternatives Considered**:
- Consul: More features but more complex
- etcd: Lower-level, requires more implementation
- Kubernetes service discovery: Tied to Kubernetes platform

### Decision: Centralized Configuration with Config Server

**Rationale**:
- Single source of truth for configuration
- Environment-specific configurations (dev, staging, prod)
- Encrypted secrets management
- Configuration versioning
- Runtime configuration updates without redeployment

**Alternatives Considered**:
- Environment variables: Less secure, harder to manage
- External secrets management (HashiCorp Vault): Additional infrastructure
- Git-based config: Already supported by Spring Cloud Config

## Data Management Patterns

### Decision: Database per Service Pattern

**Rationale**:
- Service autonomy and independent schema evolution
- No shared database coupling
- Independent scaling and backups
- Technology flexibility (though using PostgreSQL for all)
- Data ownership and encapsulation

**Alternatives Considered**:
- Shared database: Tight coupling, scaling challenges
- Database sharding: Unnecessary complexity for this scale

### Decision: Event Sourcing for Audit Trails

**Rationale**:
- Complete audit history of all changes
- Compliance and regulatory requirements
- Event replay capabilities
- Immutable event log

**Implementation**: 
- Audit Service consumes events from all services
- Stores events with timestamps, user, action details
- Queryable audit logs for compliance

**Alternatives Considered**:
- Database triggers: Tight coupling, harder to maintain
- Change data capture: Additional infrastructure complexity

### Decision: Search Index Synchronization via Events

**Rationale**:
- Elasticsearch index updated asynchronously via events
- No direct database queries from search service
- Eventual consistency acceptable for search
- Decoupled from source of truth

**Alternatives Considered**:
- Direct database queries: Performance and coupling issues
- Periodic batch sync: Stale data, complexity

## Testing Strategy

### Decision: Test-First Development (TDD) with >80% Coverage

**Rationale**:
- Catches design issues early
- Ensures testable code structure
- Prevents regressions
- Enables confident refactoring
- Constitution requirement (Principle IV)

**Test Types**:
- **Unit Tests**: Business logic, services, utilities (>80% coverage, 100+ tests per service)
- **Integration Tests**: Database interactions, event publishing/consumption
- **Contract Tests**: API endpoint specifications (Spring Cloud Contract)
- **End-to-End Tests**: Critical user journeys

**Testing Tools**:
- JUnit 5: Test framework
- Mockito: Mocking dependencies
- Testcontainers: Integration tests with real databases/message queues
- Spring Boot Test: Application context testing

**Alternatives Considered**:
- Test-after development: Less effective, harder to maintain
- Lower coverage threshold: Insufficient for quality assurance

## Security Patterns

### Decision: OAuth2/JWT Token-Based Authentication

**Rationale**:
- Stateless authentication (no server-side sessions)
- Scalable across microservices
- Token validation at API Gateway
- Standard protocol (OAuth2)
- Keycloak integration

**Token Flow**:
1. User authenticates with Keycloak
2. Keycloak issues JWT token
3. Token included in API requests
4. API Gateway validates token
5. Token forwarded to services with user context

**Alternatives Considered**:
- Session-based authentication: Stateful, harder to scale
- API keys: Less secure, no user context
- Mutual TLS: More complex, less user-friendly

### Decision: Role-Based Access Control (RBAC)

**Rationale**:
- Clear permission model (Administrator, Teacher, Student, Parent)
- Enforced at API Gateway and service level
- Keycloak manages roles and permissions
- Fine-grained access control per endpoint

**Alternatives Considered**:
- Attribute-based access control: More complex, less clear
- Permission-based: Harder to manage at scale

## Observability Patterns

### Decision: ELK Stack for Centralized Logging

**Rationale**:
- Structured logging with correlation IDs
- Centralized log aggregation and search
- Real-time log analysis
- Integration with Spring Boot logging

**Alternatives Considered**:
- Splunk: Cost concerns
- CloudWatch: Vendor lock-in
- Local logging: No centralized view

### Decision: Prometheus + Grafana for Metrics

**Rationale**:
- Standard metrics format (Prometheus)
- Rich visualization (Grafana)
- Service-level metrics (requests, latency, errors)
- Business metrics (enrollments, attendance, grades)
- Spring Boot Actuator integration

**Alternatives Considered**:
- CloudWatch: Vendor lock-in
- Datadog: Cost concerns
- Custom metrics: Less standardized

### Decision: Distributed Tracing

**Rationale**:
- Track requests across multiple services
- Identify performance bottlenecks
- Debug distributed system issues
- Spring Cloud Sleuth integration

**Alternatives Considered**:
- No tracing: Harder to debug distributed issues
- Custom tracing: Less standardized

## Deployment Patterns

### Decision: Docker Containerization

**Rationale**:
- Consistent deployment across environments
- Easy scaling and orchestration
- Docker Compose for local development
- Kubernetes-ready (future scaling)

**Alternatives Considered**:
- Virtual machines: More overhead, slower startup
- Bare metal: Less flexible, harder to manage

### Decision: Infrastructure as Code (Docker Compose)

**Rationale**:
- Reproducible infrastructure setup
- Version-controlled infrastructure
- Easy local development setup
- Quick infrastructure provisioning

**Alternatives Considered**:
- Manual setup: Error-prone, not reproducible
- Terraform: Overkill for Docker Compose setup

## Performance Optimization Strategies

### Decision: Database Indexing Strategy

**Rationale**:
- Indexes on frequently queried fields (student ID, email, class ID)
- Composite indexes for common query patterns
- Regular index analysis and optimization

### Decision: Caching Strategy

**Rationale**:
- Redis caching for frequently accessed data (user profiles, class lists)
- Cache invalidation via events
- Reduces database load

**Alternatives Considered**:
- No caching: Performance concerns with 500 concurrent users
- Application-level caching: Less efficient than Redis

### Decision: Async Processing for Non-Critical Operations

**Rationale**:
- Email notifications processed asynchronously
- Report generation via background jobs
- Better user experience (non-blocking)

**Alternatives Considered**:
- Synchronous processing: Slower response times, poor UX

### Decision: K12 Education Model with Differentiated Class Teacher Roles

**Rationale**:
- K12 system (grades 1-12) requires different class management models for different grade levels
- Grades 1-6 (elementary) use homeroom model with single teacher responsible for all subjects
- Grades 7-12 (secondary) use subject-based classes with designated class teacher/coordinator
- Class teacher/coordinator (grades 7-12) needs special permissions to collect exam results and reports

**Architectural Decision**:
- **Grades 1-6**: Homeroom classes with single `homeroomTeacherId` per class
  - One homeroom teacher responsible for entire class
  - Class entity includes `classType` enum (HOMEROOM) and `homeroomTeacherId`
  - Validation: Only one homeroom class per grade level, homeroom teacher assignment only for grades 1-6
- **Grades 7-12**: Subject classes with designated class teacher/coordinator
  - Multiple subject classes per grade, each with subject teacher
  - One subject teacher designated as class teacher/coordinator via `isClassTeacher` flag in TeacherAssignment
  - Class teacher has permission `COLLECT_EXAM_RESULTS` and `SUBMIT_REPORTS` to collect results and submit to school
  - Validation: Class teacher must be one of the subject teachers for that class
- **Grade Level Validation**: Grade level must be between 1 and 12 (K12 system)

**Alternatives Considered**:
- Single class model for all grades: Doesn't reflect real-world K12 education structure
- No class teacher distinction: Lacks administrative workflow for exam result collection and reporting

### Decision: Permission-Based Access Control Model

**Rationale**:
- Fine-grained access control needed beyond basic roles (Administrator, Teacher, Student, Parent)
- Different teachers need different permissions (e.g., class teacher can collect reports, regular teacher cannot)
- Permissions can be assigned dynamically based on assignments (e.g., class teacher permission)
- Keycloak supports custom permissions in addition to roles

**Architectural Decision**:
- **Permission Entity**: Stores custom permissions (e.g., `COLLECT_EXAM_RESULTS`, `SUBMIT_REPORTS`, `VIEW_ALL_STUDENTS`, `MANAGE_GRADES`)
- **Permission-Role Mapping**: Maps permissions to roles (e.g., Class Teacher role includes `COLLECT_EXAM_RESULTS`)
- **Permission-User Assignment**: Permissions can be assigned directly to users or via roles
- **Keycloak Integration**: Permissions stored in Keycloak and included in JWT tokens
- **Service-Level Checks**: Spring Security permission evaluators check permissions at service endpoints
- **Permission Examples**:
  - `COLLECT_EXAM_RESULTS`: Class teachers (grades 7-12) can collect exam results from subject teachers
  - `SUBMIT_REPORTS`: Class teachers can submit aggregated reports to school administration
  - `MANAGE_HOMEROOM`: Homeroom teachers (grades 1-6) can manage their homeroom class
  - `VIEW_ALL_STUDENTS`: Administrators can view all student records

**Alternatives Considered**:
- Role-only RBAC: Insufficient granularity for class teacher vs regular teacher permissions
- Custom permission system outside Keycloak: Duplicates Keycloak functionality, increases complexity

## Conclusion

All technical decisions align with the constitution principles and support the requirements for:
- Secure, scalable microservices architecture
- Event-driven communication
- Comprehensive testing
- Centralized authentication with permission-based access control
- K12 education system with differentiated class teacher models
- Full-text search capabilities
- File storage and management
- Real-time notifications
- Advanced reporting

No further research needed - all technical choices are well-established patterns with proven implementations in the Spring/Kotlin ecosystem.

