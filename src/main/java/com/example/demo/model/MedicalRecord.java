package com.example.demo.model;

import java.time.LocalDateTime;

/**
 * Model class representing a medical record for a patient
 */
public class MedicalRecord {
    private int recordId;
    private int patientId;
    private int doctorId;
    private String visitType;
    private LocalDateTime visitDate;
    private String symptoms;
    private String diagnosis;
    private String treatment;
    private String notes;

    /**
     * Default constructor
     */
    public MedicalRecord() {
        this.doctorId = 0;
        this.visitDate = LocalDateTime.now();
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
     * Full constructor with all fields
     * 
     * @param recordId The record ID
     * @param patientId The patient ID
     * @param doctorId The doctor ID
     * @param visitType The type of visit
     * @param visitDate The date and time of the visit
     * @param symptoms The patient's symptoms
     * @param diagnosis The diagnosis
     * @param treatment The treatment plan
     * @param notes Additional notes
     */
    public MedicalRecord(int recordId, int patientId, int doctorId, String visitType, 
                         LocalDateTime visitDate, String symptoms, String diagnosis, 
                         String treatment, String notes) {
        this.recordId = recordId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.visitType = visitType;
        this.visitDate = visitDate;
        this.symptoms = symptoms;
        this.diagnosis = diagnosis;
        this.treatment = treatment;
        this.notes = notes;
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

    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
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