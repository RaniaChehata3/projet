package com.example.demo.model;

import java.time.LocalDateTime;

/**
 * Model class representing a laboratory test result for a patient
 */
public class LabResult {
    private int resultId;
    private int patientId;
    private int doctorId;
    private int labTechId;
    private String testName;
    private String testType;
    private String result;
    private String normalRange;
    private String unit;
    private boolean isAbnormal;
    private LocalDateTime testDate;
    private LocalDateTime resultDate;
    private String notes;
    private String status;
    private boolean isUrgent;

    /**
     * Default constructor
     */
    public LabResult() {
        this.doctorId = 0;
        this.labTechId = 0;
        this.isAbnormal = false;
        this.isUrgent = false;
        this.status = "Pending";
    }

    /**
     * Constructor with essential fields
     * 
     * @param patientId The patient ID
     * @param testName The name of the lab test
     * @param testType The type of the test
     */
    public LabResult(int patientId, String testName, String testType) {
        this();
        this.patientId = patientId;
        this.testName = testName;
        this.testType = testType;
        this.testDate = LocalDateTime.now();
    }

    /**
     * Full constructor with all fields
     * 
     * @param resultId The result ID
     * @param patientId The patient ID
     * @param doctorId The ordering doctor ID
     * @param labTechId The lab technician ID
     * @param testName The name of the lab test
     * @param testType The type of the test
     * @param result The result value
     * @param normalRange The normal range for this test
     * @param unit The unit of measurement
     * @param isAbnormal Whether the result is abnormal
     * @param testDate The date the test was performed
     * @param resultDate The date the result was available
     * @param notes Additional notes about the test
     * @param status The status of the lab result
     * @param isUrgent Whether the result is marked as urgent
     */
    public LabResult(int resultId, int patientId, int doctorId, int labTechId, String testName, 
                    String testType, String result, String normalRange, String unit, boolean isAbnormal,
                    LocalDateTime testDate, LocalDateTime resultDate, String notes, String status, boolean isUrgent) {
        this.resultId = resultId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.labTechId = labTechId;
        this.testName = testName;
        this.testType = testType;
        this.result = result;
        this.normalRange = normalRange;
        this.unit = unit;
        this.isAbnormal = isAbnormal;
        this.testDate = testDate;
        this.resultDate = resultDate;
        this.notes = notes;
        this.status = status;
        this.isUrgent = isUrgent;
    }

    // Getters and Setters

    public int getResultId() {
        return resultId;
    }

    public void setResultId(int resultId) {
        this.resultId = resultId;
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

    public int getLabTechId() {
        return labTechId;
    }

    public void setLabTechId(int labTechId) {
        this.labTechId = labTechId;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public String getTestType() {
        return testType;
    }

    public void setTestType(String testType) {
        this.testType = testType;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getNormalRange() {
        return normalRange;
    }

    public void setNormalRange(String normalRange) {
        this.normalRange = normalRange;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public boolean isAbnormal() {
        return isAbnormal;
    }

    public void setAbnormal(boolean abnormal) {
        this.isAbnormal = abnormal;
    }

    public LocalDateTime getTestDate() {
        return testDate;
    }

    public void setTestDate(LocalDateTime testDate) {
        this.testDate = testDate;
    }

    public LocalDateTime getResultDate() {
        return resultDate;
    }

    public void setResultDate(LocalDateTime resultDate) {
        this.resultDate = resultDate;
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

    public boolean isUrgent() {
        return isUrgent;
    }

    public void setUrgent(boolean urgent) {
        this.isUrgent = urgent;
    }

    @Override
    public String toString() {
        return "LabResult{" +
                "resultId=" + resultId +
                ", patientId=" + patientId +
                ", testName='" + testName + '\'' +
                ", testType='" + testType + '\'' +
                ", result='" + result + '\'' +
                ", status='" + status + '\'' +
                ", isUrgent=" + isUrgent +
                '}';
    }
} 