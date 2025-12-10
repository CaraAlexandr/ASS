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
 * Factory Method Pattern - Concrete Product for eBay
 */
@Slf4j
public class EbayScraper implements Scraper {
    
    private static final String BASE_URL = "https://www.ebay.com";
    private static final int TIMEOUT = 30000;
    
    @Override
    public List<String> scrapeUrls(String url, int maxPages) {
        Set<String> productUrls = new HashSet<>();
        List<String> pagesToVisit = new ArrayList<>();
        pagesToVisit.add(url);
        int pagesVisited = 0;
        
        while (pagesVisited < pagesToVisit.size() && pagesVisited < maxPages) {
            String currentUrl = pagesToVisit.get(pagesVisited);
            log.info("[eBay Scraper] Scraping page: {}", currentUrl);
            
            try {
                Document doc = Jsoup.connect(currentUrl)
                        .timeout(TIMEOUT)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                        .followRedirects(true)
                        .get();
                
                int before = productUrls.size();
                
                // Try new layout first
                Elements newItems = doc.select("li.brwrvr__item-card");
                if (newItems.size() > 0) {
                    for (Element item : newItems) {
                        Element linkEl = item.selectFirst("a.brwrvr__item-card__image-link[href], a[href*='/itm/'], a[href*='/p/']");
                        if (linkEl != null) {
                            String fullUrl = linkEl.absUrl("href");
                            if (fullUrl.contains("/itm/") || fullUrl.contains("/p/")) {
                                productUrls.add(fullUrl);
                            }
                        }
                    }
                }
                
                // Fallback to old layout
                if (productUrls.size() == before) {
                    Elements items = doc.select("li.s-item");
                    for (Element item : items) {
                        Element linkEl = item.selectFirst("a.s-item__link[href], a[href*='/itm/']");
                        if (linkEl != null) {
                            String fullUrl = linkEl.absUrl("href");
                            if (fullUrl.contains("/itm/") || fullUrl.contains("/p/")) {
                                productUrls.add(fullUrl);
                            }
                        }
                    }
                }
                
                // Pagination
                Elements nextLinks = doc.select("a[rel=next][href], a[aria-label='Next page'][href]");
                for (Element next : nextLinks) {
                    String nextUrl = next.absUrl("href");
                    if (nextUrl != null && !nextUrl.isEmpty() && !pagesToVisit.contains(nextUrl)) {
                        pagesToVisit.add(nextUrl);
                    }
                }
                
                pagesVisited++;
                Thread.sleep(1000);
                
            } catch (IOException | InterruptedException e) {
                log.error("Error scraping eBay URL {}: {}", currentUrl, e.getMessage());
                pagesVisited++;
            }
        }
        
        return new ArrayList<>(productUrls);
    }
    
    @Override
    public String getSupportedDomain() {
        return "ebay.com";
    }
}

