-- Add SUPER_ADMIN role to users table role constraint
-- SUPER_ADMIN can manage all users including ADMINISTRATOR users

-- Drop the existing check constraint
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_role_check;

-- Add new check constraint with SUPER_ADMIN included
ALTER TABLE users ADD CONSTRAINT users_role_check 
    CHECK (role IN ('SUPER_ADMIN', 'ADMINISTRATOR', 'TEACHER', 'STUDENT', 'PARENT'));

-- Update comment to reflect role hierarchy
COMMENT ON COLUMN users.role IS 'User role: SUPER_ADMIN (can manage administrators), ADMINISTRATOR, TEACHER, STUDENT, or PARENT';







