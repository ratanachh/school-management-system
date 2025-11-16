-- Create permissions table for fine-grained access control

CREATE TABLE IF NOT EXISTS permissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    permission_key VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    category VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_permissions_key ON permissions(permission_key);
CREATE INDEX IF NOT EXISTS idx_permissions_category ON permissions(category);

-- Comments
COMMENT ON TABLE permissions IS 'Fine-grained permissions for access control. Synced with Keycloak.';
COMMENT ON COLUMN permissions.permission_key IS 'Unique permission identifier (e.g., COLLECT_EXAM_RESULTS, SUBMIT_REPORTS)';

