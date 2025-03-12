-- SahaCare Database Setup Script

-- Create database if it doesn't exist
CREATE DATABASE IF NOT EXISTS sahacare;

-- Use the sahacare database
USE sahacare;

-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    role VARCHAR(20) NOT NULL, -- 'Doctor', 'Patient', 'Laboratory', 'Admin'
    specialization VARCHAR(100), -- For doctors
    address VARCHAR(255),
    laboratory VARCHAR(100), -- For laboratory accounts
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create any other necessary tables for the application
-- For example, appointments, medical records, prescriptions, etc.

-- Example:
CREATE TABLE IF NOT EXISTS appointments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id INT NOT NULL,
    doctor_id INT NOT NULL,
    appointment_date DATETIME NOT NULL,
    status VARCHAR(20) DEFAULT 'Scheduled', -- 'Scheduled', 'Completed', 'Cancelled'
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES users(id),
    FOREIGN KEY (doctor_id) REFERENCES users(id)
);

-- You may add more tables as needed based on the application requirements 