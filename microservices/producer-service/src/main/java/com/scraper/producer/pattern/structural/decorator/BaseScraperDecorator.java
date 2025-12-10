package com.scraper.producer.pattern.structural.decorator;

import com.scraper.producer.pattern.creational.factory.Scraper;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * Decorator Pattern - Base Decorator
 */
@RequiredArgsConstructor
public abstract class BaseScraperDecorator implements ScraperDecorator {
    
    protected final Scraper scraper;
    
    @Override
    public List<String> scrapeUrls(String url, int maxPages) {
        return scraper.scrapeUrls(url, maxPages);
    }
    
    @Override
    public String getSupportedDomain() {
        return scraper.getSupportedDomain();
    }
}

