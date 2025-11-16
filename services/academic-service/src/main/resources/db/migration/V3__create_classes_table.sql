-- Create classes table for Academic Service
-- Supports both homeroom classes (grades 1-6) and subject classes (all grades)

CREATE TABLE IF NOT EXISTS classes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    class_name VARCHAR(255) NOT NULL,
    class_type VARCHAR(50) NOT NULL CHECK (class_type IN ('HOMEROOM', 'SUBJECT')),
    subject VARCHAR(255),
    grade_level INTEGER NOT NULL CHECK (grade_level >= 1 AND grade_level <= 12),
    homeroom_teacher_id UUID,
    class_teacher_id UUID,
    academic_year VARCHAR(20) NOT NULL,
    term VARCHAR(50) NOT NULL CHECK (term IN ('FIRST_TERM', 'SECOND_TERM', 'THIRD_TERM', 'FULL_YEAR')),
    
    -- Schedule fields (embedded)
    schedule_days_of_week VARCHAR(255),
    schedule_start_time TIME,
    schedule_end_time TIME,
    schedule_room VARCHAR(100),
    
    max_capacity INTEGER,
    current_enrollment INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(50) NOT NULL DEFAULT 'SCHEDULED' CHECK (status IN ('SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED')),
    start_date DATE NOT NULL,
    end_date DATE,
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_classes_name ON classes(class_name);
CREATE INDEX IF NOT EXISTS idx_classes_grade_level ON classes(grade_level);
CREATE INDEX IF NOT EXISTS idx_classes_class_type ON classes(class_type);
CREATE INDEX IF NOT EXISTS idx_classes_homeroom_teacher ON classes(homeroom_teacher_id);
CREATE INDEX IF NOT EXISTS idx_classes_class_teacher ON classes(class_teacher_id);
CREATE INDEX IF NOT EXISTS idx_classes_academic_year ON classes(academic_year);

-- Comments
COMMENT ON TABLE classes IS 'Class records supporting homeroom (grades 1-6) and subject classes (all grades)';
COMMENT ON COLUMN classes.class_type IS 'HOMEROOM for grades 1-6, SUBJECT for all grades';
COMMENT ON COLUMN classes.homeroom_teacher_id IS 'Homeroom teacher for grades 1-6 (only for HOMEROOM type)';
COMMENT ON COLUMN classes.class_teacher_id IS 'Class teacher/coordinator for grades 7-12 (only for SUBJECT type)';
COMMENT ON COLUMN classes.grade_level IS 'Grade level must be between 1 and 12 (K12 system)';

