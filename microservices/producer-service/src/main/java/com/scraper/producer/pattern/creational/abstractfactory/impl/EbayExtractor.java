package com.scraper.producer.pattern.creational.abstractfactory.impl;

import com.scraper.producer.pattern.creational.abstractfactory.ExtractorProduct;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract Factory Pattern - Concrete Product B1 (eBay Extractor)
 */
@Slf4j
public class EbayExtractor implements ExtractorProduct {
    
    @Override
    public Map<String, String> extractData(String url) {
        Map<String, String> data = new HashMap<>();
        
        try {
            Document doc = Jsoup.connect(url)
                    .timeout(30000)
                    .userAgent("Mozilla/5.0")
                    .get();
            
            Element titleEl = doc.selectFirst("h1.x-item-title__mainTitle, h1#itemTitle");
            if (titleEl != null) {
                data.put("title", titleEl.text().trim());
            }
            
            Element priceEl = doc.selectFirst(".x-price-primary, span#prcIsum");
            if (priceEl != null) {
                data.put("price", priceEl.text().trim());
            }
            
            log.info("[Abstract Factory - eBay Extractor] Extracted data from: {}", url);
            
        } catch (IOException e) {
            log.error("Error extracting data: {}", e.getMessage());
        }
        
        return data;
    }
}

