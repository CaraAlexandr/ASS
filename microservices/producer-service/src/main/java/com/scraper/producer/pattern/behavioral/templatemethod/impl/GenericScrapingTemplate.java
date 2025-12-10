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
 * Template Method Pattern - Concrete implementation for Generic websites
 */
@Slf4j
public class GenericScrapingTemplate extends AbstractScrapingTemplate {
    
    @Override
    protected List<String> executeScraping(String url, int maxPages) {
        log.info("[Template Method - Generic] Executing generic scraping");
        List<String> urls = new ArrayList<>();
        
        try {
            Document doc = Jsoup.connect(url)
                    .timeout(30000)
                    .userAgent("Mozilla/5.0")
                    .get();
            
            // Generic extraction logic
            Elements links = doc.select("a[href]");
            for (Element a : links) {
                String href = a.absUrl("href");
                if (href != null && !href.isEmpty()) {
                    urls.add(href);
                }
            }
            
        } catch (IOException e) {
            log.error("[Template Method - Generic] Error during scraping: {}", e.getMessage());
        }
        
        return urls;
    }
}

