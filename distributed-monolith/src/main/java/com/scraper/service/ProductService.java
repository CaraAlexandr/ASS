package com.scraper.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scraper.dto.ProductInfo;
import com.scraper.dto.ProductResponse;
import com.scraper.entity.ProductDetails;
import com.scraper.repository.ProductDetailsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {
    
    private final ProductDetailsRepository repository;
    private final WebScraperService scraperService;
    private final ObjectMapper objectMapper;
    
    @Async
    public CompletableFuture<Void> processAndSaveProduct(String url) {
        log.info("Processing product URL: {}", url);
        
        if (repository.existsByUrl(url)) {
            log.debug("Product already exists: {}", url);
            return CompletableFuture.completedFuture(null);
        }
        
        ProductInfo productInfo = scraperService.extractProductInfo(url);
        
        try {
            String safeUrl = cap(url, 255);
            ProductDetails productDetails = ProductDetails.builder()
                    .url(safeUrl)
                    .title(productInfo.getTitle())
                    .description(productInfo.getDescription())
                    .price(cap(productInfo.getPrice(), 255))
                    .location(cap(productInfo.getLocation(), 255))
                    .adInfo(toJson(productInfo.getAdInfo()))
                    .generalInfo(toJson(productInfo.getGeneralInfo()))
                    .features(toJson(productInfo.getFeatures()))
                    .build();
            
            repository.save(productDetails);
            log.info("Product saved successfully: {}", url);
            
        } catch (Exception e) {
            log.error("Error saving product {}: {}", url, e.getMessage());
        }
        
        return CompletableFuture.completedFuture(null);
    }
    
    @Transactional
    public void saveProduct(String url, ProductInfo productInfo) {
        if (repository.existsByUrl(url)) {
            log.debug("Product already exists: {}", url);
            return;
        }

        String safeUrl = cap(url, 255);
        if (!safeUrl.equals(url)) {
            log.warn("URL truncated from {} to {} characters to fit DB column", url.length(), safeUrl.length());
        }

        log.info("Saving product: url={}, title={}, price={}", safeUrl, productInfo.getTitle(), productInfo.getPrice());

        ProductDetails productDetails = ProductDetails.builder()
                .url(safeUrl)
                .title(productInfo.getTitle())
                .description(productInfo.getDescription())
                .price(cap(productInfo.getPrice(), 255))
                .location(cap(productInfo.getLocation(), 255))
                .adInfo(toJson(productInfo.getAdInfo()))
                .generalInfo(toJson(productInfo.getGeneralInfo()))
                .features(toJson(productInfo.getFeatures()))
                .build();

        ProductDetails saved = repository.save(productDetails);
        log.info("Product saved successfully with ID: {}", saved.getId());
    }
    
    public List<ProductResponse> getAllProducts() {
        List<ProductDetails> products = repository.findAll();
        log.info("Found {} products in database", products.size());

        return products.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    private ProductResponse convertToResponse(ProductDetails entity) {
        return ProductResponse.builder()
                .id(entity.getId())
                .url(entity.getUrl())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .price(entity.getPrice())
                .location(entity.getLocation())
                .adInfo(entity.getAdInfo())
                .generalInfo(entity.getGeneralInfo())
                .features(entity.getFeatures())
                .createdAt(entity.getCreatedAt() != null ? entity.getCreatedAt().toString() : null)
                .build();
    }
    
    private String toJson(Object obj) {
        if (obj == null) {
            return "{}"; // Return empty JSON object instead of null
        }
        try {
            String json = objectMapper.writeValueAsString(obj);
            return json != null ? json : "{}";
        } catch (JsonProcessingException e) {
            log.error("Error converting to JSON: {}", e.getMessage());
            return "{}"; // Return empty JSON object on error
        }
    }

    private String cap(String value, int maxLen) {
        if (value == null) return null;
        if (value.length() <= maxLen) return value;
        return value.substring(0, maxLen);
    }
}

