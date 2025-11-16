-- Insert user-service configuration into Config Server
-- APPLICATION: user-service, PROFILE: default, LABEL: master

INSERT INTO CONFIG_PROPERTIES (APPLICATION, PROFILE, LABEL, KEY, VALUE) VALUES
('user-service', 'default', 'master', 'server.port', '8081'),
('user-service', 'default', 'master', 'spring.datasource.url', 'jdbc:postgresql://localhost:5432/user_service'),
('user-service', 'default', 'master', 'spring.datasource.username', '${DB_USERNAME:school_user}'),
('user-service', 'default', 'master', 'spring.datasource.password', '${DB_PASSWORD:change_me}'),
('user-service', 'default', 'master', 'spring.datasource.driver-class-name', 'org.postgresql.Driver'),
('user-service', 'default', 'master', 'spring.jpa.hibernate.ddl-auto', 'validate'),
('user-service', 'default', 'master', 'spring.jpa.show-sql', 'false'),
('user-service', 'default', 'master', 'spring.jpa.properties.hibernate.dialect', 'org.hibernate.dialect.PostgreSQLDialect'),
('user-service', 'default', 'master', 'spring.jpa.properties.hibernate.format_sql', 'true'),
('user-service', 'default', 'master', 'spring.flyway.enabled', 'true'),
('user-service', 'default', 'master', 'spring.flyway.locations', 'classpath:db/migration'),
('user-service', 'default', 'master', 'spring.flyway.baseline-on-migrate', 'true'),
('user-service', 'default', 'master', 'spring.rabbitmq.host', 'localhost'),
('user-service', 'default', 'master', 'spring.rabbitmq.port', '5672'),
('user-service', 'default', 'master', 'spring.rabbitmq.username', '${RABBITMQ_USER:admin}'),
('user-service', 'default', 'master', 'spring.rabbitmq.password', '${RABBITMQ_PASSWORD:change_me}'),
('user-service', 'default', 'master', 'spring.security.oauth2.resourceserver.jwt.issuer-uri', '${KEYCLOAK_SERVER_URL:http://localhost:8080}/realms/${KEYCLOAK_REALM:school-management}'),
('user-service', 'default', 'master', 'eureka.client.service-url.defaultZone', 'http://localhost:8761/eureka/'),
('user-service', 'default', 'master', 'keycloak.realm', '${KEYCLOAK_REALM:school-management}'),
('user-service', 'default', 'master', 'keycloak.auth-server-url', '${KEYCLOAK_SERVER_URL:http://localhost:8080}'),
('user-service', 'default', 'master', 'keycloak.server-url', '${KEYCLOAK_SERVER_URL:http://localhost:8080}'),
('user-service', 'default', 'master', 'keycloak.admin-client-id', '${KEYCLOAK_ADMIN_CLIENT_ID:admin-cli}'),
('user-service', 'default', 'master', 'keycloak.admin-client-secret', '${KEYCLOAK_ADMIN_CLIENT_SECRET:change_me}'),
('user-service', 'default', 'master', 'management.endpoints.web.exposure.include', 'health,info,prometheus,metrics'),
('user-service', 'default', 'master', 'management.endpoint.health.show-details', 'always'),
('user-service', 'default', 'master', 'management.metrics.export.prometheus.enabled', 'true'),
('user-service', 'default', 'master', 'management.metrics.tags.application', '${spring.application.name}'),
('user-service', 'default', 'master', 'management.metrics.tags.environment', '${ENVIRONMENT:development}'),
('user-service', 'default', 'master', 'logging.level.com.visor.school', 'DEBUG'),
('user-service', 'default', 'master', 'logging.level.org.springframework.security', 'DEBUG')
ON CONFLICT (APPLICATION, PROFILE, LABEL, KEY) DO NOTHING;

