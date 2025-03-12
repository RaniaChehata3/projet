package com.example.demo.model;

import java.sql.Date;

/**
 * Represents a user profile in the SahaCare system.
 * Contains additional information about users beyond the basic authentication data.
 */
public class UserProfile {
    private int profileId;
    private int userId;
    private Gender gender;
    private Date dateOfBirth;
    private String bloodType;
    private String medicalHistory;
    private String specialization; // For doctors
    private String licenseNumber;  // For doctors and laboratories
    private String profileImage;   // Path to profile image
    
    // Enum for gender options
    public enum Gender {
        MALE, FEMALE, OTHER;
        
        public static Gender fromString(String gender) {
            if (gender == null) return null;
            try {
                return Gender.valueOf(gender.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid gender: " + gender);
            }
        }
    }
    
    // Default constructor
    public UserProfile() {
    }
    
    // Constructor with essential fields
    public UserProfile(int userId, Gender gender, Date dateOfBirth) {
        this.userId = userId;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
    }
    
    // Full constructor
    public UserProfile(int profileId, int userId, Gender gender, Date dateOfBirth, 
                      String bloodType, String medicalHistory, String specialization, 
                      String licenseNumber, String profileImage) {
        this.profileId = profileId;
        this.userId = userId;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.bloodType = bloodType;
        this.medicalHistory = medicalHistory;
        this.specialization = specialization;
        this.licenseNumber = licenseNumber;
        this.profileImage = profileImage;
    }
    
    // Getters and Setters
    public int getProfileId() {
        return profileId;
    }
    
    public void setProfileId(int profileId) {
        this.profileId = profileId;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public Gender getGender() {
        return gender;
    }
    
    public void setGender(Gender gender) {
        this.gender = gender;
    }
    
    public Date getDateOfBirth() {
        return dateOfBirth;
    }
    
    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
    
    public String getBloodType() {
        return bloodType;
    }
    
    public void setBloodType(String bloodType) {
        this.bloodType = bloodType;
    }
    
    public String getMedicalHistory() {
        return medicalHistory;
    }
    
    public void setMedicalHistory(String medicalHistory) {
        this.medicalHistory = medicalHistory;
    }
    
    public String getSpecialization() {
        return specialization;
    }
    
    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }
    
    public String getLicenseNumber() {
        return licenseNumber;
    }
    
    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }
    
    public String getProfileImage() {
        return profileImage;
    }
    
    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
    
    @Override
    public String toString() {
        return "UserProfile{" +
                "profileId=" + profileId +
                ", userId=" + userId +
                ", gender=" + gender +
                ", dateOfBirth=" + dateOfBirth +
                ", specialization='" + specialization + '\'' +
                '}';
    }
} 