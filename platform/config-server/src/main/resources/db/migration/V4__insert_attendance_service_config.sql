-- Insert attendance-service configuration into Config Server
-- APPLICATION: attendance-service, PROFILE: default, LABEL: master

INSERT INTO CONFIG_PROPERTIES (APPLICATION, PROFILE, LABEL, KEY, VALUE) VALUES
('attendance-service', 'default', 'master', 'server.port', '8083'),
('attendance-service', 'default', 'master', 'spring.datasource.url', 'jdbc:postgresql://localhost:6432/attendance_service'),
('attendance-service', 'default', 'master', 'spring.datasource.username', '${DB_USERNAME}'),
('attendance-service', 'default', 'master', 'spring.datasource.password', '${DB_PASSWORD}'),
('attendance-service', 'default', 'master', 'spring.datasource.driver-class-name', 'org.postgresql.Driver'),
('attendance-service', 'default', 'master', 'spring.jpa.hibernate.ddl-auto', 'validate'),
('attendance-service', 'default', 'master', 'spring.jpa.show-sql', 'false'),
('attendance-service', 'default', 'master', 'spring.jpa.properties.hibernate.dialect', 'org.hibernate.dialect.PostgreSQLDialect'),
('attendance-service', 'default', 'master', 'spring.jpa.properties.hibernate.format_sql', 'true'),
('attendance-service', 'default', 'master', 'spring.flyway.enabled', 'true'),
('attendance-service', 'default', 'master', 'spring.flyway.locations', 'classpath:db/migration'),
('attendance-service', 'default', 'master', 'spring.flyway.baseline-on-migrate', 'true'),
('attendance-service', 'default', 'master', 'spring.rabbitmq.host', 'localhost'),
('attendance-service', 'default', 'master', 'spring.rabbitmq.port', '5672'),
('attendance-service', 'default', 'master', 'spring.rabbitmq.username', '${RABBITMQ_USER}'),
('attendance-service', 'default', 'master', 'spring.rabbitmq.password', '${RABBITMQ_PASSWORD}'),
('attendance-service', 'default', 'master', 'eureka.client.service-url.defaultZone', 'http://localhost:8761/eureka/'),
('attendance-service', 'default', 'master', 'keycloak.realm', 'school-management'),
('attendance-service', 'default', 'master', 'keycloak.auth-server-url', 'http://localhost:8080'),
('attendance-service', 'default', 'master', 'keycloak.resource', 'school-management-client'),
('attendance-service', 'default', 'master', 'keycloak.credentials.secret', '${KEYCLOAK_CLIENT_SECRET}'),
('attendance-service', 'default', 'master', 'management.endpoints.web.exposure.include', 'health,info,prometheus,metrics'),
('attendance-service', 'default', 'master', 'management.endpoint.health.show-details', 'always'),
('attendance-service', 'default', 'master', 'management.metrics.export.prometheus.enabled', 'true'),
('attendance-service', 'default', 'master', 'management.metrics.tags.application', '${spring.application.name}'),
('attendance-service', 'default', 'master', 'management.metrics.tags.environment', '${ENVIRONMENT}'),
('attendance-service', 'default', 'master', 'logging.level.com.visor.school', 'DEBUG')
ON CONFLICT (APPLICATION, PROFILE, LABEL, KEY) DO NOTHING;

