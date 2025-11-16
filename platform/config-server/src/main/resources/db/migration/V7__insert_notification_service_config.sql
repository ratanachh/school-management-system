-- Insert notification-service configuration into Config Server
-- APPLICATION: notification-service, PROFILE: default, LABEL: master

INSERT INTO CONFIG_PROPERTIES (APPLICATION, PROFILE, LABEL, KEY, VALUE) VALUES
('notification-service', 'default', 'master', 'server.port', '8006'),
('notification-service', 'default', 'master', 'spring.datasource.url', 'jdbc:postgresql://localhost:5432/notification_service'),
('notification-service', 'default', 'master', 'spring.datasource.username', '${DB_USERNAME:school_user}'),
('notification-service', 'default', 'master', 'spring.datasource.password', '${DB_PASSWORD:change_me}'),
('notification-service', 'default', 'master', 'spring.datasource.driver-class-name', 'org.postgresql.Driver'),
('notification-service', 'default', 'master', 'spring.jpa.hibernate.ddl-auto', 'validate'),
('notification-service', 'default', 'master', 'spring.jpa.show-sql', 'false'),
('notification-service', 'default', 'master', 'spring.jpa.properties.hibernate.dialect', 'org.hibernate.dialect.PostgreSQLDialect'),
('notification-service', 'default', 'master', 'spring.jpa.properties.hibernate.format_sql', 'true'),
('notification-service', 'default', 'master', 'spring.flyway.enabled', 'true'),
('notification-service', 'default', 'master', 'spring.flyway.locations', 'classpath:db/migration'),
('notification-service', 'default', 'master', 'spring.flyway.baseline-on-migrate', 'true'),
('notification-service', 'default', 'master', 'spring.rabbitmq.host', 'localhost'),
('notification-service', 'default', 'master', 'spring.rabbitmq.port', '5672'),
('notification-service', 'default', 'master', 'spring.rabbitmq.username', '${RABBITMQ_USER:admin}'),
('notification-service', 'default', 'master', 'spring.rabbitmq.password', '${RABBITMQ_PASSWORD:change_me}'),
('notification-service', 'default', 'master', 'eureka.client.service-url.defaultZone', 'http://localhost:8761/eureka/'),
('notification-service', 'default', 'master', 'keycloak.realm', 'school-management'),
('notification-service', 'default', 'master', 'keycloak.auth-server-url', 'http://localhost:8080'),
('notification-service', 'default', 'master', 'keycloak.resource', 'school-management-client'),
('notification-service', 'default', 'master', 'keycloak.credentials.secret', '${KEYCLOAK_CLIENT_SECRET:change_me}'),
('notification-service', 'default', 'master', 'management.endpoints.web.exposure.include', 'health,info,prometheus,metrics'),
('notification-service', 'default', 'master', 'management.endpoint.health.show-details', 'always'),
('notification-service', 'default', 'master', 'management.metrics.export.prometheus.enabled', 'true'),
('notification-service', 'default', 'master', 'management.metrics.tags.application', '${spring.application.name}'),
('notification-service', 'default', 'master', 'management.metrics.tags.environment', '${ENVIRONMENT:development}'),
('notification-service', 'default', 'master', 'logging.level.com.visor.school', 'DEBUG')
ON CONFLICT (APPLICATION, PROFILE, LABEL, KEY) DO NOTHING;

