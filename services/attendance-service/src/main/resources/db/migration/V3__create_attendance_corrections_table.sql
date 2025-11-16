-- Create attendance_corrections audit table for tracking attendance record changes

CREATE TABLE IF NOT EXISTS attendance_corrections (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    attendance_record_id UUID NOT NULL,
    previous_status VARCHAR(20) NOT NULL,
    new_status VARCHAR(20) NOT NULL,
    correction_reason TEXT,
    corrected_by UUID NOT NULL,
    corrected_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_corrections_record ON attendance_corrections(attendance_record_id);
CREATE INDEX IF NOT EXISTS idx_corrections_corrected_by ON attendance_corrections(corrected_by);
CREATE INDEX IF NOT EXISTS idx_corrections_date ON attendance_corrections(corrected_at);

-- Comments
COMMENT ON TABLE attendance_corrections IS 'Audit table tracking corrections made to attendance records';

