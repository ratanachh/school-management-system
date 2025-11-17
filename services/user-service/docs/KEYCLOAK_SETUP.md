# Keycloak Setup for User Service

## Overview

The user service interacts with Keycloak in two ways:

1. **Keycloak Initializer** - Uses `admin-cli` client with admin credentials to bootstrap the realm on startup
2. **User Management Client** - Uses `user-profile` service account client to manage users at runtime

## Automatic Setup via Keycloak Initializer

The Keycloak Initializer automatically creates:
- Realm: `school-management`
- Client: `school-management-client` (for user authentication)
- Client: `user-profile` (service account for user management)
- Realm roles: `SUPER_ADMIN`, `ADMINISTRATOR`, `TEACHER`, `STUDENT`, `PARENT`
- Client roles: Various permission-based roles
- Composite role mappings

## Manual Configuration Required

After the automatic setup, you need to manually configure the `user-profile` service account with proper permissions:

### 1. Access Keycloak Admin Console

Navigate to: `http://localhost:8070` (or your Keycloak URL)

Login with:
- Username: `admin`
- Password: (value from `KEYCLOAK_ADMIN_PASSWORD` env var)

### 2. Configure Service Account Roles

1. Go to **Clients** → Find `user-profile`
2. Go to the **Service account roles** tab
3. Click **Assign role**
4. Filter by clients: Select `realm-management`
5. Assign the following roles:
   - `manage-users` - Create, update, delete users
   - `view-users` - Query and view users
   - `query-users` - Search users

### 3. Get Client Secret

1. Go to **Clients** → `user-profile`
2. Go to the **Credentials** tab
3. Copy the **Client secret**
4. Update your `.env` file:
   ```bash
   KEYCLOAK_SERVICE_CLIENT_SECRET=<paste-secret-here>
   ```

## Environment Variables

### Required for Keycloak Initializer
```bash
KEYCLOAK_ADMIN=admin
KEYCLOAK_ADMIN_PASSWORD=your-admin-password
```

### Required for User Service Operations
```bash
KEYCLOAK_SERVER_URL=http://localhost:8070
KEYCLOAK_REALM=school-management
KEYCLOAK_SERVICE_CLIENT_ID=user-profile
KEYCLOAK_SERVICE_CLIENT_SECRET=<client-secret-from-keycloak>
```

## Testing the Setup

After configuration, verify the setup:

1. Start the user-service
2. Check logs for successful Keycloak initialization
3. Test user creation via the user-service API
4. Verify users are created in Keycloak Admin Console

## Troubleshooting

### Error: "HTTP 403 Forbidden" when creating users

**Cause**: The `user-profile` client doesn't have the required realm management roles.

**Solution**: Follow the "Configure Service Account Roles" steps above.

### Error: "HTTP 401 Unauthorized"

**Cause**: Invalid client secret or credentials.

**Solution**: 
- Verify `KEYCLOAK_SERVICE_CLIENT_SECRET` matches the secret in Keycloak
- Ensure the `user-profile` client exists in the realm

### Keycloak Initializer Fails

**Cause**: Keycloak not running or wrong URL.

**Solution**:
- Verify Keycloak is running: `docker ps | grep keycloak`
- Check `KEYCLOAK_URL` environment variable points to correct URL
- Check Keycloak logs: `docker logs school-keycloak`

## Production Considerations

1. **Use strong client secrets** - Generate cryptographically secure secrets
2. **Rotate credentials regularly** - Update client secrets periodically
3. **Limit permissions** - Only grant necessary realm management roles
4. **Use HTTPS** - Always use TLS in production
5. **Monitor access** - Enable and review Keycloak audit logs
