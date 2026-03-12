-- Create report_submissions table for class teachers (grades 7-12) to submit reports to school

CREATE TABLE IF NOT EXISTS report_submissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    collection_id UUID NOT NULL,
    submitted_by UUID NOT NULL,
    class_id UUID NOT NULL,
    report_data JSONB NOT NULL,
    submitted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewed_by UUID,
    reviewed_at TIMESTAMP,
    review_notes TEXT
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_submission_collection ON report_submissions(collection_id);
CREATE INDEX IF NOT EXISTS idx_submission_class ON report_submissions(class_id);
CREATE INDEX IF NOT EXISTS idx_submission_teacher ON report_submissions(submitted_by);
CREATE INDEX IF NOT EXISTS idx_submission_submitted ON report_submissions(submitted_at);

-- Foreign key constraint (optional, if you want referential integrity)
-- ALTER TABLE report_submissions ADD CONSTRAINT fk_submission_collection 
--   FOREIGN KEY (collection_id) REFERENCES exam_result_collections(id);

-- Comments
COMMENT ON TABLE report_submissions IS 'Report submissions by class teachers (grades 7-12) to school administration';
COMMENT ON COLUMN report_submissions.submitted_by IS 'Class teacher/coordinator who submitted the report';
COMMENT ON COLUMN report_submissions.report_data IS 'Aggregated report data in JSON format';
COMMENT ON COLUMN report_submissions.reviewed_by IS 'Administrator who reviewed the report';

