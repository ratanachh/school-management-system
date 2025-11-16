-- Add version columns to entities for optimistic locking
-- These columns are required by the @Version annotation in entity models

-- Assessments table
ALTER TABLE assessments 
ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

-- Grades table
ALTER TABLE grades 
ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

-- Comments
COMMENT ON COLUMN assessments.version IS 'Version field for optimistic locking - prevents concurrent modification conflicts';
COMMENT ON COLUMN grades.version IS 'Version field for optimistic locking - prevents concurrent modification conflicts';

