package com.example.demo.controller;

import com.example.demo.database.DatabaseConnection;
import com.example.demo.model.SidebarModel;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.control.ComboBox;
import javafx.scene.effect.BoxBlur;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SidebarController {

    @FXML
    private StackPane rootContainer; // Root container (StackPane)

    @FXML
    private BorderPane mainContainer; // Main content (BorderPane)

    @FXML
    private HBox header; // Header

    @FXML
    private VBox sidebar; // Sidebar

    @FXML
    private ListView<String> sidebarList; // Sidebar menu list

    @FXML
    private VBox loginOverlay; // Login overlay

    @FXML
    private VBox registerOverlay; // Register overlay

    @FXML
    private TextField emailField, usernameField, phoneField, regEmailField, regUsernameField, specializationField, addressField, laboratoryField; // Input fields

    @FXML
    private PasswordField passwordField, regPasswordField; // Password fields

    @FXML
    private ComboBox<String> roleComboBox; // Role ComboBox

    public void toggleLaboratoryFieldVisibility() {
        laboratoryField.setVisible(false);  // or true
        laboratoryField.setManaged(false);  // or true
    }

    public void someMethod() {
        // Dynamically setting visibility and managed properties in controller
        specializationField.setVisible(false);  // or true
        specializationField.setManaged(false);  // or true
    }

    @FXML
    public void initialize() {
        // Initialize sidebar items
        initializeSidebar();

        // Show the login overlay by default
        showLoginForm();

        // Add listener to roleComboBox to handle dynamic fields
        setupRoleComboBoxListener();

        // Add listener to sidebarList to handle item selection
        setupSidebarListListener();
    }

    private void initializeSidebar() {
        if (sidebarList != null) {
            sidebarList.getItems().addAll(
                    "Tableau de bord",
                    "Symptômes",
                    "Calendrier",
                    "Historiques",
                    "Médecins",
                    "Laboratoires",
                    "Bilans"
            );

        } else {
            System.out.println("Sidebar ListView is null. Check FXML.");
        }
    }

    private void setupRoleComboBoxListener() {
        if (roleComboBox != null) {
            roleComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if ("Doctor".equals(newValue)) {
                    specializationField.setVisible(true);
                    specializationField.setManaged(true);
                    laboratoryField.setVisible(false);
                    laboratoryField.setManaged(false);
                } else if ("Laboratory".equals(newValue)) {
                    laboratoryField.setVisible(true);
                    laboratoryField.setManaged(true);
                    specializationField.setVisible(false);
                    specializationField.setManaged(false);
                } else {
                    specializationField.setVisible(false);
                    specializationField.setManaged(false);
                    laboratoryField.setVisible(false);
                    laboratoryField.setManaged(false);
                }
            });
        } else {
            System.out.println("Role ComboBox is null. Check FXML.");
        }
    }

       
    
    private void setupSidebarListListener() {
        sidebarList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if ("Symptômes".equals(newValue)) {
                loadChatbotView();
            }
            // Handle other sidebar items...
        });
    }

    private void loadChatbotView() {
        try {
            Parent chatbotView = FXMLLoader.load(getClass().getResource("/com/example/demo/view/ChatbotView.fxml"));
            mainContainer.setCenter(chatbotView);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading ChatbotView: " + e.getMessage());
        }
    }
    

    


    private void showLoginForm() {
        if (loginOverlay != null) {
            loginOverlay.setVisible(true);
            registerOverlay.setVisible(false); // Hide register overlay
            applyBlurEffect(true);
        }
    }

    private void showRegisterPage() {
        if (registerOverlay != null) {
            registerOverlay.setVisible(true);
            loginOverlay.setVisible(false); // Hide login overlay
            applyBlurEffect(true);
        }
    }

    private void showLoginPage() {
        if (loginOverlay != null) {
            loginOverlay.setVisible(true);
            registerOverlay.setVisible(false); // Hide register overlay
            applyBlurEffect(true);
        }
    }

    private void applyBlurEffect(boolean enable) {
        BoxBlur blur = new BoxBlur(5, 5, 3); // Adjust blur intensity

        if (header != null) header.setEffect(enable ? blur : null);
        if (sidebar != null) sidebar.setEffect(enable ? blur : null);
        if (mainContainer != null) mainContainer.setEffect(enable ? blur : null);
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();

        // Hash the password (use the same method as in registration)
        String hashedPassword = hashPassword(password);

        // Validate credentials against the database
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM users WHERE email = ? AND password = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, email);
                statement.setString(2, hashedPassword);

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        String username = resultSet.getString("username");
                        String role = resultSet.getString("role");
                        System.out.println("Login successful! Welcome, " + username + " (" + role + ")");

                        // Hide the login overlay
                        loginOverlay.setVisible(false);
                        applyBlurEffect(false);
                    } else {
                        System.out.println("Invalid email or password.");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error during login: " + e.getMessage());
        }
    }

    @FXML
    private void handleRegister() {
        String email = regEmailField.getText();
        String username = regUsernameField.getText();
        String password = regPasswordField.getText();
        String phone = phoneField.getText();
        String specialization = specializationField.getText();
        String address = addressField.getText();
        String laboratory = laboratoryField.getText();
        String role = roleComboBox.getValue();

        // Hash the password (for security)
        String hashedPassword = hashPassword(password);

        // Insert data into the database
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "INSERT INTO users (email, username, password, phone, role, specialization, address, laboratory) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, email);
                statement.setString(2, username);
                statement.setString(3, hashedPassword);
                statement.setString(4, phone);
                statement.setString(5, role);
                statement.setString(6, specialization);
                statement.setString(7, address);
                statement.setString(8, laboratory);

                int rowsInserted = statement.executeUpdate();
                if (rowsInserted > 0) {
                    System.out.println("User registered successfully!");
                    showLoginForm(); // Switch back to the login form
                }
            }
        } catch (SQLException e) {
            System.err.println("Error registering user: " + e.getMessage());
        }
    }

    // Helper method to hash passwords (use a library like BCrypt for production)
    private String hashPassword(String password) {
        // For now, just return the plain password (replace with proper hashing)
        return password;
    }

    // These methods will handle the Hyperlink click events
    @FXML
    private void onShowRegisterPage() {
        showRegisterPage(); // Show the register page
    }

    @FXML
    private void onShowLoginPage() {
        showLoginPage(); // Show the login page
    }
}