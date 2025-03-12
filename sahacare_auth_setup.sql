-- SahaCare Authentication Database Setup
-- This script creates the necessary tables for authentication and adds some test data

-- Create the database if it doesn't exist
CREATE DATABASE IF NOT EXISTS sahacare;

-- Use the sahacare database
USE sahacare;

-- Create users table with authentication details
CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL, -- Will store hashed passwords
    role ENUM('ADMIN', 'DOCTOR', 'PATIENT', 'LABORATORY') NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    address TEXT,
    registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL,
    active BOOLEAN DEFAULT TRUE
);

-- Create a table for user profile information
CREATE TABLE IF NOT EXISTS user_profiles (
    profile_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    gender ENUM('MALE', 'FEMALE', 'OTHER') NULL,
    date_of_birth DATE NULL,
    blood_type VARCHAR(5) NULL,
    medical_history TEXT NULL,
    specialization VARCHAR(100) NULL, -- For doctors
    license_number VARCHAR(50) NULL, -- For doctors and laboratories
    profile_image VARCHAR(255) NULL, -- Path to profile image
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Create a table for authentication sessions
-- Using DATETIME for expiry_time to avoid the default value issue
CREATE TABLE IF NOT EXISTS auth_sessions (
    session_id VARCHAR(128) PRIMARY KEY,
    user_id INT NOT NULL,
    login_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expiry_time DATETIME NOT NULL,
    ip_address VARCHAR(45) NULL,
    device_info VARCHAR(255) NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Create a table for password reset requests
CREATE TABLE IF NOT EXISTS password_resets (
    reset_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    reset_token VARCHAR(64) NOT NULL,
    request_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expiry_time DATETIME NOT NULL,
    completed BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Create a table for login attempts (for security)
CREATE TABLE IF NOT EXISTS login_attempts (
    attempt_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    ip_address VARCHAR(45) NULL,
    attempt_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    success BOOLEAN DEFAULT FALSE
);

-- Insert test users (passwords are "password123" - in a real application these would be hashed)
-- In our Java application, we'll implement proper password hashing

-- Insert admin user
INSERT INTO users (username, email, password, role, full_name, phone, address, active)
VALUES ('admin', 'admin@sahacare.com', 'password123', 'ADMIN', 'System Administrator', '+212600000000', 'SahaCare Main Office', TRUE);

-- Insert doctors
INSERT INTO users (username, email, password, role, full_name, phone, address, active)
VALUES 
('dr.smith', 'drsmith@sahacare.com', 'password123', 'DOCTOR', 'Dr. John Smith', '+212600000001', 'Medical Center, Casablanca', TRUE),
('dr.garcia', 'drgarcia@sahacare.com', 'password123', 'DOCTOR', 'Dr. Maria Garcia', '+212600000002', 'Health Clinic, Rabat', TRUE);

-- Insert patients
INSERT INTO users (username, email, password, role, full_name, phone, address, active)
VALUES 
('patient1', 'patient1@example.com', 'password123', 'PATIENT', 'Ahmed Bensouda', '+212600000003', 'Rue Hassan II, Rabat', TRUE),
('patient2', 'patient2@example.com', 'password123', 'PATIENT', 'Fatima Zahra', '+212600000004', 'Avenue Mohammed V, Casablanca', TRUE),
('patient3', 'patient3@example.com', 'password123', 'PATIENT', 'Youssef El Mansouri', '+212600000005', 'Quartier Bourgogne, Casablanca', TRUE);

-- Insert laboratory
INSERT INTO users (username, email, password, role, full_name, phone, address, active)
VALUES 
('lab_central', 'central@lab.com', 'password123', 'LABORATORY', 'Central Medical Laboratory', '+212600000006', 'Medical Complex, Agdal, Rabat', TRUE);

-- Add profiles for the users
-- Doctor profiles
INSERT INTO user_profiles (user_id, gender, specialization, license_number)
VALUES 
(2, 'MALE', 'Cardiology', 'MED-12345'),
(3, 'FEMALE', 'Pediatrics', 'MED-67890');

-- Patient profiles
INSERT INTO user_profiles (user_id, gender, date_of_birth, blood_type, medical_history)
VALUES 
(4, 'MALE', '1985-03-12', 'O+', 'No significant medical history'),
(5, 'FEMALE', '1990-07-25', 'A+', 'Mild asthma'),
(6, 'MALE', '1978-11-05', 'B-', 'Hypertension, controlled with medication');

-- Laboratory profile
INSERT INTO user_profiles (user_id, license_number)
VALUES 
(7, 'LAB-54321');

-- For testing the authentication, you can use these users:
-- Username: admin, password: password123
-- Username: dr.smith, password: password123
-- Username: patient1, password: password123
-- Username: lab_central, password: password123 