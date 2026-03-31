# Browser walkthrough: Swagger UI verification

Use this checklist when the stack (Config Server, Discovery, Keycloak, Postgres, RabbitMQ, services, gateway) is running.

## 1. Preconditions

- Keycloak reachable at `KEYCLOAK_SERVER_URL` / realm `KEYCLOAK_REALM` (see `.env` or deployment config).
- Config Server loaded migrations including JWT issuer for services (`V11__oauth2_jwt_issuer_resource_services.sql`).
- You have a test user and a way to obtain a JWT (manual authorize in Swagger).

## 2. Open Swagger UI per service

| Service | URL |
|---------|-----|
| user-service | http://localhost:8081/swagger-ui/index.html |
| academic-service | http://localhost:8082/swagger-ui/index.html |
| attendance-service | http://localhost:8083/swagger-ui/index.html |
| academic-assessment-service | http://localhost:8084/swagger-ui/index.html |
| notification-service | http://localhost:8006/swagger-ui/index.html |
| search-service | http://localhost:8011/swagger-ui/index.html |
| audit-service | http://localhost:8012/swagger-ui/index.html |

## 3. Authorize (manual)

1. Obtain a JWT (login API or Keycloak).
2. In Swagger UI: **Authorize** → HTTP Bearer → paste token → **Authorize** → **Close**.

## 4. Smoke “Try it out”

Per service, pick 1–2 safe endpoints (e.g. GET with query params or health-related flows). Record:

- HTTP method + path
- Status code
- Whether response matches the documented schema

## 5. Gateway parity check

From Swagger, requests use the **OpenAPI `servers`** URL (often the service port). To verify **gateway** behavior, use the `.http` examples in [`../http/gateway.api.http`](../http/gateway.api.http) with `@gatewayUrl = http://localhost:8080` and the same Bearer token.

## 6. If something fails

| Symptom | Check |
|---------|--------|
| 401 on gateway | JWT missing/expired/wrong issuer; compare with `SecurityConfig` allowlist. |
| 404 on gateway, 200 on service direct | Path mapping vs `StripPrefix` — see [GATEWAY_ROUTE_ALIGNMENT.md](GATEWAY_ROUTE_ALIGNMENT.md). |
| 403 | Roles/permissions in `@PreAuthorize` vs token claims. |

## Note on method security

`@EnableMethodSecurity` is explicitly enabled in **user-service** only. Other services still declare `@PreAuthorize` on controllers; ensure method security is enabled where those annotations must be enforced (follow-up if you see inconsistent authorization behavior).
