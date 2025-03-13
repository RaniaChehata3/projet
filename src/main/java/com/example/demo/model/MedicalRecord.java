package com.example.demo.model;

import java.time.LocalDateTime;

/**
 * Model class representing a medical record for a patient
 */
public class MedicalRecord {
    private int recordId;
    private int patientId;
    private String patientName;
    private int doctorId;
    private String doctorName;
    private String visitType;
    private LocalDateTime visitDate;
    private String symptoms;
    private String diagnosis;
    private String treatment;
    private String notes;
    private String status;
    private String diagnosisCodes;
    private String attachments;
    private LocalDateTime followUpDate;
    private String recordType;

    /**
     * Default constructor
     */
    public MedicalRecord() {
        this.doctorId = 0;
        this.visitDate = LocalDateTime.now();
        this.status = "Pending";
    }

    /**
     * Constructor with essential fields
     * 
     * @param patientId The patient ID
     * @param doctorId The doctor ID
     * @param visitType The type of visit
     */
    public MedicalRecord(int patientId, int doctorId, String visitType) {
        this();
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.visitType = visitType;
    }

    /**
     * Constructor compatible with the controller's string-based structure
     * 
     * @param id The record ID as string
     * @param patient The patient name
     * @param date The visit date as string
     * @param type The visit type
     * @param doctor The doctor name
     * @param status The status of the record
     * @param notes Additional notes
     * @param diagnosisCodes Diagnosis codes
     * @param attachments File attachments
     */
    public MedicalRecord(String id, String patient, String date, String type, 
                         String doctor, String status, String notes, 
                         String diagnosisCodes, String attachments) {
        this.recordId = id.startsWith("MR-") ? Integer.parseInt(id.substring(3)) : Integer.parseInt(id);
        this.patientName = patient;
        // Date parsing would be handled separately
        this.visitType = type;
        this.doctorName = doctor;
        this.status = status;
        this.notes = notes;
        this.diagnosisCodes = diagnosisCodes;
        this.attachments = attachments;
    }

    /**
     * Full constructor with all fields
     * 
     * @param recordId The record ID
     * @param patientId The patient ID
     * @param patientName The patient name
     * @param doctorId The doctor ID
     * @param doctorName The doctor name
     * @param visitType The type of visit
     * @param visitDate The date and time of the visit
     * @param symptoms The patient's symptoms
     * @param diagnosis The diagnosis
     * @param treatment The treatment plan
     * @param notes Additional notes
     * @param status The status of the record
     * @param diagnosisCodes Diagnosis codes
     * @param attachments File attachments
     */
    public MedicalRecord(int recordId, int patientId, String patientName, int doctorId, 
                        String doctorName, String visitType, LocalDateTime visitDate, 
                        String symptoms, String diagnosis, String treatment, String notes,
                        String status, String diagnosisCodes, String attachments) {
        this.recordId = recordId;
        this.patientId = patientId;
        this.patientName = patientName;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.visitType = visitType;
        this.visitDate = visitDate;
        this.symptoms = symptoms;
        this.diagnosis = diagnosis;
        this.treatment = treatment;
        this.notes = notes;
        this.status = status;
        this.diagnosisCodes = diagnosisCodes;
        this.attachments = attachments;
    }

    // Getters and Setters
    public int getRecordId() {
        return recordId;
    }

    public void setRecordId(int recordId) {
        this.recordId = recordId;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getVisitType() {
        return visitType;
    }

    public void setVisitType(String visitType) {
        this.visitType = visitType;
    }

    public LocalDateTime getVisitDate() {
        return visitDate;
    }

    public void setVisitDate(LocalDateTime visitDate) {
        this.visitDate = visitDate;
    }

    public String getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(String symptoms) {
        this.symptoms = symptoms;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getTreatment() {
        return treatment;
    }

    public void setTreatment(String treatment) {
        this.treatment = treatment;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDiagnosisCodes() {
        return diagnosisCodes;
    }

    public void setDiagnosisCodes(String diagnosisCodes) {
        this.diagnosisCodes = diagnosisCodes;
    }

    public String getAttachments() {
        return attachments;
    }

    public void setAttachments(String attachments) {
        this.attachments = attachments;
    }

    public LocalDateTime getFollowUpDate() {
        return followUpDate;
    }

    public void setFollowUpDate(LocalDateTime followUpDate) {
        this.followUpDate = followUpDate;
    }

    public String getRecordType() {
        return recordType;
    }

    public void setRecordType(String recordType) {
        this.recordType = recordType;
    }

    // For compatibility with the controller
    public String getId() {
        return "MR-" + recordId;
    }

    public String getPatient() {
        return patientName;
    }

    public String getDate() {
        if (visitDate != null) {
            return visitDate.toLocalDate().toString();
        }
        return "";
    }

    public String getType() {
        return visitType;
    }

    public String getDoctor() {
        return doctorName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MedicalRecord that = (MedicalRecord) o;
        return recordId == that.recordId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(recordId);
    }

    @Override
    public String toString() {
        return "MedicalRecord{" +
                "recordId=" + recordId +
                ", patientId=" + patientId +
                ", doctorId=" + doctorId +
                ", visitType='" + visitType + '\'' +
                ", visitDate=" + visitDate +
                ", diagnosis='" + diagnosis + '\'' +
                ", treatment='" + treatment + '\'' +
                '}';
    }
} 