# Keycloak Initializer Core

Shared Kotlin library that provisions Keycloak realms, clients, roles, and composite mappings in an idempotent manner. The library is consumed by `user-service` during startup and can also be executed manually via the CLI wrapper.

## Usage

### Automatic Startup (user-service)

1. Ensure `user-service` runs with the `bootstrap` profile (default in local setup).
2. Configure the following environment variables or Config Server properties:
   - `KEYCLOAK_URL` (e.g. `http://localhost:8070`)
   - `KEYCLOAK_ADMIN`, `KEYCLOAK_ADMIN_PASSWORD`, `KEYCLOAK_ADMIN_CLIENT_ID`
   - `KEYCLOAK_REALM`, `KEYCLOAK_CLIENT_ID`
3. On service startup the `KeycloakInitializerRunner` obtains the blueprint from `InitializerProperties` and applies it. If Keycloak already matches the blueprint, the initializer logs a skip message and completes in under a second.

### Manual Execution

Run the CLI wrapper for ad-hoc provisioning or drift correction:

```bash
./scripts/setup-keycloak.sh
```

This delegates to `ManualInitializerApplication` in the shared module.

## Blueprint Structure

`InitializerProperties` captures:
- Realm definition and initialization flag (`sso.system.initialized`)
- Client definitions (redirect URIs, web origins, attributes)
- Realm roles, client roles, and composite mappings
- Retry policy for transient Keycloak connectivity issues

`InitializerService` evaluates current Keycloak state using `KeycloakAdminStateReader` and only provisions missing pieces via `KeycloakRealmProvisioner`.

## Testing

- Unit tests: `DefaultInitializerStateEvaluatorTest` verifies the detection logic.
- Integration tests: `KeycloakInitializerIntegrationTest` (in user-service) runs against a Testcontainers Keycloak instance to confirm first-run provisioning and subsequent skips.
