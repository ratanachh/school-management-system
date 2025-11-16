-- Create teacher_assignments table for linking teachers to classes
-- For grades 7-12, one teacher can be designated as class teacher/coordinator

CREATE TABLE IF NOT EXISTS teacher_assignments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    teacher_id UUID NOT NULL,
    class_id UUID NOT NULL,
    is_class_teacher BOOLEAN NOT NULL DEFAULT FALSE,
    assigned_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    assigned_by UUID,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(teacher_id, class_id)
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_teacher_assignments_teacher ON teacher_assignments(teacher_id);
CREATE INDEX IF NOT EXISTS idx_teacher_assignments_class ON teacher_assignments(class_id);
CREATE INDEX IF NOT EXISTS idx_teacher_assignments_class_teacher ON teacher_assignments(class_id, is_class_teacher);

-- Comments
COMMENT ON TABLE teacher_assignments IS 'Junction table linking teachers to classes. For grades 7-12, one teacher can be designated as class teacher (is_class_teacher = true)';
COMMENT ON COLUMN teacher_assignments.is_class_teacher IS 'True if this teacher is designated as class teacher/coordinator (grades 7-12 only)';

