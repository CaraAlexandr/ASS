package com.scraper.producer.service;

import com.scraper.producer.pattern.behavioral.observer.ScrapingObserver;
import com.scraper.producer.pattern.behavioral.observer.ScrapingSubject;
import com.scraper.producer.pattern.behavioral.observer.impl.LoggingObserver;
import com.scraper.producer.pattern.behavioral.observer.impl.MetricsObserver;
import com.scraper.producer.pattern.behavioral.strategy.ScrapingContext;
import com.scraper.producer.pattern.behavioral.strategy.ScrapingStrategy;
import com.scraper.producer.pattern.behavioral.strategy.impl.AggressiveScrapingStrategy;
import com.scraper.producer.pattern.behavioral.strategy.impl.BalancedScrapingStrategy;
import com.scraper.producer.pattern.behavioral.strategy.impl.ConservativeScrapingStrategy;
import com.scraper.producer.pattern.behavioral.templatemethod.AbstractScrapingTemplate;
import com.scraper.producer.pattern.behavioral.templatemethod.impl.EbayScrapingTemplate;
import com.scraper.producer.pattern.behavioral.templatemethod.impl.GenericScrapingTemplate;
import com.scraper.producer.pattern.creational.builder.ScrapingConfig;
import com.scraper.producer.pattern.creational.factory.Scraper;
import com.scraper.producer.pattern.creational.factory.ScraperFactory;
import com.scraper.producer.pattern.structural.adapter.MessageAdapter;
import com.scraper.producer.pattern.structural.facade.ScrapingFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.List;

/**
 * Enhanced scraping service that demonstrates all design patterns
 */
@Service
@Slf4j
public class EnhancedScrapingService {
    
    private final ScrapingFacade scrapingFacade;
    private final MessageAdapter messageAdapter;
    private final MessageProducerService messageProducerService;
    
    private final ScrapingSubject scrapingSubject = new ScrapingSubject();
    private final MetricsObserver metricsObserver = new MetricsObserver();
    
    public EnhancedScrapingService(ScrapingFacade scrapingFacade, 
                                   MessageAdapter messageAdapter,
                                   MessageProducerService messageProducerService) {
        this.scrapingFacade = scrapingFacade;
        this.messageAdapter = messageAdapter;
        this.messageProducerService = messageProducerService;
        
        // Observer Pattern: Attach observers
        scrapingSubject.attach(new LoggingObserver());
        scrapingSubject.attach(metricsObserver);
    }
    
    /**
     * Demonstrates all patterns working together
     */
    public ScrapingResult scrapeWithAllPatterns(String url, int maxPages, String strategyType) {
        log.info("=== Starting scraping with all design patterns ===");
        
        // Observer Pattern: Notify start
        scrapingSubject.notifyScrapingStarted(url);
        
        try {
            // Builder Pattern: Create configuration
            ScrapingConfig config = ScrapingConfig.builder()
                    .startingUrl(url)
                    .maxPages(maxPages)
                    .timeout(30000)
                    .delayBetweenRequests(1000)
                    .enableCaching(true)
                    .build();
            log.info("[Builder Pattern] Created configuration: maxPages={}, caching={}", 
                    config.getMaxPages(), config.isEnableCaching());
            
            // Strategy Pattern: Select and use strategy
            ScrapingStrategy strategy = getStrategy(strategyType);
            ScrapingContext context = new ScrapingContext(strategy);
            log.info("[Strategy Pattern] Using strategy: {}", context.getCurrentStrategyName());
            
            // Factory Method Pattern: Create appropriate scraper
            Scraper scraper = ScraperFactory.createScraper(url);
            log.info("[Factory Method Pattern] Created scraper for domain: {}", scraper.getSupportedDomain());
            
            // Template Method Pattern: Use template for scraping
            AbstractScrapingTemplate template = createTemplate(url);
            log.info("[Template Method Pattern] Using template: {}", template.getClass().getSimpleName());
            
            // Execute scraping using Strategy Pattern
            List<String> urls = context.executeScraping(url, maxPages);
            log.info("[Strategy Pattern] Scraped {} URLs using {} strategy", urls.size(), context.getCurrentStrategyName());
            
            // Observer Pattern: Notify progress
            scrapingSubject.notifyScrapingProgress(url, urls.size());
            
            // Adapter Pattern: Adapt messages to JSON format
            List<String> adaptedUrls = urls.stream()
                    .map(messageAdapter::adaptToJson)
                    .toList();
            log.info("[Adapter Pattern] Adapted {} URLs to JSON format", adaptedUrls.size());
            
            // Publish adapted URLs using MessageProducerService
            // Note: We use the original URLs (not adapted JSON) for publishing to maintain compatibility
            // The Adapter pattern is demonstrated by the transformation above
            int publishedCount = 0;
            for (String urlToPublish : urls) {
                try {
                    messageProducerService.sendUrl(urlToPublish);
                    publishedCount++;
                } catch (Exception e) {
                    log.error("Error publishing URL {}: {}", urlToPublish, e.getMessage());
                }
            }
            log.info("[Publishing] Published {} URLs to queue", publishedCount);
            
            // Observer Pattern: Notify completion
            scrapingSubject.notifyScrapingCompleted(url, urls.size());
            
            log.info("=== Scraping completed successfully ===");
            return new ScrapingResult(urls.size(), publishedCount, true, "Success");
            
        } catch (Exception e) {
            log.error("Error during scraping: {}", e.getMessage(), e);
            scrapingSubject.notifyScrapingError(url, e.getMessage());
            return new ScrapingResult(0, 0, false, e.getMessage());
        }
    }
    
    private ScrapingStrategy getStrategy(String strategyType) {
        return switch (strategyType != null ? strategyType.toLowerCase() : "balanced") {
            case "aggressive" -> new AggressiveScrapingStrategy();
            case "conservative" -> new ConservativeScrapingStrategy();
            default -> new BalancedScrapingStrategy();
        };
    }
    
    private AbstractScrapingTemplate createTemplate(String url) {
        try {
            String host = new URI(url).getHost();
            if (host != null && host.toLowerCase().contains("ebay")) {
                return new EbayScrapingTemplate();
            }
        } catch (Exception e) {
            // Fall through to generic
        }
        return new GenericScrapingTemplate();
    }
    
    public MetricsObserver getMetricsObserver() {
        return metricsObserver;
    }
    
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

