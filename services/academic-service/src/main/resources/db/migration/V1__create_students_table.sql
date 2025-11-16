-- Create students table for Academic Service
-- Supports K12 system (grades 1-12)

CREATE TABLE IF NOT EXISTS students (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id VARCHAR(50) NOT NULL UNIQUE,
    user_id UUID NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    date_of_birth DATE NOT NULL,
    grade_level INTEGER NOT NULL CHECK (grade_level >= 1 AND grade_level <= 12),
    enrollment_status VARCHAR(50) NOT NULL DEFAULT 'ENROLLED' CHECK (enrollment_status IN ('ENROLLED', 'ACTIVE', 'INACTIVE', 'GRADUATED', 'TRANSFERRED', 'WITHDRAWN')),
    
    -- Address fields (embedded)
    street VARCHAR(255),
    city VARCHAR(255),
    state VARCHAR(100),
    postal_code VARCHAR(20),
    country VARCHAR(100) DEFAULT 'Cambodia',
    
    -- Emergency contact fields (embedded)
    emergency_contact_name VARCHAR(255),
    emergency_contact_relationship VARCHAR(50),
    emergency_contact_phone VARCHAR(50),
    emergency_contact_email VARCHAR(255),
    emergency_contact_address TEXT,
    
    enrolled_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    graduated_at TIMESTAMP
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_students_student_id ON students(student_id);
CREATE INDEX IF NOT EXISTS idx_students_user_id ON students(user_id);
CREATE INDEX IF NOT EXISTS idx_students_grade_level ON students(grade_level);
CREATE INDEX IF NOT EXISTS idx_students_enrollment_status ON students(enrollment_status);

-- Comments
COMMENT ON TABLE students IS 'Student records for K12 school management system (grades 1-12)';
COMMENT ON COLUMN students.grade_level IS 'Grade level must be between 1 and 12 (K12 system)';
COMMENT ON COLUMN students.enrollment_status IS 'Current enrollment status of the student';

