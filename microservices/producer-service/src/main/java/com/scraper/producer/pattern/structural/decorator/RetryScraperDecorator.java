package com.scraper.producer.pattern.structural.decorator;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Decorator Pattern - Concrete Decorator for Retry Logic
 */
@Slf4j
public class RetryScraperDecorator extends BaseScraperDecorator {
    
    private final int maxRetries;
    private final long retryDelayMs;
    
    public RetryScraperDecorator(ScraperDecorator scraper, int maxRetries, long retryDelayMs) {
        super(scraper);
        this.maxRetries = maxRetries;
        this.retryDelayMs = retryDelayMs;
    }
    
    @Override
    public List<String> scrapeUrls(String url, int maxPages) {
        int attempts = 0;
        Exception lastException = null;
        
        while (attempts < maxRetries) {
            try {
                log.info("[Retry Decorator] Attempt {} of {} for: {}", attempts + 1, maxRetries, url);
                return super.scrapeUrls(url, maxPages);
            } catch (Exception e) {
                lastException = e;
                attempts++;
                if (attempts < maxRetries) {
                    log.warn("[Retry Decorator] Attempt {} failed, retrying in {}ms: {}", 
                            attempts, retryDelayMs, e.getMessage());
                    try {
                        Thread.sleep(retryDelayMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Retry interrupted", ie);
                    }
                }
            }
        }
        
        log.error("[Retry Decorator] All {} attempts failed for: {}", maxRetries, url);
        throw new RuntimeException("Failed after " + maxRetries + " attempts", lastException);
    }
}

