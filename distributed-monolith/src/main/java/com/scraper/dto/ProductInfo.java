package com.scraper.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductInfo {
    private String title;
    private String description;
    private String price;
    private String location;
    private Map<String, String> adInfo;
    private Map<String, String> generalInfo;
    private Map<String, String> features;
}

