package com.scraper.producer.pattern.structural.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * Adapter Pattern
 * Adapts different message formats to a common interface
 */
@Slf4j
public class MessageAdapter {
    
    private final ObjectMapper objectMapper;
    
    public MessageAdapter() {
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Adapts a plain string URL to JSON format
     */
    public String adaptToJson(String url) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("url", url);
            message.put("timestamp", System.currentTimeMillis());
            message.put("type", "product_url");
            return objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            log.error("Error adapting message to JSON: {}", e.getMessage());
            return url; // Fallback to original format
        }
    }
    
    /**
     * Adapts JSON message back to plain string URL
     */
    public String adaptFromJson(String jsonMessage) {
        try {
            Map<String, Object> message = objectMapper.readValue(jsonMessage, Map.class);
            return (String) message.get("url");
        } catch (Exception e) {
            log.error("Error adapting message from JSON: {}", e.getMessage());
            return jsonMessage; // Fallback to original format
        }
    }
    
    /**
     * Adapts URL list to batch JSON format
     */
    public String adaptBatchToJson(java.util.List<String> urls) {
        try {
            Map<String, Object> batchMessage = new HashMap<>();
            batchMessage.put("urls", urls);
            batchMessage.put("count", urls.size());
            batchMessage.put("timestamp", System.currentTimeMillis());
            return objectMapper.writeValueAsString(batchMessage);
        } catch (Exception e) {
            log.error("Error adapting batch message to JSON: {}", e.getMessage());
            return String.join(",", urls); // Fallback
        }
    }
}

