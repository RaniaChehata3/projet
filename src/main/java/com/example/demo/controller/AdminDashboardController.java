package com.example.demo.controller;

import com.example.demo.database.DatabaseConnection;
import com.example.demo.database.UserDAO;
import com.example.demo.model.UserRole;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

/**
 * Controller for the Admin Dashboard View
 */
public class AdminDashboardController extends ModernDashboardController {

    @FXML
    private Label totalUsersLabel;
    
    @FXML
    private Label totalDoctorsLabel;
    
    @FXML
    private Label totalPatientsLabel;
    
    @FXML
    private TableView<ActivityLog> activityTable;
    
    @FXML
    private TableColumn<ActivityLog, String> activityTimeColumn;
    
    @FXML
    private TableColumn<ActivityLog, String> activityUserColumn;
    
    @FXML
    private TableColumn<ActivityLog, String> activityTypeColumn;
    
    @FXML
    private TableColumn<ActivityLog, String> activityDetailsColumn;
    
    @FXML
    private Circle databaseStatusIndicator;
    
    @FXML
    private Label databaseStatusLabel;
    
    @FXML
    private Label databaseDetailsLabel;
    
    @FXML
    private Circle serverStatusIndicator;
    
    @FXML
    private Label serverStatusLabel;
    
    @FXML
    private Label serverDetailsLabel;
    
    @FXML
    private ProgressBar storageProgressBar;
    
    @FXML
    private Label storageDetailsLabel;
    
    @FXML
    private Label updatesStatusLabel;
    
    private UserDAO userDAO;
    private ObservableList<ActivityLog> activityLogs = FXCollections.observableArrayList();
    
    @Override
    public void initialize() {
        super.initialize();
        
        // Initialize UserDAO
        userDAO = new UserDAO();
        
        // Initialize activity table
        initializeActivityTable();
        
        // Load system statistics
        loadSystemStatistics();
    }
    
    @Override
    protected void setupRoleSpecificUI() {
        // Set up navigation menu items specific to Admin role
        ObservableList<String> menuItems = FXCollections.observableArrayList(
            "Dashboard",
            "User Management",
            "System Settings",
            "Security",
            "Database",
            "Logs & Analytics",
            "Backup & Restore"
        );
        
        navigationList.setItems(menuItems);
        navigationList.getSelectionModel().select(0);
        
        // Set up navigation map for admin-specific views
        navigationMap.put("Dashboard", "/com/example/demo/view/AdminDashboardView.fxml");
        navigationMap.put("User Management", "/com/example/demo/view/UserManagementView.fxml");
        navigationMap.put("System Settings", "/com/example/demo/view/SystemSettingsView.fxml");
        navigationMap.put("Security", "/com/example/demo/view/SecurityView.fxml");
        navigationMap.put("Database", "/com/example/demo/view/DatabaseView.fxml");
        navigationMap.put("Logs & Analytics", "/com/example/demo/view/LogsView.fxml");
        navigationMap.put("Backup & Restore", "/com/example/demo/view/BackupView.fxml");
    }
    
    /**
     * Initialize the activity table columns
     */
    private void initializeActivityTable() {
        activityTimeColumn.setCellValueFactory(cellData -> cellData.getValue().timeProperty());
        activityUserColumn.setCellValueFactory(cellData -> cellData.getValue().userProperty());
        activityTypeColumn.setCellValueFactory(cellData -> cellData.getValue().typeProperty());
        activityDetailsColumn.setCellValueFactory(cellData -> cellData.getValue().detailsProperty());
        
        // Load mock activity data for now - can be replaced with real data from database
        loadMockActivityData();
        
        activityTable.setItems(activityLogs);
    }
    
    /**
     * Load mock activity data for demonstration
     */
    private void loadMockActivityData() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        activityLogs.add(new ActivityLog(
            LocalDateTime.now().minusMinutes(5).format(formatter),
            "Dr. John Smith",
            "Login",
            "User logged in successfully"
        ));
        
        activityLogs.add(new ActivityLog(
            LocalDateTime.now().minusHours(1).format(formatter),
            "Admin",
            "User Created",
            "Created new user account for Jane Doe"
        ));
        
