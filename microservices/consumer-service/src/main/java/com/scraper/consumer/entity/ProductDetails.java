package com.scraper.consumer.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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
    
    @Column(name = "url", unique = true, nullable = false, columnDefinition = "TEXT")
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
    @JdbcTypeCode(SqlTypes.JSON)
    private String adInfo;
    
    @Column(name = "general_info", columnDefinition = "JSONB")
    @JdbcTypeCode(SqlTypes.JSON)
    private String generalInfo;
    
    @Column(name = "features", columnDefinition = "JSONB")
    @JdbcTypeCode(SqlTypes.JSON)
    private String features;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

