-- Create exam_result_collections table for class teachers (grades 7-12) to collect exam results

CREATE TABLE IF NOT EXISTS exam_result_collections (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    class_id UUID NOT NULL,
    collected_by UUID NOT NULL,
    academic_year VARCHAR(20) NOT NULL,
    term VARCHAR(20) NOT NULL CHECK (term IN ('FIRST_TERM', 'SECOND_TERM', 'THIRD_TERM', 'FULL_YEAR')),
    status VARCHAR(20) NOT NULL DEFAULT 'COLLECTING' CHECK (status IN ('COLLECTING', 'COMPLETED', 'SUBMITTED')),
    summary TEXT,
    metadata JSONB,
    collected_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    submitted_at TIMESTAMP
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_collection_class ON exam_result_collections(class_id);
CREATE INDEX IF NOT EXISTS idx_collection_teacher ON exam_result_collections(collected_by);
CREATE INDEX IF NOT EXISTS idx_collection_year_term ON exam_result_collections(academic_year, term);
CREATE INDEX IF NOT EXISTS idx_collection_status ON exam_result_collections(status);

-- Comments
COMMENT ON TABLE exam_result_collections IS 'Exam result collections by class teachers (grades 7-12) from subject teachers';
COMMENT ON COLUMN exam_result_collections.collected_by IS 'Class teacher/coordinator who collected the exam results';
COMMENT ON COLUMN exam_result_collections.status IS 'Collection status: COLLECTING, COMPLETED, SUBMITTED';
COMMENT ON COLUMN exam_result_collections.metadata IS 'Additional metadata in JSON format';

