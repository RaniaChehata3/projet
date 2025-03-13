-- Update medical_records table to add missing columns
ALTER TABLE medical_records
ADD COLUMN IF NOT EXISTS patient_name VARCHAR(100) AFTER patient_id,
ADD COLUMN IF NOT EXISTS doctor_name VARCHAR(100) AFTER doctor_id,
ADD COLUMN IF NOT EXISTS visit_type VARCHAR(50) AFTER doctor_name,
ADD COLUMN IF NOT EXISTS status VARCHAR(50) DEFAULT 'Pending' AFTER notes,
ADD COLUMN IF NOT EXISTS diagnosis_codes VARCHAR(255) AFTER status,
ADD COLUMN IF NOT EXISTS attachments VARCHAR(255) AFTER diagnosis_codes;

-- Add indexes for performance
CREATE INDEX IF NOT EXISTS idx_medical_records_patient_id ON medical_records(patient_id);
CREATE INDEX IF NOT EXISTS idx_medical_records_doctor_id ON medical_records(doctor_id);
CREATE INDEX IF NOT EXISTS idx_medical_records_status ON medical_records(status);
CREATE INDEX IF NOT EXISTS idx_medical_records_visit_date ON medical_records(visit_date); 