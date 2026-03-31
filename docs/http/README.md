# REST Client / manual API notes

This folder holds **VS Code REST Client** (Humao) style `.http` files for smoke checks against the **API gateway** and optional direct **user-service** calls.

## Files

| File | Purpose |
|------|---------|
| [`gateway.api.http`](gateway.api.http) | Calls through `http://localhost:8080` (`/api/v1/...`). |
| [`user-service-direct.http`](user-service-direct.http) | Direct calls to user-service (port 8081) for Swagger-aligned checks. |

## Variables

Set at the top of each file:

- `@gatewayUrl` — default `http://localhost:8080`
- `@userServiceUrl` — default `http://localhost:8081`
- `@token` — paste JWT after login (never commit real values)

Auth strategy and gateway path rules: [`AUTH_STRATEGY.md`](AUTH_STRATEGY.md), [`../swagger/GATEWAY_ROUTE_ALIGNMENT.md`](../swagger/GATEWAY_ROUTE_ALIGNMENT.md).

## Swagger UI

See [`../swagger/BROWSER_WALKTHROUGH.md`](../swagger/BROWSER_WALKTHROUGH.md) and [`../swagger/SWAGGER_ENDPOINTS.md`](../swagger/SWAGGER_ENDPOINTS.md).
