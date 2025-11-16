-- Create grades table for student assessment scores

CREATE TABLE IF NOT EXISTS grades (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id UUID NOT NULL,
    assessment_id UUID NOT NULL,
    score DECIMAL(10,2) NOT NULL CHECK (score >= 0),
    total_points DECIMAL(10,2) NOT NULL CHECK (total_points > 0),
    percentage DECIMAL(5,2) NOT NULL CHECK (percentage >= 0 AND percentage <= 100),
    letter_grade VARCHAR(10),
    recorded_by UUID NOT NULL,
    recorded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by UUID,
    notes TEXT,
    UNIQUE(student_id, assessment_id),
    CHECK (score <= total_points)
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_grades_student ON grades(student_id);
CREATE INDEX IF NOT EXISTS idx_grades_assessment ON grades(assessment_id);
CREATE INDEX IF NOT EXISTS idx_grades_student_assessment ON grades(student_id, assessment_id);
CREATE INDEX IF NOT EXISTS idx_grades_recorded_by ON grades(recorded_by);

-- Comments
COMMENT ON TABLE grades IS 'Student scores on assessments';
COMMENT ON COLUMN grades.percentage IS 'Calculated percentage (score / total_points * 100)';
COMMENT ON COLUMN grades.letter_grade IS 'Letter grade (A, B, C, D, F, with +/- modifiers)';
COMMENT ON COLUMN grades.updated_by IS 'User who updated the grade (for audit trail)';

