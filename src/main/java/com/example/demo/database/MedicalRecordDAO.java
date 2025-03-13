package com.example.demo.database;

import com.example.demo.model.MedicalRecord;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for MedicalRecord-related database operations
 */
public class MedicalRecordDAO {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
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
            System.err.println("Error retrieving medical record: " + e.getMessage());
            e.printStackTrace();
        }
        
        return Optional.empty();
    }
    
    /**
     * Create a new medical record in the database
     * 
     * @param medicalRecord The medical record to create
     * @return true if successful, false otherwise
     */
    public boolean createMedicalRecord(MedicalRecord medicalRecord) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO medical_records (patient_id, patient_name, doctor_id, doctor_name, " +
                         "visit_type, visit_date, symptoms, diagnosis, treatment, notes, status, " +
                         "diagnosis_codes, attachments, follow_up_date, record_type) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                // Handle patient_id - Required field (NOT NULL)
                if (medicalRecord.getPatientId() > 0) {
                    stmt.setInt(1, medicalRecord.getPatientId());
                } else {
                    // Default to 1 if not set - this prevents NOT NULL constraint violations
                    // In a real app, you might want to throw an exception instead
                    stmt.setInt(1, 1);
                    System.err.println("Warning: Setting default patient_id=1 because no valid ID was provided");
                }
                
                stmt.setString(2, medicalRecord.getPatientName());
                
                // Handle doctor_id - Required field (NOT NULL)
                if (medicalRecord.getDoctorId() > 0) {
                    stmt.setInt(3, medicalRecord.getDoctorId());
                } else {
                    // Default to 1 if not set
                    stmt.setInt(3, 1);
                    System.err.println("Warning: Setting default doctor_id=1 because no valid ID was provided");
                }
                
                stmt.setString(4, medicalRecord.getDoctorName());
                stmt.setString(5, medicalRecord.getVisitType());
                
                if (medicalRecord.getVisitDate() != null) {
                    stmt.setTimestamp(6, Timestamp.valueOf(medicalRecord.getVisitDate()));
                } else {
                    stmt.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
                }
                
                stmt.setString(7, medicalRecord.getSymptoms());
                stmt.setString(8, medicalRecord.getDiagnosis());
                stmt.setString(9, medicalRecord.getTreatment());
                stmt.setString(10, medicalRecord.getNotes());
                
                // Set status with default if null
                if (medicalRecord.getStatus() != null && !medicalRecord.getStatus().isEmpty()) {
                    stmt.setString(11, medicalRecord.getStatus());
                } else {
                    stmt.setString(11, "Pending"); // Default value
                }
                
                stmt.setString(12, medicalRecord.getDiagnosisCodes());
                stmt.setString(13, medicalRecord.getAttachments());
                
                // Handle follow_up_date (can be null)
                if (medicalRecord.getFollowUpDate() != null) {
                    stmt.setDate(14, java.sql.Date.valueOf(medicalRecord.getFollowUpDate().toLocalDate()));
                } else {
                    stmt.setNull(14, Types.DATE);
                }
                
                // Set record_type with default if null
                if (medicalRecord.getRecordType() != null && !medicalRecord.getRecordType().isEmpty()) {
                    stmt.setString(15, medicalRecord.getRecordType());
                } else {
                    stmt.setString(15, "Regular visit"); // Default value
                }
                
                int affectedRows = stmt.executeUpdate();
                
                if (affectedRows == 0) {
                    return false;
                }
                
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        medicalRecord.setRecordId(generatedKeys.getInt(1));
                        System.out.println("Created record with ID: " + medicalRecord.getRecordId());
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
     * Update an existing medical record in the database
     * 
     * @param medicalRecord The medical record to update
     * @return true if successful, false otherwise
     */
    public boolean updateMedicalRecord(MedicalRecord medicalRecord) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "UPDATE medical_records SET patient_id = ?, patient_name = ?, " +
                         "doctor_id = ?, doctor_name = ?, visit_type = ?, visit_date = ?, " +
                         "symptoms = ?, diagnosis = ?, treatment = ?, notes = ?, status = ?, " +
                         "diagnosis_codes = ?, attachments = ?, follow_up_date = ?, record_type = ? " +
                         "WHERE record_id = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                // Handle patient_id - Required field (NOT NULL)
                if (medicalRecord.getPatientId() > 0) {
                    stmt.setInt(1, medicalRecord.getPatientId());
                } else {
                    // Default to 1 if not set
                    stmt.setInt(1, 1);
                    System.err.println("Warning: Setting default patient_id=1 because no valid ID was provided");
                }
                
                stmt.setString(2, medicalRecord.getPatientName());
                
                // Handle doctor_id - Required field (NOT NULL)
                if (medicalRecord.getDoctorId() > 0) {
                    stmt.setInt(3, medicalRecord.getDoctorId());
                } else {
                    // Default to 1 if not set
                    stmt.setInt(3, 1);
                    System.err.println("Warning: Setting default doctor_id=1 because no valid ID was provided");
                }
                
                stmt.setString(4, medicalRecord.getDoctorName());
                stmt.setString(5, medicalRecord.getVisitType());
                
                if (medicalRecord.getVisitDate() != null) {
                    stmt.setTimestamp(6, Timestamp.valueOf(medicalRecord.getVisitDate()));
                } else {
                    stmt.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
                }
                
                stmt.setString(7, medicalRecord.getSymptoms());
                stmt.setString(8, medicalRecord.getDiagnosis());
                stmt.setString(9, medicalRecord.getTreatment());
                stmt.setString(10, medicalRecord.getNotes());
                
                // Set status with default if null
                if (medicalRecord.getStatus() != null && !medicalRecord.getStatus().isEmpty()) {
                    stmt.setString(11, medicalRecord.getStatus());
                } else {
                    stmt.setString(11, "Pending"); // Default value
                }
                
                stmt.setString(12, medicalRecord.getDiagnosisCodes());
                stmt.setString(13, medicalRecord.getAttachments());
                
                // Handle follow_up_date (can be null)
                if (medicalRecord.getFollowUpDate() != null) {
                    stmt.setDate(14, java.sql.Date.valueOf(medicalRecord.getFollowUpDate().toLocalDate()));
                } else {
                    stmt.setNull(14, Types.DATE);
                }
                
                // Set record_type with default if null
                if (medicalRecord.getRecordType() != null && !medicalRecord.getRecordType().isEmpty()) {
                    stmt.setString(15, medicalRecord.getRecordType());
                } else {
                    stmt.setString(15, "Regular visit"); // Default value
                }
                
                stmt.setInt(16, medicalRecord.getRecordId());
                
                int affectedRows = stmt.executeUpdate();
                System.out.println("Updated record ID: " + medicalRecord.getRecordId() + ", Affected rows: " + affectedRows);
                return affectedRows > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error updating medical record: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Delete a medical record from the database
     * 
     * @param recordId The ID of the medical record to delete
     * @return true if successful, false otherwise
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
     * Get all medical records with pagination
     * 
     * @param offset The offset for pagination
     * @param limit The maximum number of records to return
     * @return List of medical records
     */
    public List<MedicalRecord> getAllMedicalRecords(int offset, int limit) {
        List<MedicalRecord> records = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM medical_records ORDER BY visit_date DESC LIMIT ? OFFSET ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, limit);
                stmt.setInt(2, offset);
                
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
     * Get total count of medical records
     * 
     * @return The total number of records
     */
    public int getTotalRecordsCount() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT COUNT(*) FROM medical_records";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error counting medical records: " + e.getMessage());
            e.printStackTrace();
        }
        
        return 0;
    }
    
    /**
     * Convert a ResultSet row to a MedicalRecord object
     * 
     * @param rs The ResultSet to map
     * @return A MedicalRecord object
     * @throws SQLException If a database access error occurs
     */
    private MedicalRecord mapResultSetToMedicalRecord(ResultSet rs) throws SQLException {
        MedicalRecord record = new MedicalRecord();
        
        record.setRecordId(rs.getInt("record_id"));
        record.setPatientId(rs.getInt("patient_id"));
        
        // Handle nullable fields
        record.setPatientName(rs.getString("patient_name"));
        
        try {
            record.setDoctorId(rs.getInt("doctor_id"));
            if (rs.wasNull()) {
                record.setDoctorId(0);
            }
        } catch (SQLException e) {
            record.setDoctorId(0);
        }
        
        record.setDoctorName(rs.getString("doctor_name"));
        record.setVisitType(rs.getString("visit_type"));
        
        Timestamp visitDate = rs.getTimestamp("visit_date");
        if (visitDate != null) {
            record.setVisitDate(visitDate.toLocalDateTime());
        }
        
        record.setSymptoms(rs.getString("symptoms"));
        record.setDiagnosis(rs.getString("diagnosis"));
        record.setTreatment(rs.getString("treatment"));
        record.setNotes(rs.getString("notes"));
        record.setStatus(rs.getString("status"));
        record.setDiagnosisCodes(rs.getString("diagnosis_codes"));
        record.setAttachments(rs.getString("attachments"));
        
        // Map follow_up_date if it exists
        try {
            java.sql.Date followUpDate = rs.getDate("follow_up_date");
            if (followUpDate != null) {
                // Convert java.sql.Date to LocalDateTime at midnight
                record.setFollowUpDate(followUpDate.toLocalDate().atStartOfDay());
            }
        } catch (SQLException e) {
            // Column might not exist in older database versions, ignore
            System.out.println("Note: follow_up_date column might not exist: " + e.getMessage());
        }
        
        // Map record_type if it exists
        try {
            String recordType = rs.getString("record_type");
            record.setRecordType(recordType);
        } catch (SQLException e) {
            // Column might not exist in older database versions, ignore
            System.out.println("Note: record_type column might not exist: " + e.getMessage());
        }
        
        return record;
    }
    
    /**
     * Get medical records filtered by multiple criteria
     * 
     * @param patientName Patient name filter (optional)
     * @param recordType Record type filter (optional)
     * @param status Status filter (optional)
     * @param startDate Start date filter (optional)
     * @param endDate End date filter (optional)
     * @param offset Pagination offset
     * @param limit Pagination limit
     * @return List of filtered medical records
     */
    public List<MedicalRecord> getFilteredMedicalRecords(String patientName, String recordType, 
                                                        String status, Date startDate, Date endDate, 
                                                        int offset, int limit) {
        List<MedicalRecord> records = new ArrayList<>();
        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM medical_records WHERE 1=1");
        List<Object> params = new ArrayList<>();
        
        if (patientName != null && !patientName.isEmpty()) {
            sqlBuilder.append(" AND patient_name LIKE ?");
            params.add("%" + patientName + "%");
        }
        
        if (recordType != null && !recordType.isEmpty()) {
            sqlBuilder.append(" AND visit_type = ?");
            params.add(recordType);
        }
        
        if (status != null && !status.isEmpty()) {
            sqlBuilder.append(" AND status = ?");
            params.add(status);
        }
        
        if (startDate != null) {
            sqlBuilder.append(" AND visit_date >= ?");
            params.add(startDate);
        }
        
        if (endDate != null) {
            sqlBuilder.append(" AND visit_date <= ?");
            params.add(endDate);
        }
        
        sqlBuilder.append(" ORDER BY visit_date DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(sqlBuilder.toString())) {
                for (int i = 0; i < params.size(); i++) {
                    Object param = params.get(i);
                    if (param instanceof String) {
                        stmt.setString(i + 1, (String) param);
                    } else if (param instanceof Integer) {
                        stmt.setInt(i + 1, (Integer) param);
                    } else if (param instanceof Date) {
                        stmt.setDate(i + 1, (Date) param);
                    }
                }
                
                ResultSet rs = stmt.executeQuery();
                
                while (rs.next()) {
                    records.add(mapResultSetToMedicalRecord(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving filtered medical records: " + e.getMessage());
            e.printStackTrace();
        }
        
        return records;
    }
    
    /**
     * Convert between controller's MedicalRecord format and database format
     * 
     * @param controllerRecord The record from the controller
     * @return A MedicalRecord suitable for database operations
     */
    public MedicalRecord convertToDbFormat(com.example.demo.controller.MedicalRecordsController.MedicalRecord controllerRecord) {
        MedicalRecord dbRecord = new MedicalRecord();
        
        // Parse ID (remove "MR-" prefix if present)
        String id = controllerRecord.getId();
        if (id.startsWith("MR-")) {
            try {
                dbRecord.setRecordId(Integer.parseInt(id.substring(3)));
            } catch (NumberFormatException e) {
                // Generate a new ID if parsing fails
                dbRecord.setRecordId(0);
            }
        } else {
            try {
                dbRecord.setRecordId(Integer.parseInt(id));
            } catch (NumberFormatException e) {
                // Generate a new ID if parsing fails
                dbRecord.setRecordId(0);
            }
        }
        
        // Set patient and doctor names
        dbRecord.setPatientName(controllerRecord.getPatient());
        dbRecord.setDoctorName(controllerRecord.getDoctor());
        
        // Set type, status, notes, etc.
        dbRecord.setVisitType(controllerRecord.getType());
        dbRecord.setStatus(controllerRecord.getStatus());
        dbRecord.setNotes(controllerRecord.getNotes());
        dbRecord.setDiagnosisCodes(controllerRecord.getDiagnosisCodes());
        dbRecord.setAttachments(controllerRecord.getAttachments());
        dbRecord.setSymptoms(controllerRecord.getSymptoms());
        dbRecord.setDiagnosis(controllerRecord.getDiagnosis());
        dbRecord.setTreatment(controllerRecord.getTreatment());
        
        // For compatibility with both tables - visit_type and record_type are the same
        dbRecord.setRecordType(controllerRecord.getType());
        
        // Parse date
        try {
            LocalDateTime visitDate = LocalDateTime.parse(controllerRecord.getDate() + "T00:00:00");
            dbRecord.setVisitDate(visitDate);
        } catch (Exception e) {
            dbRecord.setVisitDate(LocalDateTime.now());
        }
        
        // Print debug info
        System.out.println("Converting to DB format - ID: " + dbRecord.getRecordId() + 
                           ", Patient: " + dbRecord.getPatientName() + 
                           ", Type: " + dbRecord.getVisitType());
        
        return dbRecord;
    }
    
    /**
     * Convert from database MedicalRecord to controller format
     * 
     * @param dbRecord The record from the database
     * @return A MedicalRecord suitable for the controller
     */
    public com.example.demo.controller.MedicalRecordsController.MedicalRecord convertToControllerFormat(MedicalRecord dbRecord) {
        String id = "MR-" + dbRecord.getRecordId();
        String patient = dbRecord.getPatientName();
        String date = dbRecord.getVisitDate() != null ? dbRecord.getVisitDate().format(DATE_FORMATTER) : "";
        String type = dbRecord.getVisitType();
        String doctor = dbRecord.getDoctorName();
        String status = dbRecord.getStatus();
        String notes = dbRecord.getNotes();
        String diagnosisCodes = dbRecord.getDiagnosisCodes();
        String attachments = dbRecord.getAttachments();
        String symptoms = dbRecord.getSymptoms();
        String diagnosis = dbRecord.getDiagnosis();
        String treatment = dbRecord.getTreatment();
        
        // Print debug info
        System.out.println("Converting from DB format - ID: " + id + 
                          ", Patient: " + patient + 
                          ", Type: " + type);
        
        return new com.example.demo.controller.MedicalRecordsController.MedicalRecord(
            id, patient, date, type, doctor, status, 
            notes, diagnosisCodes, attachments,
            symptoms, diagnosis, treatment
        );
    }
    
    /**
     * Check if a medical record exists with the given ID
     * 
     * @param recordId The ID of the medical record to check
     * @return True if the record exists, false otherwise
     */
    public boolean recordExists(int recordId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT COUNT(*) FROM medical_records WHERE record_id = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, recordId);
                
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking if medical record exists: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
} 