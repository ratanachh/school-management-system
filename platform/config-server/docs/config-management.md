# Config Server Configuration Management

## Overview

The School Management System uses Spring Cloud Config Server with a JDBC backend (PostgreSQL) for centralized configuration management. All service-specific configurations are stored in the `CONFIG_PROPERTIES` table and retrieved by services at startup.

## Architecture

- **Config Server**: `platform/config-server` (port 8888)
- **Backend**: PostgreSQL database (`config_server`)
- **Table**: `CONFIG_PROPERTIES`
- **Schema**: `APPLICATION`, `PROFILE`, `LABEL`, `KEY`, `VALUE`

## Configuration Structure

### Table Schema

```sql
CREATE TABLE CONFIG_PROPERTIES (
    ID BIGSERIAL PRIMARY KEY,
    APPLICATION VARCHAR(255) NOT NULL,  -- Service name (e.g., 'user-service')
    PROFILE VARCHAR(255) NOT NULL,       -- Environment profile (e.g., 'default', 'dev', 'prod')
    LABEL VARCHAR(255) NOT NULL,        -- Version/branch (e.g., 'master', 'v1.0')
    KEY VARCHAR(255) NOT NULL,          -- Property key (e.g., 'server.port')
    VALUE TEXT NOT NULL,                -- Property value
    CREATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UPDATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(APPLICATION, PROFILE, LABEL, KEY)
);
```

### Property Key Format

Properties use dot notation matching Spring Boot property hierarchy:
- `server.port` → `server.port` in application.yml
- `spring.datasource.url` → `spring.datasource.url` in application.yml
- `management.endpoints.web.exposure.include` → `management.endpoints.web.exposure.include` in application.yml

## Adding Configuration

### 1. Add New Property

To add a new configuration property for a service:

```sql
INSERT INTO CONFIG_PROPERTIES (APPLICATION, PROFILE, LABEL, KEY, VALUE) 
VALUES ('user-service', 'default', 'master', 'new.property.key', 'property-value')
ON CONFLICT (APPLICATION, PROFILE, LABEL, KEY) DO UPDATE 
SET VALUE = EXCLUDED.VALUE, UPDATED_AT = CURRENT_TIMESTAMP;
```

### 2. Update Existing Property

To update an existing property:

```sql
UPDATE CONFIG_PROPERTIES 
SET VALUE = 'new-value', UPDATED_AT = CURRENT_TIMESTAMP
WHERE APPLICATION = 'user-service' 
  AND PROFILE = 'default' 
  AND LABEL = 'master' 
  AND KEY = 'server.port';
```

### 3. Environment-Specific Configuration

To add configuration for a specific environment (e.g., `production`):

```sql
INSERT INTO CONFIG_PROPERTIES (APPLICATION, PROFILE, LABEL, KEY, VALUE) 
VALUES ('user-service', 'production', 'master', 'server.port', '8081')
ON CONFLICT (APPLICATION, PROFILE, LABEL, KEY) DO UPDATE 
SET VALUE = EXCLUDED.VALUE, UPDATED_AT = CURRENT_TIMESTAMP;
```

Services can activate profiles using `spring.profiles.active=production` in their `application.yml` or via environment variables.

### 4. Version-Specific Configuration

To add configuration for a specific version/label:

```sql
INSERT INTO CONFIG_PROPERTIES (APPLICATION, PROFILE, LABEL, KEY, VALUE) 
VALUES ('user-service', 'default', 'v1.0', 'server.port', '8081')
ON CONFLICT (APPLICATION, PROFILE, LABEL, KEY) DO UPDATE 
SET VALUE = EXCLUDED.VALUE, UPDATED_AT = CURRENT_TIMESTAMP;
```

## Migration Scripts

Configuration properties are managed through Flyway migration scripts in:
- `platform/config-server/src/main/resources/db/migration/`

### Creating a New Migration

1. Create a new migration file: `V{version}__description.sql`
2. Use INSERT statements with `ON CONFLICT DO NOTHING` or `ON CONFLICT DO UPDATE`
3. Include comments describing the configuration

