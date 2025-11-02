package com.scraper.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scraper.dto.ProductInfo;
import com.scraper.entity.ProductDetails;
import com.scraper.repository.ProductDetailsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;

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
            ProductDetails productDetails = ProductDetails.builder()
                    .url(url)
                    .title(productInfo.getTitle())
                    .description(productInfo.getDescription())
                    .price(productInfo.getPrice())
                    .location(productInfo.getLocation())
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
        
        ProductDetails productDetails = ProductDetails.builder()
                .url(url)
                .title(productInfo.getTitle())
                .description(productInfo.getDescription())
                .price(productInfo.getPrice())
                .location(productInfo.getLocation())
                .adInfo(toJson(productInfo.getAdInfo()))
                .generalInfo(toJson(productInfo.getGeneralInfo()))
                .features(toJson(productInfo.getFeatures()))
                .build();
        
        repository.save(productDetails);
    }
    
    public List<ProductDetails> getAllProducts() {
        return repository.findAll();
    }
    
    private String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Error converting to JSON: {}", e.getMessage());
            return null;
        }
    }
}

