-- Create attendance_sessions table for class leader delegation workflow

CREATE TABLE IF NOT EXISTS attendance_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    class_id UUID NOT NULL,
    date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'COLLECTED', 'APPROVED', 'REJECTED')),
    delegated_to UUID,
    created_by UUID NOT NULL,
    approved_by UUID,
    rejected_by UUID,
    rejection_reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    collected_at TIMESTAMP,
    approved_at TIMESTAMP,
    rejected_at TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(class_id, date)
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_sessions_class_date ON attendance_sessions(class_id, date);
CREATE INDEX IF NOT EXISTS idx_sessions_delegated_to ON attendance_sessions(delegated_to);
CREATE INDEX IF NOT EXISTS idx_sessions_status ON attendance_sessions(status);
CREATE INDEX IF NOT EXISTS idx_sessions_created_by ON attendance_sessions(created_by);

-- Comments
COMMENT ON TABLE attendance_sessions IS 'Attendance collection sessions for class leader delegation. One session per class per date.';
COMMENT ON COLUMN attendance_sessions.delegated_to IS 'Student ID (class leader) assigned to collect attendance';
COMMENT ON COLUMN attendance_sessions.status IS 'Session status: PENDING, COLLECTED, APPROVED, or REJECTED';

