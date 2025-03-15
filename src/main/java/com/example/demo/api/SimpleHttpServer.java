package com.example.demo.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.json.JSONObject;

import com.example.demo.database.MedicalRecordDAO;
import com.example.demo.model.MedicalRecord;

/**
 * Client for the external API server
 * This class has been modified to remove the internal HTTP server implementation
 * and only forward requests to the external API running on port 5000
 */
public class SimpleHttpServer {
    private final int port;
    private static final Map<String, Integer> recordLinks = new HashMap<>();
    private final MedicalRecordDAO medicalRecordDAO = new MedicalRecordDAO();
    private final String externalApiBaseUrl;
    
    /**
     * Constructor with default port
     */
    public SimpleHttpServer() {
        this(5000); // External API port
    }
    
    /**
     * Constructor with specified port
     * 
     * @param port The port of the external API
     */
    public SimpleHttpServer(int port) {
        this.port = port;
        this.externalApiBaseUrl = "http://localhost:" + port;
    }
    
    /**
     * Start the API client
     * This method is kept for backward compatibility but no longer starts a server
     * @throws IOException If server cannot be started
     */
    public void start() throws IOException {
        System.out.println("API client initialized for external API at " + externalApiBaseUrl);
    }
    
    /**
     * Stop the API client
     * This method is kept for backward compatibility
     */
    public void stop() {
        System.out.println("API client shutdown");
    }
    
    /**
     * Generate a link for a medical record through the external API
     * 
     * @param record The medical record to generate a link for
     * @return The generated link or null if generation failed
     */
    public String generateLink(MedicalRecord record) throws IOException {
        // Convert the record to JSON
        JSONObject requestJson = new JSONObject();
        
        // Add record fields
        requestJson.put("patient_id", record.getPatientId());
        requestJson.put("patient_name", record.getPatientName());
        requestJson.put("doctor_id", record.getDoctorId());
        requestJson.put("doctor_name", record.getDoctorName());
        requestJson.put("visit_type", record.getVisitType());
        requestJson.put("visit_date", record.getVisitDate());
        requestJson.put("diagnosis", record.getDiagnosis());
        requestJson.put("symptoms", record.getSymptoms());
        requestJson.put("treatment", record.getTreatment());
        requestJson.put("notes", record.getNotes());
        requestJson.put("status", record.getStatus());
        requestJson.put("diagnosis_codes", record.getDiagnosisCodes());
        requestJson.put("record_type", record.getRecordType());
        requestJson.put("follow_up_date", record.getFollowUpDate());
        requestJson.put("attachments", record.getAttachments());
        
        // Make request to external API
        URL url = new URL(externalApiBaseUrl + "/api/records/generate-link");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);
                
                // Send request
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = requestJson.toString().getBytes("utf-8");
                    os.write(input, 0, input.length);
                }
                
                // Read response
                int responseCode = connection.getResponseCode();
        if (responseCode >= 200 && responseCode < 300) {
                StringBuilder response = new StringBuilder();
                try (BufferedReader br = new BufferedReader(
                 new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                }
                
            // Parse response
            JSONObject responseJson = new JSONObject(response.toString());
            if (responseJson.has("link")) {
                return responseJson.getString("link");
            }
        }
        
        return null;
    }
    
    /**
     * Get record ID by key
     * 
     * @param key The record key
     * @return The record ID or null if not found
     */
    public static Integer getRecordIdByKey(String key) {
        return recordLinks.get(key);
    }
} 