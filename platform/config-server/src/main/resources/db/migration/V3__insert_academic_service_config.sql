-- Insert academic-service configuration into Config Server
-- APPLICATION: academic-service, PROFILE: default, LABEL: master

INSERT INTO CONFIG_PROPERTIES (APPLICATION, PROFILE, LABEL, KEY, VALUE) VALUES
('academic-service', 'default', 'master', 'server.port', '8082'),
('academic-service', 'default', 'master', 'spring.datasource.url', 'jdbc:postgresql://localhost:5432/academic_service'),
('academic-service', 'default', 'master', 'spring.datasource.username', '${DB_USERNAME:school_user}'),
('academic-service', 'default', 'master', 'spring.datasource.password', '${DB_PASSWORD:change_me}'),
('academic-service', 'default', 'master', 'spring.datasource.driver-class-name', 'org.postgresql.Driver'),
('academic-service', 'default', 'master', 'spring.jpa.hibernate.ddl-auto', 'validate'),
('academic-service', 'default', 'master', 'spring.jpa.show-sql', 'false'),
('academic-service', 'default', 'master', 'spring.jpa.properties.hibernate.dialect', 'org.hibernate.dialect.PostgreSQLDialect'),
('academic-service', 'default', 'master', 'spring.jpa.properties.hibernate.format_sql', 'true'),
('academic-service', 'default', 'master', 'spring.flyway.enabled', 'true'),
('academic-service', 'default', 'master', 'spring.flyway.locations', 'classpath:db/migration'),
('academic-service', 'default', 'master', 'spring.flyway.baseline-on-migrate', 'true'),
('academic-service', 'default', 'master', 'spring.rabbitmq.host', 'localhost'),
('academic-service', 'default', 'master', 'spring.rabbitmq.port', '5672'),
('academic-service', 'default', 'master', 'spring.rabbitmq.username', '${RABBITMQ_USER:admin}'),
('academic-service', 'default', 'master', 'spring.rabbitmq.password', '${RABBITMQ_PASSWORD:change_me}'),
('academic-service', 'default', 'master', 'eureka.client.service-url.defaultZone', 'http://localhost:8761/eureka/'),
('academic-service', 'default', 'master', 'keycloak.realm', 'school-management'),
('academic-service', 'default', 'master', 'keycloak.auth-server-url', 'http://localhost:8080'),
('academic-service', 'default', 'master', 'keycloak.resource', 'school-management-client'),
('academic-service', 'default', 'master', 'keycloak.credentials.secret', '${KEYCLOAK_CLIENT_SECRET:change_me}'),
('academic-service', 'default', 'master', 'management.endpoints.web.exposure.include', 'health,info,prometheus,metrics'),
('academic-service', 'default', 'master', 'management.endpoint.health.show-details', 'always'),
('academic-service', 'default', 'master', 'management.metrics.export.prometheus.enabled', 'true'),
('academic-service', 'default', 'master', 'management.metrics.tags.application', '${spring.application.name}'),
('academic-service', 'default', 'master', 'management.metrics.tags.environment', '${ENVIRONMENT:development}'),
('academic-service', 'default', 'master', 'logging.level.com.visor.school', 'DEBUG')
ON CONFLICT (APPLICATION, PROFILE, LABEL, KEY) DO NOTHING;

