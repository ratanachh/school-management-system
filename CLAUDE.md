# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

### Build entire project
```bash
mvn clean install -DskipTests
```

### Build a single module
```bash
mvn clean install -pl services/user-service -am -DskipTests
```

### Run a specific service
```bash
cd services/user-service
mvn spring-boot:run
```

### Run tests for a specific service
```bash
mvn test -pl services/user-service
```

### Run a single test class
```bash
mvn test -pl services/user-service -Dtest=UserServiceTest
```

### Start infrastructure (PostgreSQL, Keycloak, RabbitMQ, Elasticsearch, MinIO)
```bash
cd docker && docker compose up -d
```

## Environment Setup

Copy `.env.example` to both locations before running anything:
```bash
cp .env.example .env
cp .env.example docker/.env
```

Key variables consumed by all services:
- `CONFIG_SERVER_URL` — Spring Cloud Config Server (default: `http://localhost:8888`)
- `DB_HOST`, `DB_PORT`, `DB_USERNAME`, `DB_PASSWORD` — PostgreSQL connection
- `KEYCLOAK_SERVER_URL`, `KEYCLOAK_REALM` — OAuth2 provider
- `DISCOVERY_SERVER_URL` — Eureka registry (default: `http://localhost:8761`)
- `RABBITMQ_HOST`, `RABBITMQ_PORT`, `RABBITMQ_USER`, `RABBITMQ_PASSWORD`

## Architecture Overview

Spring Boot 3 / Spring Cloud microservices with Java 21, all traffic entering through a single API gateway. Service-specific runtime configuration lives in the **Config Server** (PostgreSQL-backed), not in local `application.yml` files — local yamls only contain enough config to reach the Config Server.

### Required Startup Order

1. **Infrastructure** (`docker/docker-compose.yml`): PostgreSQL, Keycloak, RabbitMQ, Elasticsearch, MinIO
2. **`platform/config-server`** (port 8888) — reads from `config_server` PostgreSQL DB; config populated by its own Flyway migrations
3. **`platform/discovery-server`** (port 8761) — Eureka registry
4. **`platform/api-gateway`** (port 8080) — reactive Spring Cloud Gateway; validates JWTs against Keycloak
5. **Business services** (any order)

### API Gateway Routing

The gateway strips `/api/v1` and routes by Eureka service name:

| Path prefix | Target service |
|---|---|
| `/api/v1/users/**`, `/api/v1/auth/**`, `/api/v1/permissions/**`, `/api/v1/parents/**` | `user-service` |
| `/api/v1/academic/**`, `/api/v1/students/**`, `/api/v1/academic-records/**` | `academic-service` |
| `/api/v1/attendance/**`, `/api/v1/reports/**` | `attendance-service` |
| `/api/v1/assessments/**`, `/api/v1/grades/**`, `/api/v1/gradebooks/**`, `/api/v1/classes/**` | `assessment-service` |
| `/api/v1/search/**` | `search-service` |
| `/api/v1/audit/**` | `audit-service` |
| `/api/v1/notifications/**` | `notification-service` |

### Business Services

Each service has its own PostgreSQL database managed by Flyway:

| Service | Database |
|---|---|
| `user-service` | `user_service` |
| `academic-service` | `academic_service` |
| `attendance-service` | `attendance_service` |
| `assessment-service` | `academic_assessment_service` |
| `audit-service` | `audit_service` |
| `notification-service` | `notification_service` |
| `search-service` | Elasticsearch |

### Shared Libraries (`shared/`)

- **`common`** — `ApiResponse<T>`, `GlobalExceptionHandler`, `Permissions` constants, `BaseEvent`, base `SecurityConfig`, `RateLimitConfig` (Bucket4j). Every service depends on this.
- **`keycloak-integration`** — Keycloak realm/client provisioner (`KeycloakRealmProvisioner`, `InitializerService`). Used by `user-service` to bootstrap the realm on startup.

### Security Model

- Tokens are issued by Keycloak (`school-management` realm) and validated at the gateway and again at each service (OAuth2 Resource Server).
- Role hierarchy: `SUPER_ADMIN` → `Admin` → `Director` → `Teacher` → `Parent`/`Student`.
- Fine-grained permissions are stored in `user_service` DB and enforced via `@PreAuthorize` + `CustomPermissionEvaluator`.
- Inter-service admin calls use the `user-profile` Keycloak service account client.

### Event-Driven Communication (Outbox Pattern)

Domain events are written to an `outbox_events` table in the same DB transaction as the business operation, then `OutboxPublisherService` polls the table and forwards events to RabbitMQ. Event classes extend `BaseEvent` from `shared/common`.

### Configuration Management

All runtime config for each service lives in `CONFIG_PROPERTIES` rows in the `config_server` database. These rows are seeded by Flyway migrations in `platform/config-server/src/main/resources/db/migration/` (one migration file per service). To change a service's config, update or add a migration there.

### Infrastructure Ports (docker/docker-compose.yml)

| Service | Host Port |
|---|---|
| PostgreSQL 17 | 6432 |
| Keycloak 26.4.2 | 8070 (admin: `http://localhost:8070/admin/`) |
| RabbitMQ management UI | 15672 |
| Elasticsearch 8.11.0 | 9200 |
| MinIO API / Console | 5000 / 5001 |

PostgreSQL databases are created by `docker/postgres-init/01-init-databases.sql` on first container start.

### OpenAPI

Each service exposes Swagger UI at `/swagger-ui/index.html`. HTTP request samples are in `docs/http/` and OpenAPI specs in `docs/swagger/`.
