package com.example.demo.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

// Use Java's built-in HTTP server
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import org.json.JSONObject;

import com.example.demo.database.MedicalRecordDAO;
import com.example.demo.model.MedicalRecord;
import com.example.demo.controller.RecordViewController;

/**
 * Lightweight embedded HTTP server for handling API requests
 * Uses Java's built-in HTTP server
 */
public class SimpleHttpServer {
    private HttpServer server;
    private final int port;
    private static final Map<String, Integer> recordLinks = new HashMap<>();
    private final MedicalRecordDAO medicalRecordDAO = new MedicalRecordDAO();
    
    /**
     * Constructor with default port
     */
    public SimpleHttpServer() {
        this(5000); // Default port
    }
    
    /**
     * Constructor with specified port
     * 
     * @param port The port to listen on
     */
    public SimpleHttpServer(int port) {
        this.port = port;
    }
    
    /**
     * Start the API server
     * 
     * @throws IOException If server cannot be started
     */
    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        
        // Register API context
        server.createContext("/api/records/generate-link", new LinkGenerationHandler());
        server.createContext("/api/health", new HealthCheckHandler());
        server.createContext("/api/records/print", new PrintRecordHandler());
        
        // Set executor
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        
        System.out.println("API Server started on port " + port);
    }
    
    /**
     * Stop the API server
     */
    public void stop() {
        if (server != null) {
            server.stop(0);
            System.out.println("API Server stopped");
        }
    }
    
    /**
     * Handler for record link generation
     */
    private class LinkGenerationHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            try {
                // Only handle POST requests
                if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                    sendResponse(exchange, 405, "{ \"error\": \"Method not allowed\" }");
                    return;
                }
                
                // Set CORS headers
                exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
                exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
                
                if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                    exchange.sendResponseHeaders(204, -1);
                    return;
                }
                
                // Parse request body
                String requestBody = new BufferedReader(new InputStreamReader(exchange.getRequestBody()))
                        .lines().collect(Collectors.joining("\n"));
                
                JSONObject requestJson = new JSONObject(requestBody);
                
                // Create medical record
                MedicalRecord record = new MedicalRecord();
                
                // Map fields from the request with defaults for missing fields
                // Patient info
                if (requestJson.has("patient_id") && !requestJson.isNull("patient_id")) {
                    record.setPatientId(requestJson.getInt("patient_id"));
                }
                
                if (requestJson.has("patient_name") && !requestJson.isNull("patient_name")) {
                    record.setPatientName(requestJson.getString("patient_name"));
                } else {
                    record.setPatientName("Unknown Patient");
                }
                
                // Doctor info
                if (requestJson.has("doctor_id") && !requestJson.isNull("doctor_id")) {
                    record.setDoctorId(requestJson.getInt("doctor_id"));
                }
                
                if (requestJson.has("doctor_name") && !requestJson.isNull("doctor_name")) {
                    record.setDoctorName(requestJson.getString("doctor_name"));
                } else {
                    record.setDoctorName("Unknown Doctor");
                }
                
                // Visit info
                if (requestJson.has("visit_type") && !requestJson.isNull("visit_type")) {
                    record.setVisitType(requestJson.getString("visit_type"));
                } else {
                    record.setVisitType("Regular visit");
                }
                
                if (requestJson.has("record_type") && !requestJson.isNull("record_type")) {
                    record.setRecordType(requestJson.getString("record_type"));
                } else if (requestJson.has("visit_type") && !requestJson.isNull("visit_type")) {
                    // Default to visit_type if record_type is not specified
                    record.setRecordType(requestJson.getString("visit_type"));
                } else {
                    record.setRecordType("Regular visit");
                }
                
                // Set visit date with appropriate parsing
                if (requestJson.has("visit_date") && !requestJson.isNull("visit_date")) {
                    try {
                        String dateStr = requestJson.getString("visit_date");
                        // Handle ISO format with timezone (e.g., 2023-08-15T10:30:00Z)
                        if (dateStr.contains("T") && (dateStr.endsWith("Z") || dateStr.contains("+"))) {
                            // Parse ISO date time
                            java.time.Instant instant = java.time.Instant.parse(dateStr);
                            record.setVisitDate(java.time.LocalDateTime.ofInstant(instant, java.time.ZoneId.systemDefault()));
                        } else {
                            // Fallback to simple date parsing
                            record.setVisitDate(java.time.LocalDateTime.parse(dateStr));
                        }
                    } catch (Exception e) {
                        System.err.println("Error parsing visit_date: " + e.getMessage());
                        // Default to current date/time
                        record.setVisitDate(java.time.LocalDateTime.now());
                    }
                } else {
                    record.setVisitDate(java.time.LocalDateTime.now());
                }
                
                // Set follow-up date
                if (requestJson.has("follow_up_date") && !requestJson.isNull("follow_up_date")) {
                    try {
                        String dateStr = requestJson.getString("follow_up_date");
                        // Handle ISO format with timezone
                        if (dateStr.contains("T") && (dateStr.endsWith("Z") || dateStr.contains("+"))) {
                            java.time.Instant instant = java.time.Instant.parse(dateStr);
                            record.setFollowUpDate(java.time.LocalDateTime.ofInstant(instant, java.time.ZoneId.systemDefault()));
                        } else {
                            record.setFollowUpDate(java.time.LocalDateTime.parse(dateStr));
                        }
                    } catch (Exception e) {
                        System.err.println("Error parsing follow_up_date: " + e.getMessage());
                        // Leave as null if parsing fails
                    }
                }
                
                // Clinical info
                if (requestJson.has("symptoms") && !requestJson.isNull("symptoms")) {
                    record.setSymptoms(requestJson.getString("symptoms"));
                } else {
                    record.setSymptoms("Not specified");
                }
                
                if (requestJson.has("diagnosis") && !requestJson.isNull("diagnosis")) {
                    record.setDiagnosis(requestJson.getString("diagnosis"));
                } else {
                    record.setDiagnosis("Not specified");
                }
                
                if (requestJson.has("treatment") && !requestJson.isNull("treatment")) {
                    record.setTreatment(requestJson.getString("treatment"));
                } else {
                    record.setTreatment("Not specified");
                }
                
                // Additional info
                if (requestJson.has("notes") && !requestJson.isNull("notes")) {
                    record.setNotes(requestJson.getString("notes"));
                } else {
                    record.setNotes("");
                }
                
                if (requestJson.has("status") && !requestJson.isNull("status")) {
                    record.setStatus(requestJson.getString("status"));
                } else {
                    record.setStatus("Pending");
                }
                
                if (requestJson.has("diagnosis_codes") && !requestJson.isNull("diagnosis_codes")) {
                    record.setDiagnosisCodes(requestJson.getString("diagnosis_codes"));
                } else {
                    record.setDiagnosisCodes("");
                }
                
                if (requestJson.has("attachments") && !requestJson.isNull("attachments")) {
                    record.setAttachments(requestJson.getString("attachments"));
                } else {
                    record.setAttachments("");
                }
                
                // Save record to database
                boolean success = medicalRecordDAO.createMedicalRecord(record);
                
                // Generate response
                if (!success) {
                    sendResponse(exchange, 500, "{ \"success\": false, \"message\": \"Failed to save record\" }");
                    return;
                }
                
                // Create response according to expected format
                JSONObject responseJson = new JSONObject();
                responseJson.put("success", true);
                responseJson.put("message", "Record created successfully");
                responseJson.put("record_id", record.getRecordId());
                responseJson.put("patient_id", record.getPatientId());
                
                // Generate unique links
                String recordKey = java.util.UUID.randomUUID().toString();
                recordLinks.put(recordKey, record.getRecordId());
                
                // Register the key with RecordViewController
                RecordViewController.registerRecordKey(recordKey, record.getRecordId());
                
                // Create links object
                JSONObject links = new JSONObject();
                links.put("record_page", "http://localhost:5000/api/records/print/" + record.getRecordId());
                responseJson.put("links", links);
                
                sendResponse(exchange, 200, responseJson.toString());
                
            } catch (Exception e) {
                e.printStackTrace();
                sendResponse(exchange, 500, "{ \"success\": false, \"message\": \"" + e.getMessage().replace("\"", "'") + "\" }");
            }
        }
    }
    
    /**
     * Simple health check handler
     */
    private class HealthCheckHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            sendResponse(exchange, 200, "{ \"status\": \"healthy\", \"api\": \"records\", \"version\": \"1.0\" }");
        }
    }
    
    /**
     * Handler for record printing/viewing
     */
    private class PrintRecordHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            try {
                // Set CORS headers
                exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, OPTIONS");
                exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
                
                if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                    exchange.sendResponseHeaders(204, -1);
                    return;
                }
                
                // Only handle GET requests
                if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                    sendResponse(exchange, 405, "{ \"error\": \"Method not allowed\" }");
                    return;
                }
                
                // Parse record ID from path
                String path = exchange.getRequestURI().getPath();
                String[] pathParts = path.split("/");
                
                if (pathParts.length < 4) {
                    sendResponse(exchange, 400, "{ \"error\": \"Missing record ID\" }");
                    return;
                }
                
                // Extract record ID (the last part of the path)
                String recordIdStr = pathParts[pathParts.length - 1];
                int recordId;
                
                try {
                    recordId = Integer.parseInt(recordIdStr);
                } catch (NumberFormatException e) {
                    sendResponse(exchange, 400, "{ \"error\": \"Invalid record ID format\" }");
                    return;
                }
                
                // Fetch record from database
                java.util.Optional<MedicalRecord> recordOpt = medicalRecordDAO.getMedicalRecordById(recordId);
                
                if (!recordOpt.isPresent()) {
                    sendResponse(exchange, 404, "{ \"error\": \"Record not found\" }");
                    return;
                }
                
                MedicalRecord record = recordOpt.get();
                
                // Generate HTML view
                String html = generateRecordHtml(record);
                
                // Return HTML response
                exchange.getResponseHeaders().set("Content-Type", "text/html");
                exchange.sendResponseHeaders(200, html.length());
                
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(html.getBytes());
                }
                
            } catch (Exception e) {
                e.printStackTrace();
                sendResponse(exchange, 500, "{ \"success\": false, \"message\": \"" + e.getMessage().replace("\"", "'") + "\" }");
            }
        }
        
        /**
         * Generate HTML view for a medical record
         */
        private String generateRecordHtml(MedicalRecord record) {
            StringBuilder html = new StringBuilder();
            
            // Build HTML document with a modern, clean design
            html.append("<!DOCTYPE html>\n");
            html.append("<html lang=\"en\">\n");
            html.append("<head>\n");
            html.append("    <meta charset=\"UTF-8\">\n");
            html.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
            html.append("    <title>Medical Record #").append(record.getRecordId()).append("</title>\n");
            html.append("    <style>\n");
            html.append("        body {\n");
            html.append("            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;\n");
            html.append("            line-height: 1.6;\n");
            html.append("            color: #333;\n");
            html.append("            max-width: 800px;\n");
            html.append("            margin: 0 auto;\n");
            html.append("            padding: 20px;\n");
            html.append("        }\n");
            html.append("        .record-container {\n");
            html.append("            border: 1px solid #ddd;\n");
            html.append("            border-radius: 8px;\n");
            html.append("            padding: 20px;\n");
            html.append("            box-shadow: 0 2px 4px rgba(0,0,0,0.1);\n");
            html.append("        }\n");
            html.append("        .record-header {\n");
            html.append("            background-color: #f8f9fa;\n");
            html.append("            padding: 15px;\n");
            html.append("            border-radius: 6px;\n");
            html.append("            margin-bottom: 20px;\n");
            html.append("            border-left: 4px solid #4a6bff;\n");
            html.append("        }\n");
            html.append("        .record-title {\n");
            html.append("            color: #2c3e50;\n");
            html.append("            margin: 0;\n");
            html.append("            font-size: 1.8rem;\n");
            html.append("        }\n");
            html.append("        .record-id {\n");
            html.append("            color: #6c757d;\n");
            html.append("            font-size: 1rem;\n");
            html.append("            margin: 5px 0 0 0;\n");
            html.append("        }\n");
            html.append("        .section {\n");
            html.append("            margin-bottom: 25px;\n");
            html.append("        }\n");
            html.append("        .section-title {\n");
            html.append("            color: #4a6bff;\n");
            html.append("            border-bottom: 2px solid #eee;\n");
            html.append("            padding-bottom: 5px;\n");
            html.append("            margin-bottom: 15px;\n");
            html.append("            font-size: 1.4rem;\n");
            html.append("        }\n");
            html.append("        .field {\n");
            html.append("            margin-bottom: 10px;\n");
            html.append("        }\n");
            html.append("        .field-label {\n");
            html.append("            font-weight: 600;\n");
            html.append("            color: #495057;\n");
            html.append("            display: inline-block;\n");
            html.append("            width: 150px;\n");
            html.append("        }\n");
            html.append("        .field-value {\n");
            html.append("            display: inline-block;\n");
            html.append("        }\n");
            html.append("        .status {\n");
            html.append("            display: inline-block;\n");
            html.append("            padding: 3px 10px;\n");
            html.append("            border-radius: 15px;\n");
            html.append("            font-size: 0.85rem;\n");
            html.append("            font-weight: 600;\n");
            html.append("        }\n");
            html.append("        .status-completed {\n");
            html.append("            background-color: #d4edda;\n");
            html.append("            color: #155724;\n");
            html.append("        }\n");
            html.append("        .status-pending {\n");
            html.append("            background-color: #fff3cd;\n");
            html.append("            color: #856404;\n");
            html.append("        }\n");
            html.append("        .status-canceled {\n");
            html.append("            background-color: #f8d7da;\n");
            html.append("            color: #721c24;\n");
            html.append("        }\n");
            html.append("        .notes {\n");
            html.append("            background-color: #f8f9fa;\n");
            html.append("            padding: 15px;\n");
            html.append("            border-radius: 6px;\n");
            html.append("            white-space: pre-wrap;\n");
            html.append("        }\n");
            html.append("        .print-button {\n");
            html.append("            display: block;\n");
            html.append("            text-align: center;\n");
            html.append("            margin: 30px 0;\n");
            html.append("        }\n");
            html.append("        .btn {\n");
            html.append("            background-color: #4a6bff;\n");
            html.append("            color: white;\n");
            html.append("            border: none;\n");
            html.append("            padding: 10px 20px;\n");
            html.append("            border-radius: 4px;\n");
            html.append("            cursor: pointer;\n");
            html.append("            font-size: 1rem;\n");
            html.append("        }\n");
            html.append("        .btn:hover {\n");
            html.append("            background-color: #3a59e0;\n");
            html.append("        }\n");
            html.append("        @media print {\n");
            html.append("            .print-button { display: none; }\n");
            html.append("            body { max-width: 100%; }\n");
            html.append("        }\n");
            html.append("    </style>\n");
            html.append("</head>\n");
            html.append("<body>\n");
            
            // Record container
            html.append("    <div class=\"record-container\">\n");
            
            // Header
            html.append("        <div class=\"record-header\">\n");
            html.append("            <h1 class=\"record-title\">Medical Record</h1>\n");
            html.append("            <p class=\"record-id\">ID: MR-").append(record.getRecordId()).append("</p>\n");
            html.append("        </div>\n");
            
            // Patient Information
            html.append("        <div class=\"section\">\n");
            html.append("            <h2 class=\"section-title\">Patient Information</h2>\n");
            html.append("            <div class=\"field\">\n");
            html.append("                <span class=\"field-label\">Patient:</span>\n");
            html.append("                <span class=\"field-value\">").append(escapeHtml(record.getPatientName())).append("</span>\n");
            html.append("            </div>\n");
            html.append("            <div class=\"field\">\n");
            html.append("                <span class=\"field-label\">Patient ID:</span>\n");
            html.append("                <span class=\"field-value\">").append(record.getPatientId()).append("</span>\n");
            html.append("            </div>\n");
            html.append("        </div>\n");
            
            // Visit Information
            html.append("        <div class=\"section\">\n");
            html.append("            <h2 class=\"section-title\">Visit Information</h2>\n");
            html.append("            <div class=\"field\">\n");
            html.append("                <span class=\"field-label\">Date:</span>\n");
            html.append("                <span class=\"field-value\">").append(formatDateTime(record.getVisitDate())).append("</span>\n");
            html.append("            </div>\n");
            html.append("            <div class=\"field\">\n");
            html.append("                <span class=\"field-label\">Type:</span>\n");
            html.append("                <span class=\"field-value\">").append(escapeHtml(record.getVisitType())).append("</span>\n");
            html.append("            </div>\n");
            html.append("            <div class=\"field\">\n");
            html.append("                <span class=\"field-label\">Doctor:</span>\n");
            html.append("                <span class=\"field-value\">").append(escapeHtml(record.getDoctorName())).append("</span>\n");
            html.append("            </div>\n");
            html.append("            <div class=\"field\">\n");
            html.append("                <span class=\"field-label\">Status:</span>\n");
            String statusClass = "status-pending";
            if (record.getStatus().equalsIgnoreCase("completed")) {
                statusClass = "status-completed";
            } else if (record.getStatus().equalsIgnoreCase("canceled")) {
                statusClass = "status-canceled";
            }
            html.append("                <span class=\"field-value status ").append(statusClass).append("\">")
                .append(escapeHtml(record.getStatus())).append("</span>\n");
            html.append("            </div>\n");
            
            // Follow-up date if present
            if (record.getFollowUpDate() != null) {
                html.append("            <div class=\"field\">\n");
                html.append("                <span class=\"field-label\">Follow-up Date:</span>\n");
                html.append("                <span class=\"field-value\">").append(formatDateTime(record.getFollowUpDate())).append("</span>\n");
                html.append("            </div>\n");
            }
            
            html.append("        </div>\n");
            
            // Clinical Information
            html.append("        <div class=\"section\">\n");
            html.append("            <h2 class=\"section-title\">Clinical Information</h2>\n");
            html.append("            <div class=\"field\">\n");
            html.append("                <span class=\"field-label\">Symptoms:</span>\n");
            html.append("                <span class=\"field-value\">").append(escapeHtml(record.getSymptoms())).append("</span>\n");
            html.append("            </div>\n");
            html.append("            <div class=\"field\">\n");
            html.append("                <span class=\"field-label\">Diagnosis:</span>\n");
            html.append("                <span class=\"field-value\">").append(escapeHtml(record.getDiagnosis())).append("</span>\n");
            html.append("            </div>\n");
            
            if (record.getDiagnosisCodes() != null && !record.getDiagnosisCodes().isEmpty()) {
                html.append("            <div class=\"field\">\n");
                html.append("                <span class=\"field-label\">Diagnosis Codes:</span>\n");
                html.append("                <span class=\"field-value\">").append(escapeHtml(record.getDiagnosisCodes())).append("</span>\n");
                html.append("            </div>\n");
            }
            
            html.append("            <div class=\"field\">\n");
            html.append("                <span class=\"field-label\">Treatment:</span>\n");
            html.append("                <span class=\"field-value\">").append(escapeHtml(record.getTreatment())).append("</span>\n");
            html.append("            </div>\n");
            html.append("        </div>\n");
            
            // Notes
            if (record.getNotes() != null && !record.getNotes().isEmpty()) {
                html.append("        <div class=\"section\">\n");
                html.append("            <h2 class=\"section-title\">Additional Notes</h2>\n");
                html.append("            <div class=\"notes\">").append(escapeHtml(record.getNotes())).append("</div>\n");
                html.append("        </div>\n");
            }
            
            // Attachments if present
            if (record.getAttachments() != null && !record.getAttachments().isEmpty() && !record.getAttachments().equals("null")) {
                html.append("        <div class=\"section\">\n");
                html.append("            <h2 class=\"section-title\">Attachments</h2>\n");
                try {
                    JSONObject attachmentsJson = new JSONObject(record.getAttachments());
                    // If it's a JSONArray
                    if (record.getAttachments().startsWith("[")) {
                        org.json.JSONArray attachments = new org.json.JSONArray(record.getAttachments());
                        for (int i = 0; i < attachments.length(); i++) {
                            JSONObject attachment = attachments.getJSONObject(i);
                            html.append("            <div class=\"field\">\n");
                            html.append("                <span class=\"field-label\">").append(escapeHtml(attachment.getString("name"))).append(":</span>\n");
                            html.append("                <span class=\"field-value\"><a href=\"").append(escapeHtml(attachment.getString("url")))
                                .append("\" target=\"_blank\">View Attachment</a></span>\n");
                            html.append("            </div>\n");
                        }
                    } else {
                        // Single attachment
                        html.append("            <div class=\"field\">\n");
                        html.append("                <span class=\"field-label\">").append(escapeHtml(attachmentsJson.getString("name"))).append(":</span>\n");
                        html.append("                <span class=\"field-value\"><a href=\"").append(escapeHtml(attachmentsJson.getString("url")))
                            .append("\" target=\"_blank\">View Attachment</a></span>\n");
                        html.append("            </div>\n");
                    }
                } catch (Exception e) {
                    // If it's not valid JSON, just display as text
                    html.append("            <div class=\"field\">\n");
                    html.append("                <span class=\"field-value\">").append(escapeHtml(record.getAttachments())).append("</span>\n");
                    html.append("            </div>\n");
                }
                html.append("        </div>\n");
            }
            
            html.append("    </div>\n");
            
            // Print button
            html.append("    <div class=\"print-button\">\n");
            html.append("        <button class=\"btn\" onclick=\"window.print()\">Print Record</button>\n");
            html.append("    </div>\n");
            
            // JavaScript
            html.append("    <script>\n");
            html.append("        document.addEventListener('DOMContentLoaded', function() {\n");
            html.append("            // Any additional JavaScript functionality can be added here\n");
            html.append("        });\n");
            html.append("    </script>\n");
            
            html.append("</body>\n");
            html.append("</html>");
            
            return html.toString();
        }
        
        /**
         * Format date time for display
         */
        private String formatDateTime(java.time.LocalDateTime dateTime) {
            if (dateTime == null) return "N/A";
            return dateTime.format(java.time.format.DateTimeFormatter.ofPattern("MMMM d, yyyy h:mm a"));
        }
        
        /**
         * Escape HTML for safe output
         */
        private String escapeHtml(String input) {
            if (input == null) return "";
            return input.replace("&", "&amp;")
                        .replace("<", "&lt;")
                        .replace(">", "&gt;")
                        .replace("\"", "&quot;")
                        .replace("'", "&#39;");
        }
    }
    
    /**
     * Send HTTP response
     */
    private void sendResponse(HttpExchange exchange, int statusCode, String body) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, body.length());
        
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body.getBytes());
        }
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