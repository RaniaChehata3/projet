package com.example.demo.util;

import com.example.demo.model.MedicalRecord;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;

/**
 * Client for interacting with the API that generates links to patient records
 */
public class RecordLinkApiClient {
    private static final String API_URL = "http://localhost:5000/api/records/generate-link";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Generate a web link for a medical record asynchronously
     * 
     * @param record The medical record to generate a link for
     * @return CompletableFuture with the record page URL if successful, null if failed
     */
    public static CompletableFuture<String> generateRecordLink(MedicalRecord record) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Create the URL and open connection
                URL url = new URL(API_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                // Create JSON payload
                JSONObject requestBody = new JSONObject();
                requestBody.put("patient_name", record.getPatientName());
                requestBody.put("doctor_id", record.getDoctorId());
                requestBody.put("doctor_name", record.getDoctorName());
                requestBody.put("visit_type", record.getVisitType());
                
                if (record.getVisitDate() != null) {
                    requestBody.put("visit_date", record.getVisitDate().format(DATE_FORMATTER));
                }
                
                requestBody.put("diagnosis", record.getDiagnosis());
                requestBody.put("symptoms", record.getSymptoms());
                requestBody.put("treatment", record.getTreatment());
                requestBody.put("notes", record.getNotes());
                requestBody.put("status", record.getStatus());
                requestBody.put("diagnosis_codes", record.getDiagnosisCodes());
                requestBody.put("attachments", record.getAttachments());
                
                if (record.getFollowUpDate() != null) {
                    requestBody.put("follow_up_date", record.getFollowUpDate().format(DATE_FORMATTER));
                }
                
                requestBody.put("record_type", record.getRecordType());

                // Send request
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                // Read response
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    // Parse JSON response
                    JSONObject jsonResponse = new JSONObject(response.toString());
                    if (jsonResponse.getBoolean("success")) {
                        JSONObject links = jsonResponse.getJSONObject("links");
                        return links.getString("record_page");
                    } else {
                        showError("API Error", "Failed to generate record link: " + 
                                  jsonResponse.getString("message"));
                    }
                } else {
                    showError("API Error", "API request failed with status: " + responseCode);
                }
            } catch (Exception e) {
                showError("Connection Error", "Failed to connect to API: " + e.getMessage());
                e.printStackTrace();
            }
            return null;
        });
    }

    /**
     * Opens the URL in the default browser
     * 
     * @param url The URL to open
     * @return true if successful, false otherwise
     */
    public static boolean openInBrowser(String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }
        
        try {
            // Encode the URL properly for opening in browser
            String encodedUrl = URLEncoder.encode(url, StandardCharsets.UTF_8.toString())
                    .replace("+", "%20")
                    .replace("%3A", ":")
                    .replace("%2F", "/")
                    .replace("%3F", "?")
                    .replace("%3D", "=")
                    .replace("%26", "&");
            
            // Different commands based on operating system
            String os = System.getProperty("os.name").toLowerCase();
            Runtime rt = Runtime.getRuntime();
            
            if (os.contains("win")) {
                // For Windows
                rt.exec("rundll32 url.dll,FileProtocolHandler " + url);
            } else if (os.contains("mac")) {
                // For Mac
                rt.exec("open " + url);
            } else if (os.contains("nix") || os.contains("nux")) {
                // For Linux
                String[] browsers = {"google-chrome", "firefox", "mozilla", "opera", "safari"};
                String browser = null;
                
                for (String b : browsers) {
                    Process p = rt.exec("which " + b);
                    if (p.waitFor() == 0) {
                        browser = b;
                        break;
                    }
                }
                
                if (browser != null) {
                    rt.exec(new String[]{browser, url});
                } else {
                    rt.exec(new String[]{"xdg-open", url});
                }
            }
            
            return true;
        } catch (Exception e) {
            showError("Browser Error", "Failed to open browser: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    private static void showError(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText("Error generating record link");
            alert.setContentText(message);
            alert.getDialogPane().getStyleClass().add("custom-alert");
            alert.showAndWait();
        });
    }
} 