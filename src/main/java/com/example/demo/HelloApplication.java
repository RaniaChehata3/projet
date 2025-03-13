package com.example.demo;

import com.example.demo.api.SimpleHttpServer;
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
    private static SimpleHttpServer apiServer;

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
                
                // Start the API server
                initializeApiServer();
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
            } else {
                // Fallback to file path if resource not found
                stage.getIcons().add(new Image("file:src/main/resources/com/example/demo/images/app_icon.png"));
            }
        } catch (Exception e) {
            System.err.println("Could not load application icon: " + e.getMessage());
        }
        
        // Use the NavigationUtil to adjust stage size
        NavigationUtil.adjustStageToScreen(primaryStage);
        
        // Register a shutdown hook to stop the API server when the application closes
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (apiServer != null) {
                System.out.println("Shutting down API server...");
                apiServer.stop();
            }
        }));
        
        // Handle application close event
        stage.setOnCloseRequest(event -> {
            if (apiServer != null) {
                System.out.println("Stopping API server...");
                apiServer.stop();
            }
        });
        
        // Show the stage
        stage.show();
    }
    
    /**
     * Initialize the API server
     */
    private void initializeApiServer() {
        try {
            System.out.println("Starting API server...");
            apiServer = new SimpleHttpServer(5000); // Use port 5000 for the API server
            apiServer.start();
            System.out.println("API server started successfully on port 5000");
            
            // Display an info alert to notify the user
            javafx.application.Platform.runLater(() -> {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                alert.setTitle("API Server");
                alert.setHeaderText("API Server Started");
                alert.setContentText("The API server has been started on port 5000.\n\n" +
                                    "You can now generate web links for medical records instead of exporting PDFs.");
                alert.show();
            });
        } catch (Exception e) {
            System.err.println("Failed to start API server: " + e.getMessage());
            e.printStackTrace();
            
            // Display an error alert
            javafx.application.Platform.runLater(() -> {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                alert.setTitle("API Server Error");
                alert.setHeaderText("Failed to start API server");
                alert.setContentText("Error: " + e.getMessage() + "\n\nThe application will continue to run, but web link generation for medical records will not be available.");
                alert.show();
            });
        }
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
     * Set the root of the scene to a different FXML
     * 
     * @param fxml The path to the FXML file (without extension)
     * @throws IOException If the file cannot be loaded
     */
    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
        primaryStage.centerOnScreen();
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * Test login with default credentials to verify system
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
            
            // Define ALL test users to verify
            String[][] testUsers = {
                {"sys_admin", "password", "ADMIN", "System Administrator"},
                {"dr_smith", "password", "DOCTOR", "Dr. John Smith"},
                {"dr_jones", "password", "DOCTOR", "Dr. Sarah Jones"},
                {"john_patient", "password", "PATIENT", "John Doe"},
                {"jane_patient", "password", "PATIENT", "Jane Doe"},
                {"lab_tech", "password", "LABORATORY", "Michael Johnson"},
                {"lab_manager", "password", "LABORATORY", "Emily Davis"}
            };
            
            int successCount = 0;
            int totalUsers = testUsers.length;
            
            System.out.println("\n====================================================");
            System.out.println("  TESTING ALL USERS WITH AUTO-LOGIN");
            System.out.println("====================================================");
            
            for (String[] userInfo : testUsers) {
                String username = userInfo[0];
                String password = userInfo[1];
                String expectedRole = userInfo[2];
                String fullName = userInfo[3];
                
                try {
                    System.out.println("\nTesting login for: " + fullName + " (" + username + ")");
                    Optional<User> userOpt = authService.testLogin(username, password);
                    
                    if (userOpt.isPresent()) {
                        User user = userOpt.get();
                        System.out.println("✓ SUCCESS: Verified login for " + username + 
                                           " with role: " + user.getRole());
                        successCount++;
                    } else {
                        System.err.println("✗ FAILED: Login failed for user: " + username);
                        System.err.println("  - Expected role: " + expectedRole);
                        System.err.println("  - Expected user: " + fullName);
                        System.err.println("  - Common issues:");
                        System.err.println("    1. Database hash might not match expected MD5 hash");
                        System.err.println("    2. User might not exist or be inactive");
                    }
                } catch (Exception e) {
                    System.err.println("✗ ERROR: Test login exception for user '" + username + "': " + e.getMessage());
                    e.printStackTrace(System.err);
                }
            }
            
            System.out.println("\n====================================================");
            System.out.println("TEST LOGIN SUMMARY: " + successCount + " of " + totalUsers + " users verified");
            if (successCount == totalUsers) {
                System.out.println("✓ ALL LOGINS SUCCESSFUL");
            } else if (successCount == 0) {
                System.err.println("✗ ALL LOGINS FAILED - Check database connection and users table");
            } else {
                System.out.println("⚠ SOME LOGINS FAILED - See error messages above");
                float successPercentage = (float) successCount / totalUsers * 100;
                System.out.printf("Success rate: %.1f%%\n", successPercentage);
            }
            System.out.println("====================================================\n");
        } catch (Exception e) {
            System.err.println("Error initializing test login system: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
