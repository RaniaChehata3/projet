package com.example.demo.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.util.Duration;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public class ChatController {

    @FXML
    private ListView<String> chatList;

    @FXML
    private TextField messageField;

    // Define conversation states
    private enum ChatState {
        ENTER_ORIGINAL_SYMPTOM, // User enters initial symptom
        SELECT_SYMPTOM, // User selects from API1 search results
        ENTER_DAYS, // User enters number of days
        ASK_ASSOCIATED_SYMPTOMS, // Ask yes/no for each associated symptom from API2
        WAITING_FOR_PREDICTION // Waiting for final prediction (API3)
    }

    private ChatState currentState = ChatState.ENTER_ORIGINAL_SYMPTOM;

    // Conversation variables
    private String originalSymptom;
    private List<String> searchResults = new ArrayList<>(); // results from API1 (/search)
    private String selectedSymptom;
    private int days;
    private List<String> associatedSymptoms = new ArrayList<>(); // from API2 (/suggest)
    private List<String> finalSymptoms = new ArrayList<>(); // symptoms to send to API3 (/predict)
    private int currentAssociatedIndex = 0;

    @FXML
    public void initialize() {
        addBotMessage("Enter the symptom you are experiencing:");
    }

    @FXML
    public void handleSendMessage(ActionEvent event) {
        String userInput = messageField.getText().trim();
        if (userInput.isEmpty()) {
            return;
        }
        addUserMessage(userInput);
        messageField.clear();

        switch (currentState) {
            case ENTER_ORIGINAL_SYMPTOM:
                // Save the original symptom and call API1 (/search)
                originalSymptom = userInput;
                addBotMessage("Searching for related symptoms...");
                callSearchAPI(originalSymptom);
                break;

            case SELECT_SYMPTOM:
                // Expect the user to enter an index (number)
                try {
                    int index = Integer.parseInt(userInput);
                    if (index < 0 || index >= searchResults.size()) {
                        addBotMessage("Invalid selection. Please enter a number between 0 and "
                                + (searchResults.size() - 1) + ".");
                        return;
                    }
                    selectedSymptom = searchResults.get(index);
                    addBotMessage("You selected: " + selectedSymptom);
                    addBotMessage("Enter the number of days you've been experiencing these symptoms:");
                    currentState = ChatState.ENTER_DAYS;
                } catch (NumberFormatException e) {
                    addBotMessage("Please enter a valid number for selection.");
                }
                break;

            case ENTER_DAYS:
                // Parse the number of days and call API2 (/suggest)
                try {
                    days = Integer.parseInt(userInput);
                    addBotMessage("Calling suggestion API...");
                    callSuggestAPI(originalSymptom, selectedSymptom, days);
                } catch (NumberFormatException e) {
                    addBotMessage("Invalid number. Please enter a valid number of days.");
                }
                break;

            case ASK_ASSOCIATED_SYMPTOMS:
                // Expect "yes" or "no" for the current associated symptom
                if (userInput.equalsIgnoreCase("yes")) {
                    finalSymptoms.add(associatedSymptoms.get(currentAssociatedIndex));
                }
                currentAssociatedIndex++;
                if (currentAssociatedIndex < associatedSymptoms.size()) {
                    addBotMessage(
                            "Are you experiencing " + associatedSymptoms.get(currentAssociatedIndex) + "? (yes/no)");
                } else {
                    // Finished asking for all associated symptoms.
                    // Also include the selected symptom if not already added.
                    if (!finalSymptoms.contains(selectedSymptom)) {
                        finalSymptoms.add(selectedSymptom);
                    }
                    addBotMessage("Finalizing your symptoms and predicting...");
                    currentState = ChatState.WAITING_FOR_PREDICTION;
                    callPredictAPI(finalSymptoms, days);
                }
                break;

            case WAITING_FOR_PREDICTION:
                // Do nothing while waiting for prediction.
                break;
        }
    }

    // Utility methods to add messages to the chat view.
    private void addUserMessage(String message) {
        chatList.getItems().add("You: " + message);
    }

    private void addBotMessage(String message) {
        chatList.getItems().add("Bot: " + message);
    }

    // -------------------------------
    // API1: Search API (/search) 
    // -------------------------------
    private void callSearchAPI(String symptom) {
        String apiUrl = "http://127.0.0.1:5000/search";
        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {
                JSONObject json = new JSONObject();
                json.put("symptom", symptom);
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(new URI(apiUrl))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                        .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                return response.body();
            }
        };
        task.setOnSucceeded(e -> {
            try {
                JSONObject resObj = new JSONObject(task.getValue());
                JSONArray matches = resObj.getJSONArray("matching_symptoms");
                searchResults.clear();
                for (int i = 0; i < matches.length(); i++) {
                    searchResults.add(matches.getString(i));
                }
                if (searchResults.isEmpty()) {
                    // If no matches found, use the original symptom as the selected one.
                    selectedSymptom = originalSymptom;
                    addBotMessage("No related symptoms found. Using: " + selectedSymptom);
                    addBotMessage("Enter the number of days you've been experiencing these symptoms:");
                    currentState = ChatState.ENTER_DAYS;
                } else {
                    addBotMessage("Searches related to input:");
                    for (int i = 0; i < searchResults.size(); i++) {
                        addBotMessage(i + " ) " + searchResults.get(i));
                    }
                    addBotMessage("Select the one you meant (enter a number between 0 and " + (searchResults.size() - 1)
                            + "):");
                    currentState = ChatState.SELECT_SYMPTOM;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                addBotMessage("Error processing search results.");
                currentState = ChatState.ENTER_ORIGINAL_SYMPTOM;
            }
        });
        task.setOnFailed(e -> {
            addBotMessage("Error calling search API.");
            currentState = ChatState.ENTER_ORIGINAL_SYMPTOM;
        });
        new Thread(task).start();
    }

    // -------------------------------
    // API2: Suggest API (/suggest)
    // -------------------------------
    private void callSuggestAPI(String originalSymptom, String selectedSymptom, int days) {
        String apiUrl = "http://127.0.0.1:5000/suggest";
        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {
                JSONObject json = new JSONObject();
                json.put("selected_symptom", selectedSymptom);
                json.put("original_symptom", originalSymptom);
                json.put("days", days);
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(new URI(apiUrl))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                        .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                return response.body();
            }
        };
        task.setOnSucceeded(e -> {
            try {
                JSONObject resObj = new JSONObject(task.getValue());
                JSONArray assocArray = resObj.getJSONArray("associated_symptoms");
                associatedSymptoms.clear();
                for (int i = 0; i < assocArray.length(); i++) {
                    associatedSymptoms.add(assocArray.getString(i));
                }
                if (associatedSymptoms.isEmpty()) {
                    addBotMessage("No additional associated symptoms found.");
                    // Use selected symptom as final symptom.
                    finalSymptoms.clear();
                    finalSymptoms.add(selectedSymptom);
                    currentState = ChatState.WAITING_FOR_PREDICTION;
                    callPredictAPI(finalSymptoms, days);
                } else {
                    addBotMessage("Please answer yes/no for the following associated symptoms:");
                    currentAssociatedIndex = 0;
                    addBotMessage(
                            "Are you experiencing " + associatedSymptoms.get(currentAssociatedIndex) + "? (yes/no)");
                    currentState = ChatState.ASK_ASSOCIATED_SYMPTOMS;
                    finalSymptoms.clear(); // reset final symptoms
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                addBotMessage("Error processing suggestion results.");
                currentState = ChatState.ENTER_ORIGINAL_SYMPTOM;
            }
        });
        task.setOnFailed(e -> {
            addBotMessage("Error calling suggestion API.");
            currentState = ChatState.ENTER_ORIGINAL_SYMPTOM;
        });
        new Thread(task).start();
    }

    // -------------------------------
    // API3: Predict API (/predict)
    // -------------------------------
    private void callPredictAPI(List<String> symptoms, int days) {
        String apiUrl = "http://127.0.0.1:5000/predict";
        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {
                JSONObject json = new JSONObject();
                JSONArray sympArray = new JSONArray(symptoms);
                json.put("symptoms", sympArray);
                json.put("days", days);
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(new URI(apiUrl))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                        .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                return response.body();
            }
        };
        task.setOnSucceeded(e -> {
            displayChatResponse(task.getValue());
            resetConversation();
        });
        task.setOnFailed(e -> {
            addBotMessage("Error calling prediction API.");
            resetConversation();
        });
        new Thread(task).start();
    }

    // Resets conversation to start a new diagnosis.
    private void resetConversation() {
        currentState = ChatState.ENTER_ORIGINAL_SYMPTOM;
        originalSymptom = "";
        searchResults.clear();
        selectedSymptom = "";
        days = 0;
        associatedSymptoms.clear();
        finalSymptoms.clear();
        currentAssociatedIndex = 0;
        Timeline newPrompt = new Timeline(new KeyFrame(Duration.seconds(3), ev -> {
            addBotMessage("You can start a new diagnosis by entering a symptom.");
        }));
        newPrompt.play();
    }

    // Displays a multi-line response with a delay between each line.
    private void displayChatResponse(String response) {
        String[] messages = response.split("\n");
        Timeline timeline = new Timeline();
        for (int i = 0; i < messages.length; i++) {
            final int index = i;
            KeyFrame kf = new KeyFrame(Duration.seconds(index * 1.5), event -> {
                addBotMessage(messages[index]);
            });
            timeline.getKeyFrames().add(kf);
        }
        timeline.play();
    }
}
