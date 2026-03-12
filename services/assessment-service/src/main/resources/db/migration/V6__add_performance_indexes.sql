-- Performance optimization: Add missing indexes for common query patterns

-- Index for grade queries by class and academic period
CREATE INDEX IF NOT EXISTS idx_grades_assessment_class ON grades(assessment_id) 
WHERE assessment_id IS NOT NULL;

-- Index for gradebook queries by student and term
CREATE INDEX IF NOT EXISTS idx_grades_student_assessment ON grades(student_id, assessment_id);

-- Composite index for grade average calculations
CREATE INDEX IF NOT EXISTS idx_grades_student_recorded ON grades(student_id, recorded_at);

-- Index for assessment queries by class and status
CREATE INDEX IF NOT EXISTS idx_assessments_class_status ON assessments(class_id, status);

-- Comments
COMMENT ON INDEX idx_grades_assessment_class IS 'Optimizes grade queries by assessment';
COMMENT ON INDEX idx_grades_student_assessment IS 'Optimizes gradebook queries';
COMMENT ON INDEX idx_grades_student_recorded IS 'Optimizes grade history queries';
COMMENT ON INDEX idx_assessments_class_status IS 'Optimizes assessment queries by class and status';

