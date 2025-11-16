-- Create users table for User Service
-- Password management is handled by Keycloak, so no password_hash column

CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    keycloak_id VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    role VARCHAR(50) NOT NULL CHECK (role IN ('ADMINISTRATOR', 'TEACHER', 'STUDENT', 'PARENT')),
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(50),
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    account_status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE' CHECK (account_status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED', 'DELETED')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_users_keycloak_id ON users(keycloak_id);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);
CREATE INDEX IF NOT EXISTS idx_users_account_status ON users(account_status);

-- Comments
COMMENT ON TABLE users IS 'User accounts with Keycloak integration. Password management delegated to Keycloak.';
COMMENT ON COLUMN users.keycloak_id IS 'Keycloak user ID - links to Keycloak identity provider';
COMMENT ON COLUMN users.email_verified IS 'Email verification status - set to true after email verification';
COMMENT ON COLUMN users.account_status IS 'Account status - ACTIVE, INACTIVE, SUSPENDED, or DELETED';

