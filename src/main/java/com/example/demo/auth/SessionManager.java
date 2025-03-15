package com.example.demo.auth;

import com.example.demo.database.DatabaseConnection;
import com.example.demo.model.User;
import com.example.demo.model.UserRole;

import java.security.SecureRandom;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

/**
 * Manager for user authentication sessions.
 * Handles session creation, validation, and removal.
 */
public class SessionManager {
    
    private static final int SESSION_ID_LENGTH = 32;
    private static final int DEFAULT_SESSION_HOURS = 24; // Default session duration is 24 hours
    
    private static SessionManager instance;
    
    // Private constructor for singleton pattern
    private SessionManager() {
    }
    
    // Singleton pattern
    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }
    
    /**
     * Create a new session for a user
     * 
     * @param user The user to create a session for
     * @param ipAddress The IP address of the client
     * @param deviceInfo Information about the client device
     * @return The session ID of the created session
     */
    public String createSession(User user, String ipAddress, String deviceInfo) {
        String sessionId = generateSessionId();
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO sessions (session_id, user_id, ip_address, device_info, expires_at) " +
                         "VALUES (?, ?, ?, ?, ?)";
            
            // Calculate expiry time (current time + session duration)
            LocalDateTime expiryTime = LocalDateTime.now().plusHours(DEFAULT_SESSION_HOURS);
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, sessionId);
            stmt.setInt(2, user.getUserId());
            stmt.setString(3, ipAddress);
            stmt.setString(4, deviceInfo);
            stmt.setTimestamp(5, Timestamp.valueOf(expiryTime));
            
            stmt.executeUpdate();
            
            return sessionId;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Validate a session and get the user associated with it
     * 
     * @param sessionId The session ID to validate
     * @return An Optional containing the User if the session is valid, or empty if invalid
     */
    public Optional<User> validateSession(String sessionId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT u.* FROM sessions s " +
                         "JOIN users u ON s.user_id = u.user_id " +
                         "WHERE s.session_id = ? AND s.expires_at > ?";
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, sessionId);
            stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                // Map the user from the result set
                User user = new User(
                    rs.getInt("user_id"),
                    rs.getString("username"),
                    rs.getString("email"),
                    rs.getString("password"),
                    UserRole.fromString(rs.getString("role")),
                    rs.getString("full_name"),
                    rs.getString("phone"),
                    rs.getString("address"),
                    rs.getTimestamp("created_at"),
                    rs.getTimestamp("last_login"),
                    rs.getBoolean("active")
                );
                
                return Optional.of(user);
            }
            
            return Optional.empty();
            
        } catch (SQLException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }
    
    /**
     * Remove a session (logout)
     * 
     * @param sessionId The session ID to remove
     * @return true if the session was successfully removed, false otherwise
     */
    public boolean removeSession(String sessionId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "DELETE FROM sessions WHERE session_id = ?";
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, sessionId);
            
            int affectedRows = stmt.executeUpdate();
            
            return affectedRows > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Clean up expired sessions from the database
     * This should be called periodically, e.g., via a scheduled task
     * 
     * @return The number of expired sessions removed
     */
    public int cleanupExpiredSessions() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "DELETE FROM sessions WHERE expires_at <= ?";
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            
            return stmt.executeUpdate();
            
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    /**
     * Generate a secure random session ID
     * 
     * @return A secure random session ID
     */
    private String generateSessionId() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[SESSION_ID_LENGTH];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
} 