package com.scraper.producer.pattern.structural.decorator;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Decorator Pattern - Concrete Decorator for Enhanced Logging
 */
@Slf4j
public class LoggingScraperDecorator extends BaseScraperDecorator {
    
    public LoggingScraperDecorator(ScraperDecorator scraper) {
        super(scraper);
    }
    
    @Override
    public List<String> scrapeUrls(String url, int maxPages) {
        long startTime = System.currentTimeMillis();
        log.info("[Logging Decorator] Starting scrape operation for: {} (maxPages: {})", url, maxPages);
        
        try {
            List<String> result = super.scrapeUrls(url, maxPages);
            long duration = System.currentTimeMillis() - startTime;
            log.info("[Logging Decorator] Scrape completed in {}ms. Found {} URLs", duration, result.size());
            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[Logging Decorator] Scrape failed after {}ms: {}", duration, e.getMessage());
            throw e;
        }
    }
}

