package com.example.demo.auth;

import com.example.demo.database.DatabaseConnection;
import com.example.demo.database.UserDAO;
import com.example.demo.model.User;
import com.example.demo.model.UserRole;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.*;
import java.util.Base64;
import java.util.Optional;

/**
 * Service responsible for user authentication in the SahaCare system.
 * Handles login, logout, password hashing, and session management.
 */
public class AuthService {
    
    private static AuthService instance;
    private User currentUser;
    private String currentSessionId;
    private final UserDAO userDAO = new UserDAO();
    private final SessionManager sessionManager = SessionManager.getInstance();
    
    // Private constructor for singleton pattern
    private AuthService() {
    }
    
    // Singleton pattern
    public static synchronized AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
        }
        return instance;
    }
    
    /**
     * Attempt to log in a user with the given credentials
     * 
     * @param usernameOrEmail The username or email to log in with
     * @param password The password to authenticate with
     * @return An Optional containing the logged-in User if successful, or empty if authentication failed
     */
    public Optional<User> login(String usernameOrEmail, String password) {
        // First record the login attempt (for security auditing)
        recordLoginAttempt(usernameOrEmail, false);
        
        // Try to find the user by username or email
        Optional<User> userOptional = userDAO.getUserByUsername(usernameOrEmail);
        
        if (!userOptional.isPresent()) {
            // If not found by username, try by email
            userOptional = userDAO.getUserByEmail(usernameOrEmail);
        }
        
        // If user found, check password
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            
            boolean passwordMatches = false;
            
            // Check if this is an old MD5 password (the ones from the existing database)
            if (user.getPassword().length() == 32 && user.getSalt().equals("salt123")) {
                // For legacy users with MD5 hashed passwords
                String md5Hash = md5Hash(password);
                passwordMatches = md5Hash.equals(user.getPassword());
                
                // Optional: Upgrade password to new format if matched
                if (passwordMatches) {
                    upgradePasswordHash(user, password);
                }
            } else {
                // For new users with SHA-256 + salt
                String hashedPassword = hashPassword(password, user.getSalt());
                passwordMatches = hashedPassword.equals(user.getPassword());
            }
            
            if (passwordMatches && user.isActive()) {
                // Update last login time
                updateLastLogin(user.getUserId());
                
                // Update login attempt record to success
                recordLoginAttempt(usernameOrEmail, true);
                
                // Store the current user in the service
                this.currentUser = user;
                
                return Optional.of(user);
            }
        }
        
        return Optional.empty();
    }
    
    /**
     * Log out the current user
     */
    public void logout() {
        if (currentSessionId != null) {
            // Remove the session if it exists
            sessionManager.removeSession(currentSessionId);
            currentSessionId = null;
        }
        
        this.currentUser = null;
    }
    
    /**
     * Set the current session ID
     * 
     * @param sessionId The session ID to set
     */
    public void setCurrentSessionId(String sessionId) {
        this.currentSessionId = sessionId;
    }
    
    /**
     * Get the current session ID
     * 
     * @return The current session ID
     */
    public String getCurrentSessionId() {
        return currentSessionId;
    }
    
    /**
     * Try to resume a session from a session ID
     * 
     * @param sessionId The session ID to resume
     * @return true if the session was resumed successfully, false otherwise
     */
    public boolean resumeSession(String sessionId) {
        Optional<User> userOptional = sessionManager.validateSession(sessionId);
        
        if (userOptional.isPresent()) {
            this.currentUser = userOptional.get();
            this.currentSessionId = sessionId;
            return true;
        }
        
        return false;
    }
    
    /**
     * Get the currently logged-in user
     * 
     * @return An Optional containing the current User if logged in, or empty if no user is logged in
     */
    public Optional<User> getCurrentUser() {
        return Optional.ofNullable(currentUser);
    }
    
    /**
     * Check if the current user has a specific role
     * 
     * @param role The role to check for
     * @return true if the current user has the specified role, false otherwise
     */
    public boolean hasRole(UserRole role) {
        return currentUser != null && currentUser.getRole() == role;
    }
    
    /**
     * Register a new user
     * 
     * @param username The username for the new user
     * @param email The email for the new user
     * @param password The password for the new user
     * @param role The role for the new user
     * @param fullName The full name of the new user
     * @param phoneNumber The phone number of the new user
     * @param city The city of the new user
     * @return An Optional containing the newly registered user, or empty if registration failed
     */
    public Optional<User> registerUser(String username, String email, String password, UserRole role, 
                                     String fullName, String phoneNumber, String city) {
        // Check if username already exists
        if (userDAO.getUserByUsername(username).isPresent()) {
            return Optional.empty();
        }
        
        // Check if email already exists
        if (userDAO.getUserByEmail(email).isPresent()) {
            return Optional.empty();
        }
        
        // Generate a salt for password hashing
        String salt = generateSalt();
        
        // Hash the password with the salt
        String hashedPassword = hashPassword(password, salt);
        
        // Create new user with hashed password and salt
        User newUser = new User(username, email, hashedPassword, salt, role, fullName, phoneNumber, city);
        
        // Save to database
        return userDAO.createUser(newUser);
    }
    
    /**
     * Register a new user with base information (for backward compatibility)
     */
    public Optional<User> registerUser(String username, String email, String password, UserRole role, String fullName) {
        // Default values for new fields
        return registerUser(username, email, password, role, fullName, "", "");
    }
    
    /**
     * Register a new user with a role string and additional fields
     * 
     * @param username The username for the new user
     * @param password The password for the new user
     * @param fullName The full name of the new user
     * @param email The email for the new user
     * @param roleString The role string for the new user (Patient, Doctor, Admin)
     * @param phoneNumber The phone number of the new user
     * @param city The city of the new user
     * @return true if registration was successful, false otherwise
     */
    public boolean registerUser(String username, String password, String fullName, String email, 
                              String roleString, String phoneNumber, String city) {
        try {
            // Convert the role string to UserRole enum
            UserRole role = UserRole.valueOf(roleString.toUpperCase());
            
            // Call the main registration method
            Optional<User> result = registerUser(username, email, password, role, fullName, phoneNumber, city);
            
            // Return true if a user was created
            return result.isPresent();
        } catch (IllegalArgumentException e) {
            // Invalid role string
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Register a new user with a role string (for backward compatibility)
     */
    public boolean registerUser(String username, String password, String fullName, String email, String roleString) {
        return registerUser(username, password, fullName, email, roleString, "", "");
    }
    
    /**
     * Hash a password with a salt using SHA-256
     * Note: In a production application, you should use a more secure algorithm like bcrypt
     * 
     * @param password The password to hash
     * @param salt The salt to use
     * @return The hashed password
     */
    private String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes());
            byte[] hashedPassword = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashedPassword);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
    
    /**
     * Generate a random salt for password hashing
     * 
     * @return A random salt as a Base64 encoded string
     */
    private String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }
    
    /**
     * Update a user's last login timestamp
     * 
     * @param userId The ID of the user to update
     */
    private void updateLastLogin(int userId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "UPDATE users SET last_login = CURRENT_TIMESTAMP WHERE user_id = ?";
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Record a login attempt for security audit purposes
     * 
     * @param username The username that was used in the attempt
     * @param success Whether the login attempt was successful
     */
    private void recordLoginAttempt(String username, boolean success) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // First check if the login_attempts table exists to avoid errors
            boolean tableExists = false;
            try {
                DatabaseMetaData dbm = conn.getMetaData();
                ResultSet tables = dbm.getTables(null, null, "login_attempts", null);
                tableExists = tables.next();
            } catch (SQLException e) {
                System.err.println("Error checking if login_attempts table exists: " + e.getMessage());
                return; // Exit early if we can't even check for the table
            }
            
            // Create table if it doesn't exist
            if (!tableExists) {
                try {
                    System.out.println("Creating login_attempts table for security auditing...");
                    String createTable = "CREATE TABLE IF NOT EXISTS login_attempts (" +
                                        "attempt_id INT AUTO_INCREMENT PRIMARY KEY, " +
                                        "username VARCHAR(50) NOT NULL, " +
                                        "success BOOLEAN NOT NULL, " +
                                        "attempt_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                                        "ip_address VARCHAR(50))";
                    
                    conn.createStatement().execute(createTable);
                    // Check if table was created successfully
                    DatabaseMetaData dbm = conn.getMetaData();
                    ResultSet tables = dbm.getTables(null, null, "login_attempts", null);
                    if (!tables.next()) {
                        System.err.println("Failed to create login_attempts table. Login attempts will not be recorded.");
                        return; // Exit if table creation failed
                    }
                } catch (SQLException e) {
                    System.err.println("Error creating login_attempts table: " + e.getMessage());
                    return; // Exit if table creation failed
                }
            }
            
            // Now we can safely insert the record
            try {
                String sql = "INSERT INTO login_attempts (username, success, attempt_time, ip_address) VALUES (?, ?, NOW(), ?)";
                
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, username);
                stmt.setBoolean(2, success);
                stmt.setString(3, "127.0.0.1"); // In a real app, you'd get the actual IP
                
                stmt.executeUpdate();
            } catch (SQLException e) {
                System.err.println("Error inserting login attempt record: " + e.getMessage());
            }
        } catch (SQLException e) {
            // Don't let login attempt recording failure block the login process
            System.err.println("Failed to record login attempt: " + e.getMessage());
        }
    }
    
    /**
     * Generate an MD5 hash for a password (for compatibility with legacy data)
     * 
     * @param password The password to hash
     * @return The MD5 hash as a hex string
     */
    private String md5Hash(String password) {
        try {
            // This method must produce "5f4dcc3b5aa765d61d8327deb882cf99" for the string "password"
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b & 0xff));
            }
            
            // If hashing the string "password", verify it matches expected value
            if (password.equals("password")) {
                String expected = "5f4dcc3b5aa765d61d8327deb882cf99";
                String actual = sb.toString();
                if (!actual.equals(expected)) {
                    System.err.println("WARNING: MD5 hash for 'password' doesn't match expected value:");
                    System.err.println("  Expected: " + expected);
                    System.err.println("  Actual:   " + actual);
                }
            }
            
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password with MD5", e);
        }
    }
    
    /**
     * Upgrade a user's password from MD5 to SHA-256 with salt
     * 
     * @param user The user whose password to upgrade
     * @param plainPassword The plain text password to rehash
     */
    private void upgradePasswordHash(User user, String plainPassword) {
        // Generate a new salt
        String newSalt = generateSalt();
        
        // Hash the password with the new salt using SHA-256
        String newHash = hashPassword(plainPassword, newSalt);
        
        // Update the user in the database
        user.setSalt(newSalt);
        user.setPassword(newHash);
        userDAO.updateUser(user);
        
        System.out.println("Upgraded password hash for user: " + user.getUsername());
    }
    
    /**
     * Test login method that doesn't record login attempts
     * This is meant for automated testing only
     * 
     * @param usernameOrEmail The username or email to log in with
     * @param password The password to authenticate with
     * @return An Optional containing the logged-in User if successful, or empty if authentication failed
     */
    public Optional<User> testLogin(String usernameOrEmail, String password) {
        System.out.println("\n----------------------------------");
        System.out.println("TESTING LOGIN FOR: " + usernameOrEmail);
        System.out.println("----------------------------------");
        
        // Try to find the user by username
        Optional<User> userOptional = userDAO.getUserByUsername(usernameOrEmail);
        
        if (!userOptional.isPresent()) {
            System.out.println("✗ User not found by username: " + usernameOrEmail);
            
            // If not found by username, try by email
            userOptional = userDAO.getUserByEmail(usernameOrEmail);
            if (!userOptional.isPresent()) {
                System.err.println("✗ User not found by email either: " + usernameOrEmail);
                return Optional.empty();
            } else {
                System.out.println("✓ User found by email instead of username");
            }
        } else {
            System.out.println("✓ User found by username: " + usernameOrEmail);
        }
        
        // If user found, check password
        User user = userOptional.get();
        System.out.println("USER DETAILS:");
        System.out.println("  - ID: " + user.getUserId());
        System.out.println("  - Username: " + user.getUsername());
        System.out.println("  - Role: " + user.getRole());
        System.out.println("  - Status: " + (user.isActive() ? "Active" : "Inactive"));
        
        boolean passwordMatches = false;
        
        try {
            // Check if this is an old MD5 password (the ones from the existing database)
            if (user.getPassword().length() == 32 && user.getSalt().equals("salt123")) {
                System.out.println("\nPASSWORD CHECK (MD5):");
                
                // For legacy users with MD5 hashed passwords
                String storedHash = user.getPassword();
                String md5Hash = md5Hash(password);
                passwordMatches = md5Hash.equals(storedHash);
                
                System.out.println("  - Password type: MD5 hash (legacy format)");
                System.out.println("  - Stored hash:   " + storedHash);
                System.out.println("  - Generated hash:" + md5Hash);
                System.out.println("  - Match result:  " + (passwordMatches ? "✓ MATCH" : "✗ NO MATCH"));
                
                // Extra debug for the common "password" value
                if (password.equals("password")) {
                    System.out.println("  - Expected MD5 for 'password': 5f4dcc3b5aa765d61d8327deb882cf99");
                }
            } else {
                System.out.println("\nPASSWORD CHECK (SHA-256):");
                
                // For new users with SHA-256 + salt
                String storedHash = user.getPassword();
                String salt = user.getSalt();
                String hashedPassword = hashPassword(password, salt);
                passwordMatches = hashedPassword.equals(storedHash);
                
                System.out.println("  - Password type: SHA-256 hash with salt");
                System.out.println("  - Salt used:     " + salt);
                System.out.println("  - Stored hash:   " + storedHash);
                System.out.println("  - Generated hash:" + hashedPassword);
                System.out.println("  - Match result:  " + (passwordMatches ? "✓ MATCH" : "✗ NO MATCH"));
            }
        } catch (Exception e) {
            System.err.println("\n✗ ERROR during password verification: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
        
        if (passwordMatches && user.isActive()) {
            // Don't update last login or create a session for test login
            System.out.println("\n✓ TEST LOGIN SUCCESSFUL for: " + user.getUsername() + " with role: " + user.getRole());
            System.out.println("----------------------------------");
            return Optional.of(user);
        } else if (!passwordMatches) {
            System.err.println("\n✗ PASSWORD MISMATCH for user: " + user.getUsername());
            System.err.println("----------------------------------");
        } else if (!user.isActive()) {
            System.err.println("\n✗ USER ACCOUNT NOT ACTIVE: " + user.getUsername());
            System.err.println("----------------------------------");
        }
        
        return Optional.empty();
    }
} 