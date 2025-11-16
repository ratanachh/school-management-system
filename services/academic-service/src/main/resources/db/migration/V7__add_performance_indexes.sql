-- Performance optimization: Add missing indexes for common query patterns

-- Composite index for student search queries
CREATE INDEX IF NOT EXISTS idx_students_name_search ON students USING gin(to_tsvector('english', first_name || ' ' || last_name));

-- Index for teacher assignment queries
CREATE INDEX IF NOT EXISTS idx_teacher_assignments_class_teacher ON teacher_assignments(class_id, is_class_teacher) 
WHERE is_class_teacher = true;

-- Composite index for class queries by grade and year
CREATE INDEX IF NOT EXISTS idx_classes_grade_year ON classes(grade_level, academic_year, term);

-- Index for academic record queries
CREATE INDEX IF NOT EXISTS idx_academic_records_student_year ON academic_records(student_id, academic_year);

-- Comments
COMMENT ON INDEX idx_students_name_search IS 'Full-text search index for student names';
COMMENT ON INDEX idx_teacher_assignments_class_teacher IS 'Optimizes class teacher queries';
COMMENT ON INDEX idx_classes_grade_year IS 'Optimizes class queries by grade and academic year';
COMMENT ON INDEX idx_academic_records_student_year IS 'Optimizes academic record queries';

