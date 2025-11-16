-- Create notifications table for important events

CREATE TABLE IF NOT EXISTS notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    type VARCHAR(50) NOT NULL CHECK (type IN ('GRADE_POSTED', 'ATTENDANCE_MARKED', 'ACCOUNT_CREATED', 'ATTENDANCE_SESSION_APPROVED', 'ATTENDANCE_SESSION_REJECTED', 'ASSESSMENT_CREATED')),
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    metadata JSONB,
    read BOOLEAN NOT NULL DEFAULT false,
    read_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_notification_user ON notifications(user_id);
CREATE INDEX IF NOT EXISTS idx_notification_read ON notifications(user_id, read);
CREATE INDEX IF NOT EXISTS idx_notification_created ON notifications(created_at);
CREATE INDEX IF NOT EXISTS idx_notification_type ON notifications(type);

-- Comments
COMMENT ON TABLE notifications IS 'Notifications for important events (grades, attendance, account creation, etc.)';
COMMENT ON COLUMN notifications.type IS 'Type of notification: GRADE_POSTED, ATTENDANCE_MARKED, ACCOUNT_CREATED, etc.';
COMMENT ON COLUMN notifications.metadata IS 'Additional metadata in JSON format';
COMMENT ON COLUMN notifications.read IS 'Whether the notification has been read by the user';

