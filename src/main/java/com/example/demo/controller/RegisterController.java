package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.auth.AuthService;
import com.example.demo.utils.NavigationUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.application.Platform;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class RegisterController implements Initializable {

    @FXML
    private TextField fullNameField;

    @FXML
    private TextField usernameField;

    @FXML
    private TextField emailField;
    
    @FXML
    private TextField phoneField;
    
    @FXML
    private ComboBox<String> cityComboBox;

    @FXML
    private ComboBox<String> roleComboBox;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Button registerButton;

    @FXML
    private Label errorLabel;

    @FXML
    private Label loginLink;
    
    @FXML
    private StackPane loadingSpinner;

    private final AuthService authService = AuthService.getInstance();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Set up role dropdown
        roleComboBox.getItems().addAll("Patient", "Doctor", "Admin");
        roleComboBox.setValue("Patient");  // Default value
        
        // Set up city dropdown with Tunisian cities
        cityComboBox.getItems().addAll(
            "Tunis", "Sfax", "Sousse", "Kairouan", "Bizerte", 
            "Gabès", "Ariana", "Gafsa", "Monastir", "Ben Arous", 
            "Kasserine", "Médenine", "Nabeul", "Tataouine", "Béja", 
            "Jendouba", "El Kef", "Mahdia", "Sidi Bouzid", "Tozeur", 
            "Siliana", "Zaghouan", "Kébili", "Manouba"
        );
        cityComboBox.setValue("Tunis");  // Default value
        
        // Add listeners to clear error message when user types
        usernameField.textProperty().addListener((observable, oldValue, newValue) -> {
            errorLabel.setVisible(false);
        });
        
        emailField.textProperty().addListener((observable, oldValue, newValue) -> {
            errorLabel.setVisible(false);
        });
        
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            errorLabel.setVisible(false);
        });
        
        confirmPasswordField.textProperty().addListener((observable, oldValue, newValue) -> {
            errorLabel.setVisible(false);
        });
        
        // Hide loading spinner initially
        if (loadingSpinner != null) {
            loadingSpinner.setVisible(false);
            loadingSpinner.setManaged(false);
        }
        
        // Add listeners to ComboBoxes
        cityComboBox.valueProperty().addListener((observable, oldValue, newValue) -> errorLabel.setVisible(false));
        roleComboBox.valueProperty().addListener((observable, oldValue, newValue) -> errorLabel.setVisible(false));
    }

    @FXML
    private void onRegister() {
        // Clear previous errors
        errorLabel.setVisible(false);
        
        // Validate inputs
        if (!validateInputs()) {
            return;
        }
        
        // Show loading spinner
        showLoading(true);
        
        // Get values from fields
        String fullName = fullNameField.getText().trim();
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String city = cityComboBox.getValue();
        String role = roleComboBox.getValue();
        String password = passwordField.getText();
        
        // Run registration in a background thread to keep UI responsive
        new Thread(() -> {
            try {
                // Register the user
                boolean success = registerUser(username, password, fullName, email, phone, city, role);
                
                Platform.runLater(() -> {
                    if (success) {
                        // Show success message
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Registration Successful");
                        alert.setHeaderText(null);
                        alert.setContentText("Your account has been created successfully. Please log in.");
                        alert.showAndWait();
                        
                        // Redirect to login screen
                        navigateToLogin();
                    } else {
                        showError("Registration failed. Username or email may already exist.");
                        showLoading(false);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showError("Registration error: " + e.getMessage());
                    showLoading(false);
                });
            }
        }).start();
    }
    
    private boolean registerUser(String username, String password, String fullName, String email, 
                               String phone, String city, String role) {
        // This is a placeholder. You'll need to implement this method in AuthService
        // to handle the new fields
        return authService.registerUser(username, password, fullName, email, role, phone, city);
    }

    private boolean validateInputs() {
        // Check if fields are empty
        if (fullNameField.getText().trim().isEmpty()) {
            showError("Full name is required");
            return false;
        }
        
        if (usernameField.getText().trim().isEmpty()) {
            showError("Username is required");
            return false;
        }
        
        if (emailField.getText().trim().isEmpty()) {
            showError("Email is required");
            return false;
        }
        
        if (phoneField.getText().trim().isEmpty()) {
            showError("Phone number is required");
            return false;
        }
        
        if (cityComboBox.getValue() == null) {
            showError("Please select your city");
            return false;
        }
        
        if (passwordField.getText().isEmpty()) {
            showError("Password is required");
            return false;
        }
        
        if (confirmPasswordField.getText().isEmpty()) {
            showError("Please confirm your password");
            return false;
        }
        
        // Check if passwords match
        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            showError("Passwords do not match");
            return false;
        }
        
        // Validate email format
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        if (!emailField.getText().matches(emailRegex)) {
            showError("Invalid email format");
            return false;
        }
        
        // Validate phone number (simple validation for now)
        String phoneRegex = "^[0-9+\\s-]{8,}$";
        if (!phoneField.getText().matches(phoneRegex)) {
            showError("Invalid phone number format. Please enter at least 8 digits.");
            return false;
        }
        
        return true;
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
    
    private void showLoading(boolean show) {
        if (loadingSpinner != null) {
            loadingSpinner.setVisible(show);
            loadingSpinner.setManaged(show);
        }
    }

    @FXML
    private void onLoginLinkClicked(MouseEvent event) {
        navigateToLogin();
    }

    private void navigateToLogin() {
        try {
            NavigationUtil.switchView("view/LoginView", loginLink);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error navigating to login: " + e.getMessage());
        }
    }
} 