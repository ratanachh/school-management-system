-- Insert search-service configuration into Config Server
-- APPLICATION: search-service, PROFILE: default, LABEL: master
-- Note: search-service does not use a database (no datasource config)

INSERT INTO CONFIG_PROPERTIES (APPLICATION, PROFILE, LABEL, KEY, VALUE) VALUES
('search-service', 'default', 'master', 'server.port', '8011'),
('search-service', 'default', 'master', 'spring.data.elasticsearch.cluster-name', '${ELASTICSEARCH_CLUSTER_NAME}'),
('search-service', 'default', 'master', 'spring.data.elasticsearch.cluster-nodes', '${ELASTICSEARCH_NODES}'),
('search-service', 'default', 'master', 'spring.data.elasticsearch.repositories.enabled', 'true'),
('search-service', 'default', 'master', 'spring.rabbitmq.host', '${RABBITMQ_HOST}'),
('search-service', 'default', 'master', 'spring.rabbitmq.port', '${RABBITMQ_PORT}'),
('search-service', 'default', 'master', 'spring.rabbitmq.username', '${RABBITMQ_USER}'),
('search-service', 'default', 'master', 'spring.rabbitmq.password', '${RABBITMQ_PASSWORD}'),
('search-service', 'default', 'master', 'eureka.client.service-url.defaultZone', '${DISCOVERY_SERVER_URL}/eureka/'),
('search-service', 'default', 'master', 'keycloak.realm', 'school-management'),
('search-service', 'default', 'master', 'keycloak.auth-server-url', '${KEYCLOAK_SERVER_URL}'),
('search-service', 'default', 'master', 'keycloak.resource', 'school-management-client'),
('search-service', 'default', 'master', 'keycloak.credentials.secret', '${KEYCLOAK_CLIENT_SECRET}'),
('search-service', 'default', 'master', 'management.endpoints.web.exposure.include', 'health,info,prometheus,metrics'),
('search-service', 'default', 'master', 'management.endpoint.health.show-details', 'always'),
('search-service', 'default', 'master', 'management.metrics.export.prometheus.enabled', 'true'),
('search-service', 'default', 'master', 'management.metrics.tags.application', '${spring.application.name}'),
('search-service', 'default', 'master', 'management.metrics.tags.environment', '${ENVIRONMENT}'),
('search-service', 'default', 'master', 'logging.level.com.visor.school', 'DEBUG')
ON CONFLICT (APPLICATION, PROFILE, LABEL, KEY) DO NOTHING;

