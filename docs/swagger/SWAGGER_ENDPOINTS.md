# Swagger / OpenAPI endpoints

Spring Boot uses **springdoc-openapi** (`springdoc-openapi-starter-webmvc-ui` 2.6.0). Default paths (no custom `springdoc.*` overrides in this repo):

| Resource | Path |
|----------|------|
| Swagger UI | `/swagger-ui/index.html` (redirect from `/swagger-ui.html`) |
| OpenAPI JSON | `/v3/api-docs` |

## Service ports (from Config Server)

Defined in `platform/config-server/src/main/resources/db/migration/V*.sql` (`server.port`):

| Application | Port |
|-------------|------|
| user-service | 8081 |
| academic-service | 8082 |
| attendance-service | 8083 |
| academic-assessment-service | 8084 |
| notification-service | 8006 |
| search-service | 8011 |
| audit-service | 8012 |

## Local verification (stack running)

Replace `<port>` with the table above.

```bash
# Swagger UI (browser)
open "http://localhost:<port>/swagger-ui/index.html"

# OpenAPI document
curl -sS "http://localhost:<port>/v3/api-docs" | head
```

### user-service

[`SecurityConfig`](../../services/user-service/src/main/java/com/visor/school/userservice/config/SecurityConfig.java) allows **anonymous** access to `/swagger-ui/**` and `/v3/api-docs/**`. API calls still need a JWT unless the path is in `permitAll()` (e.g. `/v1/auth/login`).

### Other services

They depend on Spring Security + OAuth2 resource server. With Config Server applying `spring.security.oauth2.resourceserver.jwt.issuer-uri` (see `V11__oauth2_jwt_issuer_resource_services.sql`), expect **JWT required** for most endpoints including “Try it out” unless you add explicit `permitAll` for Swagger (only user-service does today). Use **Authorize** in Swagger UI with your Bearer token, or call through the gateway with the same token.

## API gateway

The gateway (`platform/api-gateway`, port **8080**) does **not** host Swagger UI. Use it for **client-style** calls: `http://localhost:8080/api/v1/...` with `Authorization: Bearer <jwt>`. See [AUTH_STRATEGY.md](../http/AUTH_STRATEGY.md).
