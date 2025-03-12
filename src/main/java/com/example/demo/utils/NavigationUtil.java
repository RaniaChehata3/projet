package com.example.demo.utils;

import com.example.demo.HelloApplication;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Utility class for handling navigation between views in the application
 */
public class NavigationUtil {

    /**
     * Switch to a different view
     * 
     * @param fxmlPath The path to the FXML file (without extension)
     * @param currentScene The current scene to replace
     * @throws IOException If the FXML file cannot be loaded
     */
    public static void switchView(String fxmlPath, Scene currentScene) throws IOException {
        // Load the new view
        FXMLLoader loader = new FXMLLoader(NavigationUtil.class.getResource("/com/example/demo/" + fxmlPath + ".fxml"));
        Parent root = loader.load();
        
        // Replace the scene content
        Stage stage = (Stage) currentScene.getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        
        // Configure stage size to fit screen
        adjustStageToScreen(stage);
    }

    /**
     * Switch to a different view
     * 
     * @param fxmlPath The path to the FXML file (without extension)
     * @param node Any node from the current scene
     * @throws IOException If the FXML file cannot be loaded
     */
    public static void switchView(String fxmlPath, javafx.scene.Node node) throws IOException {
        Scene currentScene = node.getScene();
        switchView(fxmlPath, currentScene);
    }
    
    /**
     * Navigate to a different view using the full FXML path
     * 
     * @param currentScene The current scene to replace
     * @param fullFxmlPath The full path to the FXML file (including extension)
     * @throws IOException If the FXML file cannot be loaded
     */
    public static void navigateTo(Scene currentScene, String fullFxmlPath) throws IOException {
        // Load the new view
        FXMLLoader loader = new FXMLLoader(NavigationUtil.class.getResource(fullFxmlPath));
        Parent root = loader.load();
        
        // Replace the scene content
        Stage stage = (Stage) currentScene.getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        
        // Configure stage size to fit screen
        adjustStageToScreen(stage);
    }
    
    /**
     * Adjust a stage to fit properly on the screen
     * 
     * @param stage The stage to adjust
     */
    public static void adjustStageToScreen(Stage stage) {
        // Get screen dimensions
        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();
        
        // Set optimal size that fits within screen bounds
        double width = Math.min(bounds.getWidth() * 0.95, 1366);
        double height = Math.min(bounds.getHeight() * 0.95, 768);
        
        stage.setWidth(width);
        stage.setHeight(height);
        
        // Set minimum size constraints
        stage.setMinWidth(800);
        stage.setMinHeight(600);
        
        // Center on screen and ensure not maximized to prevent overflow
        stage.centerOnScreen();
        stage.setMaximized(false);
    }
} 