package com.example.demo.database;

import com.example.demo.model.Medication;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Medication-related database operations
 */
public class MedicationDAO {
    
    /**
     * Get all medications for a patient
     * 
     * @param patientId The ID of the patient
     * @return List of medications for the patient
     */
    public List<Medication> getMedicationsByPatientId(int patientId) {
        List<Medication> medications = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM medications WHERE patient_id = ? ORDER BY is_current DESC, start_date DESC";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, patientId);
                
                ResultSet rs = stmt.executeQuery();
                
                while (rs.next()) {
                    medications.add(mapResultSetToMedication(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving medications: " + e.getMessage());
            e.printStackTrace();
        }
        
        return medications;
    }
    
    /**
     * Get a specific medication by ID
     * 
     * @param medicationId The ID of the medication
     * @return Optional containing the medication if found
     */
    public Optional<Medication> getMedicationById(int medicationId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM medications WHERE medication_id = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, medicationId);
                
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    return Optional.of(mapResultSetToMedication(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving medication by ID: " + e.getMessage());
            e.printStackTrace();
        }
        
        return Optional.empty();
    }
    
    /**
     * Create a new medication
     * 
     * @param medication The medication to create
     * @return true if creation was successful
     */
    public boolean createMedication(Medication medication) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO medications (patient_id, doctor_id, name, dosage, frequency, " +
                    "instructions, purpose, start_date, end_date, is_current, side_effects, notes) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, medication.getPatientId());
                
                if (medication.getDoctorId() > 0) {
                    stmt.setInt(2, medication.getDoctorId());
                } else {
                    stmt.setNull(2, Types.INTEGER);
                }
                
                stmt.setString(3, medication.getName());
                stmt.setString(4, medication.getDosage());
                stmt.setString(5, medication.getFrequency());
                stmt.setString(6, medication.getInstructions());
                stmt.setString(7, medication.getPurpose());
                
                // Handle dates
                if (medication.getStartDate() != null) {
                    stmt.setDate(8, Date.valueOf(medication.getStartDate()));
                } else {
                    stmt.setDate(8, Date.valueOf(LocalDate.now()));
                }
                
                if (medication.getEndDate() != null) {
                    stmt.setDate(9, Date.valueOf(medication.getEndDate()));
                } else {
                    stmt.setNull(9, Types.DATE);
                }
                
                stmt.setBoolean(10, medication.isCurrent());
                stmt.setString(11, medication.getSideEffects());
                stmt.setString(12, medication.getNotes());
                
                int affectedRows = stmt.executeUpdate();
                
                if (affectedRows == 0) {
                    return false;
                }
                
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        medication.setMedicationId(generatedKeys.getInt(1));
                        return true;
                    } else {
                        return false;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error creating medication: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Update an existing medication
     * 
     * @param medication The medication to update
     * @return true if update was successful
     */
    public boolean updateMedication(Medication medication) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "UPDATE medications SET patient_id = ?, doctor_id = ?, name = ?, dosage = ?, " +
                    "frequency = ?, instructions = ?, purpose = ?, start_date = ?, end_date = ?, " +
                    "is_current = ?, side_effects = ?, notes = ? WHERE medication_id = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, medication.getPatientId());
                
                if (medication.getDoctorId() > 0) {
                    stmt.setInt(2, medication.getDoctorId());
                } else {
                    stmt.setNull(2, Types.INTEGER);
                }
                
                stmt.setString(3, medication.getName());
                stmt.setString(4, medication.getDosage());
                stmt.setString(5, medication.getFrequency());
                stmt.setString(6, medication.getInstructions());
                stmt.setString(7, medication.getPurpose());
                
                // Handle dates
                if (medication.getStartDate() != null) {
                    stmt.setDate(8, Date.valueOf(medication.getStartDate()));
                } else {
                    stmt.setDate(8, Date.valueOf(LocalDate.now()));
                }
                
                if (medication.getEndDate() != null) {
                    stmt.setDate(9, Date.valueOf(medication.getEndDate()));
                } else {
                    stmt.setNull(9, Types.DATE);
                }
                
                stmt.setBoolean(10, medication.isCurrent());
                stmt.setString(11, medication.getSideEffects());
                stmt.setString(12, medication.getNotes());
                stmt.setInt(13, medication.getMedicationId());
                
                int affectedRows = stmt.executeUpdate();
                
                return affectedRows > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error updating medication: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Delete a medication
     * 
     * @param medicationId The ID of the medication to delete
     * @return true if deletion was successful
     */
    public boolean deleteMedication(int medicationId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "DELETE FROM medications WHERE medication_id = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, medicationId);
                
                int affectedRows = stmt.executeUpdate();
                
                return affectedRows > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error deleting medication: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Get all current medications for a patient
     * 
     * @param patientId The ID of the patient
     * @return List of current medications for the patient
     */
    public List<Medication> getCurrentMedications(int patientId) {
        List<Medication> medications = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM medications WHERE patient_id = ? AND is_current = 1 " +
                         "ORDER BY start_date DESC";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, patientId);
                
                ResultSet rs = stmt.executeQuery();
                
                while (rs.next()) {
                    medications.add(mapResultSetToMedication(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving current medications: " + e.getMessage());
            e.printStackTrace();
        }
        
        return medications;
    }
    
    /**
     * Map a ResultSet row to a Medication object
     * 
     * @param rs The ResultSet to map
     * @return A Medication object with data from the ResultSet
     * @throws SQLException If there's an error accessing the ResultSet
     */
    private Medication mapResultSetToMedication(ResultSet rs) throws SQLException {
        Medication medication = new Medication();
        
        medication.setMedicationId(rs.getInt("medication_id"));
        medication.setPatientId(rs.getInt("patient_id"));
        
        // Handle nullable doctor_id
        try {
            medication.setDoctorId(rs.getInt("doctor_id"));
            if (rs.wasNull()) {
                medication.setDoctorId(0);
            }
        } catch (SQLException e) {
            medication.setDoctorId(0);
        }
        
        medication.setName(rs.getString("name"));
        medication.setDosage(rs.getString("dosage"));
        medication.setFrequency(rs.getString("frequency"));
        medication.setInstructions(rs.getString("instructions"));
        medication.setPurpose(rs.getString("purpose"));
        
        Date startDate = rs.getDate("start_date");
        if (startDate != null) {
            medication.setStartDate(startDate.toLocalDate());
        }
        
        Date endDate = rs.getDate("end_date");
        if (endDate != null) {
            medication.setEndDate(endDate.toLocalDate());
        }
        
        medication.setCurrent(rs.getBoolean("is_current"));
        medication.setSideEffects(rs.getString("side_effects"));
        medication.setNotes(rs.getString("notes"));
        
        return medication;
    }
} 