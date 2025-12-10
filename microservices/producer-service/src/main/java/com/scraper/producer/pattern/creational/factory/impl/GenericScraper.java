package com.scraper.producer.pattern.creational.factory.impl;

import com.scraper.producer.pattern.creational.factory.Scraper;
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
 * Factory Method Pattern - Concrete Product for Generic websites
 */
@Slf4j
public class GenericScraper implements Scraper {
    
    private static final int TIMEOUT = 30000;
    
    @Override
    public List<String> scrapeUrls(String url, int maxPages) {
        Set<String> productUrls = new HashSet<>();
        List<String> pagesToVisit = new ArrayList<>();
        pagesToVisit.add(url);
        int pagesVisited = 0;
        
        while (pagesVisited < pagesToVisit.size() && pagesVisited < maxPages) {
            String currentUrl = pagesToVisit.get(pagesVisited);
            log.info("[Generic Scraper] Scraping page: {}", currentUrl);
            
            try {
                Document doc = Jsoup.connect(currentUrl)
                        .timeout(TIMEOUT)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                        .followRedirects(true)
                        .get();
                
                int before = productUrls.size();
                Elements links = doc.select("a[href]");
                for (Element a : links) {
                    String href = a.absUrl("href");
                    if (href != null && !href.isEmpty()) {
                        productUrls.add(href);
                        if (productUrls.size() - before > 100) break;
                    }
                }
                
                // Pagination
                doc.select("a[rel=next][href], a[aria-label='Next'][href], a[href*='page=']")
                        .forEach(next -> {
                            String nextUrl = next.absUrl("href");
                            if (nextUrl != null && !nextUrl.isEmpty() && !pagesToVisit.contains(nextUrl)) {
                                pagesToVisit.add(nextUrl);
                            }
                        });
                
                pagesVisited++;
                Thread.sleep(1000);
                
            } catch (IOException | InterruptedException e) {
                log.error("Error scraping generic URL {}: {}", currentUrl, e.getMessage());
                pagesVisited++;
            }
        }
        
        return new ArrayList<>(productUrls);
    }
    
    @Override
    public String getSupportedDomain() {
        return "generic";
    }
}

