package com.scraper.producer.pattern.structural.facade;

import com.scraper.producer.pattern.creational.builder.ScrapingConfig;
import com.scraper.producer.pattern.creational.factory.Scraper;
import com.scraper.producer.pattern.creational.factory.ScraperFactory;
import com.scraper.producer.pattern.structural.decorator.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Facade Pattern
 * Provides a simplified interface to the complex subsystem of scraping, decorating, and messaging
 */
@Component
@Slf4j
public class ScrapingFacade {
    
    private final RabbitTemplate rabbitTemplate;
    
    @Value("${queue.name}")
    private String queueName;
    
    public ScrapingFacade(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }
    
    /**
     * Simplified method that handles the entire scraping and publishing workflow
     */
    public ScrapingResult scrapeAndPublish(ScrapingConfig config) {
        log.info("[Facade] Starting scraping and publishing workflow");
        
        try {
            // Step 1: Create scraper using Factory Method
            Scraper baseScraper = ScraperFactory.createScraper(config.getStartingUrl());
            log.info("[Facade] Created scraper for domain: {}", baseScraper.getSupportedDomain());
            
            // Step 2: Decorate scraper with additional functionality
            ScraperDecorator decoratedScraper = createDecoratedScraper(baseScraper, config);
            
            // Step 3: Perform scraping
            List<String> urls = decoratedScraper.scrapeUrls(config.getStartingUrl(), config.getMaxPages());
            log.info("[Facade] Scraped {} URLs", urls.size());
            
            // Step 4: Publish to queue
            int publishedCount = publishUrls(urls);
            log.info("[Facade] Published {} URLs to queue", publishedCount);
            
            return new ScrapingResult(urls.size(), publishedCount, true, "Success");
            
        } catch (Exception e) {
            log.error("[Facade] Error in scraping workflow: {}", e.getMessage(), e);
            return new ScrapingResult(0, 0, false, e.getMessage());
        }
    }
    
    private ScraperDecorator createDecoratedScraper(Scraper baseScraper, ScrapingConfig config) {
        // Wrap base scraper to make it a decorator
        ScraperDecorator decorator = new BaseScraperDecorator(baseScraper) {};
        
        // Apply decorators based on configuration
        if (config.isEnableCaching()) {
            decorator = new CachingScraperDecorator(decorator);
        }
        
        decorator = new LoggingScraperDecorator(decorator);
        decorator = new RetryScraperDecorator(decorator, 3, 2000);
        
        return decorator;
    }
    
    private int publishUrls(List<String> urls) {
        int count = 0;
        for (String url : urls) {
            try {
                // Using default exchange ("") with queueName as routing key
                // This routes to the queue with the same name as the routing key
                rabbitTemplate.convertAndSend("", queueName, url.getBytes());
                count++;
            } catch (Exception e) {
                log.error("[Facade] Error publishing URL {}: {}", url, e.getMessage());
            }
        }
        return count;
    }
    
    /**
     * Result class for scraping operations
     */
    public static class ScrapingResult {
        private final int urlsFound;
        private final int urlsPublished;
        private final boolean success;
        private final String message;
        
        public ScrapingResult(int urlsFound, int urlsPublished, boolean success, String message) {
            this.urlsFound = urlsFound;
            this.urlsPublished = urlsPublished;
            this.success = success;
            this.message = message;
        }
        
        public int getUrlsFound() { return urlsFound; }
        public int getUrlsPublished() { return urlsPublished; }
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
    }
}

