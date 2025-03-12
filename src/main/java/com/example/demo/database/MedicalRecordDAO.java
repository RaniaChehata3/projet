package com.example.demo.database;

import com.example.demo.model.MedicalRecord;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for MedicalRecord-related database operations
 */
public class MedicalRecordDAO {
    
    /**
     * Get all medical records for a patient
     * 
     * @param patientId The ID of the patient
     * @return List of medical records for the patient
     */
    public List<MedicalRecord> getMedicalRecordsByPatientId(int patientId) {
        List<MedicalRecord> records = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM medical_records WHERE patient_id = ? ORDER BY visit_date DESC";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, patientId);
                
                ResultSet rs = stmt.executeQuery();
                
                while (rs.next()) {
                    records.add(mapResultSetToMedicalRecord(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving medical records: " + e.getMessage());
            e.printStackTrace();
        }
        
        return records;
    }
    
    /**
     * Get a specific medical record by ID
     * 
     * @param recordId The ID of the medical record
     * @return Optional containing the medical record if found
     */
    public Optional<MedicalRecord> getMedicalRecordById(int recordId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM medical_records WHERE record_id = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, recordId);
                
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    return Optional.of(mapResultSetToMedicalRecord(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving medical record by ID: " + e.getMessage());
            e.printStackTrace();
        }
        
        return Optional.empty();
    }
    
    /**
     * Create a new medical record
     * 
     * @param medicalRecord The medical record to create
     * @return true if creation was successful
     */
    public boolean createMedicalRecord(MedicalRecord medicalRecord) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO medical_records (patient_id, doctor_id, visit_type, visit_date, " +
                         "symptoms, diagnosis, treatment, notes) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, medicalRecord.getPatientId());
                
                if (medicalRecord.getDoctorId() > 0) {
                    stmt.setInt(2, medicalRecord.getDoctorId());
                } else {
                    stmt.setNull(2, Types.INTEGER);
                }
                
                stmt.setString(3, medicalRecord.getVisitType());
                
                if (medicalRecord.getVisitDate() != null) {
                    stmt.setTimestamp(4, Timestamp.valueOf(medicalRecord.getVisitDate()));
                } else {
                    stmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
                }
                
                stmt.setString(5, medicalRecord.getSymptoms());
                stmt.setString(6, medicalRecord.getDiagnosis());
                stmt.setString(7, medicalRecord.getTreatment());
                stmt.setString(8, medicalRecord.getNotes());
                
                int affectedRows = stmt.executeUpdate();
                
                if (affectedRows == 0) {
                    return false;
                }
                
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        medicalRecord.setRecordId(generatedKeys.getInt(1));
                        return true;
                    } else {
                        return false;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error creating medical record: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Update an existing medical record
     * 
     * @param medicalRecord The medical record to update
     * @return true if update was successful
     */
    public boolean updateMedicalRecord(MedicalRecord medicalRecord) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "UPDATE medical_records SET patient_id = ?, doctor_id = ?, visit_type = ?, " +
                         "visit_date = ?, symptoms = ?, diagnosis = ?, treatment = ?, notes = ? " +
                         "WHERE record_id = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, medicalRecord.getPatientId());
                
                if (medicalRecord.getDoctorId() > 0) {
                    stmt.setInt(2, medicalRecord.getDoctorId());
                } else {
                    stmt.setNull(2, Types.INTEGER);
                }
                
                stmt.setString(3, medicalRecord.getVisitType());
                
                if (medicalRecord.getVisitDate() != null) {
                    stmt.setTimestamp(4, Timestamp.valueOf(medicalRecord.getVisitDate()));
                } else {
                    stmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
                }
                
                stmt.setString(5, medicalRecord.getSymptoms());
                stmt.setString(6, medicalRecord.getDiagnosis());
                stmt.setString(7, medicalRecord.getTreatment());
                stmt.setString(8, medicalRecord.getNotes());
                stmt.setInt(9, medicalRecord.getRecordId());
                
                int affectedRows = stmt.executeUpdate();
                
                return affectedRows > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error updating medical record: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Delete a medical record
     * 
     * @param recordId The ID of the medical record to delete
     * @return true if deletion was successful
     */
    public boolean deleteMedicalRecord(int recordId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "DELETE FROM medical_records WHERE record_id = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, recordId);
                
                int affectedRows = stmt.executeUpdate();
                
                return affectedRows > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error deleting medical record: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Map a ResultSet row to a MedicalRecord object
     * 
     * @param rs The ResultSet to map
     * @return A MedicalRecord object with data from the ResultSet
     * @throws SQLException If there's an error accessing the ResultSet
     */
    private MedicalRecord mapResultSetToMedicalRecord(ResultSet rs) throws SQLException {
        MedicalRecord record = new MedicalRecord();
        
        record.setRecordId(rs.getInt("record_id"));
        record.setPatientId(rs.getInt("patient_id"));
        
        // Handle nullable doctor_id
        try {
            record.setDoctorId(rs.getInt("doctor_id"));
            if (rs.wasNull()) {
                record.setDoctorId(0);
            }
        } catch (SQLException e) {
            record.setDoctorId(0);
        }
        
        record.setVisitType(rs.getString("visit_type"));
        
        Timestamp visitDate = rs.getTimestamp("visit_date");
        if (visitDate != null) {
            record.setVisitDate(visitDate.toLocalDateTime());
        }
        
        record.setSymptoms(rs.getString("symptoms"));
        record.setDiagnosis(rs.getString("diagnosis"));
        record.setTreatment(rs.getString("treatment"));
        record.setNotes(rs.getString("notes"));
        
        return record;
    }
    
    /**
     * Get the most recent medical records for a patient (limited number)
     * 
     * @param patientId The ID of the patient
     * @param limit The maximum number of records to return
     * @return List of the most recent medical records for the patient
     */
    public List<MedicalRecord> getRecentMedicalRecords(int patientId, int limit) {
        List<MedicalRecord> records = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM medical_records WHERE patient_id = ? " +
                         "ORDER BY visit_date DESC LIMIT ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, patientId);
                stmt.setInt(2, limit);
                
                ResultSet rs = stmt.executeQuery();
                
                while (rs.next()) {
                    records.add(mapResultSetToMedicalRecord(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving recent medical records: " + e.getMessage());
            e.printStackTrace();
        }
        
        return records;
    }
} 