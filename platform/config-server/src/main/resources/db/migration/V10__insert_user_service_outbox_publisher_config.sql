-- Insert outbox publisher configuration for user-service
-- APPLICATION: user-service, PROFILE: default, LABEL: master

INSERT INTO CONFIG_PROPERTIES (APPLICATION, PROFILE, LABEL, KEY, VALUE) VALUES
('user-service', 'default', 'master', 'outbox.publisher.max-attempts', '${OUTBOX_PUBLISHER_MAX_ATTEMPTS}'),
('user-service', 'default', 'master', 'outbox.publisher.initial-backoff-ms', '${OUTBOX_PUBLISHER_INITIAL_BACKOFF_MS}'),
('user-service', 'default', 'master', 'outbox.publisher.max-backoff-ms', '${OUTBOX_PUBLISHER_MAX_BACKOFF_MS}'),
('user-service', 'default', 'master', 'outbox.publisher.poll-interval-ms', '${OUTBOX_PUBLISHER_POLL_INTERVAL_MS}'),
('user-service', 'default', 'master', 'password.reset.path', '${PASSWORD_RESET_PATH}'),
('user-service', 'default', 'master', 'password.reset.token.expiry.hours', '${PASSWORD_RESET_TOKEN_EXPIRY_HOURS}'),
('user-service', 'default', 'master', 'password.reset.cleanup.cron', '${PASSWORD_RESET_CLEANUP_CRON}')
ON CONFLICT (APPLICATION, PROFILE, LABEL, KEY) DO NOTHING;
