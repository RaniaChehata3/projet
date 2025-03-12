package com.example.demo.database;

import com.example.demo.model.LabResult;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for LabResult-related database operations
 */
public class LabResultDAO {
    
    /**
     * Get all lab results for a patient
     * 
     * @param patientId The ID of the patient
     * @return List of lab results for the patient
     */
    public List<LabResult> getLabResultsByPatientId(int patientId) {
        List<LabResult> results = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM lab_results WHERE patient_id = ? ORDER BY test_date DESC";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, patientId);
                
                ResultSet rs = stmt.executeQuery();
                
                while (rs.next()) {
                    results.add(mapResultSetToLabResult(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving lab results: " + e.getMessage());
            e.printStackTrace();
        }
        
        return results;
    }
    
    /**
     * Get a specific lab result by ID
     * 
     * @param resultId The ID of the lab result
     * @return Optional containing the lab result if found
     */
    public Optional<LabResult> getLabResultById(int resultId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM lab_results WHERE result_id = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, resultId);
                
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    return Optional.of(mapResultSetToLabResult(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving lab result by ID: " + e.getMessage());
            e.printStackTrace();
        }
        
        return Optional.empty();
    }
    
    /**
     * Create a new lab result
     * 
     * @param labResult The lab result to create
     * @return true if creation was successful
     */
    public boolean createLabResult(LabResult labResult) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO lab_results (patient_id, doctor_id, lab_tech_id, test_name, " +
                    "test_type, result, normal_range, unit, is_abnormal, test_date, result_date, " +
                    "notes, status, is_urgent) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, labResult.getPatientId());
                
                if (labResult.getDoctorId() > 0) {
                    stmt.setInt(2, labResult.getDoctorId());
                } else {
                    stmt.setNull(2, Types.INTEGER);
                }
                
                if (labResult.getLabTechId() > 0) {
                    stmt.setInt(3, labResult.getLabTechId());
                } else {
                    stmt.setNull(3, Types.INTEGER);
                }
                
                stmt.setString(4, labResult.getTestName());
                stmt.setString(5, labResult.getTestType());
                stmt.setString(6, labResult.getResult());
                stmt.setString(7, labResult.getNormalRange());
                stmt.setString(8, labResult.getUnit());
                stmt.setBoolean(9, labResult.isAbnormal());
                
                // Handle dates
                if (labResult.getTestDate() != null) {
                    stmt.setTimestamp(10, Timestamp.valueOf(labResult.getTestDate()));
                } else {
                    stmt.setTimestamp(10, new Timestamp(System.currentTimeMillis()));
                }
                
                if (labResult.getResultDate() != null) {
                    stmt.setTimestamp(11, Timestamp.valueOf(labResult.getResultDate()));
                } else {
                    stmt.setNull(11, Types.TIMESTAMP);
                }
                
                stmt.setString(12, labResult.getNotes());
                
                // Default to "Pending" if no status
                if (labResult.getStatus() == null || labResult.getStatus().isEmpty()) {
                    stmt.setString(13, "Pending");
                } else {
                    stmt.setString(13, labResult.getStatus());
                }
                
                stmt.setBoolean(14, labResult.isUrgent());
                
                int affectedRows = stmt.executeUpdate();
                
                if (affectedRows == 0) {
                    return false;
                }
                
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        labResult.setResultId(generatedKeys.getInt(1));
                        return true;
                    } else {
                        return false;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error creating lab result: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Update an existing lab result
     * 
     * @param labResult The lab result to update
     * @return true if update was successful
     */
    public boolean updateLabResult(LabResult labResult) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "UPDATE lab_results SET patient_id = ?, doctor_id = ?, lab_tech_id = ?, " +
                    "test_name = ?, test_type = ?, result = ?, normal_range = ?, unit = ?, " +
                    "is_abnormal = ?, test_date = ?, result_date = ?, notes = ?, status = ?, " +
                    "is_urgent = ? WHERE result_id = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, labResult.getPatientId());
                
                if (labResult.getDoctorId() > 0) {
                    stmt.setInt(2, labResult.getDoctorId());
                } else {
                    stmt.setNull(2, Types.INTEGER);
                }
                
                if (labResult.getLabTechId() > 0) {
                    stmt.setInt(3, labResult.getLabTechId());
                } else {
                    stmt.setNull(3, Types.INTEGER);
                }
                
                stmt.setString(4, labResult.getTestName());
                stmt.setString(5, labResult.getTestType());
                stmt.setString(6, labResult.getResult());
                stmt.setString(7, labResult.getNormalRange());
                stmt.setString(8, labResult.getUnit());
                stmt.setBoolean(9, labResult.isAbnormal());
                
                // Handle dates
                if (labResult.getTestDate() != null) {
                    stmt.setTimestamp(10, Timestamp.valueOf(labResult.getTestDate()));
                } else {
                    stmt.setTimestamp(10, new Timestamp(System.currentTimeMillis()));
                }
                
                if (labResult.getResultDate() != null) {
                    stmt.setTimestamp(11, Timestamp.valueOf(labResult.getResultDate()));
                } else {
                    stmt.setNull(11, Types.TIMESTAMP);
                }
                
                stmt.setString(12, labResult.getNotes());
                
                // Default to "Pending" if no status
                if (labResult.getStatus() == null || labResult.getStatus().isEmpty()) {
                    stmt.setString(13, "Pending");
                } else {
                    stmt.setString(13, labResult.getStatus());
                }
                
                stmt.setBoolean(14, labResult.isUrgent());
                stmt.setInt(15, labResult.getResultId());
                
                int affectedRows = stmt.executeUpdate();
                
                return affectedRows > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error updating lab result: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Delete a lab result
     * 
     * @param resultId The ID of the lab result to delete
     * @return true if deletion was successful
     */
    public boolean deleteLabResult(int resultId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "DELETE FROM lab_results WHERE result_id = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, resultId);
                
                int affectedRows = stmt.executeUpdate();
                
                return affectedRows > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error deleting lab result: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Get all pending lab results for a patient
     * 
     * @param patientId The ID of the patient
     * @return List of pending lab results for the patient
     */
    public List<LabResult> getPendingLabResults(int patientId) {
        List<LabResult> results = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM lab_results WHERE patient_id = ? AND status = 'Pending' " +
                         "ORDER BY is_urgent DESC, test_date DESC";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, patientId);
                
                ResultSet rs = stmt.executeQuery();
                
                while (rs.next()) {
                    results.add(mapResultSetToLabResult(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving pending lab results: " + e.getMessage());
            e.printStackTrace();
        }
        
        return results;
    }
    
    /**
     * Map a ResultSet row to a LabResult object
     * 
     * @param rs The ResultSet to map
     * @return A LabResult object with data from the ResultSet
     * @throws SQLException If there's an error accessing the ResultSet
     */
    private LabResult mapResultSetToLabResult(ResultSet rs) throws SQLException {
        LabResult labResult = new LabResult();
        
        labResult.setResultId(rs.getInt("result_id"));
        labResult.setPatientId(rs.getInt("patient_id"));
        
        // Handle nullable foreign keys
        try {
            labResult.setDoctorId(rs.getInt("doctor_id"));
            if (rs.wasNull()) {
                labResult.setDoctorId(0);
            }
        } catch (SQLException e) {
            labResult.setDoctorId(0);
        }
        
        try {
            labResult.setLabTechId(rs.getInt("lab_tech_id"));
            if (rs.wasNull()) {
                labResult.setLabTechId(0);
            }
        } catch (SQLException e) {
            labResult.setLabTechId(0);
        }
        
        labResult.setTestName(rs.getString("test_name"));
        labResult.setTestType(rs.getString("test_type"));
        labResult.setResult(rs.getString("result"));
        labResult.setNormalRange(rs.getString("normal_range"));
        labResult.setUnit(rs.getString("unit"));
        labResult.setAbnormal(rs.getBoolean("is_abnormal"));
        
        Timestamp testDate = rs.getTimestamp("test_date");
        if (testDate != null) {
            labResult.setTestDate(testDate.toLocalDateTime());
        }
        
        Timestamp resultDate = rs.getTimestamp("result_date");
        if (resultDate != null) {
            labResult.setResultDate(resultDate.toLocalDateTime());
        }
        
        labResult.setNotes(rs.getString("notes"));
        labResult.setStatus(rs.getString("status"));
        labResult.setUrgent(rs.getBoolean("is_urgent"));
        
        return labResult;
    }
} 