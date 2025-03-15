package com.example.demo;

import com.example.demo.database.DatabaseConnection;
import com.example.demo.auth.AuthService;
import com.example.demo.model.User;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import com.example.demo.utils.NavigationUtil;
import java.io.StringWriter;
import java.io.PrintWriter;

public class HelloApplication extends Application {
    
    private static Scene scene;
    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        
        // Initialize database connection
        System.out.println("Initializing database connection...");
        try {
            DatabaseConnection dbConnection = DatabaseConnection.getInstance();
            
            // Test database connection first
            if (dbConnection.testConnection()) {
                System.out.println("Successfully connected to the sahacare database");
                
                // Initialize database schema if needed
                dbConnection.initializeDatabase();
                
                // Check if test data should be inserted (will only insert if no data exists)
                dbConnection.insertTestDataIfNeeded();
                
                // Test a login with existing credentials to verify system works
                if (dbConnection.hasExistingData()) {
                    testLoginWithExistingCredentials();
                }
            } else {
                System.err.println("Failed to connect to the database. Please check your database configuration.");
                // Show an error dialog to inform the user
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                alert.setTitle("Database Connection Error");
                alert.setHeaderText("Failed to connect to the database");
                alert.setContentText("Please ensure:\n1. MySQL server is running\n2. Database configuration is correct\n3. Execute setup_database.bat first if this is the first run");
                alert.showAndWait();
            }
        } catch (Exception e) {
            System.err.println("Database initialization error: " + e.getMessage());
            e.printStackTrace();
            
            // Show a more detailed error dialog
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Database Error");
            alert.setHeaderText("Database initialization failed");
            alert.setContentText("Error: " + e.getMessage() + "\n\nPlease check the console output for more details.");
            
            // Add exception details to the expandable content
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            
            javafx.scene.control.TextArea textArea = new javafx.scene.control.TextArea(sw.toString());
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setMaxWidth(Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);
            
            javafx.scene.layout.GridPane expContent = new javafx.scene.layout.GridPane();
            expContent.setMaxWidth(Double.MAX_VALUE);
            expContent.add(new javafx.scene.control.Label("Exception stacktrace:"), 0, 0);
            expContent.add(textArea, 0, 1);
            
            alert.getDialogPane().setExpandableContent(expContent);
            alert.showAndWait();
        }
        
        // Load the login view
        scene = new Scene(loadFXML("view/LoginView"));
        stage.setScene(scene);
        
        // Set application title
        stage.setTitle("Healthcare Management System");
        
        // Set application icon
        try {
            InputStream iconStream = getClass().getResourceAsStream("/com/example/demo/images/app_icon.png");
            if (iconStream != null) {
                stage.getIcons().add(new Image(iconStream));
            }
        } catch (Exception e) {
            System.out.println("Could not load application icon: " + e.getMessage());
        }
        
        // Show the stage
        stage.show();
    }
    
    /**
     * Load an FXML file
     * 
     * @param fxml The path to the FXML file (without extension)
     * @return The loaded FXML Parent node
     * @throws IOException If the file cannot be loaded
     */
    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("/com/example/demo/" + fxml + ".fxml"));
        return fxmlLoader.load();
    }
    
    /**
     * Set the root element of the scene
     * 
     * @param fxml The path to the FXML file (without extension)
     * @throws IOException If the file cannot be loaded
     */
    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }
    
    /**
     * Get the primary stage
     * 
     * @return The primary stage
     */
    public static Stage getPrimaryStage() {
        return primaryStage;
    }
    
    /**
     * Test login with existing credentials
     */
    private void testLoginWithExistingCredentials() {
        try {
            // Use the test login method instead of regular login
            AuthService authService = AuthService.getInstance();
            
            // Skip the automatic test login if there are startup issues
            if (authService == null) {
                System.err.println("Auth service not available, skipping test login");
                return;
            }
            
            // Define test users to match the auto-login buttons in LoginController
            String[][] testUsers = {
                {"admin", "password", "ADMIN", "System Administrator"},
                {"doctor", "password", "DOCTOR", "Dr. John Smith"},
                {"patient", "password", "PATIENT", "John Doe"},
                {"lab", "password", "LABORATORY", "Lab Technician"}
            };
            
            int successCount = 0;
            int totalUsers = testUsers.length;
            
            // Silent test login without console output
            for (String[] userInfo : testUsers) {
                String username = userInfo[0];
                String password = userInfo[1];
                
                try {
                    Optional<User> userOpt = authService.testLogin(username, password);
                    if (userOpt.isPresent()) {
                        successCount++;
                    }
                } catch (Exception e) {
                    // Silently ignore test login exceptions
                }
            }
            
            // Just log a simple summary without details
            if (successCount == 0) {
                System.out.println("Note: No test users could be authenticated. This may be normal for first run.");
            }
        } catch (Exception e) {
            // Silently ignore errors in test login system
        }
    }
    
    /**
     * Application entry point
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        launch();
    }
}
