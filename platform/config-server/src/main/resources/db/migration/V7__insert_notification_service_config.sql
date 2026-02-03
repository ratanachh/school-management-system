-- Insert notification-service configuration into Config Server
-- APPLICATION: notification-service, PROFILE: default, LABEL: master

INSERT INTO CONFIG_PROPERTIES (APPLICATION, PROFILE, LABEL, KEY, VALUE) VALUES
('notification-service', 'default', 'master', 'server.port', '8006'),
('notification-service', 'default', 'master', 'spring.datasource.url', 'jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}'),
('notification-service', 'default', 'master', 'spring.datasource.username', '${DB_USERNAME}'),
('notification-service', 'default', 'master', 'spring.datasource.password', '${DB_PASSWORD}'),
('notification-service', 'default', 'master', 'spring.datasource.driver-class-name', 'org.postgresql.Driver'),
('notification-service', 'default', 'master', 'spring.jpa.hibernate.ddl-auto', 'validate'),
('notification-service', 'default', 'master', 'spring.jpa.show-sql', '${JPA_SHOW_SQL}'),
('notification-service', 'default', 'master', 'spring.jpa.properties.hibernate.dialect', 'org.hibernate.dialect.PostgreSQLDialect'),
('notification-service', 'default', 'master', 'spring.jpa.properties.hibernate.format_sql', '${HIBERNATE_FORMAT_SQL}'),
('notification-service', 'default', 'master', 'spring.flyway.enabled', 'true'),
('notification-service', 'default', 'master', 'spring.flyway.locations', 'classpath:db/migration'),
('notification-service', 'default', 'master', 'spring.flyway.baseline-on-migrate', '${FLYWAY_BASELINE_ON_MIGRATE}'),
('notification-service', 'default', 'master', 'spring.rabbitmq.host', '${RABBITMQ_HOST}'),
('notification-service', 'default', 'master', 'spring.rabbitmq.port', '${RABBITMQ_PORT}'),
('notification-service', 'default', 'master', 'spring.rabbitmq.username', '${RABBITMQ_USER}'),
('notification-service', 'default', 'master', 'spring.rabbitmq.password', '${RABBITMQ_PASSWORD}'),
('notification-service', 'default', 'master', 'eureka.client.service-url.defaultZone', '${DISCOVERY_SERVER_URL}/eureka/'),
('notification-service', 'default', 'master', 'keycloak.realm', 'school-management'),
('notification-service', 'default', 'master', 'keycloak.auth-server-url', '${KEYCLOAK_SERVER_URL}'),
('notification-service', 'default', 'master', 'keycloak.resource', 'school-management-client'),
('notification-service', 'default', 'master', 'keycloak.credentials.secret', '${KEYCLOAK_CLIENT_SECRET}'),
('notification-service', 'default', 'master', 'management.endpoints.web.exposure.include', 'health,info,prometheus,metrics'),
('notification-service', 'default', 'master', 'management.endpoint.health.show-details', 'always'),
('notification-service', 'default', 'master', 'management.metrics.export.prometheus.enabled', 'true'),
('notification-service', 'default', 'master', 'management.metrics.tags.application', '${spring.application.name}'),
('notification-service', 'default', 'master', 'management.metrics.tags.environment', '${ENVIRONMENT}'),
('notification-service', 'default', 'master', 'logging.level.com.visor.school', 'DEBUG')
ON CONFLICT (APPLICATION, PROFILE, LABEL, KEY) DO NOTHING;

