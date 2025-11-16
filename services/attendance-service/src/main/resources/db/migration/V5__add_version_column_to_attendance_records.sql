-- Add version column to attendance_records table for optimistic locking
-- This column is required by the @Version annotation in the AttendanceRecord entity

ALTER TABLE attendance_records 
ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

-- Comment
COMMENT ON COLUMN attendance_records.version IS 'Version field for optimistic locking - prevents concurrent modification conflicts';

