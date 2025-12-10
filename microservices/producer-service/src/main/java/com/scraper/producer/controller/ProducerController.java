package com.scraper.producer.controller;

import com.scraper.producer.service.EnhancedScrapingService;
import com.scraper.producer.service.MessageProducerService;
import com.scraper.producer.service.WebScraperService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/producer")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:80", "http://127.0.0.1:3000"})
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Producer", description = "Producer service operations")
public class ProducerController {
    
    private final WebScraperService scraperService;
    private final MessageProducerService messageProducerService;
    private final EnhancedScrapingService enhancedScrapingService;
    
    @PostMapping("/start")
    @Operation(summary = "Start scraping and publish URLs", 
               description = "Start scraping product URLs from eBay and publish them to RabbitMQ")
    public ResponseEntity<Map<String, Object>> startScraping(
            @RequestParam(defaultValue = "https://www.ebay.com/sch/i.html?_nkw=cell+phones") String startingUrl,
            @RequestParam(defaultValue = "10") int maxPages) {
        
        log.info("Starting scraping from: {} with max pages: {}", startingUrl, maxPages);
        
        List<String> urls = scraperService.scrapeProductUrls(startingUrl, maxPages);
        messageProducerService.sendUrls(urls);
        
        return ResponseEntity.ok(Map.of(
                "message", "Scraping completed and URLs published to queue",
                "urlsFound", urls.size(),
                "urlsPublished", urls.size(),
                "status", "completed"
        ));
    }
    
    @PostMapping("/start-enhanced")
    @Operation(summary = "Start enhanced scraping with design patterns", 
               description = "Start scraping using all design patterns (Factory, Builder, Strategy, Observer, etc.)")
    public ResponseEntity<Map<String, Object>> startEnhancedScraping(
            @RequestParam(defaultValue = "https://www.ebay.com/sch/i.html?_nkw=cell+phones") String startingUrl,
            @RequestParam(defaultValue = "10") int maxPages,
            @RequestParam(defaultValue = "balanced") String strategy) {
        
        log.info("Starting enhanced scraping with patterns from: {} with max pages: {} and strategy: {}", 
                startingUrl, maxPages, strategy);
        
        EnhancedScrapingService.ScrapingResult result = 
                enhancedScrapingService.scrapeWithAllPatterns(startingUrl, maxPages, strategy);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", result.isSuccess() ? "Enhanced scraping completed" : "Enhanced scraping failed");
        response.put("urlsFound", result.getUrlsFound());
        response.put("urlsPublished", result.getUrlsPublished());
        response.put("status", result.isSuccess() ? "completed" : "failed");
        response.put("error", result.getMessage());
        
        // Add metrics from Observer pattern
        var metrics = enhancedScrapingService.getMetricsObserver();
        response.put("metrics", Map.of(
                "totalOperations", metrics.getTotalOperations(),
                "totalUrlsScraped", metrics.getTotalUrlsScraped(),
                "totalErrors", metrics.getTotalErrors()
        ));
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/publish")
    @Operation(summary = "Publish URL to queue", description = "Publish a single URL to RabbitMQ queue")
    public ResponseEntity<Map<String, String>> publishUrl(@RequestParam String url) {
        messageProducerService.sendUrl(url);
        return ResponseEntity.ok(Map.of(
                "message", "URL published to queue",
                "url", url
        ));
    }
    
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check if the producer service is healthy")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "producer-service"));
    }
}

