# HTTP / Swagger auth strategy

## Recommended split

| Goal | Base URL | Notes |
|------|-----------|--------|
| **Explore & try APIs like a client** | `http://localhost:8080` (API gateway) | Paths: `/api/v1/...`. Matches production-style routing. JWT required for almost all routes; a few auth endpoints are public (see gateway security). |
| **Read OpenAPI & use Swagger UI** | `http://localhost:<service-port>` | Each service serves Swagger UI at `/swagger-ui/index.html`. OpenAPI is at `/v3/api-docs`. |

## Gateway security (reference)

[`platform/api-gateway/src/main/java/com/visor/school/gateway/security/SecurityConfig.java`](../../platform/api-gateway/src/main/java/com/visor/school/gateway/security/SecurityConfig.java) permits (without JWT), among others:

- `/api/v1/auth/login`
- `/api/v1/auth/reset-password`
- `/api/v1/auth/refresh-token`
- `/api/v1/auth/verify-email`
- `/actuator/**`, `/health`

Everything else under the gateway requires a **valid Bearer JWT** issued by your Keycloak realm (`issuer-uri` in gateway config).

## Getting a JWT (manual, as in your workflow)

1. Log in via the permitted login API (e.g. `POST /api/v1/auth/login`) or obtain a token from Keycloak directly.
2. In Swagger UI, click **Authorize** and set the Bearer token.
3. In `.http` files, set the `@token` variable (never commit real tokens).

## Path convention behind the gateway

The gateway uses `StripPrefix=1`: the first path segment `api` is removed before the request hits a service. So external path `/api/v1/students/...` becomes internal `/v1/students/...` on the service. Controllers must be mapped under `/v1/...` (not `/api/v1/...`) for gateway routing to work. Details: [`../swagger/GATEWAY_ROUTE_ALIGNMENT.md`](../swagger/GATEWAY_ROUTE_ALIGNMENT.md).
