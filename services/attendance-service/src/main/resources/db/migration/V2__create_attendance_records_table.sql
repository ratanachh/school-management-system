-- Create attendance_records table
-- Supports both direct teacher marking and session-based class leader collection

CREATE TABLE IF NOT EXISTS attendance_records (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id UUID NOT NULL,
    class_id UUID NOT NULL,
    date DATE NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('PRESENT', 'ABSENT', 'LATE', 'EXCUSED')),
    marked_by UUID,
    collected_by UUID,
    session_id UUID,
    approved_by UUID,
    notes TEXT,
    marked_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by UUID,
    UNIQUE(student_id, class_id, date)
);

-- Check constraint for marking method validation
ALTER TABLE attendance_records
    ADD CONSTRAINT check_marking_method CHECK (
        (marked_by IS NOT NULL AND collected_by IS NULL AND session_id IS NULL) OR
        (marked_by IS NULL AND collected_by IS NOT NULL AND session_id IS NOT NULL)
    );

-- Indexes
CREATE INDEX IF NOT EXISTS idx_attendance_student_class_date ON attendance_records(student_id, class_id, date);
CREATE INDEX IF NOT EXISTS idx_attendance_class ON attendance_records(class_id);
CREATE INDEX IF NOT EXISTS idx_attendance_date ON attendance_records(date);
CREATE INDEX IF NOT EXISTS idx_attendance_session ON attendance_records(session_id);
CREATE INDEX IF NOT EXISTS idx_attendance_collected_by ON attendance_records(collected_by);

-- Comments
COMMENT ON TABLE attendance_records IS 'Attendance records supporting both direct teacher marking and session-based class leader collection';
COMMENT ON COLUMN attendance_records.marked_by IS 'Teacher ID (for direct marking)';
COMMENT ON COLUMN attendance_records.collected_by IS 'Student ID - class leader who collected attendance (for session-based)';
COMMENT ON COLUMN attendance_records.session_id IS 'AttendanceSession ID (for session-based collection)';
COMMENT ON COLUMN attendance_records.approved_by IS 'Teacher ID who approved the session (for session-based collection)';

