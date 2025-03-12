package com.example.demo.controller;

import com.example.demo.HelloApplication;
import com.example.demo.auth.AuthService;
import com.example.demo.model.User;
import com.example.demo.model.UserRole;
import com.example.demo.utils.NavigationUtil;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Base controller for the modern dashboard UI.
 * Provides common functionality for all role-specific dashboards.
 */
public class ModernDashboardController {

    @FXML
    protected ListView<String> navigationList;
    
    @FXML
    protected StackPane contentArea;
    
    @FXML
    protected StackPane loadingOverlay;
    
    @FXML
    protected Label userNameLabel;
    
    @FXML
    protected Label userRoleLabel;
    
    @FXML
    protected Label userInitials;
    
    @FXML
    protected Button notificationsButton;
    
    @FXML
    protected Circle notificationBadge;
    
    @FXML
    protected ImageView logoImage;
    
    protected AuthService authService;
    protected User currentUser;
    protected Map<String, String> navigationMap = new HashMap<>();
    
    @FXML
    public void initialize() {
        // Initialize auth service
        authService = AuthService.getInstance();
        
        // Show loading overlay
        showLoading(true);
        
        // Load app logo
        loadLogo();
        
        // Set up user information
        setupUserInfo();
        
        // Handle navigation selection
        navigationList.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                if (newValue != null) {
                    navigateTo(navigationMap.get(newValue));
                }
            }
        );
        
        // Set up role-specific navigation and content
        setupRoleSpecificUI();
        
        // Hide loading overlay after a delay
        PauseTransition delay = new PauseTransition(Duration.seconds(1));
        delay.setOnFinished(e -> showLoading(false));
        delay.play();
    }
    
    /**
     * Set up the user information in the UI
     */
    protected void setupUserInfo() {
        Optional<User> userOpt = authService.getCurrentUser();
        if (userOpt.isPresent()) {
            currentUser = userOpt.get();
            
            // Set user name and role
            userNameLabel.setText(currentUser.getFullName());
            userRoleLabel.setText(formatRoleForDisplay(currentUser.getRole()));
            
            // Set user initials
            setUserInitials(currentUser.getFullName());
        } else {
            // If no user is logged in, redirect to login
            Platform.runLater(() -> {
                try {
                    HelloApplication.setRoot("view/LoginView");
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });
        }
    }
    
    /**
     * Set the user initials based on the full name
     */
    protected void setUserInitials(String fullName) {
        if (fullName != null && !fullName.isEmpty()) {
            String[] nameParts = fullName.split(" ");
            if (nameParts.length > 1) {
                // First letter of first name + first letter of last name
                userInitials.setText(
                    String.valueOf(nameParts[0].charAt(0)) + 
                    String.valueOf(nameParts[nameParts.length - 1].charAt(0))
                );
            } else {
                // First letter of name
                userInitials.setText(String.valueOf(fullName.charAt(0)));
            }
        } else {
            // Default if no name
            userInitials.setText("U");
        }
    }
    
    /**
     * Format the role name for display (capitalize first letter of each word)
     */
    protected String formatRoleForDisplay(UserRole role) {
        if (role == null) return "";
        
        String roleStr = role.toString().toLowerCase();
        String[] words = roleStr.split("_");
        StringBuilder result = new StringBuilder();
        
        for (String word : words) {
            if (word.length() > 0) {
                result.append(Character.toUpperCase(word.charAt(0)))
                      .append(word.substring(1))
                      .append(" ");
            }
        }
        
        return result.toString().trim();
    }
    
    /**
     * Load the application logo
     */
    protected void loadLogo() {
        try {
            InputStream logoStream = getClass().getResourceAsStream("/com/example/demo/images/app_icon.png");
            if (logoStream != null) {
                logoImage.setImage(new Image(logoStream));
            } else {
                // Fallback to file path if resource not found
                logoImage.setImage(new Image("file:src/main/resources/com/example/demo/images/app_icon.png"));
            }
        } catch (Exception e) {
            System.err.println("Could not load application logo: " + e.getMessage());
        }
    }
    
    /**
     * Set up role-specific navigation and content
     * This method should be overridden by subclasses for role-specific implementations
     */
    protected void setupRoleSpecificUI() {
        // This method will be overridden by role-specific controllers
    }
    
    /**
     * Navigate to a different view
     * 
     * @param fxmlPath The path to the FXML file to load
     */
    protected void navigateTo(String fxmlPath) {
        if (fxmlPath != null && !fxmlPath.isEmpty()) {
            try {
                showLoading(true);
                
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                Parent view = loader.load();
                
                // Clear existing content and add the new view
                contentArea.getChildren().clear();
                contentArea.getChildren().add(view);
                
                // Hide loading overlay
                showLoading(false);
            } catch (IOException e) {
                e.printStackTrace();
                showLoading(false);
            }
        }
    }
    
    /**
     * Show or hide the loading overlay
     * 
     * @param show True to show the overlay, false to hide it
     */
    protected void showLoading(boolean show) {
        loadingOverlay.setVisible(show);
        loadingOverlay.setManaged(show);
    }
    
    /**
     * Handle the logout action
     */
    @FXML
    protected void handleLogout() {
        // Log out the user
        authService.logout();
        
        // Redirect to login screen
        try {
            HelloApplication.setRoot("view/LoginView");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Handle viewing the user profile
     */
    @FXML
    protected void handleViewProfile() {
        // To be implemented in subclasses
    }
    
    /**
     * Handle settings action
     */
    @FXML
    protected void handleSettings() {
        // To be implemented in subclasses
    }
    
    /**
     * Handle help and support action
     */
    @FXML
    protected void handleHelpSupport() {
        // To be implemented by subclasses
    }
} 