        activityLogs.add(new ActivityLog(
            LocalDateTime.now().minusHours(2).format(formatter),
            "System",
            "Backup",
            "Automatic database backup completed"
        ));
        
        activityLogs.add(new ActivityLog(
            LocalDateTime.now().minusHours(5).format(formatter),
            "Admin",
            "Settings Changed",
            "Updated system email configuration"
        ));
        
        activityLogs.add(new ActivityLog(
            LocalDateTime.now().minusDays(1).format(formatter),
            "Jane Doe",
            "Patient Record",
            "Updated patient information for Patient ID: 12345"
        ));
    }
    
    /**
     * Load system statistics from the database
     */
    private void loadSystemStatistics() {
        // This would typically come from the database and system metrics
        // For now we'll load mock data
        try {
            // Count total users by role
            int totalUsers = userDAO.countTotalUsers();
            int totalDoctors = userDAO.countUsersByRole(UserRole.DOCTOR);
            int totalPatients = userDAO.countUsersByRole(UserRole.PATIENT);
            
            // Update UI labels
            totalUsersLabel.setText(String.valueOf(totalUsers));
            totalDoctorsLabel.setText(String.valueOf(totalDoctors));
            totalPatientsLabel.setText(String.valueOf(totalPatients));
            
            // Check database connection
            boolean databaseConnected = DatabaseConnection.getInstance().testConnection();
            if (databaseConnected) {
                databaseStatusIndicator.setFill(Color.valueOf("#00c48c"));
                databaseStatusLabel.setText("Connected");
            } else {
                databaseStatusIndicator.setFill(Color.valueOf("#ff4757"));
                databaseStatusLabel.setText("Disconnected");
            }
            
            // For now, we'll mock the other system health indicators
            // In a real implementation, these would be populated with actual system metrics
        } catch (Exception e) {
            // Handle any errors when loading statistics
            e.printStackTrace();
        }
    }
    
    /**
     * Handle add user button click
     */
    @FXML
    private void handleAddUser() {
        // This would launch the add user dialog/view
        showNotImplementedAlert("Add User");
    }
    
    /**
     * Handle system settings button click
     */
    @FXML
    private void handleSystemSettings() {
        // Navigate to system settings view
        navigateTo("/com/example/demo/view/SystemSettingsView.fxml");
    }
    
    /**
     * Handle database backup button click
     */
    @FXML
    private void handleDatabaseBackup() {
        // This would trigger a database backup process
        showNotImplementedAlert("Database Backup");
    }
    
    /**
     * Handle view logs button click
     */
    @FXML
    private void handleViewLogs() {
        // Navigate to logs view
        navigateTo("/com/example/demo/view/LogsView.fxml");
    }
    
    /**
     * Handle check for updates button click
     */
    @FXML
    private void handleCheckUpdates() {
        // This would check for system updates
        showNotImplementedAlert("Check for Updates");
    }
    
    /**
     * Show an alert for not implemented features
     */
    private void showNotImplementedAlert(String feature) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Feature Not Implemented");
        alert.setHeaderText(feature + " Feature");
        alert.setContentText("The " + feature + " feature is not implemented in this preview.");
        alert.showAndWait();
    }
    
    /**
     * Inner class to represent activity log entries
     */
    public static class ActivityLog {
        private final javafx.beans.property.SimpleStringProperty time;
        private final javafx.beans.property.SimpleStringProperty user;
        private final javafx.beans.property.SimpleStringProperty type;
        private final javafx.beans.property.SimpleStringProperty details;
        
        public ActivityLog(String time, String user, String type, String details) {
            this.time = new javafx.beans.property.SimpleStringProperty(time);
            this.user = new javafx.beans.property.SimpleStringProperty(user);
            this.type = new javafx.beans.property.SimpleStringProperty(type);
            this.details = new javafx.beans.property.SimpleStringProperty(details);
        }
        
        public javafx.beans.property.StringProperty timeProperty() { return time; }
        public javafx.beans.property.StringProperty userProperty() { return user; }
        public javafx.beans.property.StringProperty typeProperty() { return type; }
        public javafx.beans.property.StringProperty detailsProperty() { return details; }
    }
} 