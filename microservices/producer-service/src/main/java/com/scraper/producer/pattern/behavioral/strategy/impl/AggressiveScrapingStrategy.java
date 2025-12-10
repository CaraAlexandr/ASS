package com.scraper.producer.pattern.behavioral.strategy.impl;

import com.scraper.producer.pattern.behavioral.strategy.ScrapingStrategy;
import com.scraper.producer.pattern.creational.factory.Scraper;
import com.scraper.producer.pattern.creational.factory.ScraperFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Strategy Pattern - Concrete Strategy A
 * Aggressive scraping with no delays and maximum pages
 */
@Slf4j
public class AggressiveScrapingStrategy implements ScrapingStrategy {
    
    @Override
    public List<String> execute(String url, int maxPages) {
        log.info("[Aggressive Strategy] Executing aggressive scraping for: {}", url);
        Scraper scraper = ScraperFactory.createScraper(url);
        // Aggressive: use maxPages * 2 for more aggressive scraping
        return scraper.scrapeUrls(url, maxPages * 2);
    }
    
    @Override
    public String getStrategyName() {
        return "Aggressive";
    }
}