Example:
```sql
-- Insert new configuration for user-service
INSERT INTO CONFIG_PROPERTIES (APPLICATION, PROFILE, LABEL, KEY, VALUE) VALUES
('user-service', 'default', 'master', 'new.feature.enabled', 'true')
ON CONFLICT (APPLICATION, PROFILE, LABEL, KEY) DO UPDATE 
SET VALUE = EXCLUDED.VALUE, UPDATED_AT = CURRENT_TIMESTAMP;
```

## Service Configuration

### Bootstrap Configuration

Each service has a `bootstrap.yml` file that contains:
- `spring.application.name` - Service identifier for Config Server lookup
- `spring.cloud.config.uri` - Config Server URL
- `spring.cloud.config.fail-fast` - Whether to fail if Config Server is unavailable
- `spring.cloud.config.retry` - Retry configuration for connection resilience

### Local Configuration

Each service's `application.yml` contains only minimal local configuration:
- `spring.application.name` - Must match Config Server APPLICATION value

All other configuration is retrieved from Config Server.

## Refreshing Configuration

### Manual Refresh

To refresh configuration without restarting services:

1. Update properties in `CONFIG_PROPERTIES` table
2. Call the refresh endpoint (if actuator is enabled):
   ```bash
   curl -X POST http://localhost:8081/actuator/refresh
   ```

### Automatic Refresh

For automatic refresh, consider implementing:
- Spring Cloud Bus with RabbitMQ
- Webhook notifications
- Scheduled refresh tasks

## Environment Variables

Services can override Config Server properties using environment variables:
- Format: Convert dots to underscores and uppercase
- Example: `server.port` → `SERVER_PORT`
- Example: `spring.datasource.url` → `SPRING_DATASOURCE_URL`

Environment variables take precedence over Config Server properties.

## Best Practices

1. **Use Environment Variables for Secrets**: Never store sensitive values (passwords, API keys) in Config Server. Use environment variables with placeholders in Config Server:
   ```sql
   VALUE = '${DB_PASSWORD:default-value}'
   ```

2. **Version Control**: Keep migration scripts in version control for audit and rollback

3. **Environment Separation**: Use different PROFILE values for different environments (dev, staging, prod)

4. **Documentation**: Comment migration scripts explaining why properties are set

5. **Testing**: Test configuration changes in development before applying to production

6. **Fallback Values**: Use default values in Config Server (e.g., `${VAR:default}`) for resilience

## Troubleshooting

### Service Cannot Connect to Config Server

1. Check Config Server is running: `curl http://localhost:8888/actuator/health`
2. Verify `bootstrap.yml` has correct `spring.cloud.config.uri`
3. Check network connectivity between service and Config Server
4. Verify `fail-fast: false` is set if Config Server may be unavailable

### Service Not Loading Configuration

1. Verify property exists in `CONFIG_PROPERTIES` table:
   ```sql
   SELECT * FROM CONFIG_PROPERTIES 
   WHERE APPLICATION = 'user-service' 
     AND PROFILE = 'default' 
     AND LABEL = 'master';
   ```

2. Check application name matches: `spring.application.name` in service must match `APPLICATION` in database

3. Verify profile is correct: Check `spring.profiles.active` matches `PROFILE` in database

4. Check Config Server logs for errors

### Configuration Not Updating

1. Services cache configuration at startup
2. Use `/actuator/refresh` endpoint to reload (requires Spring Cloud Actuator)
3. Or restart the service to pick up new configuration

## Service Configuration Files

- **Bootstrap**: `services/{service-name}/src/main/resources/bootstrap.yml`
- **Application**: `services/{service-name}/src/main/resources/application.yml`
- **Migration Scripts**: `platform/config-server/src/main/resources/db/migration/V{version}__*.sql`

## Related Documentation

- [Spring Cloud Config Documentation](https://docs.spring.io/spring-cloud-config/docs/current/reference/html/)
- [Config Server Application Configuration](../src/main/resources/application.yml)
- [Database Schema](../src/main/resources/db/migration/V1__create_config_properties_table.sql)

