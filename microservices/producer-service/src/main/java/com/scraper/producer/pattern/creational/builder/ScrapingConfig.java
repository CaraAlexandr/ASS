package com.scraper.producer.pattern.creational.builder;

import lombok.Data;

/**
 * Builder Pattern - Product class
 * Configuration object for scraping operations
 */
@Data
public class ScrapingConfig {
    private String startingUrl;
    private int maxPages;
    private int timeout;
    private int delayBetweenRequests;
    private String userAgent;
    private boolean followRedirects;
    private int maxBodySize;
    private boolean enableCaching;
    
    private ScrapingConfig(Builder builder) {
        this.startingUrl = builder.startingUrl;
        this.maxPages = builder.maxPages;
        this.timeout = builder.timeout;
        this.delayBetweenRequests = builder.delayBetweenRequests;
        this.userAgent = builder.userAgent;
        this.followRedirects = builder.followRedirects;
        this.maxBodySize = builder.maxBodySize;
        this.enableCaching = builder.enableCaching;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Builder Pattern - Builder class
     */
    public static class Builder {
        private String startingUrl;
        private int maxPages = 10;
        private int timeout = 30000;
        private int delayBetweenRequests = 1000;
        private String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";
        private boolean followRedirects = true;
        private int maxBodySize = 10 * 1024 * 1024; // 10MB
        private boolean enableCaching = false;
        
        public Builder startingUrl(String startingUrl) {
            this.startingUrl = startingUrl;
            return this;
        }
        
        public Builder maxPages(int maxPages) {
            this.maxPages = maxPages;
            return this;
        }
        
        public Builder timeout(int timeout) {
            this.timeout = timeout;
            return this;
        }
        
        public Builder delayBetweenRequests(int delay) {
            this.delayBetweenRequests = delay;
            return this;
        }
        
        public Builder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }
        
        public Builder followRedirects(boolean followRedirects) {
            this.followRedirects = followRedirects;
            return this;
        }
        
        public Builder maxBodySize(int maxBodySize) {
            this.maxBodySize = maxBodySize;
            return this;
        }
        
        public Builder enableCaching(boolean enableCaching) {
            this.enableCaching = enableCaching;
            return this;
        }
        
        public ScrapingConfig build() {
            if (startingUrl == null || startingUrl.isEmpty()) {
                throw new IllegalArgumentException("Starting URL is required");
            }
            return new ScrapingConfig(this);
        }
    }
}

