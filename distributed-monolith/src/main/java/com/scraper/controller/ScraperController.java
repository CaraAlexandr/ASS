package com.scraper.controller;

import com.scraper.dto.ProductInfo;
import com.scraper.entity.ProductDetails;
import com.scraper.service.ProductService;
import com.scraper.service.WebScraperService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/scraper")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Scraper", description = "Web scraping operations")
public class ScraperController {
    
    private final WebScraperService scraperService;
    private final ProductService productService;
    
    @PostMapping("/start")
    @Operation(summary = "Start scraping", description = "Start scraping product URLs from 999.md")
    public ResponseEntity<Map<String, Object>> startScraping(
            @RequestParam(defaultValue = "https://999.md/ru/list/animals-and-plants/the-birds") String startingUrl,
            @RequestParam(defaultValue = "10") int maxPages) {
        
        log.info("Starting scraping from: {} with max pages: {}", startingUrl, maxPages);
        
        List<String> urls = scraperService.scrapeProductUrls(startingUrl, maxPages);
        
        // Process URLs asynchronously
        CompletableFuture<?>[] futures = urls.stream()
                .map(productService::processAndSaveProduct)
                .toArray(CompletableFuture[]::new);
        
        CompletableFuture.allOf(futures).thenRun(() -> 
            log.info("All products processed"));
        
        return ResponseEntity.ok(Map.of(
                "message", "Scraping started",
                "urlsFound", urls.size(),
                "status", "processing"
        ));
    }
    
    @GetMapping("/products")
    @Operation(summary = "Get all products", description = "Retrieve all scraped products from database")
    public ResponseEntity<List<ProductDetails>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }
    
    @GetMapping("/extract/{url}")
    @Operation(summary = "Extract product info", description = "Extract product information from a single URL")
    public ResponseEntity<ProductInfo> extractProductInfo(@PathVariable String url) {
        String fullUrl = url.startsWith("http") ? url : "https://999.md" + url;
        return ResponseEntity.ok(scraperService.extractProductInfo(fullUrl));
    }
}

