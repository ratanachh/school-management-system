-- Insert audit-service configuration into Config Server
-- APPLICATION: audit-service, PROFILE: default, LABEL: master

INSERT INTO CONFIG_PROPERTIES (APPLICATION, PROFILE, LABEL, KEY, VALUE) VALUES
('audit-service', 'default', 'master', 'server.port', '8012'),
('audit-service', 'default', 'master', 'spring.datasource.url', 'jdbc:postgresql://localhost:6432/audit_service'),
('audit-service', 'default', 'master', 'spring.datasource.username', '${DB_USERNAME}'),
('audit-service', 'default', 'master', 'spring.datasource.password', '${DB_PASSWORD}'),
('audit-service', 'default', 'master', 'spring.datasource.driver-class-name', 'org.postgresql.Driver'),
('audit-service', 'default', 'master', 'spring.jpa.hibernate.ddl-auto', 'validate'),
('audit-service', 'default', 'master', 'spring.jpa.show-sql', 'false'),
('audit-service', 'default', 'master', 'spring.jpa.properties.hibernate.dialect', 'org.hibernate.dialect.PostgreSQLDialect'),
('audit-service', 'default', 'master', 'spring.jpa.properties.hibernate.format_sql', 'true'),
('audit-service', 'default', 'master', 'spring.flyway.enabled', 'true'),
('audit-service', 'default', 'master', 'spring.flyway.locations', 'classpath:db/migration'),
('audit-service', 'default', 'master', 'spring.flyway.baseline-on-migrate', 'true'),
('audit-service', 'default', 'master', 'spring.rabbitmq.host', 'localhost'),
('audit-service', 'default', 'master', 'spring.rabbitmq.port', '5672'),
('audit-service', 'default', 'master', 'spring.rabbitmq.username', '${RABBITMQ_USER}'),
('audit-service', 'default', 'master', 'spring.rabbitmq.password', '${RABBITMQ_PASSWORD}'),
('audit-service', 'default', 'master', 'eureka.client.service-url.defaultZone', 'http://localhost:8761/eureka/'),
('audit-service', 'default', 'master', 'keycloak.realm', 'school-management'),
('audit-service', 'default', 'master', 'keycloak.auth-server-url', 'http://localhost:8080'),
('audit-service', 'default', 'master', 'keycloak.resource', 'school-management-client'),
('audit-service', 'default', 'master', 'keycloak.credentials.secret', '${KEYCLOAK_CLIENT_SECRET}'),
('audit-service', 'default', 'master', 'management.endpoints.web.exposure.include', 'health,info,prometheus,metrics'),
('audit-service', 'default', 'master', 'management.endpoint.health.show-details', 'always'),
('audit-service', 'default', 'master', 'management.metrics.export.prometheus.enabled', 'true'),
('audit-service', 'default', 'master', 'management.metrics.tags.application', '${spring.application.name}'),
('audit-service', 'default', 'master', 'management.metrics.tags.environment', '${ENVIRONMENT}'),
('audit-service', 'default', 'master', 'logging.level.com.visor.school', 'DEBUG')
ON CONFLICT (APPLICATION, PROFILE, LABEL, KEY) DO NOTHING;

