package com.scraper.service;

import com.scraper.dto.ProductInfo;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
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
    
    public ProductInfo extractProductInfo(String url) {
        try {
            Document doc = Jsoup.connect(url)
                    .timeout(TIMEOUT)
                    .userAgent("Mozilla/5.0")
                    .get();
            
            ProductInfo.ProductInfoBuilder builder = ProductInfo.builder();
            
            // Title
            Element titleElement = doc.selectFirst("h1[itemprop=name]");
            if (titleElement != null) {
                builder.title(titleElement.text());
            }
            
            // Description
            Element descElement = doc.selectFirst("div[itemprop=description]");
            if (descElement != null) {
                builder.description(descElement.text());
            }
            
            // Price
            Element priceElement = doc.selectFirst("span.adPage__content__price-feature__prices__price__value");
            Element currencyElement = doc.selectFirst("span[itemprop=priceCurrency]");
            if (priceElement != null) {
                String price = priceElement.text();
                if (price.contains("negociabil")) {
                    builder.price(price);
                } else if (currencyElement != null) {
                    builder.price(price + " " + currencyElement.attr("content"));
                } else {
                    builder.price(price);
                }
            }
            
            // Location
            Element countryElement = doc.selectFirst("meta[itemprop=addressCountry]");
            Element localityElement = doc.selectFirst("meta[itemprop=addressLocality]");
            if (countryElement != null && localityElement != null) {
                builder.location(localityElement.attr("content") + ", " + countryElement.attr("content"));
            }
            
            // Ad Info
            Map<String, String> adInfo = new HashMap<>();
            Element viewsElement = doc.selectFirst("div.adPage__aside__stats__views");
            if (viewsElement != null) {
                adInfo.put("Views", viewsElement.text());
            }
            
            Element dateElement = doc.selectFirst("div.adPage__aside__stats__date");
            if (dateElement != null) {
                adInfo.put("Update Date", dateElement.text());
            }
            
            Element adTypeElement = doc.selectFirst("div.adPage__aside__stats__type");
            if (adTypeElement != null) {
                adInfo.put("Ad Type", adTypeElement.text());
            }
            
            Element ownerElement = doc.selectFirst("a.adPage__aside__stats__owner__login");
            if (ownerElement != null) {
                adInfo.put("Owner Username", ownerElement.text());
            }
            
            builder.adInfo(adInfo);
            
            // General Info
            Map<String, String> generalInfo = new HashMap<>();
            Element generalDiv = doc.selectFirst("div.adPage__content__features__col");
            if (generalDiv != null) {
                generalDiv.select("li").forEach(li -> {
                    Element keyElement = li.selectFirst("span.adPage__content__features__key");
                    Element valueElement = li.selectFirst("span.adPage__content__features__value");
                    if (keyElement != null && valueElement != null) {
                        generalInfo.put(keyElement.text().trim(), valueElement.text().trim());
                    }
                });
            }
            builder.generalInfo(generalInfo);
            
            // Features
            Map<String, String> features = new HashMap<>();
            Element featuresDiv = doc.selectFirst("div.adPage__content__features__col.grid_7.suffix_1");
            if (featuresDiv != null) {
                featuresDiv.select("li").forEach(li -> {
                    Element keyElement = li.selectFirst("span.adPage__content__features__key");
                    Element valueElement = li.selectFirst("span.adPage__content__features__value");
                    if (keyElement != null && valueElement != null) {
                        features.put(keyElement.text().trim(), valueElement.text().trim());
                    }
                });
            }
            builder.features(features);
            
            return builder.build();
            
        } catch (IOException e) {
            log.error("Error extracting product info from {}: {}", url, e.getMessage());
            return ProductInfo.builder().build();
        }
    }
}

