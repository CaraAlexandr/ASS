package com.scraper.producer.pattern.behavioral.templatemethod.impl;

import com.scraper.producer.pattern.behavioral.templatemethod.AbstractScrapingTemplate;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Template Method Pattern - Concrete implementation for eBay
 */
@Slf4j
public class EbayScrapingTemplate extends AbstractScrapingTemplate {
    
    @Override
    protected List<String> executeScraping(String url, int maxPages) {
        log.info("[Template Method - eBay] Executing eBay-specific scraping");
        List<String> urls = new ArrayList<>();
        
        try {
            Document doc = Jsoup.connect(url)
                    .timeout(30000)
                    .userAgent("Mozilla/5.0")
                    .get();
            
            // eBay-specific extraction logic
            Elements items = doc.select("li.s-item, li.brwrvr__item-card");
            for (Element item : items) {
                Element linkEl = item.selectFirst("a[href*='/itm/'], a[href*='/p/']");
                if (linkEl != null) {
                    String fullUrl = linkEl.absUrl("href");
                    if (fullUrl.contains("/itm/") || fullUrl.contains("/p/")) {
                        urls.add(fullUrl);
                    }
                }
            }
            
        } catch (IOException e) {
            log.error("[Template Method - eBay] Error during scraping: {}", e.getMessage());
        }
        
        return urls;
    }
    
    @Override
    protected List<String> postProcess(List<String> urls) {
        log.info("[Template Method - eBay] Post-processing {} URLs", urls.size());
        // Call parent to remove duplicates, then add eBay-specific processing
        List<String> processed = super.postProcess(urls);
        // Could add eBay-specific filtering here
        return processed;
    }
}

