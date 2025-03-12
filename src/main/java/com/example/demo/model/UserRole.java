package com.example.demo.model;

/**
 * Enum representing the different user roles in the SahaCare system.
 */
public enum UserRole {
    ADMIN,
    DOCTOR,
    PATIENT,
    LABORATORY;
    
    /**
     * Convert a string to the corresponding UserRole enum value
     * 
     * @param role The role string to convert
     * @return The matching UserRole enum value
     */
    public static UserRole fromString(String role) {
        try {
            return UserRole.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + role);
        }
    }
} 