package com.scraper.producer.service;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.util.*;

@Service
@Slf4j
public class WebScraperService {
    
    private static final String BASE_URL = "https://www.ebay.com";
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
                // Enhanced browser headers to better mimic a real browser
                Document doc = Jsoup.connect(currentUrl)
                        .timeout(TIMEOUT)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                        .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
                        .header("Accept-Language", "en-US,en;q=0.9")
                        .header("Accept-Encoding", "gzip, deflate, br")
                        .header("Connection", "keep-alive")
                        .header("Upgrade-Insecure-Requests", "1")
                        .header("Sec-Fetch-Dest", "document")
                        .header("Sec-Fetch-Mode", "navigate")
                        .header("Sec-Fetch-Site", "none")
                        .header("Cache-Control", "max-age=0")
                        .followRedirects(true)
                        .maxBodySize(10 * 1024 * 1024) // 10MB max
                        .get();
                
                log.info("Page title: {}", doc.title());
                
                // Domain-specific handling: eBay listings
                String host = "";
                try { host = new URI(currentUrl).getHost(); } catch (Exception ignore) {}
                if (host != null && host.toLowerCase().contains("ebay")) {
                    int before = productUrls.size();
                    
                    // Try new layout first (brwrvr__item-card)
                    Elements newItems = doc.select("li.brwrvr__item-card");
                    log.info("[eBay] li.brwrvr__item-card count: {}", newItems.size());
                    
                    if (newItems.size() > 0) {
                        for (Element item : newItems) {
                            Element linkEl = item.selectFirst("a.brwrvr__item-card__image-link[href], a.bsig__title__wrapper[href], a[href*='/p/'], a[href*='/itm/']");
                            if (linkEl == null) continue;
                            String href = linkEl.attr("href");
                            if (href == null || href.isEmpty()) continue;
                            String fullUrl = linkEl.absUrl("href");
                            if (fullUrl == null || fullUrl.isEmpty()) {
                                if (href.startsWith("/")) fullUrl = BASE_URL + href; else fullUrl = href;
                            }
                            if (fullUrl.contains("/itm/") || fullUrl.contains("/p/")) {
                                productUrls.add(fullUrl);
                            }
                        }
                    }
                    
                    // Fallback to old layout (s-item)
                    if (productUrls.size() == before) {
                        Elements items = doc.select("li.s-item");
                        log.info("[eBay] li.s-item count: {}", items.size());
                        
                        for (Element item : items) {
                            Element linkEl = item.selectFirst("a.s-item__link[href], a[href*='/itm/']");
                            if (linkEl == null) continue;
                            String href = linkEl.attr("href");
                            if (href == null || href.isEmpty()) continue;
                            String fullUrl = linkEl.absUrl("href");
                            if (fullUrl == null || fullUrl.isEmpty()) {
                                if (href.startsWith("/")) fullUrl = BASE_URL + href; else fullUrl = href;
                            }
                            if (fullUrl.contains("/itm/") || fullUrl.contains("/p/")) {
                                productUrls.add(fullUrl);
                            }
                        }
                    }
                    
                    // Final fallback: directly scan /itm/ and /p/ links
                    if (productUrls.size() == before) {
                        Elements itmLinks = doc.select("a[href*='/itm/'], a[href*='/p/']");
                        log.info("[eBay] Direct /itm/ and /p/ links found: {}", itmLinks.size());
                        for (Element linkEl : itmLinks) {
                            String href = linkEl.attr("href");
                            if (href == null || href.isEmpty()) continue;
                            String fullUrl = linkEl.absUrl("href");
                            if (fullUrl == null || fullUrl.isEmpty()) {
                                if (href.startsWith("/")) fullUrl = BASE_URL + href; else fullUrl = href;
                            }
                            if (fullUrl.contains("/itm/") || fullUrl.contains("/p/")) {
                                productUrls.add(fullUrl);
                            }
                        }
                    }
                    
                    log.info("[eBay] Collected {} product URLs on this page", productUrls.size() - before);

                    // eBay pagination: next page links
                    Elements nextLinks = doc.select("a[rel=next][href], a[aria-label='Next page'][href], a.pagination__next[href]");
                    for (Element next : nextLinks) {
                        String href = next.attr("href");
                        if (href == null || href.isEmpty()) continue;
                        String nextUrl = next.absUrl("href");
                        if (nextUrl == null || nextUrl.isEmpty()) continue;
                        if (!pagesToVisit.contains(nextUrl)) {
                            pagesToVisit.add(nextUrl);
                        }
                    }
                    
                    pagesVisited++;
                    Thread.sleep(1000);
                    continue;
                }
                
                // Generic fallback for non-eBay: collect product URLs
                log.warn("Non-eBay domain detected, using generic extraction");
                int before = productUrls.size();
                Elements links = doc.select("a[href]");
                for (Element a : links) {
                    String href = a.absUrl("href");
                    if (href != null && !href.isEmpty()) {
                        productUrls.add(href);
                        if (productUrls.size() - before > 100) break; // cap per page
                    }
                }
                log.info("[Generic] Collected {} URLs on this page", productUrls.size() - before);

                // Generic pagination
                doc.select("a[rel=next][href], a[aria-label='Next'][href], a[aria-label='Next page'][href], a[href*='page=']").forEach(next -> {
                    String nextUrl = next.absUrl("href");
                    if (nextUrl != null && !nextUrl.isEmpty() && !pagesToVisit.contains(nextUrl)) {
                        pagesToVisit.add(nextUrl);
                    }
                });
                
                pagesVisited++;
                Thread.sleep(1000); // Rate limiting
                
            } catch (IOException | InterruptedException e) {
                log.error("Error scraping URL {}: {}", currentUrl, e.getMessage(), e);
                pagesVisited++;
            }
        }
        
        log.info("Total URLs collected: {}", productUrls.size());
        return new ArrayList<>(productUrls);
    }
}
