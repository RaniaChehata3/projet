package com.example.demo.model;

import java.time.LocalDate;

/**
 * Represents a medication prescribed to a patient
 */
public class Medication {
    private int medicationId;
    private int patientId;
    private int doctorId;
    private String name;
    private String dosage;
    private String frequency;
    private String instructions;
    private String purpose;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean isCurrent;
    private String sideEffects;
    private String notes;
    
    // Default constructor
    public Medication() {
        this.startDate = LocalDate.now();
        this.isCurrent = true;
    }
    
    // Constructor with basic fields
    public Medication(int patientId, int doctorId, String name, String dosage, String frequency) {
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.name = name;
        this.dosage = dosage;
        this.frequency = frequency;
        this.startDate = LocalDate.now();
        this.isCurrent = true;
    }
    
    // Full constructor
    public Medication(int medicationId, int patientId, int doctorId, String name, String dosage,
                     String frequency, String instructions, String purpose, LocalDate startDate,
                     LocalDate endDate, boolean isCurrent, String sideEffects, String notes) {
        this.medicationId = medicationId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.name = name;
        this.dosage = dosage;
        this.frequency = frequency;
        this.instructions = instructions;
        this.purpose = purpose;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isCurrent = isCurrent;
        this.sideEffects = sideEffects;
        this.notes = notes;
    }
    
    // Getters and Setters
    public int getMedicationId() {
        return medicationId;
    }
    
    public void setMedicationId(int medicationId) {
        this.medicationId = medicationId;
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
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDosage() {
        return dosage;
    }
    
    public void setDosage(String dosage) {
        this.dosage = dosage;
    }
    
    public String getFrequency() {
        return frequency;
    }
    
    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }
    
    public String getInstructions() {
        return instructions;
    }
    
    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }
    
    public String getPurpose() {
        return purpose;
    }
    
    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }
    
    public LocalDate getStartDate() {
        return startDate;
    }
    
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }
    
    public LocalDate getEndDate() {
        return endDate;
    }
    
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
    
    public boolean isCurrent() {
        return isCurrent;
    }
    
    public void setCurrent(boolean current) {
        isCurrent = current;
    }
    
    public String getSideEffects() {
        return sideEffects;
    }
    
    public void setSideEffects(String sideEffects) {
        this.sideEffects = sideEffects;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    @Override
    public String toString() {
        return name + " " + dosage + " " + frequency;
    }
} 