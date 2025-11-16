-- Create student_class_leadership table for class leader assignments

CREATE TABLE IF NOT EXISTS student_class_leadership (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id UUID NOT NULL,
    class_id UUID NOT NULL,
    leadership_position VARCHAR(20) NOT NULL CHECK (leadership_position IN ('FIRST_LEADER', 'SECOND_LEADER', 'THIRD_LEADER')),
    assigned_by UUID NOT NULL,
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(class_id, leadership_position),
    UNIQUE(student_id, class_id)
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_leadership_student ON student_class_leadership(student_id);
CREATE INDEX IF NOT EXISTS idx_leadership_class ON student_class_leadership(class_id);

-- Comments
COMMENT ON TABLE student_class_leadership IS 'Class leader assignments. Each class can have one 1st leader, one 2nd leader, and one 3rd leader.';
COMMENT ON COLUMN student_class_leadership.leadership_position IS 'Leadership position: FIRST_LEADER, SECOND_LEADER, or THIRD_LEADER';

