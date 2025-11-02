package com.scraper.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {
    private Long id;
    private String url;
    private String title;
    private String description;
    private String price;
    private String location;
    private String adInfo;
    private String generalInfo;
    private String features;
    private String createdAt;
}
