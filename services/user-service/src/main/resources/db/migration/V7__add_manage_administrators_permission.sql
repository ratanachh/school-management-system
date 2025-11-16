-- Add MANAGE_ADMINISTRATORS permission to permissions table
-- This permission allows users to create, update, and manage administrator accounts

INSERT INTO permissions (id, permission_key, name, description, category, created_at, updated_at)
VALUES (
    gen_random_uuid(),
    'MANAGE_ADMINISTRATORS',
    'Manage Administrators',
    'Permission to create, update, and manage administrator accounts',
    'ADMINISTRATIVE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (permission_key) DO NOTHING;

-- Comment
COMMENT ON TABLE permissions IS 'Fine-grained permissions for access control. MANAGE_ADMINISTRATORS allows managing administrator users.';







