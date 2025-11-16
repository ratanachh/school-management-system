# PostgreSQL Database Initialization

This directory contains initialization scripts for the PostgreSQL database container.

## Overview

The PostgreSQL container automatically executes any `.sql` scripts found in `/docker-entrypoint-initdb.d/` when the container is first created (when the data directory is empty).

## Files

- `01-init-databases.sql` - Creates all databases required by the microservices

## Databases Created

The initialization script creates the following databases:

1. **user_service** - User Service database
2. **academic_service** - Academic Service database
3. **attendance_service** - Attendance Service database
4. **academic_assessment_service** - Academic Assessment Service database
5. **notification_service** - Notification Service database
6. **audit_service** - Audit Service database
7. **config_server** - Config Server database

## Permissions

All databases are created with full privileges granted to the `school_user` user, including:
- Database-level permissions
- Schema-level permissions (public schema)
- Default privileges for tables and sequences

## Usage

The initialization script runs automatically when you start the PostgreSQL container for the first time:

```bash
docker-compose up postgres
```

Or start all services:

```bash
docker-compose up
```

## Important Notes

1. **One-time execution**: Initialization scripts only run when the data directory is empty. If you need to reinitialize:
   ```bash
   docker-compose down -v  # Removes volumes including data
   docker-compose up postgres  # Recreates with initialization
   ```

2. **Script order**: Scripts are executed in alphabetical order. The `01-` prefix ensures this script runs first.

3. **Flyway migrations**: After databases are created, each service will run its own Flyway migrations to create tables and schema.

4. **No data loss**: Once initialized, the databases persist in the `postgres_data` volume. Removing the container won't delete the data.

## Verification

After starting the container, you can verify all databases were created:

```bash
docker exec -it school-postgres psql -U school_user -d school_management -c "\l"
```

Or connect to the database:

```bash
docker exec -it school-postgres psql -U school_user -d user_service
```

