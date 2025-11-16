package com.scraper.controller;

import com.scraper.dto.ProductInfo;
import com.scraper.dto.ProductResponse;
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
    @Operation(summary = "Start scraping", description = "Extract products directly from 999.md listing pages and save to database")
    public ResponseEntity<Map<String, Object>> startScraping(
            @RequestParam(defaultValue = "https://www.ebay.com/sch/i.html?_nkw=cell+phones") String startingUrl,
            @RequestParam(defaultValue = "5") int maxPages) {
        
        log.info("Starting scraping from listing page: {} with max pages: {}", startingUrl, maxPages);
        
        // Extract products directly from listing pages (more efficient - no need to visit individual pages)
        Map<String, ProductInfo> products = scraperService.scrapeProductsFromListing(startingUrl, maxPages);
        
        log.info("Found {} products to save", products.size());
        
        // Save products asynchronously to database
        CompletableFuture<?>[] futures = products.entrySet().stream()
                .map(entry -> {
                    String url = entry.getKey();
                    ProductInfo info = entry.getValue();
                    return CompletableFuture.runAsync(() -> {
                        try {
                            productService.saveProduct(url, info);
                            log.debug("Saved product: {} - {}", url, info.getTitle());
                        } catch (Exception e) {
                            log.error("Error saving product {}: {}", url, e.getMessage());
                        }
                    });
                })
                .toArray(CompletableFuture[]::new);
        
        CompletableFuture.allOf(futures).thenRun(() -> 
            log.info("All {} products saved to database", products.size()));
        
        return ResponseEntity.ok(Map.of(
                "message", "Scraping completed and products are being saved",
                "productsFound", products.size(),
                "status", "processing",
                "startingUrl", startingUrl
        ));
    }
    
    @GetMapping("/products")
    @Operation(summary = "Get all products", description = "Retrieve all scraped products from database")
    public ResponseEntity<String> getAllProducts() {
        List<ProductResponse> products = productService.getAllProducts();
        try {
            // Simple manual JSON conversion to avoid Jackson issues
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < products.size(); i++) {
                ProductResponse p = products.get(i);
                json.append("{");
                json.append("\"id\":").append(p.getId()).append(",");
                json.append("\"url\":\"").append(p.getUrl()).append("\",");
                json.append("\"title\":\"").append(p.getTitle()).append("\",");
                json.append("\"price\":\"").append(p.getPrice() != null ? p.getPrice() : "").append("\",");
                json.append("\"adInfo\":\"").append(p.getAdInfo() != null ? p.getAdInfo() : "{}").append("\",");
                json.append("\"generalInfo\":\"").append(p.getGeneralInfo() != null ? p.getGeneralInfo() : "{}").append("\",");
                json.append("\"features\":\"").append(p.getFeatures() != null ? p.getFeatures() : "{}").append("\"");
                json.append("}");
                if (i < products.size() - 1) json.append(",");
            }
            json.append("]");
            return ResponseEntity.ok(json.toString());
        } catch (Exception e) {
            return ResponseEntity.ok("[]");
        }
    }
    
    @GetMapping("/extract/{url}")
    @Operation(summary = "Extract product info", description = "Extract product information from a single URL")
    public ResponseEntity<ProductInfo> extractProductInfo(@PathVariable String url) {
        String fullUrl = url.startsWith("http") ? url : "https://999.md" + url;
        return ResponseEntity.ok(scraperService.extractProductInfo(fullUrl));
    }

    @PostMapping("/test-html")
    @Operation(summary = "Test HTML parsing", description = "Test parsing with provided HTML content")
    public ResponseEntity<String> testHtmlParsing(@RequestBody String htmlContent) {
        try {
            Map<String, ProductInfo> products = scraperService.testParseHtml(htmlContent);
            return ResponseEntity.ok("Found " + products.size() + " products in test HTML");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error parsing HTML: " + e.getMessage());
        }
    }
}

