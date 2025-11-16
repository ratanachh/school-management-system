-- Performance optimization: Add missing indexes for common query patterns

-- Composite index for attendance queries by class and date range
CREATE INDEX IF NOT EXISTS idx_attendance_class_date_range ON attendance_records(class_id, date);

-- Index for attendance session queries
CREATE INDEX IF NOT EXISTS idx_attendance_sessions_class_status ON attendance_sessions(class_id, status);

-- Composite index for attendance report queries
CREATE INDEX IF NOT EXISTS idx_attendance_student_date ON attendance_records(student_id, date);

-- Comments
COMMENT ON INDEX idx_attendance_class_date_range IS 'Optimizes attendance queries by class and date';
COMMENT ON INDEX idx_attendance_sessions_class_status IS 'Optimizes attendance session queries';
COMMENT ON INDEX idx_attendance_student_date IS 'Optimizes student attendance history queries';

