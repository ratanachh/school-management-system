-- Create audit_records table for security event logging

CREATE TABLE IF NOT EXISTS audit_records (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    action VARCHAR(50) NOT NULL CHECK (action IN ('AUTHENTICATION', 'ACCESS_ATTEMPT', 'DATA_MODIFICATION', 'DATA_CREATION', 'DATA_DELETION', 'PASSWORD_RESET', 'EMAIL_VERIFICATION')),
    resource_type VARCHAR(100) NOT NULL,
    resource_id VARCHAR(255),
    ip_address VARCHAR(45),
    user_agent TEXT,
    details JSONB,
    success BOOLEAN NOT NULL DEFAULT true,
    error_message TEXT,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_audit_user ON audit_records(user_id);
CREATE INDEX IF NOT EXISTS idx_audit_action ON audit_records(action);
CREATE INDEX IF NOT EXISTS idx_audit_timestamp ON audit_records(timestamp);
CREATE INDEX IF NOT EXISTS idx_audit_resource ON audit_records(resource_type, resource_id);
CREATE INDEX IF NOT EXISTS idx_audit_user_timestamp ON audit_records(user_id, timestamp);

-- Comments
COMMENT ON TABLE audit_records IS 'Audit log for security-relevant events (authentication, access attempts, data modifications)';
COMMENT ON COLUMN audit_records.action IS 'Type of action: AUTHENTICATION, ACCESS_ATTEMPT, DATA_MODIFICATION, etc.';
COMMENT ON COLUMN audit_records.details IS 'Additional details in JSON format';
COMMENT ON COLUMN audit_records.success IS 'Whether the action was successful';
COMMENT ON COLUMN audit_records.error_message IS 'Error message if action failed';

