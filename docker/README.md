# Docker Compose Setup for School Management System

This directory contains the Docker Compose configuration to run all infrastructure services required by the School Management System.

## Prerequisites

- Docker Engine 20.10 or higher
- Docker Compose v2.0 or higher

## Services Included

The docker-compose setup includes the following services:

1. **PostgreSQL 17** - Main database for all services
   - Port: 5432
   - Database: school_management
   - User: school_user

2. **Keycloak 26.4.2** - OAuth2/JWT authentication and authorization
   - Port: 8070 (mapped from internal 8080)
   - Admin Console: http://localhost:8070/admin/

3. **PostgreSQL 17** - Keycloak database
   - Internal only (not exposed to host)
   - Database: keycloak
   - User: keycloak

4. **RabbitMQ 3** - Message broker for event-driven communication
   - AMQP Port: 5672
   - Management UI: http://localhost:15672/
   - Default credentials: admin / (from .env)

5. **Elasticsearch 8.11.0** - Search engine
   - HTTP Port: 9200
   - Transport Port: 9300
   - Cluster Health: http://localhost:9200/_cluster/health

6. **MinIO** - S3-compatible object storage
   - API Port: 9000
   - Console Port: 9001
   - Console UI: http://localhost:9001/

## Quick Start

1. **Create environment file**
   ```bash
   # From the project root directory
   cp .env.example .env
   cp .env.example docker/.env
   ```
   **Important:** Copy `.env.example` to `.env` and never commit `.env` (it contains secrets). The `.env` file is in `.gitignore`.

2. **Edit the .env file** with your desired passwords:
   ```bash
   cd docker
   nano .env  # or use your preferred editor
   ```
   
   Important: Change all `change_me_in_production` values to secure passwords!

3. **Start all services**
   ```bash
   cd docker
   docker compose up -d
   ```

4. **Verify services are running**
   ```bash
   docker compose ps
   ```
   
   All services should show status as "healthy" after a minute or two.

5. **View logs** (if needed)
   ```bash
   docker compose logs -f [service-name]
   ```

## Stopping Services

```bash
cd docker
docker compose down
```

To also remove volumes (⚠️ this will delete all data):
```bash
docker compose down -v
```

## Service Health Checks

All services have health checks configured:

- **PostgreSQL**: Checks database readiness
- **Keycloak**: Checks realm endpoint availability
- **Keycloak DB**: Checks database readiness
- **RabbitMQ**: Uses rabbitmq-diagnostics ping
- **Elasticsearch**: Checks cluster health endpoint
- **MinIO**: Checks health/live endpoint

## Troubleshooting

### Services not starting
1. Check if ports are already in use:
   ```bash
   netstat -tuln | grep -E '5432|8070|5672|15672|9200|9000|9001'
   ```

2. View service logs:
   ```bash
   docker compose logs [service-name]
   ```

### Environment variables not loading
- Ensure `.env` file is in the `docker/` directory (same location as docker-compose.yml)
- Check file permissions: `chmod 644 .env`

### Keycloak not becoming healthy
- Keycloak takes 30-60 seconds to fully start
- Check logs: `docker compose logs keycloak`
- The health check uses the realms endpoint which is more reliable than the deprecated health endpoints

## Accessing Services

| Service | URL | Default Credentials |
|---------|-----|---------------------|
| RabbitMQ Management | http://localhost:15672 | admin / (from .env) |
| Keycloak Admin Console | http://localhost:8070/admin/ | admin / (from .env) |
| MinIO Console | http://localhost:9001 | admin / (from .env) |
| Elasticsearch | http://localhost:9200 | No auth required |

## Notes

- The setup uses persistent volumes for all data
- Volumes are prefixed with `docker_` (e.g., `docker_postgres_data`)
- All services are connected to a bridge network named `docker_school-network`
- Keycloak uses updated environment variables (KC_BOOTSTRAP_ADMIN_USERNAME instead of deprecated KEYCLOAK_ADMIN)
