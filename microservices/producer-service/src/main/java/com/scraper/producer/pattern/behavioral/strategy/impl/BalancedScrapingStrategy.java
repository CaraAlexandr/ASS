package com.scraper.producer.pattern.behavioral.strategy.impl;

import com.scraper.producer.pattern.behavioral.strategy.ScrapingStrategy;
import com.scraper.producer.pattern.creational.factory.Scraper;
import com.scraper.producer.pattern.creational.factory.ScraperFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Strategy Pattern - Concrete Strategy C
 * Balanced scraping with moderate rate limiting
 */
@Slf4j
public class BalancedScrapingStrategy implements ScrapingStrategy {
    
    @Override
    public List<String> execute(String url, int maxPages) {
        log.info("[Balanced Strategy] Executing balanced scraping for: {}", url);
        Scraper scraper = ScraperFactory.createScraper(url);
        // Balanced: use maxPages as-is with standard delays
        return scraper.scrapeUrls(url, maxPages);
    }
    
    @Override
    public String getStrategyName() {
        return "Balanced";
    }
}

