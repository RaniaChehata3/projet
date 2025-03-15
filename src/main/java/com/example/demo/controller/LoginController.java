package com.example.demo.controller;

import com.example.demo.HelloApplication;
import com.example.demo.auth.AuthService;
import com.example.demo.auth.SessionManager;
import com.example.demo.database.DatabaseConnection;
import com.example.demo.model.User;
import com.example.demo.model.UserRole;
import com.example.demo.utils.NavigationUtil;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Screen;
import javafx.geometry.Rectangle2D;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.application.Platform;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;

/**
 * Controller for the login view, handling user authentication.
 */
public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;
    
    @FXML
    private Label registerLink;

    @FXML
    private StackPane loadingSpinner;
    
    @FXML
    private Button testConnectionButton;

    private final AuthService authService = AuthService.getInstance();
    private final SessionManager sessionManager = SessionManager.getInstance();

    /**
     * Initialize the controller
     */
    @FXML
    public void initialize() {
        // Add focus listener to clear error when user starts typing
        usernameField.textProperty().addListener((observable, oldValue, newValue) -> {
            errorLabel.setVisible(false);
        });

        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            errorLabel.setVisible(false);
        });
        
        // Ensure loading spinner is hidden initially
        if (loadingSpinner != null) {
            loadingSpinner.setVisible(false);
            loadingSpinner.setManaged(false);
        }
        
        // Check for existing session (could be loaded from preferences or cookies in a real app)
        checkForExistingSession();
    }
    
    /**
     * Check if there's an existing session we can use
     */
    private void checkForExistingSession() {
        // In a real app, you would load the sessionId from somewhere like preferences
        // For now, we'll just demonstrate how it would work
        
        // For demo purposes, we're not actually resuming a session here
        // This is just to show how it would work in a complete app
        
        /*
        String savedSessionId = loadSessionIdFromPreferences();
        if (savedSessionId != null) {
            boolean sessionValid = authService.resumeSession(savedSessionId);
            if (sessionValid) {
                // Skip login screen and go directly to home page
                navigateToHomePage();
            }
        }
        */
    }

    /**
     * Handle user login
     */
    @FXML
    private void onLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username/email and password");
            return;
        }

        // Show loading spinner
        showLoading(true);
        
        // Use a separate thread for login to prevent UI freezing
        new Thread(() -> {
            try {
                // Attempt login using the AuthService
                Optional<User> loggedInUser = authService.login(username, password);
                
                // Update UI on the JavaFX Application Thread
                Platform.runLater(() -> {
                    if (loggedInUser.isPresent()) {
                        // Login successful, create a session
                        User user = loggedInUser.get();
                        createUserSession(user);
                        
                        // Navigate to home page
                        navigateToHomePage();
                    } else {
                        // Login failed
                        showError("Invalid username or password");
                        // Hide loading spinner
                        showLoading(false);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showError("Login error: " + e.getMessage());
                    showLoading(false);
                });
            }
        }).start();
    }

    /**
     * Create a session for the logged-in user
     */
    private void createUserSession(User user) {
        try {
            // Get client info
            String ipAddress = InetAddress.getLocalHost().getHostAddress();
            String deviceInfo = System.getProperty("os.name") + " " + System.getProperty("os.version");
            
            // Create the session
            String sessionId = sessionManager.createSession(user, ipAddress, deviceInfo);
            
            if (sessionId != null) {
                System.out.println("Session created: " + sessionId);
                
                // Store the session ID in the AuthService
                authService.setCurrentSessionId(sessionId);
                
                // In a real app, you would save this to preferences or cookies
                // saveSessionIdToPreferences(sessionId);
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handle "Forgot Password" click
     */
    @FXML
    private void onForgotPassword(MouseEvent event) {
        // In a real application, this would open a forgot password form
        // For now, we'll just show a message
        showError("Password reset functionality will be implemented in a future version.");
    }

    /**
     * Demo login as Admin
     */
    @FXML
    private void loginAsAdmin(ActionEvent event) {
        loginWithCredentials("admin", "password");
    }

    /**
     * Demo login as Doctor
     */
    @FXML
    private void loginAsDoctor(ActionEvent event) {
        loginWithCredentials("doctor", "password");
    }

    /**
     * Demo login as Patient
     */
    @FXML
    private void loginAsPatient(ActionEvent event) {
        loginWithCredentials("patient", "password");
    }

    /**
     * Demo login as Laboratory
     */
    @FXML
    private void loginAsLaboratory(ActionEvent event) {
        loginWithCredentials("lab", "password");
    }

    /**
     * Helper method to login with specific credentials
     */
    private void loginWithCredentials(String username, String password) {
        usernameField.setText(username);
        passwordField.setText(password);
        onLogin(new ActionEvent());
    }

    /**
     * Show error message
     */
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);

        // Hide the error after 5 seconds
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.seconds(5), evt -> errorLabel.setVisible(false))
        );
        timeline.setCycleCount(1);
        timeline.play();
    }

    /**
     * Show or hide loading spinner
     */
    private void showLoading(boolean show) {
        if (loadingSpinner != null) {
            loadingSpinner.setVisible(show);
            loadingSpinner.setManaged(show);
        }
    }

    /**
     * Navigate to the role-specific dashboard after successful login
     */
    private void navigateToHomePage() {
        try {
            Optional<User> userOpt = authService.getCurrentUser();
            if (!userOpt.isPresent()) {
                showError("User session not found");
                showLoading(false);
                return;
            }
            
            User user = userOpt.get();
            UserRole role = user.getRole();
            String dashboardPath = "";
            
            // Determine which dashboard to load based on user role
            switch (role) {
                case ADMIN:
                    dashboardPath = "/com/example/demo/view/AdminDashboardView.fxml";
                    break;
                case DOCTOR:
                    dashboardPath = "/com/example/demo/view/DoctorDashboardView.fxml";
                    break;
                case PATIENT:
                    dashboardPath = "/com/example/demo/view/PatientDashboardView.fxml";
                    break;
                case LABORATORY:
                    dashboardPath = "/com/example/demo/view/LabDashboardView.fxml";
                    break;
                default:
                    // Fall back to generic dashboard if role not recognized
                    dashboardPath = "/com/example/demo/view/DashboardView.fxml";
                    break;
            }
            
            // Load the appropriate dashboard
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource(dashboardPath));
            Parent dashboardView = loader.load();

            // Get current stage
            Stage stage = (Stage) usernameField.getScene().getWindow();
            
            // Set up the scene
            Scene scene = new Scene(dashboardView);
            stage.setScene(scene);
            
            // Configure the window to fit the screen
            Screen screen = Screen.getPrimary();
            Rectangle2D bounds = screen.getVisualBounds();
            
            // Set stage size to fit screen bounds
            stage.setWidth(bounds.getWidth());
            stage.setHeight(bounds.getHeight());
            
            // Optional: set min size to maintain usability
            stage.setMinWidth(800);
            stage.setMinHeight(600);
            
            // Center on screen and ensure it's not maximized
            stage.centerOnScreen();
            stage.setMaximized(false);

        } catch (IOException e) {
            e.printStackTrace();
            showError("Error loading dashboard: " + e.getMessage());
            showLoading(false);
        }
    }

    /**
     * Handle "Register Here" link click
     */
    @FXML
    private void onRegisterLinkClicked(MouseEvent event) {
        try {
            NavigationUtil.switchView("view/RegisterView", registerLink);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error navigating to registration page: " + e.getMessage());
        }
    }
    
    /**
     * Test the database connection and display the results in a dialog
     */
    @FXML
    private void testDatabaseConnection(ActionEvent event) {
        // Show loading spinner
        showLoading(true);
        
        // Use a separate thread to test connection to prevent UI freezing
        new Thread(() -> {
            // Get the connection report
            String report = DatabaseConnection.getInstance().testAndReportConnection();
            
            Platform.runLater(() -> {
                // Hide loading spinner
                showLoading(false);
                
                // Display the report in a dialog
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Database Connection Test");
                alert.setHeaderText("Database Connection Status");
                
                TextArea textArea = new TextArea(report);
                textArea.setEditable(false);
                textArea.setWrapText(true);
                textArea.setPrefWidth(600);
                textArea.setPrefHeight(400);
                
                alert.getDialogPane().setContent(textArea);
                alert.getDialogPane().setMinWidth(650);
                alert.getDialogPane().setPrefHeight(450);
                
                alert.showAndWait();
            });
        }).start();
    }
} 