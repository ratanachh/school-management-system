-- JWT issuer for OAuth2 resource server on microservices (required for JWT validation at runtime).
-- user-service already sets this in V2.

INSERT INTO CONFIG_PROPERTIES (APPLICATION, PROFILE, LABEL, KEY, VALUE) VALUES
('academic-service', 'default', 'master', 'spring.security.oauth2.resourceserver.jwt.issuer-uri', '${KEYCLOAK_SERVER_URL}/realms/${KEYCLOAK_REALM}'),
('academic-assessment-service', 'default', 'master', 'spring.security.oauth2.resourceserver.jwt.issuer-uri', '${KEYCLOAK_SERVER_URL}/realms/${KEYCLOAK_REALM}'),
('attendance-service', 'default', 'master', 'spring.security.oauth2.resourceserver.jwt.issuer-uri', '${KEYCLOAK_SERVER_URL}/realms/${KEYCLOAK_REALM}'),
('audit-service', 'default', 'master', 'spring.security.oauth2.resourceserver.jwt.issuer-uri', '${KEYCLOAK_SERVER_URL}/realms/${KEYCLOAK_REALM}'),
('notification-service', 'default', 'master', 'spring.security.oauth2.resourceserver.jwt.issuer-uri', '${KEYCLOAK_SERVER_URL}/realms/${KEYCLOAK_REALM}'),
('search-service', 'default', 'master', 'spring.security.oauth2.resourceserver.jwt.issuer-uri', '${KEYCLOAK_SERVER_URL}/realms/${KEYCLOAK_REALM}')
ON CONFLICT (APPLICATION, PROFILE, LABEL, KEY) DO NOTHING;
