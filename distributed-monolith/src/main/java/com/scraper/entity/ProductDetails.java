package com.scraper.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "product_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDetails {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "url", unique = true, nullable = false)
    private String url;
    
    @Column(name = "title", columnDefinition = "TEXT")
    private String title;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "price")
    private String price;
    
    @Column(name = "location")
    private String location;
    
    @Column(name = "ad_info", columnDefinition = "JSONB")
    private String adInfo;
    
    @Column(name = "general_info", columnDefinition = "JSONB")
    private String generalInfo;
    
    @Column(name = "features", columnDefinition = "JSONB")
    private String features;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

