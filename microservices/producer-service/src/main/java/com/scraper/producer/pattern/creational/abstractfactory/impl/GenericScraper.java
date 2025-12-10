package com.scraper.producer.pattern.creational.abstractfactory.impl;

import com.scraper.producer.pattern.creational.abstractfactory.ScraperProduct;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Abstract Factory Pattern - Concrete Product A2 (Generic Scraper)
 */
@Slf4j
public class GenericScraper implements ScraperProduct {
    
    @Override
    public List<String> scrapeUrls(String url, int maxPages) {
        Set<String> productUrls = new HashSet<>();
        List<String> pagesToVisit = new ArrayList<>();
        pagesToVisit.add(url);
        int pagesVisited = 0;
        
        while (pagesVisited < pagesToVisit.size() && pagesVisited < maxPages) {
            String currentUrl = pagesToVisit.get(pagesVisited);
            log.info("[Abstract Factory - Generic Scraper] Scraping: {}", currentUrl);
            
            try {
                Document doc = Jsoup.connect(currentUrl)
                        .timeout(30000)
                        .userAgent("Mozilla/5.0")
                        .get();
                
                Elements links = doc.select("a[href]");
                for (Element a : links) {
                    String href = a.absUrl("href");
                    if (href != null && !href.isEmpty()) {
                        productUrls.add(href);
                    }
                }
                
                pagesVisited++;
                Thread.sleep(1000);
                
            } catch (IOException | InterruptedException e) {
                log.error("Error: {}", e.getMessage());
                pagesVisited++;
            }
        }
        
        return new ArrayList<>(productUrls);
    }
}

