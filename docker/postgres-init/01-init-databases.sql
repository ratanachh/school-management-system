-- =============================================================================
-- School Management System - Database Initialization Script
-- =============================================================================
-- This script creates all databases required by the microservices
-- It runs automatically when the PostgreSQL container starts for the first time
-- =============================================================================

-- Create database for User Service
CREATE DATABASE user_service;

-- Create database for Academic Service
CREATE DATABASE academic_service;

-- Create database for Attendance Service
CREATE DATABASE attendance_service;

-- Create database for Academic Assessment Service
CREATE DATABASE academic_assessment_service;

-- Create database for Notification Service
CREATE DATABASE notification_service;

-- Create database for Audit Service
CREATE DATABASE audit_service;

-- Create database for Config Server
CREATE DATABASE config_server;

-- =============================================================================
-- Grant permissions to school_user for all databases
-- =============================================================================
GRANT ALL PRIVILEGES ON DATABASE user_service TO school_user;
GRANT ALL PRIVILEGES ON DATABASE academic_service TO school_user;
GRANT ALL PRIVILEGES ON DATABASE attendance_service TO school_user;
GRANT ALL PRIVILEGES ON DATABASE academic_assessment_service TO school_user;
GRANT ALL PRIVILEGES ON DATABASE notification_service TO school_user;
GRANT ALL PRIVILEGES ON DATABASE audit_service TO school_user;
GRANT ALL PRIVILEGES ON DATABASE config_server TO school_user;

-- =============================================================================
-- Connect to each database and grant schema privileges
-- =============================================================================
\c user_service
GRANT ALL ON SCHEMA public TO school_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO school_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO school_user;

\c academic_service
GRANT ALL ON SCHEMA public TO school_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO school_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO school_user;

\c attendance_service
GRANT ALL ON SCHEMA public TO school_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO school_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO school_user;

\c academic_assessment_service
GRANT ALL ON SCHEMA public TO school_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO school_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO school_user;

\c notification_service
GRANT ALL ON SCHEMA public TO school_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO school_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO school_user;

\c audit_service
GRANT ALL ON SCHEMA public TO school_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO school_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO school_user;

\c config_server
GRANT ALL ON SCHEMA public TO school_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO school_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO school_user;

-- =============================================================================
-- Return to default database
-- =============================================================================
\c school_management

-- =============================================================================
-- Verification query (commented out - uncomment to verify)
-- =============================================================================
-- SELECT datname FROM pg_database WHERE datname LIKE '%_service' OR datname = 'config_server';

