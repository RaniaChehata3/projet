package com.example.demo.controller;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class SymptomesController {

    @FXML
    private VBox chatContainer;

    @FXML
    private TextField userInput;

    @FXML
    private Button sendButton;

    @FXML
    private ScrollPane chatScrollPane;

    @FXML
    public void initialize() {
        // Initialize the chatbot interface
    }

    @FXML
    private void handleSend() {
        String userMessage = userInput.getText();
        if (!userMessage.trim().isEmpty()) {
            addMessage("You: " + userMessage, Pos.CENTER_RIGHT, "user-message");
            userInput.clear();
            respondToUser(userMessage);
        }
    }

    private void addMessage(String text, Pos alignment, String styleClass) {
        Label messageLabel = new Label(text);
        messageLabel.getStyleClass().add(styleClass);
        messageLabel.setWrapText(true);

        HBox messageContainer = new HBox(messageLabel);
        messageContainer.setAlignment(alignment);
        chatContainer.getChildren().add(messageContainer);

        // Scroll to the bottom of the chat
        chatScrollPane.layout();
        chatScrollPane.setVvalue(1.0);
    }

    private void respondToUser(String userMessage) {
        String response = switch (userMessage.toLowerCase()) {
            case "headache" -> "It might be due to stress or dehydration. Drink water and rest.";
            case "fever" -> "You might have an infection. Monitor your temperature and stay hydrated.";
            case "cough" -> "It could be a common cold or allergies. Consider seeing a doctor if it persists.";
            default -> "I am not sure. Please consult a healthcare professional.";
        };

        addMessage("Bot: " + response, Pos.CENTER_LEFT, "bot-message");
    }
}