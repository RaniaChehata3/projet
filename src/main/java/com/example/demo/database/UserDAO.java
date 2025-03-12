package com.example.demo.database;

import com.example.demo.model.User;
import com.example.demo.model.UserProfile;
import com.example.demo.model.UserRole;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for User-related database operations.
 */
public class UserDAO {
    
    /**
     * Retrieve a user by ID
     * 
     * @param userId The ID of the user to retrieve
     * @return An Optional containing the user if found, or empty if not found
     */
    public Optional<User> getUserById(int userId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM users WHERE user_id = ?";
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapResultSetToUser(rs));
            }
            
            return Optional.empty();
            
        } catch (SQLException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }
    
    /**
     * Retrieve a user by username
     * 
     * @param username The username to search for
     * @return An Optional containing the user if found, or empty if not found
     */
    public Optional<User> getUserByUsername(String username) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM users WHERE username = ?";
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapResultSetToUser(rs));
            }
            
            return Optional.empty();
            
        } catch (SQLException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }
    
    /**
     * Retrieve a user by email
     * 
     * @param email The email to search for
     * @return An Optional containing the user if found, or empty if not found
     */
    public Optional<User> getUserByEmail(String email) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM users WHERE email = ?";
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, email);
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapResultSetToUser(rs));
            }
            
            return Optional.empty();
            
        } catch (SQLException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }
    
    /**
     * Retrieve all users of a specific role
     * 
     * @param role The role to filter by
     * @return A list of users with the specified role
     */
    public List<User> getUsersByRole(UserRole role) {
        List<User> users = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM users WHERE role = ?";
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, role.toString());
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return users;
    }
    
    /**
     * Create a new user in the database
     * 
     * @param user The user to create
     * @return The created user with the generated ID, or empty if creation failed
     */
    public Optional<User> createUser(User user) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO users (username, email, password, salt, role, full_name, phone, address, city, active) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPassword());
            stmt.setString(4, user.getSalt());
            stmt.setString(5, user.getRole().toString());
            stmt.setString(6, user.getFullName());
            stmt.setString(7, user.getPhone());
            stmt.setString(8, user.getAddress());
            stmt.setString(9, user.getCity());
            stmt.setBoolean(10, user.isActive());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                return Optional.empty();
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setUserId(generatedKeys.getInt(1));
                    return Optional.of(user);
                } else {
                    return Optional.empty();
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }
    
    /**
     * Update an existing user in the database
     * 
     * @param user The user to update
     * @return true if the update was successful, false otherwise
     */
    public boolean updateUser(User user) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "UPDATE users SET username = ?, email = ?, password = ?, salt = ?, role = ?, " +
                         "full_name = ?, phone = ?, address = ?, city = ?, active = ? " +
                         "WHERE user_id = ?";
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPassword());
            stmt.setString(4, user.getSalt());
            stmt.setString(5, user.getRole().toString());
            stmt.setString(6, user.getFullName());
            stmt.setString(7, user.getPhone());
            stmt.setString(8, user.getAddress());
            stmt.setString(9, user.getCity());
            stmt.setBoolean(10, user.isActive());
            stmt.setInt(11, user.getUserId());
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Delete a user from the database
     * 
     * @param userId The ID of the user to delete
     * @return true if the deletion was successful, false otherwise
     */
    public boolean deleteUser(int userId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "DELETE FROM users WHERE user_id = ?";
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            
            int affectedRows = stmt.executeUpdate();
            
            return affectedRows > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Get a user's profile
     * 
     * @param userId The ID of the user whose profile to retrieve
     * @return An Optional containing the user profile if found, or empty if not found
     */
    public Optional<UserProfile> getUserProfile(int userId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM user_profiles WHERE user_id = ?";
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                UserProfile profile = new UserProfile();
                profile.setProfileId(rs.getInt("profile_id"));
                profile.setUserId(rs.getInt("user_id"));
                
                String genderStr = rs.getString("gender");
                if (genderStr != null) {
                    profile.setGender(UserProfile.Gender.fromString(genderStr));
                }
                
                profile.setDateOfBirth(rs.getDate("date_of_birth"));
                profile.setBloodType(rs.getString("blood_type"));
                profile.setMedicalHistory(rs.getString("medical_history"));
                profile.setSpecialization(rs.getString("specialization"));
                profile.setLicenseNumber(rs.getString("license_number"));
                profile.setProfileImage(rs.getString("profile_image"));
                
                return Optional.of(profile);
            }
            
            return Optional.empty();
            
        } catch (SQLException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }
    
    /**
     * Map a database result set to a User object
     * 
     * @param rs The result set containing user data
     * @return A User object populated with data from the result set
     * @throws SQLException If there is an error accessing the result set
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        return new User(
            rs.getInt("user_id"),
            rs.getString("username"),
            rs.getString("email"),
            rs.getString("password"),
            rs.getString("salt"),
            UserRole.fromString(rs.getString("role")),
            rs.getString("full_name"),
            rs.getString("phone"),
            rs.getString("address"),
            rs.getString("city"),
            rs.getTimestamp("created_at"),
            rs.getTimestamp("last_login"),
            rs.getBoolean("active")
        );
    }
    
    /**
     * Count the total number of users in the system
     * 
     * @return The total number of users
     */
    public int countTotalUsers() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT COUNT(*) FROM users";
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
            return 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    /**
     * Count the number of users with a specific role
     * 
     * @param role The role to count users for
     * @return The number of users with the specified role
     */
    public int countUsersByRole(UserRole role) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT COUNT(*) FROM users WHERE role = ?";
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, role.toString());
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
            return 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
} 