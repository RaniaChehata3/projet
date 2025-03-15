package com.example.demo.database;

import com.example.demo.model.MedicalRecord;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

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
            // Debug info
            System.out.println("Attempting to create new medical record with:");
            System.out.println("- Patient ID: " + medicalRecord.getPatientId());
            System.out.println("- Patient Name: " + medicalRecord.getPatientName());
            System.out.println("- Doctor ID: " + medicalRecord.getDoctorId());
            System.out.println("- Doctor Name: " + medicalRecord.getDoctorName());
            System.out.println("- Visit Type: " + medicalRecord.getVisitType());
            System.out.println("- Visit Date: " + (medicalRecord.getVisitDate() != null ? medicalRecord.getVisitDate() : "null"));
            
            // Check for valid patient ID (required constraint)
            if (medicalRecord.getPatientId() <= 0) {
                // Try to find patient ID by name
                if (medicalRecord.getPatientName() != null && !medicalRecord.getPatientName().trim().isEmpty()) {
                    int patientId = findPatientIdByName(conn, medicalRecord.getPatientName());
                    if (patientId > 0) {
                        medicalRecord.setPatientId(patientId);
                        System.out.println("Found patient ID " + patientId + " for name: " + medicalRecord.getPatientName());
                    } else {
                        // If no patient found, use default patient ID 1 (John Doe)
                        medicalRecord.setPatientId(1);
                        System.out.println("Using default patient ID 1");
                    }
                } else {
                    // No patient name, use default
                    medicalRecord.setPatientId(1);
                    System.out.println("Using default patient ID 1 due to missing patient information");
                }
            }
            
            // Check for valid doctor ID (required constraint)
            if (medicalRecord.getDoctorId() <= 0) {
                // Try to find doctor ID by name
                if (medicalRecord.getDoctorName() != null && !medicalRecord.getDoctorName().trim().isEmpty()) {
                    int doctorId = findDoctorIdByName(conn, medicalRecord.getDoctorName());
                    if (doctorId > 0) {
                        medicalRecord.setDoctorId(doctorId);
                        System.out.println("Found doctor ID " + doctorId + " for name: " + medicalRecord.getDoctorName());
                    } else {
                        // If no doctor found, use default doctor ID 2 (Dr. John Smith)
                        medicalRecord.setDoctorId(2);
                        System.out.println("Using default doctor ID 2");
                    }
                } else {
                    // No doctor name, use default
                    medicalRecord.setDoctorId(2);
                    System.out.println("Using default doctor ID 2 due to missing doctor information");
                }
            }
            
            // Ensure visit_type has a value (DEFAULT clause doesn't exist in table)
            if (medicalRecord.getVisitType() == null || medicalRecord.getVisitType().trim().isEmpty()) {
                medicalRecord.setVisitType("Regular visit");
                System.out.println("Using default visit type: Regular visit");
            }
            
            // Ensure visit_date has a value
            if (medicalRecord.getVisitDate() == null) {
                medicalRecord.setVisitDate(LocalDateTime.now());
                System.out.println("Using current date/time for visit date");
            }
            
            // Ensure status has a value
            if (medicalRecord.getStatus() == null || medicalRecord.getStatus().trim().isEmpty()) {
                medicalRecord.setStatus("Pending");
                System.out.println("Using default status: Pending");
            }
            
            // Prepare SQL statement with explicit column names
            String sql = "INSERT INTO medical_records (patient_id, patient_name, doctor_id, doctor_name, " +
                         "visit_type, visit_date, symptoms, diagnosis, treatment, notes, status, " +
                         "diagnosis_codes, attachments, follow_up_date, record_type) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                // Set patient_id (NOT NULL field)
                stmt.setInt(1, medicalRecord.getPatientId());
                
                // Set patient_name (can be NULL)
                if (medicalRecord.getPatientName() != null) {
                    stmt.setString(2, medicalRecord.getPatientName());
                } else {
                    stmt.setNull(2, Types.VARCHAR);
                }
                
                // Set doctor_id (NOT NULL field)
                stmt.setInt(3, medicalRecord.getDoctorId());
                
                // Set doctor_name (can be NULL)
                if (medicalRecord.getDoctorName() != null) {
                    stmt.setString(4, medicalRecord.getDoctorName());
                } else {
                    stmt.setNull(4, Types.VARCHAR);
                }
                
                // Set visit_type
                if (medicalRecord.getVisitType() != null) {
                    stmt.setString(5, medicalRecord.getVisitType());
                } else {
                    stmt.setString(5, "Regular visit"); // Default value
                }
                
                // Set visit_date (NOT NULL field)
                if (medicalRecord.getVisitDate() != null) {
                    stmt.setTimestamp(6, Timestamp.valueOf(medicalRecord.getVisitDate()));
                } else {
                    stmt.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
                }
                
                // Set optional fields, handling null values properly
                setStringOrNull(stmt, 7, medicalRecord.getSymptoms());
                setStringOrNull(stmt, 8, medicalRecord.getDiagnosis());
                setStringOrNull(stmt, 9, medicalRecord.getTreatment());
                setStringOrNull(stmt, 10, medicalRecord.getNotes());
                
                // Set status with default if null
                if (medicalRecord.getStatus() != null && !medicalRecord.getStatus().isEmpty()) {
                    stmt.setString(11, medicalRecord.getStatus());
                } else {
                    stmt.setString(11, "Pending"); // Default value
                }
                
                setStringOrNull(stmt, 12, medicalRecord.getDiagnosisCodes());
                setStringOrNull(stmt, 13, medicalRecord.getAttachments());
                
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
                
                // Execute the insert
                int affectedRows = stmt.executeUpdate();
                
                if (affectedRows == 0) {
                    System.err.println("Creating medical record failed, no rows affected.");
                    return false;
                }
                
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int newId = generatedKeys.getInt(1);
                        medicalRecord.setRecordId(newId);
                        System.out.println("Created record with ID: " + newId);
                        return true;
                    } else {
                        System.err.println("Creating medical record failed, no ID obtained.");
                        return false;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL Error creating medical record: " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            System.err.println("Unexpected error creating medical record: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Helper method to set a string value or NULL in a PreparedStatement
     */
    private void setStringOrNull(PreparedStatement stmt, int parameterIndex, String value) throws SQLException {
        if (value != null) {
            stmt.setString(parameterIndex, value);
        } else {
            stmt.setNull(parameterIndex, Types.VARCHAR);
        }
    }
    
    /**
     * Find a patient ID by name
     */
    private int findPatientIdByName(Connection conn, String patientName) {
        try {
            String sql = "SELECT patient_id FROM patients WHERE full_name LIKE ? OR " +
                         "CONCAT(first_name, ' ', last_name) LIKE ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                String searchPattern = "%" + patientName + "%";
                stmt.setString(1, searchPattern);
                stmt.setString(2, searchPattern);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("patient_id");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding patient ID: " + e.getMessage());
        }
        return -1;
    }
    
    /**
     * Find a doctor ID by name
     */
    private int findDoctorIdByName(Connection conn, String doctorName) {
        try {
            String sql = "SELECT user_id FROM users WHERE full_name LIKE ? AND role = 'DOCTOR'";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, "%" + doctorName + "%");
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("user_id");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding doctor ID: " + e.getMessage());
        }
        return -1;
    }
    
    /**
     * Update an existing medical record in the database
     * 
     * @param medicalRecord The medical record to update
     * @return true if successful, false otherwise
     */
    public boolean updateMedicalRecord(MedicalRecord medicalRecord) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Debug info
            System.out.println("Attempting to update medical record ID " + medicalRecord.getRecordId() + " with:");
            System.out.println("- Patient ID: " + medicalRecord.getPatientId());
            System.out.println("- Patient Name: " + medicalRecord.getPatientName());
            System.out.println("- Doctor ID: " + medicalRecord.getDoctorId());
            System.out.println("- Doctor Name: " + medicalRecord.getDoctorName());
            System.out.println("- Visit Type: " + medicalRecord.getVisitType());
            System.out.println("- Visit Date: " + (medicalRecord.getVisitDate() != null ? medicalRecord.getVisitDate() : "null"));
            
            // Check for valid record ID
            if (medicalRecord.getRecordId() <= 0) {
                System.err.println("Cannot update record with invalid ID: " + medicalRecord.getRecordId());
                return false;
            }
            
            // Check for valid patient ID (required constraint)
            if (medicalRecord.getPatientId() <= 0) {
                // Try to find patient ID by name
                if (medicalRecord.getPatientName() != null && !medicalRecord.getPatientName().trim().isEmpty()) {
                    int patientId = findPatientIdByName(conn, medicalRecord.getPatientName());
                    if (patientId > 0) {
                        medicalRecord.setPatientId(patientId);
                        System.out.println("Found patient ID " + patientId + " for name: " + medicalRecord.getPatientName());
                    } else {
                        // If no patient found, use default patient ID 1 (John Doe)
                        medicalRecord.setPatientId(1);
                        System.out.println("Using default patient ID 1");
                    }
                } else {
                    // No patient name, use default
                    medicalRecord.setPatientId(1);
                    System.out.println("Using default patient ID 1 due to missing patient information");
                }
            }
            
            // Check for valid doctor ID (required constraint)
            if (medicalRecord.getDoctorId() <= 0) {
                // Try to find doctor ID by name
                if (medicalRecord.getDoctorName() != null && !medicalRecord.getDoctorName().trim().isEmpty()) {
                    int doctorId = findDoctorIdByName(conn, medicalRecord.getDoctorName());
                    if (doctorId > 0) {
                        medicalRecord.setDoctorId(doctorId);
                        System.out.println("Found doctor ID " + doctorId + " for name: " + medicalRecord.getDoctorName());
                    } else {
                        // If no doctor found, use default doctor ID 2 (Dr. John Smith)
                        medicalRecord.setDoctorId(2);
                        System.out.println("Using default doctor ID 2");
                    }
                } else {
                    // No doctor name, use default
                    medicalRecord.setDoctorId(2);
                    System.out.println("Using default doctor ID 2 due to missing doctor information");
                }
            }
            
            // Ensure visit_type has a value (DEFAULT clause doesn't exist in table)
            if (medicalRecord.getVisitType() == null || medicalRecord.getVisitType().trim().isEmpty()) {
                medicalRecord.setVisitType("Regular visit");
                System.out.println("Using default visit type: Regular visit");
            }
            
            // Ensure visit_date has a value
            if (medicalRecord.getVisitDate() == null) {
                medicalRecord.setVisitDate(LocalDateTime.now());
                System.out.println("Using current date/time for visit date");
            }
            
            // Ensure status has a value
            if (medicalRecord.getStatus() == null || medicalRecord.getStatus().trim().isEmpty()) {
                medicalRecord.setStatus("Pending");
                System.out.println("Using default status: Pending");
            }
            
            String sql = "UPDATE medical_records SET patient_id = ?, patient_name = ?, " +
                         "doctor_id = ?, doctor_name = ?, visit_type = ?, visit_date = ?, " +
                         "symptoms = ?, diagnosis = ?, treatment = ?, notes = ?, status = ?, " +
                         "diagnosis_codes = ?, attachments = ?, follow_up_date = ?, record_type = ? " +
                         "WHERE record_id = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                // Set patient_id (NOT NULL field)
                stmt.setInt(1, medicalRecord.getPatientId());
                
                // Set patient_name (can be NULL)
                if (medicalRecord.getPatientName() != null) {
                    stmt.setString(2, medicalRecord.getPatientName());
                } else {
                    stmt.setNull(2, Types.VARCHAR);
                }
                
                // Set doctor_id (NOT NULL field)
                stmt.setInt(3, medicalRecord.getDoctorId());
                
                // Set doctor_name (can be NULL)
                if (medicalRecord.getDoctorName() != null) {
                    stmt.setString(4, medicalRecord.getDoctorName());
                } else {
                    stmt.setNull(4, Types.VARCHAR);
                }
                
                // Set visit_type
                if (medicalRecord.getVisitType() != null) {
                    stmt.setString(5, medicalRecord.getVisitType());
                } else {
                    stmt.setString(5, "Regular visit"); // Default value
                }
                
                // Set visit_date (NOT NULL field)
                if (medicalRecord.getVisitDate() != null) {
                    stmt.setTimestamp(6, Timestamp.valueOf(medicalRecord.getVisitDate()));
                } else {
                    stmt.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
                }
                
                // Set optional fields, handling null values properly
                setStringOrNull(stmt, 7, medicalRecord.getSymptoms());
                setStringOrNull(stmt, 8, medicalRecord.getDiagnosis());
                setStringOrNull(stmt, 9, medicalRecord.getTreatment());
                setStringOrNull(stmt, 10, medicalRecord.getNotes());
                
                // Set status with default if null
                if (medicalRecord.getStatus() != null && !medicalRecord.getStatus().isEmpty()) {
                    stmt.setString(11, medicalRecord.getStatus());
                } else {
                    stmt.setString(11, "Pending"); // Default value
                }
                
                setStringOrNull(stmt, 12, medicalRecord.getDiagnosisCodes());
                setStringOrNull(stmt, 13, medicalRecord.getAttachments());
                
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
                
                // Set record_id for WHERE clause
                stmt.setInt(16, medicalRecord.getRecordId());
                
                // Execute the update
                int affectedRows = stmt.executeUpdate();
                System.out.println("Updated record ID: " + medicalRecord.getRecordId() + ", Affected rows: " + affectedRows);
                
                if (affectedRows > 0) {
                    return true;
                } else {
                    System.err.println("No record found with ID: " + medicalRecord.getRecordId());
                    return false;
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL Error updating medical record: " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            System.err.println("Unexpected error updating medical record: " + e.getMessage());
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
     * Get filtered medical records based on various criteria
     */
    public List<MedicalRecord> getFilteredMedicalRecords(String patientName, String recordType, 
                                                        String status, Date startDate, Date endDate, 
                                                        Integer doctorId, int offset, int limit) {
        List<MedicalRecord> records = new ArrayList<>();
        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM medical_records WHERE 1=1");
        List<Object> params = new ArrayList<>();
        
        System.out.println("Building SQL filter query:");
        
        if (patientName != null && !patientName.isEmpty()) {
            sqlBuilder.append(" AND patient_name LIKE ?");
            params.add("%" + patientName + "%");
            System.out.println("- Added patient name filter: " + patientName);
        }
        
        if (recordType != null && !recordType.isEmpty() && !"All Records".equals(recordType)) {
            // Handle "Recent Records" as a special case
            if ("Recent Records".equals(recordType)) {
                sqlBuilder.append(" AND visit_date >= ?");
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DAY_OF_MONTH, -7);
                params.add(new Date(cal.getTimeInMillis()));
                System.out.println("- Added recent records filter (last 7 days)");
            } else {
                // Normal record type filtering
                sqlBuilder.append(" AND (visit_type = ? OR record_type = ?)");
                params.add(recordType);
                params.add(recordType);
                System.out.println("- Added record type filter: " + recordType);
            }
        }
        
        if (status != null && !status.isEmpty() && !"All Statuses".equals(status)) {
            sqlBuilder.append(" AND status = ?");
            params.add(status);
            System.out.println("- Added status filter: " + status);
        }
        
        if (startDate != null) {
            // For single date filtering (when only startDate is provided),
            // ensure we get the entire day by using >= date 00:00:00 and < date+1 00:00:00
            if (endDate == null) {
                sqlBuilder.append(" AND DATE(visit_date) = DATE(?)");
                params.add(startDate);
                System.out.println("- Added date filter for specific day: " + startDate);
            } else {
                sqlBuilder.append(" AND visit_date >= ?");
                params.add(startDate);
                System.out.println("- Added start date filter: " + startDate);
            }
        }
        
        if (endDate != null) {
            // Only used if this is a date range query, not a single day query
            if (startDate != null) {
                sqlBuilder.append(" AND visit_date <= ?");
                params.add(endDate);
                System.out.println("- Added end date filter: " + endDate);
            }
        }
        
        // Add doctor ID filter if provided
        if (doctorId != null && doctorId > 0) {
            sqlBuilder.append(" AND doctor_id = ?");
            params.add(doctorId);
            System.out.println("- Added doctor filter: " + doctorId);
        }
        
        sqlBuilder.append(" ORDER BY visit_date DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);
        
        System.out.println("Final SQL query: " + sqlBuilder.toString());
        
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
                
                System.out.println("Query returned " + records.size() + " records");
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
        
        System.out.println("Converting UI record to database format:");
        
        // Parse ID (remove "MR-" prefix if present)
        String id = controllerRecord.getId();
        System.out.println("- Original ID: " + id);
        
        if (id != null) {
            if (id.startsWith("MR-")) {
                try {
                    dbRecord.setRecordId(Integer.parseInt(id.substring(3)));
                } catch (NumberFormatException e) {
                    // Generate a new ID if parsing fails
                    dbRecord.setRecordId(0);
                    System.out.println("  Could not parse ID, will generate new record");
                }
            } else {
                try {
                    dbRecord.setRecordId(Integer.parseInt(id));
                } catch (NumberFormatException e) {
                    // Generate a new ID if parsing fails
                    dbRecord.setRecordId(0);
                    System.out.println("  Could not parse ID, will generate new record");
                }
            }
        } else {
            dbRecord.setRecordId(0); // New record
            System.out.println("  Null ID, will generate new record");
        }
        
        System.out.println("- DB Record ID set to: " + dbRecord.getRecordId());
        
        // Handle patient ID and name
        try {
            Integer patientId = controllerRecord.getPatientId();
            if (patientId != null && patientId > 0) {
                dbRecord.setPatientId(patientId);
                System.out.println("- Using provided patient ID: " + patientId);
            } else {
                // Will be resolved during save/update
                dbRecord.setPatientId(0);
                System.out.println("- No valid patient ID provided");
            }
        } catch (Exception e) {
            dbRecord.setPatientId(0);
            System.out.println("- Error getting patient ID: " + e.getMessage());
        }
        
        // Set patient name
        String patientName = controllerRecord.getPatient();
        dbRecord.setPatientName(patientName);
        System.out.println("- Patient name: " + (patientName != null ? patientName : "null"));
        
        // Handle doctor ID and name
        try {
            Integer doctorId = controllerRecord.getDoctorId();
            if (doctorId != null && doctorId > 0) {
                dbRecord.setDoctorId(doctorId);
                System.out.println("- Using provided doctor ID: " + doctorId);
            } else {
                // Will be resolved during save/update
                dbRecord.setDoctorId(0);
                System.out.println("- No valid doctor ID provided");
            }
        } catch (Exception e) {
            dbRecord.setDoctorId(0);
            System.out.println("- Error getting doctor ID: " + e.getMessage());
        }
        
        // Set doctor name
        String doctorName = controllerRecord.getDoctor();
        dbRecord.setDoctorName(doctorName);
        System.out.println("- Doctor name: " + (doctorName != null ? doctorName : "null"));
        
        // Set type, status, notes, etc. with null checks
        String type = controllerRecord.getType();
        dbRecord.setVisitType(type != null ? type : "Regular visit");
        System.out.println("- Visit type: " + dbRecord.getVisitType());
        
        String status = controllerRecord.getStatus();
        dbRecord.setStatus(status != null ? status : "Pending");
        System.out.println("- Status: " + dbRecord.getStatus());
        
        // Handle optional fields
        dbRecord.setNotes(controllerRecord.getNotes());
        dbRecord.setDiagnosisCodes(controllerRecord.getDiagnosisCodes());
        dbRecord.setAttachments(controllerRecord.getAttachments());
        dbRecord.setSymptoms(controllerRecord.getSymptoms());
        dbRecord.setDiagnosis(controllerRecord.getDiagnosis());
        dbRecord.setTreatment(controllerRecord.getTreatment());
        
        // For compatibility with both tables - visit_type and record_type are the same
        dbRecord.setRecordType(type != null ? type : "Regular visit");
        
        // Parse date with error handling
        try {
            String dateStr = controllerRecord.getDate();
            if (dateStr != null && !dateStr.trim().isEmpty()) {
                try {
                    LocalDateTime visitDate = LocalDateTime.parse(dateStr + "T00:00:00");
                    dbRecord.setVisitDate(visitDate);
                    System.out.println("- Parsed visit date: " + visitDate);
                } catch (Exception e) {
                    System.out.println("- Error parsing date format '" + dateStr + "', trying alternative formats");
                    
                    // Try alternative formats
                    try {
                        // Try to parse as MM/dd/yyyy
                        DateTimeFormatter alternateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
                        LocalDate date = LocalDate.parse(dateStr, alternateFormatter);
                        dbRecord.setVisitDate(date.atStartOfDay());
                        System.out.println("- Parsed alternative date format: " + date);
                    } catch (Exception e2) {
                        // Default to current date/time
                        dbRecord.setVisitDate(LocalDateTime.now());
                        System.out.println("- Defaulting to current date/time: " + dbRecord.getVisitDate());
                    }
                }
            } else {
                // Default to current date/time if date is null or empty
                dbRecord.setVisitDate(LocalDateTime.now());
                System.out.println("- No date provided, using current date/time: " + dbRecord.getVisitDate());
            }
        } catch (Exception e) {
            // Default to current date/time for any error
            dbRecord.setVisitDate(LocalDateTime.now());
            System.out.println("- Error handling date, using current date/time: " + dbRecord.getVisitDate());
        }
        
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