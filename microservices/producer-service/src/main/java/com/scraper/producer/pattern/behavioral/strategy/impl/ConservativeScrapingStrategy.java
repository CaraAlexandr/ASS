package com.scraper.producer.pattern.behavioral.strategy.impl;

import com.scraper.producer.pattern.behavioral.strategy.ScrapingStrategy;
import com.scraper.producer.pattern.creational.factory.Scraper;
import com.scraper.producer.pattern.creational.factory.ScraperFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Strategy Pattern - Concrete Strategy B
 * Conservative scraping with rate limiting and fewer pages
 */
@Slf4j
public class ConservativeScrapingStrategy implements ScrapingStrategy {
    
    @Override
    public List<String> execute(String url, int maxPages) {
        log.info("[Conservative Strategy] Executing conservative scraping for: {}", url);
        Scraper scraper = ScraperFactory.createScraper(url);
        // Conservative: limit to half the maxPages and add delays
        int conservativeMaxPages = Math.max(1, maxPages / 2);
        List<String> urls = scraper.scrapeUrls(url, conservativeMaxPages);
        
        // Add delay between requests
        try {
            Thread.sleep(2000); // Additional delay for conservative approach
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        return urls;
    }
    
    @Override
    public String getStrategyName() {
        return "Conservative";
    }
}

