-- Migration to support multiple roles per user
-- Changes from single role column to user_roles join table

-- Step 1: Create user_roles join table
CREATE TABLE IF NOT EXISTS user_roles (
    user_id UUID NOT NULL,
    role VARCHAR(50) NOT NULL CHECK (role IN ('SUPER_ADMIN', 'ADMINISTRATOR', 'TEACHER', 'STUDENT', 'PARENT')),
    PRIMARY KEY (user_id, role),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Step 2: Migrate existing single role data to join table
INSERT INTO user_roles (user_id, role)
SELECT id, role FROM users WHERE role IS NOT NULL;

-- Step 3: Drop the old role column and its constraint
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_role_check;
ALTER TABLE users DROP COLUMN IF EXISTS role;

-- Step 4: Drop old role index (will be replaced with index on user_roles)
DROP INDEX IF EXISTS idx_users_role;

-- Step 5: Create indexes on user_roles table for performance
CREATE INDEX IF NOT EXISTS idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX IF NOT EXISTS idx_user_roles_role ON user_roles(role);

-- Comments
COMMENT ON TABLE user_roles IS 'Many-to-many relationship between users and roles. Users can have multiple roles.';
COMMENT ON COLUMN user_roles.user_id IS 'Reference to users table';
COMMENT ON COLUMN user_roles.role IS 'User role: SUPER_ADMIN, ADMINISTRATOR, TEACHER, STUDENT, or PARENT';

