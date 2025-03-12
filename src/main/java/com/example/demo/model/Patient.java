package com.example.demo.model;

import java.time.LocalDate;
import java.time.Period;

/**
 * Model class representing a patient in the healthcare system
 */
public class Patient {
    private int patientId;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String gender;
    private String address;
    private String phoneNumber;
    private String email;
    private String insuranceProvider;
    private String insuranceNumber;
    private String medicalHistory;
    private String allergies;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String bloodType;
    private LocalDate registrationDate;
    private int primaryDoctorId;
    private boolean isActive;

    /**
     * Default constructor
     */
    public Patient() {
        this.registrationDate = LocalDate.now();
        this.isActive = true;
    }

    /**
     * Constructor with essential fields
     * 
     * @param firstName The patient's first name
     * @param lastName The patient's last name
     * @param dateOfBirth The patient's date of birth
     * @param gender The patient's gender
     * @param phoneNumber The patient's phone number
     */
    public Patient(String firstName, String lastName, LocalDate dateOfBirth, String gender, String phoneNumber) {
        this();
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.phoneNumber = phoneNumber;
    }

    /**
     * Full constructor with all fields
     */
    public Patient(int patientId, String firstName, String lastName, LocalDate dateOfBirth, String gender,
                  String address, String phoneNumber, String email, String insuranceProvider,
                  String insuranceNumber, String medicalHistory, String allergies,
                  String emergencyContactName, String emergencyContactPhone, String bloodType,
                  LocalDate registrationDate, int primaryDoctorId, boolean isActive) {
        this.patientId = patientId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.insuranceProvider = insuranceProvider;
        this.insuranceNumber = insuranceNumber;
        this.medicalHistory = medicalHistory;
        this.allergies = allergies;
        this.emergencyContactName = emergencyContactName;
        this.emergencyContactPhone = emergencyContactPhone;
        this.bloodType = bloodType;
        this.registrationDate = registrationDate;
        this.primaryDoctorId = primaryDoctorId;
        this.isActive = isActive;
    }

    // Getters and Setters

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public String getFullName() {
        return firstName + " " + lastName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
    
    public int getAge() {
        if (dateOfBirth == null) {
            return 0;
        }
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getInsuranceProvider() {
        return insuranceProvider;
    }

    public void setInsuranceProvider(String insuranceProvider) {
        this.insuranceProvider = insuranceProvider;
    }

    public String getInsuranceNumber() {
        return insuranceNumber;
    }

    public void setInsuranceNumber(String insuranceNumber) {
        this.insuranceNumber = insuranceNumber;
    }

    public String getMedicalHistory() {
        return medicalHistory;
    }

    public void setMedicalHistory(String medicalHistory) {
        this.medicalHistory = medicalHistory;
    }

    public String getAllergies() {
        return allergies;
    }

    public void setAllergies(String allergies) {
        this.allergies = allergies;
    }

    public String getEmergencyContactName() {
        return emergencyContactName;
    }

    public void setEmergencyContactName(String emergencyContactName) {
        this.emergencyContactName = emergencyContactName;
    }

    public String getEmergencyContactPhone() {
        return emergencyContactPhone;
    }

    public void setEmergencyContactPhone(String emergencyContactPhone) {
        this.emergencyContactPhone = emergencyContactPhone;
    }

    public String getBloodType() {
        return bloodType;
    }

    public void setBloodType(String bloodType) {
        this.bloodType = bloodType;
    }

    public LocalDate getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDate registrationDate) {
        this.registrationDate = registrationDate;
    }

    public int getPrimaryDoctorId() {
        return primaryDoctorId;
    }

    public void setPrimaryDoctorId(int primaryDoctorId) {
        this.primaryDoctorId = primaryDoctorId;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @Override
    public String toString() {
        return "Patient{" +
                "patientId=" + patientId +
                ", name='" + firstName + " " + lastName + '\'' +
                ", age=" + getAge() +
                ", gender='" + gender + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
} 