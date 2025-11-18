-- Add PENDING account status to users table account_status constraint
-- PENDING status is used for newly registered users awaiting email verification

-- Drop the existing check constraint
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_account_status_check;

-- Add new check constraint with PENDING included
ALTER TABLE users ADD CONSTRAINT users_account_status_check 
    CHECK (account_status IN ('PENDING', 'ACTIVE', 'INACTIVE', 'SUSPENDED', 'DELETED'));

-- Update comment to reflect PENDING status
COMMENT ON COLUMN users.account_status IS 'Account status - PENDING (awaiting verification), ACTIVE, INACTIVE, SUSPENDED, or DELETED';

