-- Users table
CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    salt VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL,
    full_name VARCHAR(100),
    phone VARCHAR(20),
    address VARCHAR(255),
    city VARCHAR(100),
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP
);

-- Sessions table
CREATE TABLE IF NOT EXISTS sessions (
    session_id VARCHAR(100) PRIMARY KEY,
    user_id INT NOT NULL,
    ip_address VARCHAR(50),
    device_info VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Login attempts table
CREATE TABLE IF NOT EXISTS login_attempts (
    attempt_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    ip_address VARCHAR(50),
    success BOOLEAN DEFAULT FALSE,
    attempt_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Patients table
CREATE TABLE IF NOT EXISTS patients (
    patient_id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    date_of_birth DATE,
    gender VARCHAR(20),
    phone VARCHAR(20),
    email VARCHAR(100),
    address VARCHAR(255),
    city VARCHAR(100),
    blood_type VARCHAR(10),
    insurance_provider VARCHAR(100),
    insurance_number VARCHAR(50),
    emergency_contact VARCHAR(100),
    emergency_phone VARCHAR(20),
    registration_date DATE DEFAULT CURRENT_DATE,
    last_visit_date DATE,
    status VARCHAR(20) DEFAULT 'Active'
);

-- Medical records table
CREATE TABLE IF NOT EXISTS medical_records (
    record_id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id INT NOT NULL,
    doctor_id INT NOT NULL,
    doctor_name VARCHAR(100),
    visit_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    diagnosis VARCHAR(255),
    symptoms TEXT,
    treatment TEXT,
    notes TEXT,
    follow_up_date DATE,
    record_type VARCHAR(50) DEFAULT 'Regular visit',
    FOREIGN KEY (patient_id) REFERENCES patients(patient_id) ON DELETE CASCADE,
    FOREIGN KEY (doctor_id) REFERENCES users(user_id)
);

-- Medications table
CREATE TABLE IF NOT EXISTS medications (
    medication_id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id INT NOT NULL,
    doctor_id INT NOT NULL,
    name VARCHAR(100) NOT NULL,
    dosage VARCHAR(50),
    frequency VARCHAR(50),
    instructions TEXT,
    purpose VARCHAR(255),
    start_date DATE DEFAULT CURRENT_DATE,
    end_date DATE,
    is_current BOOLEAN DEFAULT TRUE,
    side_effects TEXT,
    notes TEXT,
    FOREIGN KEY (patient_id) REFERENCES patients(patient_id) ON DELETE CASCADE,
    FOREIGN KEY (doctor_id) REFERENCES users(user_id)
);

-- Lab results table
CREATE TABLE IF NOT EXISTS lab_results (
    result_id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id INT NOT NULL,
    doctor_id INT,
    lab_tech_id INT,
    test_name VARCHAR(100) NOT NULL,
    test_type VARCHAR(50),
    result TEXT,
    normal_range VARCHAR(100),
    unit VARCHAR(20),
    is_abnormal BOOLEAN DEFAULT FALSE,
    test_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    result_date TIMESTAMP,
    notes TEXT,
    status VARCHAR(20) DEFAULT 'Pending',
    is_urgent BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (patient_id) REFERENCES patients(patient_id) ON DELETE CASCADE,
    FOREIGN KEY (doctor_id) REFERENCES users(user_id),
    FOREIGN KEY (lab_tech_id) REFERENCES users(user_id)
);

-- Notes table
CREATE TABLE IF NOT EXISTS notes (
    note_id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id INT NOT NULL,
    author_id INT NOT NULL,
    author_name VARCHAR(100),
    author_role VARCHAR(50),
    title VARCHAR(100),
    content TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    note_type VARCHAR(50) DEFAULT 'General',
    is_private BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (patient_id) REFERENCES patients(patient_id) ON DELETE CASCADE,
    FOREIGN KEY (author_id) REFERENCES users(user_id)
);

-- Insert demo users
INSERT INTO users (username, email, password, salt, role, full_name, active)
VALUES 
('admin', 'admin@example.com', '5f4dcc3b5aa765d61d8327deb882cf99', 'salt123', 'ADMIN', 'Admin User', TRUE),
('doctor', 'doctor@example.com', '5f4dcc3b5aa765d61d8327deb882cf99', 'salt123', 'DOCTOR', 'Dr. John Smith', TRUE),
('patient', 'patient@example.com', '5f4dcc3b5aa765d61d8327deb882cf99', 'salt123', 'PATIENT', 'Jane Doe', TRUE),
('lab', 'lab@example.com', '5f4dcc3b5aa765d61d8327deb882cf99', 'salt123', 'LABORATORY', 'Lab Technician', TRUE);

-- Insert demo patients
INSERT INTO patients (full_name, date_of_birth, gender, phone, email, address, city, blood_type, 
                     insurance_provider, insurance_number, emergency_contact, emergency_phone, 
                     registration_date, status)
VALUES 
('John Doe', '1985-05-15', 'Male', '555-123-4567', 'john.doe@example.com', '123 Main St', 'New York', 'O+',
 'Health Insurance Co', 'HI12345678', 'Jane Doe', '555-987-6543', '2023-01-01', 'Active'),
 
('Alice Smith', '1990-10-20', 'Female', '555-234-5678', 'alice.smith@example.com', '456 Oak Ave', 'Boston', 'A-',
 'Medical Insurance Inc', 'MI87654321', 'Bob Smith', '555-876-5432', '2023-02-15', 'Active'),
 
('Robert Johnson', '1978-03-25', 'Male', '555-345-6789', 'robert.johnson@example.com', '789 Pine Blvd', 'Chicago', 'B+',
 'Healthcare Group', 'HG23456789', 'Mary Johnson', '555-765-4321', '2023-03-10', 'Active'),
 
('Emily Davis', '1995-07-12', 'Female', '555-456-7890', 'emily.davis@example.com', '321 Elm St', 'San Francisco', 'AB+',
 'Insurance Partners', 'IP34567890', 'Michael Davis', '555-654-3210', '2023-04-05', 'Active'),
 
('Michael Wilson', '1982-12-08', 'Male', '555-567-8901', 'michael.wilson@example.com', '654 Maple Dr', 'Seattle', 'O-',
 'Care Insurance', 'CI45678901', 'Sarah Wilson', '555-543-2109', '2023-05-20', 'Active');

-- Insert demo medical records
INSERT INTO medical_records (patient_id, doctor_id, doctor_name, visit_date, diagnosis, symptoms, treatment, notes, follow_up_date, record_type)
VALUES 
(1, 2, 'Dr. John Smith', '2023-05-15 10:30:00', 'Common Cold', 'Runny nose, sore throat, cough', 
 'Rest, fluids, over-the-counter cold medicine', 'Patient advised to rest for 3 days', '2023-05-22', 'Regular visit'),
 
(2, 2, 'Dr. John Smith', '2023-06-10 14:15:00', 'Hypertension', 'Headache, dizziness, high blood pressure (150/95)', 
 'Prescribed lisinopril 10mg daily', 'Patient to monitor blood pressure daily', '2023-07-10', 'Regular visit'),
 
(3, 2, 'Dr. John Smith', '2023-07-05 09:45:00', 'Type 2 Diabetes', 'Increased thirst, frequent urination, fatigue', 
 'Prescribed metformin 500mg twice daily, dietary changes', 'Patient referred to nutritionist', '2023-08-05', 'Regular visit');

-- Insert demo medications
INSERT INTO medications (patient_id, doctor_id, name, dosage, frequency, instructions, purpose, start_date, is_current)
VALUES 
(2, 2, 'Lisinopril', '10mg', 'Once daily', 'Take in the morning with food', 'Blood pressure control', '2023-06-10', TRUE),
(3, 2, 'Metformin', '500mg', 'Twice daily', 'Take with breakfast and dinner', 'Blood sugar control', '2023-07-05', TRUE),
(1, 2, 'Ibuprofen', '200mg', 'As needed', 'Take for pain, not more than 3 times per day', 'Pain relief', '2023-05-15', FALSE);

-- Insert demo lab results
INSERT INTO lab_results (patient_id, doctor_id, lab_tech_id, test_name, test_type, result, normal_range, unit, 
                        is_abnormal, test_date, result_date, status)
VALUES 
(2, 2, 4, 'Blood Pressure', 'Vital Signs', '150/95', '120/80', 'mmHg', 
 TRUE, '2023-06-10 14:00:00', '2023-06-10 14:10:00', 'Completed'),
 
(3, 2, 4, 'Blood Glucose', 'Blood Test', '180', '70-99', 'mg/dL', 
 TRUE, '2023-07-05 09:30:00', '2023-07-05 10:15:00', 'Completed'),
 
(1, 2, 4, 'Complete Blood Count', 'Blood Test', 'Normal', 'N/A', 'N/A', 
 FALSE, '2023-05-15 10:15:00', '2023-05-15 11:30:00', 'Completed');

-- Insert demo notes
INSERT INTO notes (patient_id, author_id, author_name, author_role, title, content, note_type)
VALUES 
(1, 2, 'Dr. John Smith', 'Doctor', 'Initial Consultation', 'Patient presents with cold symptoms. No significant medical history.', 'General'),
(2, 2, 'Dr. John Smith', 'Doctor', 'Hypertension Follow-up', 'Patient responding well to medication. Continue current treatment plan.', 'Progress'),
(3, 2, 'Dr. John Smith', 'Doctor', 'Diabetes Management', 'Patient needs to improve diet and exercise. Scheduled follow-up in 1 month.', 'Treatment'); 