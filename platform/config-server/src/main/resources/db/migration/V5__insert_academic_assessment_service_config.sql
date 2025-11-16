-- Insert academic-assessment-service configuration into Config Server
-- APPLICATION: academic-assessment-service, PROFILE: default, LABEL: master

INSERT INTO CONFIG_PROPERTIES (APPLICATION, PROFILE, LABEL, KEY, VALUE) VALUES
('academic-assessment-service', 'default', 'master', 'server.port', '8084'),
('academic-assessment-service', 'default', 'master', 'spring.datasource.url', 'jdbc:postgresql://localhost:5432/academic_assessment_service'),
('academic-assessment-service', 'default', 'master', 'spring.datasource.username', '${DB_USERNAME:school_user}'),
('academic-assessment-service', 'default', 'master', 'spring.datasource.password', '${DB_PASSWORD:change_me}'),
('academic-assessment-service', 'default', 'master', 'spring.datasource.driver-class-name', 'org.postgresql.Driver'),
('academic-assessment-service', 'default', 'master', 'spring.jpa.hibernate.ddl-auto', 'validate'),
('academic-assessment-service', 'default', 'master', 'spring.jpa.show-sql', 'false'),
('academic-assessment-service', 'default', 'master', 'spring.jpa.properties.hibernate.dialect', 'org.hibernate.dialect.PostgreSQLDialect'),
('academic-assessment-service', 'default', 'master', 'spring.jpa.properties.hibernate.format_sql', 'true'),
('academic-assessment-service', 'default', 'master', 'spring.flyway.enabled', 'true'),
('academic-assessment-service', 'default', 'master', 'spring.flyway.locations', 'classpath:db/migration'),
('academic-assessment-service', 'default', 'master', 'spring.flyway.baseline-on-migrate', 'true'),
('academic-assessment-service', 'default', 'master', 'spring.rabbitmq.host', 'localhost'),
('academic-assessment-service', 'default', 'master', 'spring.rabbitmq.port', '5672'),
('academic-assessment-service', 'default', 'master', 'spring.rabbitmq.username', '${RABBITMQ_USER:admin}'),
('academic-assessment-service', 'default', 'master', 'spring.rabbitmq.password', '${RABBITMQ_PASSWORD:change_me}'),
('academic-assessment-service', 'default', 'master', 'eureka.client.service-url.defaultZone', 'http://localhost:8761/eureka/'),
('academic-assessment-service', 'default', 'master', 'keycloak.realm', 'school-management'),
('academic-assessment-service', 'default', 'master', 'keycloak.auth-server-url', 'http://localhost:8080'),
('academic-assessment-service', 'default', 'master', 'keycloak.resource', 'school-management-client'),
('academic-assessment-service', 'default', 'master', 'keycloak.credentials.secret', '${KEYCLOAK_CLIENT_SECRET:change_me}'),
('academic-assessment-service', 'default', 'master', 'management.endpoints.web.exposure.include', 'health,info,prometheus,metrics'),
('academic-assessment-service', 'default', 'master', 'management.endpoint.health.show-details', 'always'),
('academic-assessment-service', 'default', 'master', 'management.metrics.export.prometheus.enabled', 'true'),
('academic-assessment-service', 'default', 'master', 'management.metrics.tags.application', '${spring.application.name}'),
('academic-assessment-service', 'default', 'master', 'management.metrics.tags.environment', '${ENVIRONMENT:development}'),
('academic-assessment-service', 'default', 'master', 'logging.level.com.visor.school', 'DEBUG')
ON CONFLICT (APPLICATION, PROFILE, LABEL, KEY) DO NOTHING;

