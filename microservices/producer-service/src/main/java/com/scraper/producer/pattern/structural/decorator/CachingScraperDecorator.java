package com.scraper.producer.pattern.structural.decorator;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Decorator Pattern - Concrete Decorator for Caching
 */
@Slf4j
public class CachingScraperDecorator extends BaseScraperDecorator {
    
    private final Map<String, List<String>> cache = new ConcurrentHashMap<>();
    
    public CachingScraperDecorator(ScraperDecorator scraper) {
        super(scraper);
    }
    
    @Override
    public List<String> scrapeUrls(String url, int maxPages) {
        String cacheKey = url + "_" + maxPages;
        
        if (cache.containsKey(cacheKey)) {
            log.info("[Caching Decorator] Cache hit for: {}", url);
            return cache.get(cacheKey);
        }
        
        log.info("[Caching Decorator] Cache miss, scraping: {}", url);
        List<String> result = super.scrapeUrls(url, maxPages);
        cache.put(cacheKey, result);
        
        return result;
    }
    
    public void clearCache() {
        cache.clear();
        log.info("[Caching Decorator] Cache cleared");
    }
}

