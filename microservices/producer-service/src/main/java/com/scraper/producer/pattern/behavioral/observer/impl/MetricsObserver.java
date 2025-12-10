package com.scraper.producer.pattern.behavioral.observer.impl;

import com.scraper.producer.pattern.behavioral.observer.ScrapingObserver;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Observer Pattern - Concrete Observer B
 * Tracks metrics for scraping operations
 */
@Slf4j
public class MetricsObserver implements ScrapingObserver {
    
    private final AtomicInteger totalScrapingOperations = new AtomicInteger(0);
    private final AtomicInteger totalUrlsScraped = new AtomicInteger(0);
    private final AtomicInteger totalErrors = new AtomicInteger(0);
    private final AtomicLong totalScrapingTime = new AtomicLong(0);
    
    private long currentStartTime;
    
    @Override
    public void onScrapingStarted(String url) {
        currentStartTime = System.currentTimeMillis();
        totalScrapingOperations.incrementAndGet();
        log.info("[Metrics Observer] Scraping operation #{} started", totalScrapingOperations.get());
    }
    
    @Override
    public void onScrapingProgress(String url, int urlsFound) {
        // Track progress if needed
    }
    
    @Override
    public void onScrapingCompleted(String url, int totalUrls) {
        long duration = System.currentTimeMillis() - currentStartTime;
        totalScrapingTime.addAndGet(duration);
        totalUrlsScraped.addAndGet(totalUrls);
        
        log.info("[Metrics Observer] Operation completed in {}ms. Total URLs: {}. " +
                "Overall stats - Operations: {}, Total URLs: {}, Errors: {}, Avg Time: {}ms",
                duration, totalUrls,
                totalScrapingOperations.get(),
                totalUrlsScraped.get(),
                totalErrors.get(),
                totalScrapingOperations.get() > 0 ? totalScrapingTime.get() / totalScrapingOperations.get() : 0);
    }
    
    @Override
    public void onScrapingError(String url, String error) {
        totalErrors.incrementAndGet();
        log.warn("[Metrics Observer] Error occurred. Total errors: {}", totalErrors.get());
    }
    
    public int getTotalOperations() {
        return totalScrapingOperations.get();
    }
    
    public int getTotalUrlsScraped() {
        return totalUrlsScraped.get();
    }
    
    public int getTotalErrors() {
        return totalErrors.get();
    }
}

