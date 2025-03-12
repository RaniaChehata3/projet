package com.example.demo.database;

import com.example.demo.model.Patient;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Patient-related database operations
 */
public class PatientDAO {
    
    private static PatientDAO instance;
    
    /**
     * Private constructor for singleton pattern
     */
    private PatientDAO() {
        // Private constructor for singleton pattern
    }
    
    /**
     * Get the singleton instance
     * 
     * @return The PatientDAO instance
     */
    public static synchronized PatientDAO getInstance() {
        if (instance == null) {
            instance = new PatientDAO();
        }
        return instance;
    }
    
    /**
     * Get a list of patients with pagination
     * 
     * @param page The page number (1-based)
     * @param pageSize The number of records per page
     * @return List of patients for the requested page
     */
    public List<Patient> getAllPatients(int page, int pageSize) {
        List<Patient> patients = new ArrayList<>();
        int offset = (page - 1) * pageSize;
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM patients ORDER BY last_name, first_name LIMIT ? OFFSET ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, pageSize);
                stmt.setInt(2, offset);
                
                ResultSet rs = stmt.executeQuery();
                
                while (rs.next()) {
                    patients.add(mapResultSetToPatient(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving patients: " + e.getMessage());
            e.printStackTrace();
        }
        
        return patients;
    }
    
    /**
     * Get all patients without pagination
     * 
     * @return List of all patients
     */
    public List<Patient> getAllPatients() {
        List<Patient> patients = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM patients ORDER BY last_name, first_name";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                ResultSet rs = stmt.executeQuery();
                
                while (rs.next()) {
                    patients.add(mapResultSetToPatient(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving patients: " + e.getMessage());
            e.printStackTrace();
        }
        
        return patients;
    }
    
    /**
     * Get a specific patient by ID
     * 
     * @param patientId The ID of the patient
     * @return Optional containing the patient if found
     */
    public Optional<Patient> getPatientById(int patientId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM patients WHERE patient_id = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, patientId);
                
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    return Optional.of(mapResultSetToPatient(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving patient by ID: " + e.getMessage());
            e.printStackTrace();
        }
        
        return Optional.empty();
    }
    
    /**
     * Search for patients by name, gender, or phone number
     * 
     * @param searchTerm The search term
     * @return List of matching patients
     */
    public List<Patient> searchPatients(String searchTerm) {
        List<Patient> patients = new ArrayList<>();
        
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllPatients();
        }
        
        String term = "%" + searchTerm.trim() + "%";
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM patients WHERE " +
                         "first_name LIKE ? OR last_name LIKE ? OR " +
                         "CONCAT(first_name, ' ', last_name) LIKE ? OR " +
                         "phone_number LIKE ? OR email LIKE ? " +
                         "ORDER BY last_name, first_name";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, term);
                stmt.setString(2, term);
                stmt.setString(3, term);
                stmt.setString(4, term);
                stmt.setString(5, term);
                
                ResultSet rs = stmt.executeQuery();
                
                while (rs.next()) {
                    patients.add(mapResultSetToPatient(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching patients: " + e.getMessage());
            e.printStackTrace();
        }
        
        return patients;
    }
    
    /**
     * Create a new patient
     * 
     * @param patient The patient to create
     * @return true if creation was successful
     */
    public boolean createPatient(Patient patient) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO patients (first_name, last_name, date_of_birth, gender, " +
                         "address, phone_number, email, insurance_provider, insurance_number, " +
                         "medical_history, allergies, emergency_contact_name, emergency_contact_phone, " +
                         "blood_type, registration_date, primary_doctor_id, is_active) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, patient.getFirstName());
                stmt.setString(2, patient.getLastName());
                
                if (patient.getDateOfBirth() != null) {
                    stmt.setDate(3, Date.valueOf(patient.getDateOfBirth()));
                } else {
                    stmt.setNull(3, Types.DATE);
                }
                
                stmt.setString(4, patient.getGender());
                stmt.setString(5, patient.getAddress());
                stmt.setString(6, patient.getPhoneNumber());
                stmt.setString(7, patient.getEmail());
                stmt.setString(8, patient.getInsuranceProvider());
                stmt.setString(9, patient.getInsuranceNumber());
                stmt.setString(10, patient.getMedicalHistory());
                stmt.setString(11, patient.getAllergies());
                stmt.setString(12, patient.getEmergencyContactName());
                stmt.setString(13, patient.getEmergencyContactPhone());
                stmt.setString(14, patient.getBloodType());
                
                if (patient.getRegistrationDate() != null) {
                    stmt.setDate(15, Date.valueOf(patient.getRegistrationDate()));
                } else {
                    stmt.setDate(15, Date.valueOf(LocalDate.now()));
                }
                
                if (patient.getPrimaryDoctorId() > 0) {
                    stmt.setInt(16, patient.getPrimaryDoctorId());
                } else {
                    stmt.setNull(16, Types.INTEGER);
                }
                
                stmt.setBoolean(17, patient.isActive());
                
                int affectedRows = stmt.executeUpdate();
                
                if (affectedRows == 0) {
                    return false;
                }
                
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        patient.setPatientId(generatedKeys.getInt(1));
                        return true;
                    } else {
                        return false;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error creating patient: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Update an existing patient
     * 
     * @param patient The patient to update
     * @return true if update was successful
     */
    public boolean updatePatient(Patient patient) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "UPDATE patients SET first_name = ?, last_name = ?, date_of_birth = ?, " +
                         "gender = ?, address = ?, phone_number = ?, email = ?, " +
                         "insurance_provider = ?, insurance_number = ?, medical_history = ?, " +
                         "allergies = ?, emergency_contact_name = ?, emergency_contact_phone = ?, " +
                         "blood_type = ?, registration_date = ?, primary_doctor_id = ?, " +
                         "is_active = ? WHERE patient_id = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, patient.getFirstName());
                stmt.setString(2, patient.getLastName());
                
                if (patient.getDateOfBirth() != null) {
                    stmt.setDate(3, Date.valueOf(patient.getDateOfBirth()));
                } else {
                    stmt.setNull(3, Types.DATE);
                }
                
                stmt.setString(4, patient.getGender());
                stmt.setString(5, patient.getAddress());
                stmt.setString(6, patient.getPhoneNumber());
                stmt.setString(7, patient.getEmail());
                stmt.setString(8, patient.getInsuranceProvider());
                stmt.setString(9, patient.getInsuranceNumber());
                stmt.setString(10, patient.getMedicalHistory());
                stmt.setString(11, patient.getAllergies());
                stmt.setString(12, patient.getEmergencyContactName());
                stmt.setString(13, patient.getEmergencyContactPhone());
                stmt.setString(14, patient.getBloodType());
                
                if (patient.getRegistrationDate() != null) {
                    stmt.setDate(15, Date.valueOf(patient.getRegistrationDate()));
                } else {
                    stmt.setDate(15, Date.valueOf(LocalDate.now()));
                }
                
                if (patient.getPrimaryDoctorId() > 0) {
                    stmt.setInt(16, patient.getPrimaryDoctorId());
                } else {
                    stmt.setNull(16, Types.INTEGER);
                }
                
                stmt.setBoolean(17, patient.isActive());
                stmt.setInt(18, patient.getPatientId());
                
                int affectedRows = stmt.executeUpdate();
                
                return affectedRows > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error updating patient: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Delete a patient (or mark as inactive)
     * 
     * @param patientId The ID of the patient to delete
     * @param hardDelete Whether to perform a hard delete or just mark as inactive
     * @return true if deletion was successful
     */
    public boolean deletePatient(int patientId, boolean hardDelete) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql;
            
            if (hardDelete) {
                sql = "DELETE FROM patients WHERE patient_id = ?";
            } else {
                sql = "UPDATE patients SET is_active = false WHERE patient_id = ?";
            }
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, patientId);
                
                int affectedRows = stmt.executeUpdate();
                
                return affectedRows > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error deleting patient: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Simplified delete method (soft delete by marking inactive)
     * 
     * @param patientId The ID of the patient to delete
     * @return true if deletion was successful
     */
    public boolean deletePatient(int patientId) {
        return deletePatient(patientId, false);
    }
    
    /**
     * Get patients assigned to a specific doctor
     * 
     * @param doctorId The ID of the doctor
     * @return List of patients assigned to the doctor
     */
    public List<Patient> getPatientsByDoctorId(int doctorId) {
        List<Patient> patients = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM patients WHERE primary_doctor_id = ? ORDER BY last_name, first_name";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, doctorId);
                
                ResultSet rs = stmt.executeQuery();
                
                while (rs.next()) {
                    patients.add(mapResultSetToPatient(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving patients by doctor ID: " + e.getMessage());
            e.printStackTrace();
        }
        
        return patients;
    }
    
    /**
     * Get the count of patients in the database
     * 
     * @return The number of patients
     */
    public int getPatientCount() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT COUNT(*) FROM patients";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting patient count: " + e.getMessage());
            e.printStackTrace();
        }
        
        return 0;
    }
    
    /**
     * Map a ResultSet row to a Patient object
     * 
     * @param rs The ResultSet to map
     * @return A Patient object with data from the ResultSet
     * @throws SQLException If there's an error accessing the ResultSet
     */
    private Patient mapResultSetToPatient(ResultSet rs) throws SQLException {
        Patient patient = new Patient();
        
        patient.setPatientId(rs.getInt("patient_id"));
        patient.setFirstName(rs.getString("first_name"));
        patient.setLastName(rs.getString("last_name"));
        
        Date dob = rs.getDate("date_of_birth");
        if (dob != null) {
            patient.setDateOfBirth(dob.toLocalDate());
        }
        
        patient.setGender(rs.getString("gender"));
        patient.setAddress(rs.getString("address"));
        patient.setPhoneNumber(rs.getString("phone_number"));
        patient.setEmail(rs.getString("email"));
        patient.setInsuranceProvider(rs.getString("insurance_provider"));
        patient.setInsuranceNumber(rs.getString("insurance_number"));
        patient.setMedicalHistory(rs.getString("medical_history"));
        patient.setAllergies(rs.getString("allergies"));
        patient.setEmergencyContactName(rs.getString("emergency_contact_name"));
        patient.setEmergencyContactPhone(rs.getString("emergency_contact_phone"));
        patient.setBloodType(rs.getString("blood_type"));
        
        Date regDate = rs.getDate("registration_date");
        if (regDate != null) {
            patient.setRegistrationDate(regDate.toLocalDate());
        }
        
        try {
            patient.setPrimaryDoctorId(rs.getInt("primary_doctor_id"));
            if (rs.wasNull()) {
                patient.setPrimaryDoctorId(0);
            }
        } catch (SQLException e) {
            patient.setPrimaryDoctorId(0);
        }
        
        patient.setActive(rs.getBoolean("is_active"));
        
        return patient;
    }
} 