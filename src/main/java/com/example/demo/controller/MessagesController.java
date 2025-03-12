package com.example.demo.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Controller for the Messages view
 */
public class MessagesController {

    @FXML
    private TextField searchField;
    
    @FXML
    private ListView<String> conversationsList;
    
    @FXML
    private Label contactInitialsLabel;
    
    @FXML
    private Label contactNameLabel;
    
    @FXML
    private Label contactStatusLabel;
    
    @FXML
    private VBox messagesContainer;
    
    @FXML
    private TextField messageField;
    
    /**
     * Initialize the controller
     */
    @FXML
    public void initialize() {
        // This is a placeholder controller, so just load mock data
        loadMockData();
        
        // Show placeholder alert
        showPlaceholderAlert();
    }
    
    /**
     * Load mock conversation data
     */
    private void loadMockData() {
        // Populate conversations list with sample data
        ObservableList<String> conversations = FXCollections.observableArrayList(
            "John Smith",
            "Sarah Johnson",
            "Dr. Michael Brown",
            "Lisa Chen",
            "David Wilson",
            "Dr. Emily Taylor",
            "Robert Garcia",
            "Nurse Maria Rodriguez"
        );
        
        conversationsList.setItems(conversations);
        conversationsList.getSelectionModel().selectFirst();
        
        // Set up event handler for conversation selection
        conversationsList.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                if (newValue != null) {
                    // In a real implementation, this would load the selected conversation
                    contactNameLabel.setText(newValue);
                    setContactInitials(newValue);
                }
            }
        );
    }
    
    /**
     * Set contact initials based on name
     */
    private void setContactInitials(String name) {
        if (name != null && !name.isEmpty()) {
            String[] nameParts = name.split(" ");
            if (nameParts.length > 1) {
                contactInitialsLabel.setText(
                    String.valueOf(nameParts[0].charAt(0)) + 
                    String.valueOf(nameParts[nameParts.length - 1].charAt(0))
                );
            } else {
                contactInitialsLabel.setText(String.valueOf(name.charAt(0)));
            }
        }
    }
    
    /**
     * Handle the new message button click
     */
    @FXML
    private void handleNewMessage() {
        showFeatureNotImplementedAlert("New Message");
    }
    
    /**
     * Handle the send button click
     */
    @FXML
    private void handleSend() {
        String message = messageField.getText().trim();
        if (!message.isEmpty()) {
            // In a real implementation, this would send the message
            // For now, just clear the field
            messageField.clear();
            showFeatureNotImplementedAlert("Send Message");
        }
    }
    
    /**
     * Handle the attachment button click
     */
    @FXML
    private void handleAttachment() {
        showFeatureNotImplementedAlert("Attachments");
    }
    
    /**
     * Handle the call button click
     */
    @FXML
    private void handleCall() {
        showFeatureNotImplementedAlert("Voice Call");
    }
    
    /**
     * Handle the video button click
     */
    @FXML
    private void handleVideo() {
        showFeatureNotImplementedAlert("Video Call");
    }
    
    /**
     * Handle viewing a contact's profile
     */
    @FXML
    private void handleViewProfile() {
        showFeatureNotImplementedAlert("View Profile");
    }
    
    /**
     * Handle archiving a conversation
     */
    @FXML
    private void handleArchive() {
        showFeatureNotImplementedAlert("Archive Conversation");
    }
    
    /**
     * Handle blocking a contact
     */
    @FXML
    private void handleBlock() {
        showFeatureNotImplementedAlert("Block Contact");
    }
    
    /**
     * Show an info alert informing the user this is a placeholder
     */
    private void showPlaceholderAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Placeholder View");
        alert.setHeaderText("Messages Module");
        alert.setContentText("This is a placeholder for the Messages module. The messaging functionality is not yet implemented.");
        alert.showAndWait();
    }
    
    /**
     * Show an alert for features that are not yet implemented
     */
    private void showFeatureNotImplementedAlert(String feature) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Feature Not Implemented");
        alert.setHeaderText(feature);
        alert.setContentText("This feature is not implemented in the current version.");
        alert.showAndWait();
    }
} 