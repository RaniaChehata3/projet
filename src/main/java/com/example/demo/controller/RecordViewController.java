package com.example.demo.controller;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for web-based record viewing
 * This class manages the record keys used in the API for web viewing
 */
public class RecordViewController {
    
    // Map to store record keys and their corresponding record IDs
    private static final Map<String, Integer> recordKeyMap = new HashMap<>();
    
    /**
     * Register a record key with its corresponding record ID
     * 
     * @param key The unique record key for web access
     * @param recordId The database record ID
     */
    public static void registerRecordKey(String key, int recordId) {
        recordKeyMap.put(key, recordId);
        System.out.println("Registered record key: " + key + " for record ID: " + recordId);
    }
    
    /**
     * Get the record ID corresponding to a record key
     * 
     * @param key The record key
     * @return The record ID, or null if the key is not found
     */
    public static Integer getRecordIdByKey(String key) {
        return recordKeyMap.get(key);
    }
    
    /**
     * Check if a record key exists
     * 
     * @param key The record key to check
     * @return true if the key exists, false otherwise
     */
    public static boolean keyExists(String key) {
        return recordKeyMap.containsKey(key);
    }
    
    /**
     * Remove a record key
     * Used for cleaning up old or expired keys
     * 
     * @param key The record key to remove
     */
    public static void removeRecordKey(String key) {
        recordKeyMap.remove(key);
    }
    
    /**
     * Get the number of registered record keys
     * 
     * @return The number of registered record keys
     */
    public static int getRecordKeyCount() {
        return recordKeyMap.size();
    }
    
    /**
     * Clear all record keys
     * Used when shutting down the application
     */
    public static void clearAllRecordKeys() {
        recordKeyMap.clear();
    }
} 