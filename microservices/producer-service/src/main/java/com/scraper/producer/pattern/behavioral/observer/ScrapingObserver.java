package com.scraper.producer.pattern.behavioral.observer;

/**
 * Observer Pattern - Observer interface
 */
public interface ScrapingObserver {
    void onScrapingStarted(String url);
    void onScrapingProgress(String url, int urlsFound);
    void onScrapingCompleted(String url, int totalUrls);
    void onScrapingError(String url, String error);
}

