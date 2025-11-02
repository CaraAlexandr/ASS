package com.scraper.consumer.controller;

import com.scraper.consumer.entity.ProductDetails;
import com.scraper.consumer.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/consumer")
@RequiredArgsConstructor
@Tag(name = "Consumer", description = "Consumer service operations")
public class ConsumerController {
    
    private final ProductService productService;
    
    @GetMapping("/products")
    @Operation(summary = "Get all products", description = "Retrieve all products saved in the database")
    public ResponseEntity<List<ProductDetails>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }
    
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check if the consumer service is healthy")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "consumer-service"));
    }
}

