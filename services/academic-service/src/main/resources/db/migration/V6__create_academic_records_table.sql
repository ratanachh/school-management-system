-- Create academic_records table for student academic history

CREATE TABLE IF NOT EXISTS academic_records (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id UUID NOT NULL UNIQUE,
    current_gpa DECIMAL(3,2) NOT NULL DEFAULT 0.00 CHECK (current_gpa >= 0.00 AND current_gpa <= 4.00),
    cumulative_gpa DECIMAL(3,2) NOT NULL DEFAULT 0.00 CHECK (cumulative_gpa >= 0.00 AND cumulative_gpa <= 4.00),
    credits_earned INTEGER NOT NULL DEFAULT 0 CHECK (credits_earned >= 0),
    credits_required INTEGER NOT NULL DEFAULT 120 CHECK (credits_required > 0),
    academic_standing VARCHAR(20) NOT NULL DEFAULT 'GOOD_STANDING' CHECK (academic_standing IN ('GOOD_STANDING', 'PROBATION', 'SUSPENDED', 'GRADUATED')),
    graduation_date DATE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_academic_record_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE
);

-- Create enrollment_history table (element collection)
CREATE TABLE IF NOT EXISTS enrollment_history (
    academic_record_id UUID NOT NULL,
    academic_year VARCHAR(20) NOT NULL,
    term VARCHAR(20) NOT NULL CHECK (term IN ('FIRST_TERM', 'SECOND_TERM', 'THIRD_TERM', 'FULL_YEAR')),
    grade_level INTEGER NOT NULL CHECK (grade_level >= 1 AND grade_level <= 12),
    enrollment_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('ENROLLED', 'GRADUATED', 'TRANSFERRED', 'WITHDRAWN')),
    CONSTRAINT fk_enrollment_history_academic_record FOREIGN KEY (academic_record_id) REFERENCES academic_records(id) ON DELETE CASCADE
);

-- Create course_completions table (element collection)
CREATE TABLE IF NOT EXISTS course_completions (
    academic_record_id UUID NOT NULL,
    course_name VARCHAR(255) NOT NULL,
    subject VARCHAR(100) NOT NULL,
    grade_level INTEGER NOT NULL CHECK (grade_level >= 1 AND grade_level <= 12),
    final_grade VARCHAR(10) NOT NULL,
    credits INTEGER NOT NULL CHECK (credits > 0),
    completion_date DATE NOT NULL,
    CONSTRAINT fk_course_completions_academic_record FOREIGN KEY (academic_record_id) REFERENCES academic_records(id) ON DELETE CASCADE
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_academic_records_student ON academic_records(student_id);
CREATE INDEX IF NOT EXISTS idx_academic_records_standing ON academic_records(academic_standing);
CREATE INDEX IF NOT EXISTS idx_enrollment_history_academic_record ON enrollment_history(academic_record_id);
CREATE INDEX IF NOT EXISTS idx_course_completions_academic_record ON course_completions(academic_record_id);

-- Comments
COMMENT ON TABLE academic_records IS 'Student academic records including GPA, credits, and academic standing';
COMMENT ON COLUMN academic_records.current_gpa IS 'Current term GPA (0.0 to 4.0)';
COMMENT ON COLUMN academic_records.cumulative_gpa IS 'Cumulative GPA across all terms (0.0 to 4.0)';
COMMENT ON COLUMN academic_records.academic_standing IS 'Academic standing: GOOD_STANDING, PROBATION, SUSPENDED, or GRADUATED';

