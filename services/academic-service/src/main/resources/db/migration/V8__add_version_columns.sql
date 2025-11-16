-- Add version columns to entities for optimistic locking
-- These columns are required by the @Version annotation in entity models

-- Students table
ALTER TABLE students 
ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

-- Classes table
ALTER TABLE classes 
ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

-- Teachers table
ALTER TABLE teachers 
ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

-- Comments
COMMENT ON COLUMN students.version IS 'Version field for optimistic locking - prevents concurrent modification conflicts';
COMMENT ON COLUMN classes.version IS 'Version field for optimistic locking - prevents concurrent modification conflicts';
COMMENT ON COLUMN teachers.version IS 'Version field for optimistic locking - prevents concurrent modification conflicts';

