package com.example.demo.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;

/**
 * Singleton class to manage database connections.
 */
public class DatabaseConnection {
    private static DatabaseConnection instance;
    private String url;
    private String username;
    private String password;
    private Properties properties;
    
    /**
     * Private constructor to prevent direct instantiation.
     * Loads database configuration from properties file.
     */
    private DatabaseConnection() {
        properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("database.properties")) {
            if (input == null) {
                System.err.println("Unable to find database.properties");
                // Default to MySQL database if properties file not found
                url = "jdbc:mysql://localhost:3306/sahacare";
                username = "root"; // Default MySQL username, change if needed
                password = ""; // Default empty password, change if needed
            } else {
                properties.load(input);
                url = properties.getProperty("db.url", "jdbc:mysql://localhost:3306/sahacare");
                username = properties.getProperty("db.username", "root");
                password = properties.getProperty("db.password", "");
            }
        } catch (IOException e) {
            System.err.println("Error loading database properties: " + e.getMessage());
            e.printStackTrace();
            // Default to MySQL database if error occurs
            url = "jdbc:mysql://localhost:3306/sahacare";
            username = "root";
            password = "";
        }
        
        // Attempt to load the database driver
        try {
            // Try loading MySQL driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("MySQL JDBC Driver loaded successfully");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found: " + e.getMessage());
            System.err.println("Database connections will fail!");
        }
    }
    
    /**
     * Get the singleton instance of DatabaseConnection.
     * @return The singleton instance.
     */
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }
    
    /**
     * Get a database connection.
     * @return A new database connection.
     * @throws SQLException If a database access error occurs.
     */
    public static Connection getConnection() throws SQLException {
        return getInstance().createConnection();
    }
    
    /**
     * Creates a database connection.
     * @return A new database connection.
     * @throws SQLException If a database access error occurs.
     */
    private Connection createConnection() throws SQLException {
        System.out.println("Attempting to connect to database: " + url);
        try {
            Connection conn = DriverManager.getConnection(url, username, password);
            System.out.println("Database connection successful");
            
            // Test if the connection is actually working by executing a simple query
            try (java.sql.Statement stmt = conn.createStatement()) {
                stmt.execute("SELECT 1");
                System.out.println("Database connection verified with test query");
            }
            
            return conn;
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
            System.err.println("Connection URL: " + url);
            System.err.println("Username: " + username);
            
            if (e.getMessage().contains("Communications link failure")) {
                System.err.println("Database server might not be running. Please start your MySQL server.");
            } else if (e.getMessage().contains("Access denied")) {
                System.err.println("Username or password is incorrect.");
            } else if (e.getMessage().contains("Unknown database")) {
                System.err.println("Database 'sahacare' does not exist. Make sure the database has been created.");
                System.err.println("You may need to run the SQL script first: c:\\Users\\MSI\\Downloads\\sahacare (4).sql");
            }
            
            throw e;
        }
    }
    
    /**
     * Update the database connection properties.
     * @param url The JDBC URL.
     * @param username The database username.
     * @param password The database password.
     */
    public void updateConnectionProperties(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }
    
    /**
     * Get the current database URL.
     * @return The current database URL.
     */
    public String getUrl() {
        return url;
    }
    
    /**
     * Test the database connection.
     * @return true if connection is successful, false otherwise.
     */
    public boolean testConnection() {
        try (Connection conn = createConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Database connection test failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Initialize the database schema if it doesn't exist.
     * This method creates tables necessary for the application.
     */
    public void initializeDatabase() {
        // Example schema initialization (simplified)
        String[] createTableSQL = {
            // Users table
            "CREATE TABLE IF NOT EXISTS users (" +
            "user_id INT AUTO_INCREMENT PRIMARY KEY," +
            "username VARCHAR(50) NOT NULL UNIQUE," +
            "email VARCHAR(100) NOT NULL UNIQUE," +
            "password VARCHAR(100) NOT NULL," +
            "salt VARCHAR(100) NOT NULL," +
            "role VARCHAR(20) NOT NULL," +
            "full_name VARCHAR(100)," +
            "phone VARCHAR(20)," +
            "address VARCHAR(255)," +
            "city VARCHAR(100)," +
            "active BOOLEAN DEFAULT TRUE," +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "last_login TIMESTAMP NULL DEFAULT NULL" +
            ")",
            
            // Patients table
            "CREATE TABLE IF NOT EXISTS patients (" +
            "patient_id INT AUTO_INCREMENT PRIMARY KEY," +
            "full_name VARCHAR(100) NOT NULL," +
            "date_of_birth DATE," +
            "gender VARCHAR(20)," +
            "phone VARCHAR(20)," +
            "email VARCHAR(100)," +
            "address VARCHAR(255)," +
            "city VARCHAR(100)," +
            "blood_type VARCHAR(10)," +
            "insurance_provider VARCHAR(100)," +
            "insurance_number VARCHAR(50)," +
            "emergency_contact VARCHAR(100)," +
            "emergency_phone VARCHAR(20)," +
            "registration_date DATE DEFAULT CURRENT_DATE," +
            "last_visit_date DATE," +
            "status VARCHAR(20) DEFAULT 'Active'" +
            ")",
            
            // Medical records table
            "CREATE TABLE IF NOT EXISTS medical_records (" +
            "record_id INT AUTO_INCREMENT PRIMARY KEY," +
            "patient_id INT NOT NULL," +
            "doctor_id INT NOT NULL," +
            "visit_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "diagnosis VARCHAR(255)," +
            "symptoms TEXT," +
            "treatment TEXT," +
            "notes TEXT," +
            "follow_up_date DATE," +
            "record_type VARCHAR(50)," +
            "FOREIGN KEY (patient_id) REFERENCES patients(patient_id) ON DELETE CASCADE," +
            "FOREIGN KEY (doctor_id) REFERENCES users(user_id)" +
            ")",
            
            // Medications table
            "CREATE TABLE IF NOT EXISTS medications (" +
            "medication_id INT AUTO_INCREMENT PRIMARY KEY," +
            "patient_id INT NOT NULL," +
            "doctor_id INT NOT NULL," +
            "name VARCHAR(100) NOT NULL," +
            "dosage VARCHAR(50)," +
            "frequency VARCHAR(50)," +
            "instructions TEXT," +
            "purpose VARCHAR(255)," +
            "start_date DATE," +
            "end_date DATE," +
            "is_current BOOLEAN DEFAULT TRUE," +
            "side_effects TEXT," +
            "notes TEXT," +
            "FOREIGN KEY (patient_id) REFERENCES patients(patient_id) ON DELETE CASCADE," +
            "FOREIGN KEY (doctor_id) REFERENCES users(user_id)" +
            ")",
            
            // Lab results table
            "CREATE TABLE IF NOT EXISTS lab_results (" +
            "result_id INT AUTO_INCREMENT PRIMARY KEY," +
            "patient_id INT NOT NULL," +
            "doctor_id INT," +
            "lab_tech_id INT," +
            "test_name VARCHAR(100) NOT NULL," +
            "test_type VARCHAR(50)," +
            "result TEXT," +
            "normal_range VARCHAR(100)," +
            "unit VARCHAR(20)," +
            "is_abnormal BOOLEAN DEFAULT FALSE," +
            "test_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "result_date TIMESTAMP NULL DEFAULT NULL," +
            "notes TEXT," +
            "status VARCHAR(20) DEFAULT 'Pending'," +
            "is_urgent BOOLEAN DEFAULT FALSE," +
            "FOREIGN KEY (patient_id) REFERENCES patients(patient_id) ON DELETE CASCADE," +
            "FOREIGN KEY (doctor_id) REFERENCES users(user_id)," +
            "FOREIGN KEY (lab_tech_id) REFERENCES users(user_id)" +
            ")",
            
            // Notes table
            "CREATE TABLE IF NOT EXISTS notes (" +
            "note_id INT AUTO_INCREMENT PRIMARY KEY," +
            "patient_id INT NOT NULL," +
            "author_id INT NOT NULL," +
            "title VARCHAR(100)," +
            "content TEXT," +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
            "note_type VARCHAR(50) DEFAULT 'General'," +
            "is_private BOOLEAN DEFAULT FALSE," +
            "FOREIGN KEY (patient_id) REFERENCES patients(patient_id) ON DELETE CASCADE," +
            "FOREIGN KEY (author_id) REFERENCES users(user_id)" +
            ")",
            
            // Sessions table - H2 compatible version
            "CREATE TABLE IF NOT EXISTS sessions (" +
            "session_id VARCHAR(100) PRIMARY KEY," +
            "user_id INT NOT NULL," +
            "ip_address VARCHAR(50)," +
            "device_info VARCHAR(255)," +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "expires_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
            "FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE" +
            ")",
            
            // Login attempts table for security auditing
            "CREATE TABLE IF NOT EXISTS login_attempts (" +
            "attempt_id INT AUTO_INCREMENT PRIMARY KEY," +
            "username VARCHAR(50) NOT NULL," +
            "success BOOLEAN NOT NULL," +
            "attempt_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "ip_address VARCHAR(50)" +
            ")"
        };
        
        try (Connection conn = createConnection()) {
            // Enable H2 compatibility mode for MySQL syntax if using H2
            if (url.contains("h2")) {
                conn.createStatement().execute("SET MODE MySQL");
            }
            
            for (String sql : createTableSQL) {
                conn.createStatement().execute(sql);
            }
            System.out.println("Database initialization completed successfully");
        } catch (SQLException e) {
            System.err.println("Error initializing database schema: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Check if the database already has data.
     * Useful to prevent re-initialization if data already exists.
     * @return true if database has data, false otherwise.
     */
    public boolean hasExistingData() {
        try (Connection conn = createConnection()) {
            // Check if the users table has records
            String query = "SELECT COUNT(*) FROM users";
            try (java.sql.PreparedStatement stmt = conn.prepareStatement(query)) {
                java.sql.ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            // Table might not exist yet, which is expected on first run
            System.out.println("Database check failed (likely first run): " + e.getMessage());
        }
        return false;
    }

    /**
     * Insert test data for development/testing if needed.
     * This should only be called during initial development.
     */
    public void insertTestDataIfNeeded() {
        // Only insert test data if no data exists
        if (hasExistingData()) {
            System.out.println("Database already has existing data. Skipping test data insertion.");
            return;
        }
        
        try (Connection conn = createConnection()) {
            // Insert test users with exact data from the production database
            String insertUser = "INSERT INTO users (user_id, username, email, password, salt, role, full_name, phone, address, city, active, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (java.sql.PreparedStatement stmt = conn.prepareStatement(insertUser)) {
                // System Administrator
                stmt.setInt(1, 1);  // Exact user_id
                stmt.setString(2, "sys_admin");
                stmt.setString(3, "admin@sahacare.org");
                stmt.setString(4, "5f4dcc3b5aa765d61d8327deb882cf99"); // MD5 hash of "password"
                stmt.setString(5, "salt123");
                stmt.setString(6, "ADMIN");
                stmt.setString(7, "System Administrator");
                stmt.setString(8, "555-0100"); // Adding phone numbers
                stmt.setString(9, "123 Admin Street"); // Adding addresses
                stmt.setString(10, "New York");  // Adding city
                stmt.setBoolean(11, true);
                stmt.setTimestamp(12, java.sql.Timestamp.valueOf("2025-03-12 01:49:36"));
                stmt.addBatch();
                
                // Chief Doctor
                stmt.setInt(1, 2);  // Exact user_id
                stmt.setString(2, "dr_smith");
                stmt.setString(3, "dr.smith@sahacare.org");
                stmt.setString(4, "5f4dcc3b5aa765d61d8327deb882cf99");
                stmt.setString(5, "salt123");
                stmt.setString(6, "DOCTOR");
                stmt.setString(7, "Dr. John Smith");
                stmt.setString(8, "555-0200");
                stmt.setString(9, "456 Medical Avenue");
                stmt.setString(10, "Boston");
                stmt.setBoolean(11, true);
                stmt.setTimestamp(12, java.sql.Timestamp.valueOf("2025-03-12 01:49:36"));
                stmt.addBatch();
                
                // Regular Doctor 
                stmt.setInt(1, 3);  // Exact user_id
                stmt.setString(2, "dr_jones");
                stmt.setString(3, "dr.jones@sahacare.org");
                stmt.setString(4, "5f4dcc3b5aa765d61d8327deb882cf99");
                stmt.setString(5, "salt123");
                stmt.setString(6, "DOCTOR");
                stmt.setString(7, "Dr. Sarah Jones");
                stmt.setString(8, "555-0201");
                stmt.setString(9, "789 Health Street");
                stmt.setString(10, "Chicago");
                stmt.setBoolean(11, true);
                stmt.setTimestamp(12, java.sql.Timestamp.valueOf("2025-03-12 01:49:36"));
                stmt.addBatch();
                
                // Patient 1
                stmt.setInt(1, 4);  // Exact user_id
                stmt.setString(2, "john_patient");
                stmt.setString(3, "john.doe@example.com");
                stmt.setString(4, "5f4dcc3b5aa765d61d8327deb882cf99");
                stmt.setString(5, "salt123");
                stmt.setString(6, "PATIENT");
                stmt.setString(7, "John Doe");
                stmt.setString(8, "555-0300");
                stmt.setString(9, "321 Patient Road");
                stmt.setString(10, "Miami");
                stmt.setBoolean(11, true);
                stmt.setTimestamp(12, java.sql.Timestamp.valueOf("2025-03-12 01:49:36"));
                stmt.addBatch();
                
                // Patient 2
                stmt.setInt(1, 5);  // Exact user_id
                stmt.setString(2, "jane_patient");
                stmt.setString(3, "jane.doe@example.com");
                stmt.setString(4, "5f4dcc3b5aa765d61d8327deb882cf99");
                stmt.setString(5, "salt123");
                stmt.setString(6, "PATIENT");
                stmt.setString(7, "Jane Doe");
                stmt.setString(8, "555-0301");
                stmt.setString(9, "322 Patient Avenue");
                stmt.setString(10, "Los Angeles");
                stmt.setBoolean(11, true);
                stmt.setTimestamp(12, java.sql.Timestamp.valueOf("2025-03-12 01:49:36"));
                stmt.addBatch();
                
                // Lab Technician
                stmt.setInt(1, 6);  // Exact user_id
                stmt.setString(2, "lab_tech");
                stmt.setString(3, "lab.tech@sahacare.org");
                stmt.setString(4, "5f4dcc3b5aa765d61d8327deb882cf99");
                stmt.setString(5, "salt123");
                stmt.setString(6, "LABORATORY");
                stmt.setString(7, "Michael Johnson");
                stmt.setString(8, "555-0400");
                stmt.setString(9, "987 Laboratory Lane");
                stmt.setString(10, "Dallas");
                stmt.setBoolean(11, true);
                stmt.setTimestamp(12, java.sql.Timestamp.valueOf("2025-03-12 01:49:36"));
                stmt.addBatch();
                
                // Lab Manager
                stmt.setInt(1, 7);  // Exact user_id
                stmt.setString(2, "lab_manager");
                stmt.setString(3, "lab.manager@sahacare.org");
                stmt.setString(4, "5f4dcc3b5aa765d61d8327deb882cf99");
                stmt.setString(5, "salt123");
                stmt.setString(6, "LABORATORY");
                stmt.setString(7, "Emily Davis");
                stmt.setString(8, "555-0401");
                stmt.setString(9, "654 Science Boulevard");
                stmt.setString(10, "Houston");
                stmt.setBoolean(11, true);
                stmt.setTimestamp(12, java.sql.Timestamp.valueOf("2025-03-12 01:49:36"));
                stmt.addBatch();
                
                stmt.executeBatch();
            }
            
            System.out.println("New test users created successfully with the following credentials:");
            System.out.println("  1. System Admin: username=sys_admin, password=password");
            System.out.println("  2. Chief Doctor: username=dr_smith, password=password");
            System.out.println("  3. Doctor: username=dr_jones, password=password");
            System.out.println("  4. Patient: username=john_patient, password=password");
            System.out.println("  5. Patient: username=jane_patient, password=password");
            System.out.println("  6. Lab Tech: username=lab_tech, password=password");
            System.out.println("  7. Lab Manager: username=lab_manager, password=password");
            System.out.println("All users created with the same password: 'password'");
        } catch (SQLException e) {
            System.err.println("Error inserting test data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Update a session's expiration time to be 1 hour from now.
     * This is a workaround for H2 database not supporting INTERVAL syntax.
     * @param sessionId The session ID to update
     * @return True if update was successful, false otherwise
     */
    public boolean updateSessionExpiration(String sessionId) {
        try (Connection conn = createConnection()) {
            // Calculate expiration time (1 hour from now)
            java.sql.Timestamp expiresAt = new java.sql.Timestamp(
                System.currentTimeMillis() + (60 * 60 * 1000)); // 1 hour in milliseconds
            
            String updateSQL = "UPDATE sessions SET expires_at = ? WHERE session_id = ?";
            try (java.sql.PreparedStatement stmt = conn.prepareStatement(updateSQL)) {
                stmt.setTimestamp(1, expiresAt);
                stmt.setString(2, sessionId);
                int rowsAffected = stmt.executeUpdate();
                return rowsAffected > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error updating session expiration: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Create a new session with appropriate expiration time.
     * This method is a workaround for H2 database not supporting the INTERVAL syntax.
     * @param userId The user ID associated with the session
     * @param ipAddress The IP address from which the session was created
     * @param deviceInfo Information about the device used to create the session
     * @return The generated session ID if successful, null otherwise
     */
    public String createSession(int userId, String ipAddress, String deviceInfo) {
        try (Connection conn = createConnection()) {
            String sessionId = java.util.UUID.randomUUID().toString();
            
            // Insert the session with default expiration
            String insertSQL = "INSERT INTO sessions (session_id, user_id, ip_address, device_info) VALUES (?, ?, ?, ?)";
            try (java.sql.PreparedStatement stmt = conn.prepareStatement(insertSQL)) {
                stmt.setString(1, sessionId);
                stmt.setInt(2, userId);
                stmt.setString(3, ipAddress);
                stmt.setString(4, deviceInfo);
                stmt.executeUpdate();
            }
            
            // Now update the expiration time (1 hour from now)
            updateSessionExpiration(sessionId);
            
            return sessionId;
        } catch (SQLException e) {
            System.err.println("Error creating session: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Test the database connection and report detailed information.
     * This method is intended to be called from a menu option to help users troubleshoot
     * database connection issues.
     * 
     * @return A message with database connection status details
     */
    public String testAndReportConnection() {
        StringBuilder report = new StringBuilder();
        report.append("Database Connection Report\n");
        report.append("------------------------\n");
        report.append("JDBC URL: ").append(url).append("\n");
        report.append("Username: ").append(username).append("\n");
        report.append("Password: ").append(password.replaceAll(".", "*")).append("\n\n");
        
        try {
            // Try to connect to the database server
            System.out.println("Testing database connection...");
            report.append("Attempting to connect to database server...\n");
            
            Connection conn = createConnection();
            report.append("Connection successful!\n\n");
            
            // Check if required tables exist
            report.append("Checking for required tables...\n");
            boolean hasPatients = false;
            boolean hasUsers = false;
            
            try (java.sql.Statement stmt = conn.createStatement()) {
                try {
                    stmt.execute("SELECT 1 FROM patients LIMIT 1");
                    hasPatients = true;
                    report.append("- 'patients' table exists\n");
                } catch (SQLException e) {
                    report.append("- 'patients' table NOT FOUND: ").append(e.getMessage()).append("\n");
                }
                
                try {
                    stmt.execute("SELECT 1 FROM users LIMIT 1");
                    hasUsers = true;
                    report.append("- 'users' table exists\n");
                } catch (SQLException e) {
                    report.append("- 'users' table NOT FOUND: ").append(e.getMessage()).append("\n");
                }
            }
            
            // Report overall database status
            report.append("\nDatabase Status: ");
            if (hasPatients && hasUsers) {
                report.append("READY\n");
                report.append("The database is properly configured and all required tables exist.\n");
            } else {
                report.append("NEEDS INITIALIZATION\n");
                report.append("The database is missing required tables. Please run the SQL initialization script:\n");
                report.append("c:\\Users\\MSI\\Downloads\\sahacare (4).sql\n");
            }
            
            conn.close();
        } catch (SQLException e) {
            report.append("Connection FAILED: ").append(e.getMessage()).append("\n\n");
            report.append("Troubleshooting steps:\n");
            
            if (e.getMessage().contains("Communications link failure")) {
                report.append("1. Make sure MySQL server is running\n");
                report.append("2. Check that the server is running on the default port (3306)\n");
                report.append("3. Make sure the MySQL service is started in Windows Services\n");
            } else if (e.getMessage().contains("Access denied")) {
                report.append("1. Verify the username and password are correct\n");
                report.append("2. Make sure the user has permissions to access the database\n");
            } else if (e.getMessage().contains("Unknown database")) {
                report.append("1. The 'sahacare' database does not exist\n");
                report.append("2. Run the SQL script to create the database:\n");
                report.append("   c:\\Users\\MSI\\Downloads\\sahacare (4).sql\n");
            }
        }
        
        return report.toString();
    }
}
