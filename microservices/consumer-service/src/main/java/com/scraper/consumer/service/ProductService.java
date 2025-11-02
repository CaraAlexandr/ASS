package com.scraper.consumer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scraper.consumer.dto.ProductInfo;
import com.scraper.consumer.entity.ProductDetails;
import com.scraper.consumer.repository.ProductDetailsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {
    
    private final ProductDetailsRepository repository;
    private final ProductExtractorService extractorService;
    private final ObjectMapper objectMapper;
    
    @Transactional
    public void saveProduct(String url, ProductInfo productInfo) {
        if (repository.existsByUrl(url)) {
            log.debug("Product already exists: {}", url);
            return;
        }
        
        try {
            // Truncate URL and other fields to prevent DB errors
            String safeUrl = truncate(url, 500);
            if (!safeUrl.equals(url)) {
                log.warn("URL truncated from {} to {} characters to fit DB column", url.length(), safeUrl.length());
            }
            
            ProductDetails productDetails = ProductDetails.builder()
                    .url(safeUrl)
                    .title(productInfo.getTitle())
                    .description(productInfo.getDescription())
                    .price(truncate(productInfo.getPrice(), 255))
                    .location(truncate(productInfo.getLocation(), 255))
                    .adInfo(toJson(productInfo.getAdInfo()))
                    .generalInfo(toJson(productInfo.getGeneralInfo()))
                    .features(toJson(productInfo.getFeatures()))
                    .build();
            
            repository.save(productDetails);
            log.info("Product saved successfully: {}", url);
            
        } catch (Exception e) {
            log.error("Error saving product {}: {}", url, e.getMessage());
            throw e;
        }
    }
    
    public List<ProductDetails> getAllProducts() {
        return repository.findAll();
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
    
    private String truncate(String value, int maxLen) {
        if (value == null) return null;
        if (value.length() <= maxLen) return value;
        return value.substring(0, maxLen);
    }
}

