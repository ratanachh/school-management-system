# Quick Start Guide: School Management System

**Date**: 2025-01-27  
**Feature**: School Management System

This guide provides step-by-step instructions to set up and run the school management system locally for development.

## Prerequisites

Before starting, ensure you have the following installed:

- **JDK 25** (Temurin recommended) - [Download](https://adoptium.net/)
- **Maven 3.9.11** - [Download](https://maven.apache.org/download.cgi)
- **Docker & Docker Compose** - [Download](https://docs.docker.com/get-docker/)
- **Git** - [Download](https://git-scm.com/downloads)

Verify installations:

```bash
java -version  # Should show JDK 25
mvn -version   # Should show Maven 3.9.11
docker --version
docker-compose --version
```

## Initial Setup

### 1. Clone the Repository

```bash
git clone https://github.com/ratanachh/school-management-system.git
cd school-management
```

### 2. Initialize Git Submodules

```bash
git submodule init
git submodule update --recursive
```

### 3. Run Setup Script

```bash
chmod +x scripts/setup.sh
./scripts/setup.sh
```

The setup script will:
- Create necessary directories
- Set up local configuration files
- Initialize databases
- Configure environment variables

## Infrastructure Services

### Start Infrastructure Services

Start all required infrastructure services using Docker Compose:

```bash
docker-compose up -d
```

This starts:
- **PostgreSQL 17** (ports 5432, 5433, 5434, etc. for each service)
- **RabbitMQ** (port 5672, management UI on 15672)
- **Elasticsearch 8.11.0** (port 9200)
- **Kibana** (port 5601)
- **Keycloak** (port 8080 - conflicts with API Gateway, use 8090)
- **MinIO** (port 9000, console on 9001)
- **Prometheus** (port 9090)
- **Grafana** (port 3000)

### Verify Infrastructure Services

Check that all services are running:

```bash
docker-compose ps
```

Access service UIs:
- RabbitMQ Management: http://localhost:15672 (guest/guest)
- Elasticsearch: http://localhost:9200
- Kibana: http://localhost:5601
- Keycloak: http://localhost:8090 (admin/admin)
- MinIO Console: http://localhost:9001 (minioadmin/minioadmin)
- Grafana: http://localhost:3000 (admin/admin)

### Configure Keycloak

1. Access Keycloak Admin Console: http://localhost:8090
2. Login with admin credentials (default: admin/admin)
3. Create a new realm: `school-management`
4. Configure OAuth2 clients:
   - Create client: `api-gateway` (public client)
   - Create client: `user-service` (confidential client)
   - Configure redirect URIs
5. Create user roles:
   - ADMINISTRATOR
   - TEACHER
   - STUDENT
   - PARENT
6. Create test users for each role

## Platform Services

Platform services must be started before business services.

### 1. Config Server (Port 8888)

```bash
cd platform/config-server
mvn spring-boot:run
```

Verify: http://localhost:8888/actuator/health

### 2. Discovery Server (Port 8761)

```bash
cd platform/discovery-server
mvn spring-boot:run
```

Verify: http://localhost:8761 (Eureka dashboard)

### 3. API Gateway (Port 8080)

```bash
cd platform/api-gateway
mvn spring-boot:run
```

Verify: http://localhost:8080/actuator/health

## Business Services

Once platform services are running, start business services in any order:

### User Service (Port 8089)

```bash
cd services/user-service
mvn spring-boot:run
```

### Academic Service (Port 8082)

```bash
cd services/academic-service
mvn spring-boot:run
```

### Attendance Service (Port 8081)

```bash
cd services/attendance-service
mvn spring-boot:run
```

### Academic Assessment Service (Port 8084)

```bash
cd services/academic-assessment-service
mvn spring-boot:run
```

### Other Services

Start remaining services as needed:
- Timetable Service (Port 8083)
- Notification Service (Port 8086)
- Payment Service (Port 8087)
- Reporting Service (Port 8088)
- CMS Service (Port 8091)
- File Service (Port 8092)
- Search Service (Port 8093)
- Audit Service (Port 8094)
- Integration Service (Port 8095)
- Workflow Service (Port 8096)

## Build All Services

Alternatively, use the build script to compile all services:

```bash
./scripts/build-all.sh
```

This script:
- Compiles all services
- Runs tests
- Builds Docker images (if configured)

## Verify System

### Check Service Health

All services expose health endpoints:

```bash
curl http://localhost:8089/actuator/health  # User Service
curl http://localhost:8082/actuator/health  # Academic Service
curl http://localhost:8081/actuator/health  # Attendance Service
curl http://localhost:8084/actuator/health  # Assessment Service
```

### Check Service Discovery

View registered services in Eureka:
- http://localhost:8761

### Test Authentication

1. Create a user via User Service API
2. Verify email (check logs or mock email service)
3. Login and receive JWT token
4. Use token to access protected endpoints

Example API call:

```bash
# Login
curl -X POST http://localhost:8089/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@school.edu","password":"password123"}'

# Use token in subsequent requests
curl -X GET http://localhost:8080/api/v1/users/search \
  -H "Authorization: Bearer <token>"
```

## Development Workflow

### Running Individual Services

For faster development iteration, run only the service you're working on:

```bash
# Start infrastructure
docker-compose up -d postgres rabbitmq elasticsearch keycloak

# Start platform services
cd platform/config-server && mvn spring-boot:run &
cd platform/discovery-server && mvn spring-boot:run &
cd platform/api-gateway && mvn spring-boot:run &

# Start your service
cd services/user-service
mvn spring-boot:run
```

### Running Tests

Run tests for a specific service:

```bash
cd services/user-service
mvn test
```

Run all tests:

```bash
mvn test
```

### Database Migrations

Flyway runs migrations automatically on service startup. To manually run:

```bash
cd services/user-service
mvn flyway:migrate
```

### View Logs

View logs for all services:

```bash
# Docker services
docker-compose logs -f

# Individual service logs
tail -f services/user-service/logs/application.log
```

## Common Issues

### Port Conflicts

If ports are already in use:

1. Check what's using the port:
   ```bash
   lsof -i :8080
   ```

2. Stop conflicting services or change port in `application.yml`

### Database Connection Issues

1. Verify PostgreSQL is running:
   ```bash
   docker-compose ps postgres
   ```

2. Check database credentials in Config Server

3. Verify database exists (created by setup script)

### Keycloak Connection Issues

1. Verify Keycloak is accessible: http://localhost:8090
2. Check realm configuration
3. Verify OAuth2 client settings
4. Check JWT token validation

### Service Discovery Issues

1. Verify Discovery Server is running: http://localhost:8761
2. Check service registration in Eureka dashboard
3. Verify service names match in configuration

### RabbitMQ Connection Issues

1. Verify RabbitMQ is running:
   ```bash
   docker-compose ps rabbitmq
   ```

2. Check RabbitMQ management UI: http://localhost:15672
3. Verify connection settings in service configuration

## Next Steps

1. **Explore API Documentation**: Each service has OpenAPI/Swagger docs
   - User Service: http://localhost:8089/swagger-ui.html
   - Attendance Service: http://localhost:8081/swagger-ui.html
   - Assessment Service: http://localhost:8084/swagger-ui.html

2. **Create Test Data**: Use service APIs to create test users, students, teachers, classes

3. **Explore Monitoring**: 
   - Prometheus: http://localhost:9090
   - Grafana: http://localhost:3000

4. **View Logs**: Centralized logs via Kibana: http://localhost:5601

5. **Read Documentation**: 
   - See `specs/001-school-management-system/` for detailed specifications
   - See `data-model.md` for entity relationships
   - See `contracts/` for API specifications

## Troubleshooting

### Service Won't Start

1. Check logs: `tail -f services/<service-name>/logs/application.log`
2. Verify dependencies are running (PostgreSQL, RabbitMQ, Keycloak)
3. Check configuration in Config Server
4. Verify port is not in use

### Tests Failing

1. Ensure test databases are configured
2. Run tests with Testcontainers (requires Docker)
3. Check test coverage: `mvn test jacoco:report`

### Build Failures

1. Clean and rebuild: `mvn clean install`
2. Update dependencies: `mvn versions:display-dependency-updates`
3. Check JDK version: Must be JDK 25

## Getting Help

- Check service-specific README files in each service directory
- Review architecture documentation in `docs/`
- Check logs for detailed error messages
- Review constitution principles in `.specify/memory/constitution.md`

