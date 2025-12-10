package com.scraper.producer.pattern.behavioral.observer.impl;

import com.scraper.producer.pattern.behavioral.observer.ScrapingObserver;
import lombok.extern.slf4j.Slf4j;

/**
 * Observer Pattern - Concrete Observer A
 * Logs all scraping events
 */
@Slf4j
public class LoggingObserver implements ScrapingObserver {
    
    @Override
    public void onScrapingStarted(String url) {
        log.info("[Logging Observer] Scraping started for: {}", url);
    }
    
    @Override
    public void onScrapingProgress(String url, int urlsFound) {
        log.debug("[Logging Observer] Progress for {}: {} URLs found", url, urlsFound);
    }
    
    @Override
    public void onScrapingCompleted(String url, int totalUrls) {
        log.info("[Logging Observer] Scraping completed for {}: {} total URLs", url, totalUrls);
    }
    
    @Override
    public void onScrapingError(String url, String error) {
        log.error("[Logging Observer] Scraping error for {}: {}", url, error);
    }
}

