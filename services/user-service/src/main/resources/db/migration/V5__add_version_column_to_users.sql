-- Add version column to users table for optimistic locking
-- This column is required by the @Version annotation in the User entity

ALTER TABLE users 
ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

-- Comment
COMMENT ON COLUMN users.version IS 'Version field for optimistic locking - prevents concurrent modification conflicts';

