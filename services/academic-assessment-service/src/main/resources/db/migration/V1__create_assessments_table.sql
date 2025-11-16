-- Create assessments table for evaluation activities

CREATE TABLE IF NOT EXISTS assessments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    class_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(20) NOT NULL CHECK (type IN ('TEST', 'QUIZ', 'ASSIGNMENT', 'PROJECT', 'EXAM', 'FINAL_EXAM')),
    description TEXT,
    total_points DECIMAL(10,2) NOT NULL CHECK (total_points > 0),
    weight DECIMAL(5,2) CHECK (weight >= 0 AND weight <= 100),
    due_date DATE,
    created_by UUID NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'PUBLISHED', 'GRADING', 'COMPLETED')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_assessments_class ON assessments(class_id);
CREATE INDEX IF NOT EXISTS idx_assessments_created_by ON assessments(created_by);
CREATE INDEX IF NOT EXISTS idx_assessments_status ON assessments(status);

-- Comments
COMMENT ON TABLE assessments IS 'Evaluation activities (tests, quizzes, assignments, projects)';
COMMENT ON COLUMN assessments.type IS 'Assessment type: TEST, QUIZ, ASSIGNMENT, PROJECT, EXAM, or FINAL_EXAM';
COMMENT ON COLUMN assessments.status IS 'Assessment status: DRAFT, PUBLISHED, GRADING, or COMPLETED';
COMMENT ON COLUMN assessments.weight IS 'Weight in final grade calculation (percentage, 0-100)';

