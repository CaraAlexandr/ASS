package com.scraper.producer.pattern.behavioral.observer;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * Observer Pattern - Subject class
 * Manages observers and notifies them of scraping events
 */
@Slf4j
public class ScrapingSubject {
    
    private final List<ScrapingObserver> observers = new ArrayList<>();
    
    public void attach(ScrapingObserver observer) {
        observers.add(observer);
        log.debug("[Observer Subject] Attached observer: {}", observer.getClass().getSimpleName());
    }
    
    public void detach(ScrapingObserver observer) {
        observers.remove(observer);
        log.debug("[Observer Subject] Detached observer: {}", observer.getClass().getSimpleName());
    }
    
    public void notifyScrapingStarted(String url) {
        log.info("[Observer Subject] Notifying {} observers about scraping start", observers.size());
        for (ScrapingObserver observer : observers) {
            try {
                observer.onScrapingStarted(url);
            } catch (Exception e) {
                log.error("[Observer Subject] Error notifying observer: {}", e.getMessage());
            }
        }
    }
    
    public void notifyScrapingProgress(String url, int urlsFound) {
        for (ScrapingObserver observer : observers) {
            try {
                observer.onScrapingProgress(url, urlsFound);
            } catch (Exception e) {
                log.error("[Observer Subject] Error notifying observer: {}", e.getMessage());
            }
        }
    }
    
    public void notifyScrapingCompleted(String url, int totalUrls) {
        log.info("[Observer Subject] Notifying {} observers about scraping completion", observers.size());
        for (ScrapingObserver observer : observers) {
            try {
                observer.onScrapingCompleted(url, totalUrls);
            } catch (Exception e) {
                log.error("[Observer Subject] Error notifying observer: {}", e.getMessage());
            }
        }
    }
    
    public void notifyScrapingError(String url, String error) {
        log.warn("[Observer Subject] Notifying {} observers about scraping error", observers.size());
        for (ScrapingObserver observer : observers) {
            try {
                observer.onScrapingError(url, error);
            } catch (Exception e) {
                log.error("[Observer Subject] Error notifying observer: {}", e.getMessage());
            }
        }
    }
}

