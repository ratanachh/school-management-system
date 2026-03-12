-- Create grade_corrections table for grade audit trail

CREATE TABLE IF NOT EXISTS grade_corrections (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    grade_id UUID NOT NULL,
    previous_score DECIMAL(10,2) NOT NULL,
    new_score DECIMAL(10,2) NOT NULL,
    previous_percentage DECIMAL(5,2) NOT NULL,
    new_percentage DECIMAL(5,2) NOT NULL,
    corrected_by UUID NOT NULL,
    correction_reason TEXT,
    corrected_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_grade_corrections_grade ON grade_corrections(grade_id);
CREATE INDEX IF NOT EXISTS idx_grade_corrections_corrected_by ON grade_corrections(corrected_by);
CREATE INDEX IF NOT EXISTS idx_grade_corrections_corrected_at ON grade_corrections(corrected_at);

-- Comments
COMMENT ON TABLE grade_corrections IS 'Audit trail for grade corrections and updates';
COMMENT ON COLUMN grade_corrections.correction_reason IS 'Reason for the grade correction';

