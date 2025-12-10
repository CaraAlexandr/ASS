package com.scraper.producer.pattern.behavioral.strategy;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Strategy Pattern - Context class
 * Uses a strategy to execute scraping operations
 */
@Slf4j
public class ScrapingContext {
    
    private ScrapingStrategy strategy;
    
    public ScrapingContext(ScrapingStrategy strategy) {
        this.strategy = strategy;
    }
    
    public void setStrategy(ScrapingStrategy strategy) {
        log.info("[Strategy Context] Changing strategy from {} to {}", 
                this.strategy != null ? this.strategy.getStrategyName() : "null",
                strategy.getStrategyName());
        this.strategy = strategy;
    }
    
    public List<String> executeScraping(String url, int maxPages) {
        if (strategy == null) {
            throw new IllegalStateException("Scraping strategy not set");
        }
        log.info("[Strategy Context] Executing scraping with {} strategy", strategy.getStrategyName());
        return strategy.execute(url, maxPages);
    }
    
    public String getCurrentStrategyName() {
        return strategy != null ? strategy.getStrategyName() : "None";
    }
}

