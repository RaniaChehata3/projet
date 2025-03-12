package com.example.demo.model;

import java.sql.Timestamp;

/**
 * Represents a user in the SahaCare system.
 */
public class User {
    private int userId;
    private String username;
    private String email;
    private String password; // Stored as hashed in database
    private String salt; // Added salt field for password hashing
    private UserRole role;
    private String fullName;
    private String phone;
    private String address;
    private String city; // New field for city
    private Timestamp registrationDate;
    private Timestamp lastLogin;
    private boolean active;
    
    // Default constructor
    public User() {
    }
    
    // Constructor with essential fields
    public User(String username, String email, String password, String salt, UserRole role, String fullName) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.salt = salt;
        this.role = role;
        this.fullName = fullName;
        this.active = true;
    }
    
    // Constructor with phone and city
    public User(String username, String email, String password, String salt, UserRole role, 
               String fullName, String phone, String city) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.salt = salt;
        this.role = role;
        this.fullName = fullName;
        this.phone = phone;
        this.city = city;
        this.active = true;
    }

    // Full constructor
    public User(int userId, String username, String email, String password, String salt, UserRole role, 
                String fullName, String phone, String address, String city, Timestamp registrationDate, 
                Timestamp lastLogin, boolean active) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.password = password;
        this.salt = salt;
        this.role = role;
        this.fullName = fullName;
        this.phone = phone;
        this.address = address;
        this.city = city;
        this.registrationDate = registrationDate;
        this.lastLogin = lastLogin;
        this.active = active;
    }

    // Original full constructor (without city and salt) for backward compatibility
    public User(int userId, String username, String email, String password, UserRole role, 
                String fullName, String phone, String address, Timestamp registrationDate, 
                Timestamp lastLogin, boolean active) {
        this(userId, username, email, password, "salt123", role, fullName, phone, address, "", 
             registrationDate, lastLogin, active);
    }

    // Getters and Setters
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Timestamp getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(Timestamp registrationDate) {
        this.registrationDate = registrationDate;
    }

    public Timestamp getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Timestamp lastLogin) {
        this.lastLogin = lastLogin;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", fullName='" + fullName + '\'' +
                '}';
    }
} 