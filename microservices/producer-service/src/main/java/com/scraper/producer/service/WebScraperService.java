package com.scraper.producer.service;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
@Slf4j
public class WebScraperService {
    
    private static final String BASE_URL = "https://999.md";
    private static final int TIMEOUT = 30000;
    
    public List<String> scrapeProductUrls(String startingUrl, int maxPages) {
        Set<String> productUrls = new HashSet<>();
        List<String> pagesToVisit = new ArrayList<>();
        pagesToVisit.add(startingUrl);
        int pagesVisited = 0;
        
        while (pagesVisited < pagesToVisit.size() && pagesVisited < maxPages) {
            String currentUrl = pagesToVisit.get(pagesVisited);
            log.info("Scraping page: {}", currentUrl);
            
            try {
                Document doc = Jsoup.connect(currentUrl)
                        .timeout(TIMEOUT)
                        .userAgent("Mozilla/5.0")
                        .get();
                
                // Extract product URLs
                doc.select("a.js-item-ad[href]").forEach(link -> {
                    String href = link.attr("href");
                    String fullUrl = BASE_URL + href;
                    if (productUrls.add(fullUrl)) {
                        log.debug("Found product URL: {}", fullUrl);
                    }
                });
                
                // Extract pagination links
                doc.select("nav.paginator > ul > li > a[href]").forEach(link -> {
                    String href = link.attr("href");
                    String fullUrl = BASE_URL + href;
                    if (!pagesToVisit.contains(fullUrl)) {
                        pagesToVisit.add(fullUrl);
                    }
                });
                
                pagesVisited++;
                Thread.sleep(1000); // Rate limiting
                
            } catch (IOException | InterruptedException e) {
                log.error("Error scraping URL {}: {}", currentUrl, e.getMessage());
                pagesVisited++;
            }
        }
        
        log.info("Total URLs collected: {}", productUrls.size());
        return new ArrayList<>(productUrls);
    }
}

