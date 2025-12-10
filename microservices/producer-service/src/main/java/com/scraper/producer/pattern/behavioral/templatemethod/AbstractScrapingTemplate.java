package com.scraper.producer.pattern.behavioral.templatemethod;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Template Method Pattern - Abstract class defining the algorithm skeleton
 */
@Slf4j
public abstract class AbstractScrapingTemplate {
    
    /**
     * Template Method - defines the skeleton of the algorithm
     */
    public final List<String> scrape(String url, int maxPages) {
        log.info("[Template Method] Starting scraping template for: {}", url);
        
        // Step 1: Initialize
        initialize(url);
        
        // Step 2: Validate input
        if (!validateInput(url, maxPages)) {
            log.warn("[Template Method] Input validation failed");
            return new ArrayList<>();
        }
        
        // Step 3: Prepare scraping
        prepareScraping(url);
        
        // Step 4: Execute scraping (delegated to subclasses)
        List<String> urls = executeScraping(url, maxPages);
        
        // Step 5: Post-process results
        List<String> processedUrls = postProcess(urls);
        
        // Step 6: Cleanup
        cleanup();
        
        log.info("[Template Method] Scraping template completed. Found {} URLs", processedUrls.size());
        return processedUrls;
    }
    
    // Hook methods - can be overridden by subclasses
    protected void initialize(String url) {
        log.debug("[Template Method] Initializing scraping for: {}", url);
    }
    
    protected boolean validateInput(String url, int maxPages) {
        return url != null && !url.isEmpty() && maxPages > 0;
    }
    
    protected void prepareScraping(String url) {
        log.debug("[Template Method] Preparing scraping for: {}", url);
    }
    
    // Abstract method - must be implemented by subclasses
    protected abstract List<String> executeScraping(String url, int maxPages);
    
    // Hook method - can be overridden
    protected List<String> postProcess(List<String> urls) {
        // Default: remove duplicates
        Set<String> uniqueUrls = new HashSet<>(urls);
        return new ArrayList<>(uniqueUrls);
    }
    
    protected void cleanup() {
        log.debug("[Template Method] Cleaning up");
    }
}

