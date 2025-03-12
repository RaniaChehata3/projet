-- Drop the existing table if it exists
DROP TABLE IF EXISTS auth_sessions;

-- Create auth_sessions table with columns matching the Java code exactly
CREATE TABLE auth_sessions (
    session_id VARCHAR(64) PRIMARY KEY,
    user_id INT NOT NULL,
    username VARCHAR(50) NOT NULL,
    role VARCHAR(20) NOT NULL,
    expiry_time TIMESTAMP NOT NULL, -- Matches the column name used in your Java code
    ip_address VARCHAR(45),
    device_info VARCHAR(255)        -- Matches the column name used in your Java code
);

-- You can confirm the table was created with:
DESCRIBE auth_sessions; 