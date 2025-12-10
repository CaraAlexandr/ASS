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
 * Abstract Factory Pattern - Concrete Product A1 (eBay Scraper)
 */
@Slf4j
public class EbayScraper implements ScraperProduct {
    
    @Override
    public List<String> scrapeUrls(String url, int maxPages) {
        Set<String> productUrls = new HashSet<>();
        List<String> pagesToVisit = new ArrayList<>();
        pagesToVisit.add(url);
        int pagesVisited = 0;
        
        while (pagesVisited < pagesToVisit.size() && pagesVisited < maxPages) {
            String currentUrl = pagesToVisit.get(pagesVisited);
            log.info("[Abstract Factory - eBay Scraper] Scraping: {}", currentUrl);
            
            try {
                Document doc = Jsoup.connect(currentUrl)
                        .timeout(30000)
                        .userAgent("Mozilla/5.0")
                        .get();
                
                Elements items = doc.select("li.s-item, li.brwrvr__item-card");
                for (Element item : items) {
                    Element linkEl = item.selectFirst("a[href*='/itm/'], a[href*='/p/']");
                    if (linkEl != null) {
                        String fullUrl = linkEl.absUrl("href");
                        if (fullUrl.contains("/itm/") || fullUrl.contains("/p/")) {
                            productUrls.add(fullUrl);
                        }
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

