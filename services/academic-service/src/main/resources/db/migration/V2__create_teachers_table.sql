-- Create teachers table for Academic Service

CREATE TABLE IF NOT EXISTS teachers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    employee_id VARCHAR(50) NOT NULL UNIQUE,
    user_id UUID NOT NULL,
    hire_date DATE NOT NULL,
    employment_status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE' CHECK (employment_status IN ('ACTIVE', 'ON_LEAVE', 'TERMINATED', 'RETIRED')),
    department VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Teacher qualifications table (many-to-many)
CREATE TABLE IF NOT EXISTS teacher_qualifications (
    teacher_id UUID NOT NULL REFERENCES teachers(id) ON DELETE CASCADE,
    qualification VARCHAR(255) NOT NULL,
    PRIMARY KEY (teacher_id, qualification)
);

-- Teacher subject specializations table (many-to-many)
CREATE TABLE IF NOT EXISTS teacher_subject_specializations (
    teacher_id UUID NOT NULL REFERENCES teachers(id) ON DELETE CASCADE,
    subject VARCHAR(255) NOT NULL,
    PRIMARY KEY (teacher_id, subject)
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_teachers_employee_id ON teachers(employee_id);
CREATE INDEX IF NOT EXISTS idx_teachers_user_id ON teachers(user_id);
CREATE INDEX IF NOT EXISTS idx_teachers_employment_status ON teachers(employment_status);

-- Comments
COMMENT ON TABLE teachers IS 'Teacher records with qualifications and subject specializations';
COMMENT ON COLUMN teachers.employee_id IS 'Unique employee identifier';
COMMENT ON COLUMN teachers.employment_status IS 'Current employment status';

