-- Insert search-service configuration into Config Server
-- APPLICATION: search-service, PROFILE: default, LABEL: master
-- Note: search-service does not use a database (no datasource config)

INSERT INTO CONFIG_PROPERTIES (APPLICATION, PROFILE, LABEL, KEY, VALUE) VALUES
('search-service', 'default', 'master', 'server.port', '8011'),
('search-service', 'default', 'master', 'spring.data.elasticsearch.cluster-name', '${ELASTICSEARCH_CLUSTER_NAME:elasticsearch}'),
('search-service', 'default', 'master', 'spring.data.elasticsearch.cluster-nodes', '${ELASTICSEARCH_NODES:localhost:9300}'),
('search-service', 'default', 'master', 'spring.data.elasticsearch.repositories.enabled', 'true'),
('search-service', 'default', 'master', 'spring.rabbitmq.host', 'localhost'),
('search-service', 'default', 'master', 'spring.rabbitmq.port', '5672'),
('search-service', 'default', 'master', 'spring.rabbitmq.username', '${RABBITMQ_USER:admin}'),
('search-service', 'default', 'master', 'spring.rabbitmq.password', '${RABBITMQ_PASSWORD:change_me}'),
('search-service', 'default', 'master', 'eureka.client.service-url.defaultZone', 'http://localhost:8761/eureka/'),
('search-service', 'default', 'master', 'keycloak.realm', 'school-management'),
('search-service', 'default', 'master', 'keycloak.auth-server-url', 'http://localhost:8080'),
('search-service', 'default', 'master', 'keycloak.resource', 'school-management-client'),
('search-service', 'default', 'master', 'keycloak.credentials.secret', '${KEYCLOAK_CLIENT_SECRET:change_me}'),
('search-service', 'default', 'master', 'management.endpoints.web.exposure.include', 'health,info,prometheus,metrics'),
('search-service', 'default', 'master', 'management.endpoint.health.show-details', 'always'),
('search-service', 'default', 'master', 'management.metrics.export.prometheus.enabled', 'true'),
('search-service', 'default', 'master', 'management.metrics.tags.application', '${spring.application.name}'),
('search-service', 'default', 'master', 'management.metrics.tags.environment', '${ENVIRONMENT:development}'),
('search-service', 'default', 'master', 'logging.level.com.visor.school', 'DEBUG')
ON CONFLICT (APPLICATION, PROFILE, LABEL, KEY) DO NOTHING;

