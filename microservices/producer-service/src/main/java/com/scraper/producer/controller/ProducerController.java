package com.scraper.producer.controller;

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
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Producer", description = "Producer service operations")
public class ProducerController {
    
    private final WebScraperService scraperService;
    private final MessageProducerService messageProducerService;
    
    @PostMapping("/start")
    @Operation(summary = "Start scraping and publish URLs", 
               description = "Start scraping product URLs from 999.md and publish them to RabbitMQ")
    public ResponseEntity<Map<String, Object>> startScraping(
            @RequestParam(defaultValue = "https://999.md/ru/list/animals-and-plants/the-birds") String startingUrl,
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

