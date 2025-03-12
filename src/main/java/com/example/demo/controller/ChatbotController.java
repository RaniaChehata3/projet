package com.example.demo.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class ChatbotController {

    @FXML
    private TextArea chatArea;

    @FXML
    private TextField userInput;

    @FXML
    private Button sendButton;

    @FXML
    public void sendMessage() {
        String message = userInput.getText();
        if (!message.isEmpty()) {
            chatArea.appendText("You: " + message + "\n");
            userInput.clear();
            String response = getResponse(message);
            chatArea.appendText("Bot: " + response + "\n");
        }
    }

    private String getResponse(String message) {
        // Simple logic for symptom diagnosis (you can replace this with a more complex
        // logic)
        if (message.toLowerCase().contains("fever")) {
            return "It seems you might have a fever. Please consult a doctor.";
        } else if (message.toLowerCase().contains("cough")) {
            return "A cough can be a sign of various conditions. Consider seeing a healthcare provider.";
        } else {
            return "I'm not sure about that. Please consult a healthcare professional.";
        }
    }
}