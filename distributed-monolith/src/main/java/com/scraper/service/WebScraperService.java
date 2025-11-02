package com.scraper.service;

import com.scraper.dto.ProductInfo;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

@Service
@Slf4j
public class WebScraperService {
    
    private static final String BASE_URL = "https://www.ebay.com";
    private static final int TIMEOUT = 30000;
    
    
    

    /**
     * Extract product information directly from listing pages without visiting individual product pages
     */
    public Map<String, ProductInfo> scrapeProductsFromListing(String startingUrl, int maxPages) {
        Map<String, ProductInfo> products = new HashMap<>();
        List<String> pagesToVisit = new ArrayList<>();
        pagesToVisit.add(startingUrl);
        int pagesVisited = 0;


        while (pagesVisited < pagesToVisit.size() && pagesVisited < maxPages) {
            String currentUrl = pagesToVisit.get(pagesVisited);
            log.info("Scraping products from listing page: {} (using direct HTTP requests)", currentUrl);

            try {
                // Fetch the listing page HTML directly (use browser-like headers)
                log.info("Fetching listing page via direct HTTP request: {}", currentUrl);
                Document doc = Jsoup.connect(currentUrl)
                        .timeout(TIMEOUT)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                        .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
                        .header("Accept-Language", "ro-RO,ro;q=0.9,en-US;q=0.8,en;q=0.7,ru;q=0.6")
                        .header("Accept-Encoding", "gzip, deflate, br")
                        .header("Connection", "keep-alive")
                        .header("Upgrade-Insecure-Requests", "1")
                        .header("Sec-Fetch-Dest", "document")
                        .header("Sec-Fetch-Mode", "navigate")
                        .header("Sec-Fetch-Site", "none")
                        .header("Referer", "https://999.md/ro")
                        .followRedirects(true)
                        .maxBodySize(10 * 1024 * 1024)
                        .get();
                String renderedHtml = doc.html();
                log.info("Fetched HTML length: {}", renderedHtml.length());

                // Domain-specific handling: eBay listings
                String host = "";
                try { host = new URI(currentUrl).getHost(); } catch (Exception ignore) {}
                if (host != null && host.toLowerCase().contains("ebay")) {
                    int beforeCount = products.size();
                    
                    // Try new layout first (brwrvr__item-card)
                    Elements newItems = doc.select("li.brwrvr__item-card");
                    log.info("[eBay] li.brwrvr__item-card count: {}", newItems.size());
                    
                    if (newItems.size() > 0) {
                        for (Element item : newItems) {
                            // Extract product URL
                            Element linkEl = item.selectFirst("a.brwrvr__item-card__image-link[href], a.bsig__title__wrapper[href], a[href*='/p/'], a[href*='/itm/']");
                            if (linkEl == null) continue;
                            String href = linkEl.attr("href");
                            if (href == null || href.isEmpty()) continue;
                            String fullUrl = linkEl.absUrl("href");
                            if (fullUrl == null || fullUrl.isEmpty()) {
                                if (href.startsWith("/")) fullUrl = BASE_URL + href; else fullUrl = href;
                            }
                            
                            if (!products.containsKey(fullUrl)) {
                                // Title
                                Element titleEl = item.selectFirst("h3.bsig__title__text, .bsig__title__text, h3[class*=title]");
                                String title = titleEl != null ? titleEl.text().trim() : "";
                                
                                // Subtitle/Description
                                Element subtitleEl = item.selectFirst(".bsig__subTitle, .bsig____search.subTitle");
                                String subtitle = subtitleEl != null ? subtitleEl.text().trim() : null;
                                
                                // Price
                                Element priceEl = item.selectFirst(".bsig__price, .bsig__price--displayprice");
                                String price = priceEl != null ? priceEl.text().trim() : null;
                                
                                // Condition
                                Element conditionEl = item.selectFirst(".bsig__listingCondition, .bsig__listingCondition.secondary");
                                String condition = conditionEl != null ? conditionEl.text().trim() : null;
                                
                                // Brand (usually in listingCondition)
                                String brand = null;
                                Elements conditionParts = item.select(".bsig__listingCondition.secondary");
                                for (Element part : conditionParts) {
                                    String text = part.text().trim();
                                    if (!text.equals(condition) && !text.equals("·") && !text.isEmpty()) {
                                        brand = text;
                                        break;
                                    }
                                }
                                
                                // Shipping
                                Element shippingEl = item.selectFirst(".bsig__logisticsCost");
                                String shipping = shippingEl != null ? shippingEl.text().trim() : null;
                                
                                // Sold count
                                Element soldEl = item.selectFirst(".bsig__item-hotness, .bsig__item-hotness .negative, .bsig__item-hotness .textual-display");
                                String soldCount = soldEl != null ? soldEl.text().trim() : null;
                                
                                // Rating
                                Element ratingEl = item.selectFirst(".star-rating");
                                String rating = null;
                                if (ratingEl != null) {
                                    String starsAttr = ratingEl.attr("aria-label");
                                    if (starsAttr != null && !starsAttr.isEmpty()) {
                                        rating = starsAttr;
                                    }
                                }
                                Element reviewCountEl = item.selectFirst(".bsig__product-review__count");
                                String reviewCount = reviewCountEl != null ? reviewCountEl.text().trim() : null;
                                
                                // Image
                                Element imgEl = item.selectFirst("img.brwrvr__item-card__image[src], img[data-originalsrc]");
                                String imageUrl = null;
                                if (imgEl != null) {
                                    imageUrl = imgEl.attr("data-originalsrc");
                                    if (imageUrl == null || imageUrl.isEmpty()) {
                                        imageUrl = imgEl.attr("src");
                                    }
                                }
                                
                                // Product ID from URL
                                Map<String, String> adInfo = new HashMap<>();
                                try {
                                    // Extract product ID from /p/ URL
                                    java.util.regex.Matcher pMatcher = java.util.regex.Pattern.compile("/p/(\\d+)").matcher(fullUrl);
                                    if (pMatcher.find()) {
                                        adInfo.put("Product ID", pMatcher.group(1));
                                    }
                                    // Extract item ID from iid parameter
                                    java.util.regex.Matcher iidMatcher = java.util.regex.Pattern.compile("iid=([0-9]+)").matcher(fullUrl);
                                    if (iidMatcher.find()) {
                                        adInfo.put("Item ID", iidMatcher.group(1));
                                    }
                                    // Also try /itm/ pattern
                                    java.util.regex.Matcher itmMatcher = java.util.regex.Pattern.compile("/(?:itm|i|p)/(\\d+)").matcher(fullUrl);
                                    if (itmMatcher.find() && !adInfo.containsKey("Item ID")) {
                                        adInfo.put("Item ID", itmMatcher.group(1));
                                    }
                                } catch (Exception ignoreId) {}
                                
                                if (condition != null) adInfo.put("Condition", condition);
                                if (brand != null) adInfo.put("Brand", brand);
                                if (shipping != null) adInfo.put("Shipping", shipping);
                                if (soldCount != null) adInfo.put("Sold Count", soldCount);
                                if (rating != null) adInfo.put("Rating", rating);
                                if (reviewCount != null) adInfo.put("Review Count", reviewCount);

                                Map<String, String> generalInfo = new HashMap<>();
                                if (imageUrl != null) generalInfo.put("Image URL", imageUrl);
                                if (subtitle != null) generalInfo.put("Subtitle", subtitle);

                                // Use subtitle as description if available
                                String description = subtitle;

                                ProductInfo info = ProductInfo.builder()
                                        .title(title)
                                        .description(description)
                                        .price(price)
                                        .adInfo(adInfo)
                                        .generalInfo(generalInfo)
                                        .build();
                                products.put(fullUrl, info);
                            }
                        }
                    }
                    
                    // Fallback to old layout (s-item)
                    if (products.size() == beforeCount) {
                        Elements items = doc.select("ul.srp-results li.s-item, ul.b-list__items_nofooter li.s-item, li.s-item");
                        log.info("[eBay] li.s-item count: {}", items.size());
                        
                        if (items.size() > 0) {
                            for (Element item : items) {
                                Element linkEl = item.selectFirst("a.s-item__link[href], a[href*='/itm/']");
                                if (linkEl == null) continue;
                                String href = linkEl.attr("href");
                                if (href == null || href.isEmpty()) continue;
                                String fullUrl = linkEl.absUrl("href");
                                if (fullUrl == null || fullUrl.isEmpty()) {
                                    if (href.startsWith("/")) fullUrl = BASE_URL + href; else fullUrl = href;
                                }
                                if (!fullUrl.contains("/itm/") && !fullUrl.contains("/p/")) continue;

                                if (!products.containsKey(fullUrl)) {
                                    Element titleEl = item.selectFirst("h3.s-item__title, span[role=heading], .s-item__title");
                                    String title = titleEl != null ? titleEl.text().trim() : linkEl.text().trim();
                                    
                                    // Extract subtitle/condition from old layout
                                    Element subtitleEl = item.selectFirst(".s-item__subtitle");
                                    String subtitle = subtitleEl != null ? subtitleEl.text().trim() : null;
                                    
                                    Element priceEl = item.selectFirst(".s-item__price, .x-price-primary");
                                    String price = priceEl != null ? priceEl.text().trim() : null;
                                    
                                    Element conditionEl = item.selectFirst(".s-item__condition, .SECONDARY_INFO");
                                    String condition = conditionEl != null ? conditionEl.text().trim() : null;
                                    
                                    Element shippingEl = item.selectFirst(".s-item__shipping, .s-item__freeXDays");
                                    String shipping = shippingEl != null ? shippingEl.text().trim() : null;
                                    
                                    Element soldEl = item.selectFirst(".s-item__hotness, .s-item__quantitySold");
                                    String soldCount = soldEl != null ? soldEl.text().trim() : null;
                                    
                                    Element imgEl = item.selectFirst("img.s-item__image-img[src], img[src]");
                                    String imageUrl = imgEl != null ? imgEl.attr("src") : null;

                                    Map<String, String> adInfo = new HashMap<>();
                                    try {
                                        java.util.regex.Matcher m = java.util.regex.Pattern.compile("/(?:itm|i|p)/(\\d+)").matcher(fullUrl);
                                        if (m.find()) { adInfo.put("Item ID", m.group(1)); }
                                    } catch (Exception ignoreId) {}
                                    
                                    if (condition != null) adInfo.put("Condition", condition);
                                    if (shipping != null) adInfo.put("Shipping", shipping);
                                    if (soldCount != null) adInfo.put("Sold Count", soldCount);

                                    Map<String, String> generalInfo = new HashMap<>();
                                    if (imageUrl != null) generalInfo.put("Image URL", imageUrl);
                                    if (subtitle != null) generalInfo.put("Subtitle", subtitle);

                                    ProductInfo info = ProductInfo.builder()
                                            .title(title)
                                            .description(subtitle)
                                            .price(price)
                                            .adInfo(adInfo)
                                            .generalInfo(generalInfo)
                                            .build();
                                    products.put(fullUrl, info);
                                }
                            }
                        }
                    }

                    // Fallback: directly scan /itm/ or /p/ links when structure differs
                    if (products.size() == beforeCount) {
                        Elements itmLinks = doc.select("a[href*='/itm/'], a[href*='/p/']");
                        if (itmLinks.size() > 0) {
                            for (Element linkEl : itmLinks) {
                                String href = linkEl.attr("href");
                                if (href == null || href.isEmpty()) continue;
                                String fullUrl = linkEl.absUrl("href");
                                if (fullUrl == null || fullUrl.isEmpty()) {
                                    if (href.startsWith("/")) fullUrl = "https://www.ebay.com" + href; else fullUrl = href;
                                }
                                if (!fullUrl.contains("/itm/") && !fullUrl.contains("/p/")) continue;
                                if (products.containsKey(fullUrl)) continue;

                                // Try to find a nearby container to extract price/title
                                Element container = linkEl.closest("li.s-item, li.brwrvr__item-card");
                                if (container == null) {
                                    Element p = linkEl.parent();
                                    int depth = 0;
                                    while (p != null && depth++ < 6) {
                                        if (p.hasClass("s-item") || p.hasClass("brwrvr__item-card") || 
                                            p.selectFirst(".s-item__price") != null || p.selectFirst(".bsig__price") != null) { 
                                            container = p; break; 
                                        }
                                        p = p.parent();
                                    }
                                }
                                String title = linkEl.text().trim();
                                String price = null;
                                if (container != null) {
                                    Element priceEl = container.selectFirst(".s-item__price, .x-price-primary, .bsig__price");
                                    if (priceEl != null) price = priceEl.text().trim();
                                    Element titleEl = container.selectFirst("h3.s-item__title, h3.bsig__title__text, span[role=heading], .s-item__title");
                                    if (titleEl != null && !titleEl.text().trim().isEmpty()) title = titleEl.text().trim();
                                }

                                Map<String, String> adInfo = new HashMap<>();
                                try {
                                    java.util.regex.Matcher m = java.util.regex.Pattern.compile("/(?:itm|i|p)/(\\d+)").matcher(fullUrl);
                                    if (m.find()) { adInfo.put("Item ID", m.group(1)); }
                                } catch (Exception ignoreId) {}

                                ProductInfo info = ProductInfo.builder()
                                        .title(title.isEmpty() ? "eBay Item" : title)
                                        .price(price)
                                        .adInfo(adInfo)
                                        .build();
                                products.put(fullUrl, info);
                            }
                        }
                    }

                    log.info("[eBay] Extracted {} products on this page", products.size() - beforeCount);

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

                // Generic fallback for non-eBay listings: pick anchors with visible text and nearby price
                if (host == null || !host.toLowerCase().contains("ebay")) {
                    int beforeGeneric = products.size();
                    Elements links = doc.select("a[href]");
                    int added = 0;
                    for (Element a : links) {
                        if (added >= 50) break; // avoid noise
                        String href = a.absUrl("href");
                        String text = a.text() != null ? a.text().trim() : "";
                        if (href.isEmpty() || text.isEmpty()) continue;
                        // Heuristic: prefer links inside list items with an image or a price nearby
                        Element container = a.closest("li, article, div");
                        if (container == null) continue;
                        Element priceEl = container.selectFirst("[class*='price'], .price, .amount, .x-price-primary");
                        String price = priceEl != null ? priceEl.text().trim() : null;
                        if (!products.containsKey(href)) {
                            products.put(href, ProductInfo.builder()
                                    .title(text)
                                    .price(price)
                                    .build());
                            added++;
                        }
                    }
                    log.info("[Generic] Extracted {} products on this page", products.size() - beforeGeneric);

                    // Generic pagination: rel=next, or query param page
                    doc.select("a[rel=next][href], a[aria-label='Next'][href], a[aria-label='Next page'][href], a[href*='page=']").forEach(next -> {
                        String nextUrl = next.absUrl("href");
                        if (!nextUrl.isEmpty() && !pagesToVisit.contains(nextUrl)) {
                            pagesToVisit.add(nextUrl);
                        }
                    });

                    pagesVisited++;
                    Thread.sleep(1000);
                    continue;
                }
                
                // First, try to find the main container with products
                Element adListContainer = doc.selectFirst("div.styles_adlist__3YsgA, div[class*=adlist]");
                if (adListContainer != null) {
                    log.info("Found AdList container, searching inside it...");
                } else {
                    // Try alternative selectors
                    adListContainer = doc.selectFirst("div[data-sentry-component=AdList]");
                    if (adListContainer != null) {
                        log.info("Found AdList container via data-sentry-component, searching inside it...");
                    } else {
                        log.warn("No AdList container found, searching globally...");
                    }
                }
                
                // Search for wrappers - either in container or globally
                Elements wrapperElements = adListContainer != null
                    ? adListContainer.select("div.AdPhoto_wrapper__gAOIH")
                    : doc.select("div.AdPhoto_wrapper__gAOIH");

                int wrapperCount = wrapperElements.size();
                log.info("Found {} AdPhoto wrapper divs on listing page", wrapperCount);

                // DEBUG: Log the first part of HTML to see structure
                if (wrapperCount == 0) {
                    log.warn("No wrapper divs found. HTML preview (first 1000 chars): {}",
                        renderedHtml.length() > 1000 ? renderedHtml.substring(0, 1000) : renderedHtml);

                    // Check if we can find any divs with data-index attribute
                    Elements dataIndexDivs = doc.select("div[data-index]");
                    log.info("Found {} divs with data-index attribute", dataIndexDivs.size());

                    // Check for any divs containing AdPhoto
                    Elements adPhotoDivs = doc.select("div[class*=AdPhoto]");
                    log.info("Found {} divs with AdPhoto in class name", adPhotoDivs.size());
                    if (adPhotoDivs.size() > 0) {
                        log.info("AdPhoto div classes: {}", adPhotoDivs.stream().limit(3).map(e -> e.className()).toList());
                    }

                    // Check for the specific container the user mentioned
                    Elements adListContainers = doc.select("div[data-sentry-component=AdList]");
                    log.info("Found {} AdList containers via data-sentry-component", adListContainers.size());

                    // Try to find the actual product data in the HTML
                    if (renderedHtml.contains("Dacia Logan") || renderedHtml.contains("Mercedes A-Class")) {
                        log.info("HTML contains expected car names, but selectors aren't finding them!");
                        // Look for the exact structure from user's HTML
                        Elements allDivsWithIndex = doc.select("div[data-index]");
                        log.info("Found {} divs with data-index, checking for AdPhoto_wrapper content...", allDivsWithIndex.size());

                        for (Element div : allDivsWithIndex) {
                            if (div.html().contains("AdPhoto_wrapper")) {
                                log.info("Found div with data-index containing AdPhoto_wrapper!");
                                log.info("Div content preview: {}", div.html().substring(0, Math.min(200, div.html().length())));
                                break;
                            }
                        }
                    }
                }
                
                // Filter out skeleton/loading placeholders
                Elements skeletonElements = adListContainer != null
                    ? adListContainer.select("div.AdPhoto_wrapper__skeleton__rHjT7")
                    : doc.select("div.AdPhoto_wrapper__skeleton__rHjT7");
                int skeletonCount = skeletonElements.size();
                
                // Count real (non-skeleton) wrappers
                int realWrapperCount = 0;
                for (Element wrapper : wrapperElements) {
                    if (!wrapper.hasClass("AdPhoto_wrapper__skeleton__rHjT7")) {
                        realWrapperCount++;
                    }
                }
                log.info("Found {} skeleton wrappers, {} real product wrappers", skeletonCount, realWrapperCount);
                
                // DEBUG: Check for data-id spans (in container if exists)
                int dataIdCount = (adListContainer != null 
                    ? adListContainer.select("span[data-testid=ad-favorites][data-id], span[data-testid=add-booster-ad-favorites][data-id]")
                    : doc.select("span[data-testid=ad-favorites][data-id], span[data-testid=add-booster-ad-favorites][data-id]")).size();
                log.info("Found {} span elements with data-id attribute in {}container", dataIdCount, adListContainer != null ? "AdList " : "");
                
                // DEBUG: Check for data-adid divs (in container if exists)
                int dataAdidCount = (adListContainer != null
                    ? adListContainer.select("div[data-adid]")
                    : doc.select("div[data-adid]")).size();
                log.info("Found {} divs with data-adid attribute in {}container", dataAdidCount, adListContainer != null ? "AdList " : "");
                
                // DEBUG: Log sample wrapper if found
                if (realWrapperCount > 0) {
                    for (Element wrapper : wrapperElements) {
                        if (!wrapper.hasClass("AdPhoto_wrapper__skeleton__rHjT7")) {
                            String wrapperHtml = wrapper.outerHtml();
                            log.info("Sample real wrapper HTML (first 500 chars): {}", 
                                    wrapperHtml.length() > 500 ? wrapperHtml.substring(0, 500) : wrapperHtml);
                            break; // Just log the first one
                        }
                    }
                }
                
                // Check for API endpoints in script tags and HTML
                Elements scriptTags = doc.select("script");
                log.info("Found {} script tags in HTML", scriptTags.size());

                // Initialize foundIds here for use throughout this section
                Set<String> foundIds = new HashSet<>();

                // Look for GraphQL endpoints or API calls
                Set<String> apiUrls = new HashSet<>();
                for (Element script : scriptTags) {
                    String scriptContent = script.html();
                    // Look for GraphQL endpoints
                    java.util.regex.Pattern apiPattern = java.util.regex.Pattern.compile("(?:uri|endpoint|url)[\"']\\s*:\\s*[\"']([^\"']*api[^\"']*)[\"']");
                    java.util.regex.Matcher apiMatcher = apiPattern.matcher(scriptContent);
                    while (apiMatcher.find()) {
                        String url = apiMatcher.group(1);
                        if (!url.contains("sentry") && !url.contains("analytics")) {
                            apiUrls.add(url);
                        }
                    }

                    // Look for fetch/XHR calls
                    java.util.regex.Pattern fetchPattern = java.util.regex.Pattern.compile("fetch\\([\"']([^\"']*api[^\"']*)[\"']");
                    java.util.regex.Matcher fetchMatcher = fetchPattern.matcher(scriptContent);
                    while (fetchMatcher.find()) {
                        apiUrls.add(fetchMatcher.group(1));
                    }
                }

                if (!apiUrls.isEmpty()) {
                    log.info("Found potential API endpoints:");
                    apiUrls.forEach(url -> log.info("  - {}", url));
                }

                // Look for window.__NEXT_DATA__ which often contains page props
                Element nextDataScript = doc.selectFirst("script:contains(__NEXT_DATA__)");
                if (nextDataScript != null) {
                    String nextDataContent = nextDataScript.html();
                    log.info("Found __NEXT_DATA__ script, checking for product data...");

                    // Try to extract product IDs from Next.js data
                    java.util.regex.Pattern nextIdPattern = java.util.regex.Pattern.compile("\"(?:id|adId|adid)\"\\s*:\\s*\"(\\d{6,12})\"");
                    java.util.regex.Matcher nextMatcher = nextIdPattern.matcher(nextDataContent);
                    while (nextMatcher.find()) {
                        foundIds.add(nextMatcher.group(1));
                        log.info("Found product ID in Next.js data: {}", nextMatcher.group(1));
                    }
                }
                
                // DEBUG: Check raw HTML for product IDs using multiple regex patterns
                String rawHtml = doc.html();

                // Pattern 1: /ro/ followed by digits
                java.util.regex.Pattern pattern1 = java.util.regex.Pattern.compile("/ro/(\\d{6,12})(?!\\d)");
                java.util.regex.Matcher matcher1 = pattern1.matcher(rawHtml);
                while (matcher1.find()) {
                    foundIds.add(matcher1.group(1));
                }
                
                // Pattern 2: Look for product IDs in JSON/data attributes (e.g., "adid":"102552121")
                java.util.regex.Pattern pattern2 = java.util.regex.Pattern.compile("\"adid\"\\s*:\\s*\"(\\d{6,12})\"");
                java.util.regex.Matcher matcher2 = pattern2.matcher(rawHtml);
                while (matcher2.find()) {
                    foundIds.add(matcher2.group(1));
                }
                
                // Pattern 3: Look for data-adid="102552121" 
                java.util.regex.Pattern pattern3 = java.util.regex.Pattern.compile("data-adid=[\"'](\\d{6,12})[\"']");
                java.util.regex.Matcher matcher3 = pattern3.matcher(rawHtml);
                while (matcher3.find()) {
                    foundIds.add(matcher3.group(1));
                }
                
                // Pattern 4: Look for data-id="102552121"
                java.util.regex.Pattern pattern4 = java.util.regex.Pattern.compile("data-id=[\"'](\\d{6,12})[\"']");
                java.util.regex.Matcher matcher4 = pattern4.matcher(rawHtml);
                while (matcher4.find()) {
                    foundIds.add(matcher4.group(1));
                }
                
                // Pattern 5: Look for href="/ro/102552121" or href='/ro/102552121'
                java.util.regex.Pattern pattern5 = java.util.regex.Pattern.compile("href=[\"']/ro/(\\d{6,12})(?:[?\"']|[\"']|\\s|>)");
                java.util.regex.Matcher matcher5 = pattern5.matcher(rawHtml);
                while (matcher5.find()) {
                    foundIds.add(matcher5.group(1));
                }
                
                // Pattern 6: Look in script tags for JSON arrays with product IDs
                // e.g., [{"id":"102275274",...}] or {"items":[{"id":102275274,...}]}
                for (Element script : scriptTags) {
                    String scriptContent = script.html();
                    // Look for patterns like: "id":"102275274" or 'id':102275274 or id:"102275274"
                    java.util.regex.Pattern scriptPattern = java.util.regex.Pattern.compile("[\"']id[\"']\\s*[:=]\\s*[\"']?(\\d{6,12})[\"']?");
                    java.util.regex.Matcher scriptMatcher = scriptPattern.matcher(scriptContent);
                    while (scriptMatcher.find()) {
                        foundIds.add(scriptMatcher.group(1));
                    }
                    // Also look for adid/adId/ad_id
                    scriptPattern = java.util.regex.Pattern.compile("[\"'](?:adid|adId|ad_id)[\"']\\s*[:=]\\s*[\"']?(\\d{6,12})[\"']?");
                    scriptMatcher = scriptPattern.matcher(scriptContent);
                    while (scriptMatcher.find()) {
                        foundIds.add(scriptMatcher.group(1));
                    }
                }
                
                log.info("Regex search of raw HTML found {} unique product IDs using multiple patterns", foundIds.size());
                if (foundIds.size() > 0) {
                    log.info("Sample product IDs from raw HTML: {}", 
                            foundIds.stream().limit(10).collect(java.util.stream.Collectors.joining(", ")));
                } else {
                    // Log a sample of the HTML to see what's there
                    int htmlLength = rawHtml.length();
                    log.warn("No product IDs found in HTML! HTML length: {} chars", htmlLength);
                    if (htmlLength > 0) {
                        // Search for any occurrence of "ro/" to see if the pattern exists at all
                        int roOccurrences = 0;
                        int index = 0;
                        while ((index = rawHtml.indexOf("/ro/", index)) != -1) {
                            roOccurrences++;
                            index += 4;
                            if (roOccurrences > 100) break; // Limit search
                        }
                        log.warn("Found {} occurrences of '/ro/' in HTML", roOccurrences);
                        
                        // Show a sample around first /ro/ occurrence
                        int firstRoIndex = rawHtml.indexOf("/ro/");
                        if (firstRoIndex != -1) {
                            int start = Math.max(0, firstRoIndex - 50);
                            int end = Math.min(rawHtml.length(), firstRoIndex + 100);
                            log.warn("Sample HTML around first '/ro/': ...{}...", rawHtml.substring(start, end));
                        }
                        
                        // Check script tags for product data
                        log.warn("Checking script tags for embedded product data...");
                        for (Element script : scriptTags) {
                            String type = script.attr("type");
                            String id = script.attr("id");
                            String className = script.attr("class");
                            if (script.html().length() > 100) {
                                String preview = script.html().substring(0, Math.min(200, script.html().length()));
                                log.warn("Script tag preview (type={}, id={}, class={}): {}", type, id, className, preview);
                                if (preview.contains("102275274") || preview.contains("102596782") || preview.contains("__NEXT_DATA__") || preview.contains("window.__")) {
                                    log.warn("This script tag might contain product data! Full length: {} chars", script.html().length());
                                    // Try to extract all IDs from this script
                                    java.util.regex.Pattern idPattern = java.util.regex.Pattern.compile("(?:href|url|link|path)\\s*[:=]\\s*[\"']/ro/(\\d{6,12})[\"']");
                                    java.util.regex.Matcher idMatcher = idPattern.matcher(script.html());
                                    int count = 0;
                                    while (idMatcher.find() && count < 10) {
                                        log.warn("Found product ID in script: {}", idMatcher.group(1));
                                        foundIds.add(idMatcher.group(1));
                                        count++;
                                    }
                                }
                            }
                        }
                    }
                }
                
                if (realWrapperCount == 0 && skeletonCount > 0) {
                    log.warn("Only skeleton/loading placeholders found! The page content may be loaded via JavaScript.");
                    log.warn("Attempting to extract product IDs from raw HTML/source code...");
                }
                
                // Filter out skeleton wrappers - only process real product wrappers
                String realWrapperSelector = "div.AdPhoto_wrapper__gAOIH:not(.AdPhoto_wrapper__skeleton__rHjT7)";
                
                // Strategy 1: Extract from data-id attributes (MOST RELIABLE - these are always present)
                // Search inside the container if it exists, otherwise search globally
                Elements favoriteSpans = adListContainer != null
                    ? adListContainer.select("span[data-testid=ad-favorites][data-id], span[data-testid=add-booster-ad-favorites][data-id]")
                    : doc.select("span[data-testid=ad-favorites][data-id], span[data-testid=add-booster-ad-favorites][data-id]");
                
                log.info("Found {} favorite spans with data-id in {}container", favoriteSpans.size(), adListContainer != null ? "AdList " : "");
                
                favoriteSpans.forEach(favoriteSpan -> {
                    String productId = favoriteSpan.attr("data-id");
                    if (productId != null && !productId.isEmpty() && productId.matches("\\d+")) {
                        String fullUrl = BASE_URL + "/ro/" + productId;
                        
                        if (!products.containsKey(fullUrl)) {
                            // Find the wrapper div containing this favorite span
                            Element wrapper = favoriteSpan.parent();
                            while (wrapper != null && !wrapper.equals(doc)) {
                                if (wrapper.hasClass("AdPhoto_wrapper__gAOIH")) {
                                    break;
                                }
                                wrapper = wrapper.parent();
                            }
                            
                            if (wrapper != null && wrapper.hasClass("AdPhoto_wrapper__gAOIH")) {
                                ProductInfo productInfo = extractProductInfoFromListing(wrapper, fullUrl);
                                products.put(fullUrl, productInfo);
                                log.debug("Extracted product via data-id: {} - {}", fullUrl, productInfo.getTitle());
                            }
                        }
                    }
                });
                
                log.info("Extracted {} products via data-id attributes", products.size());
                
                // Strategy 1b: Extract from data-adid on parent divs (skip skeletons)
                int beforeDataAdid = products.size();
                Elements adidDivs = adListContainer != null
                    ? adListContainer.select("div[data-adid]")
                    : doc.select("div[data-adid]");
                
                adidDivs.forEach(adDiv -> {
                    // Skip if it's a skeleton or loading placeholder
                    if (adDiv.hasClass("AdPhoto_wrapper__skeleton__rHjT7") || 
                        adDiv.selectFirst(".AdPhoto_wrapper__skeleton__rHjT7") != null) {
                        return;
                    }
                    String adId = adDiv.attr("data-adid");
                    if (adId != null && !adId.isEmpty() && adId.matches("\\d+")) {
                        String fullUrl = BASE_URL + "/ro/" + adId;
                        
                        if (!products.containsKey(fullUrl)) {
                            // Find wrapper inside this div
                            Element wrapper = adDiv.selectFirst("div.AdPhoto_wrapper__gAOIH");
                            if (wrapper == null) {
                                wrapper = adDiv; // Use the div itself as wrapper if no nested wrapper
                            }
                            
                            ProductInfo productInfo = extractProductInfoFromListing(wrapper, fullUrl);
                            products.put(fullUrl, productInfo);
                            log.debug("Extracted product via data-adid: {} - {}", fullUrl, productInfo.getTitle());
                        }
                    }
                });
                log.info("Extracted {} additional products via data-adid", products.size() - beforeDataAdid);
                
                // Strategy 2a: AGGRESSIVE - Find ANY link with /ro/[digits] pattern anywhere in the document
                if (realWrapperCount == 0) {
                    log.warn("No wrappers found, trying aggressive search for ANY links with product pattern...");
                    Elements allLinks = doc.select("a[href*='/ro/']");
                    log.info("Found {} total links containing '/ro/' in document", allLinks.size());
                    
                    for (Element link : allLinks) {
                        String href = link.attr("href");
                        if (href != null && href.matches(".*/ro/\\d{6,12}.*")) {
                            java.util.regex.Pattern linkPattern = java.util.regex.Pattern.compile("/ro/(\\d{6,12})");
                            java.util.regex.Matcher linkMatcher = linkPattern.matcher(href);
                            if (linkMatcher.find()) {
                                String productId = linkMatcher.group(1);
                                String fullUrl = BASE_URL + "/ro/" + productId;
                                
                                if (!products.containsKey(fullUrl)) {
                                    // Try to find parent container
                                    Element container = link.parent();
                                    int depth = 0;
                                    while (container != null && depth < 5 && !container.equals(doc)) {
                                        // Check if this container might be a product wrapper
                                        if (container.hasAttr("data-index") || 
                                            container.select("img").size() > 0 ||
                                            container.select("span.AdPrice_price__2L3eA").size() > 0) {
                                            ProductInfo productInfo = extractProductInfoFromListing(container, fullUrl);
                                            if (productInfo.getTitle() != null && !productInfo.getTitle().isEmpty()) {
                                                products.put(fullUrl, productInfo);
                                                log.debug("Extracted product via aggressive link search: {} - {}", fullUrl, productInfo.getTitle());
                                                break;
                                            }
                                        }
                                        container = container.parent();
                                        depth++;
                                    }
                                    
                                    // If we couldn't extract from container, create minimal entry
                                    if (!products.containsKey(fullUrl)) {
                                        String title = link.text().trim();
                                        if (title.isEmpty()) {
                                            title = "Product " + productId;
                                        }
                                        ProductInfo productInfo = ProductInfo.builder()
                                                .title(title)
                                                .build();
                                        products.put(fullUrl, productInfo);
                                        log.debug("Created minimal product entry from link: {} - {}", fullUrl, title);
                                    }
                                }
                            }
                        }
                    }
                    log.info("Aggressive search found {} products", products.size());
                }
                
                // Strategy 2: Extract from wrapper divs using links (skip skeletons)
                // Use the wrapperElements we already found
                wrapperElements.forEach(wrapper -> {
                    // Skip skeleton wrappers
                    if (wrapper.hasClass("AdPhoto_wrapper__skeleton__rHjT7")) {
                        return;
                    }
                    // Get product URL - try multiple selectors
                    Element linkElement = wrapper.selectFirst("a.AdPhoto_info__link__OwhY6[href], a[data-testid=photo-item-title][href]");
                    if (linkElement == null) {
                        linkElement = wrapper.selectFirst("a.AdPhoto_image__BMixw[href]");
                    }
                    if (linkElement == null) {
                        linkElement = wrapper.selectFirst("a[href^='/ro/']");
                    }
                    
                    String fullUrl = null;
                    if (linkElement != null) {
                        String href = linkElement.attr("href");
                        if (href != null && !href.isEmpty()) {
                            String cleanHref = href.split("\\?")[0].trim();
                            if (cleanHref.startsWith("/ro/")) {
                                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("/ro/(\\d+)");
                                java.util.regex.Matcher hrefMatcher = pattern.matcher(cleanHref);
                                if (hrefMatcher.find()) {
                                    String productId = hrefMatcher.group(1);
                                    fullUrl = BASE_URL + "/ro/" + productId;
                                }
                            }
                        }
                    }
                    
                    // Fallback: try parent div with data-adid
                    if (fullUrl == null) {
                        Element parent = wrapper.parent();
                        while (parent != null && !parent.equals(doc)) {
                            String adId = parent.attr("data-adid");
                            if (adId != null && !adId.isEmpty() && adId.matches("\\d+")) {
                                fullUrl = BASE_URL + "/ro/" + adId;
                                break;
                            }
                            parent = parent.parent();
                        }
                    }
                    
                    if (fullUrl != null && !products.containsKey(fullUrl)) {
                        // Extract product info from this wrapper
                        ProductInfo productInfo = extractProductInfoFromListing(wrapper, fullUrl);
                        products.put(fullUrl, productInfo);
                        log.debug("Extracted product via link: {} - {}", fullUrl, productInfo.getTitle());
                    }
                });
                
                // Strategy 3: Fallback - regex extraction from HTML if still no products
                // This is CRITICAL because the page may use JavaScript to load content
                if (products.isEmpty() || products.size() < 10) {
                    log.info("Few or no products found via CSS selectors (found {}), extracting from raw HTML using regex...", products.size());
                    
                    // Use the IDs we already found in debug section, or search again
                    if (foundIds.isEmpty()) {
                        // Re-search with multiple patterns
                        java.util.regex.Pattern regexPattern = java.util.regex.Pattern.compile("/ro/(\\d{6,12})(?!\\d)");
                        java.util.regex.Matcher matcher = regexPattern.matcher(rawHtml);
                        while (matcher.find()) {
                            foundIds.add(matcher.group(1));
                        }
                        
                        // Also try data-adid and data-id patterns
                        java.util.regex.Pattern adidPattern = java.util.regex.Pattern.compile("data-adid=[\"'](\\d{6,12})[\"']");
                        matcher = adidPattern.matcher(rawHtml);
                        while (matcher.find()) {
                            foundIds.add(matcher.group(1));
                        }
                        
                        java.util.regex.Pattern dataIdPattern = java.util.regex.Pattern.compile("data-id=[\"'](\\d{6,12})[\"']");
                        matcher = dataIdPattern.matcher(rawHtml);
                        while (matcher.find()) {
                            foundIds.add(matcher.group(1));
                        }
                    }
                    
                    int regexFound = foundIds.size();
                    
                    for (String productId : foundIds) {
                        String fullUrl = BASE_URL + "/ro/" + productId;
                        if (!products.containsKey(fullUrl)) {
                            // Try to find the wrapper for this product ID
                            Element wrapper = null;
                            
                            // Try to find via data-adid
                            Element adDiv = doc.selectFirst("div[data-adid=" + productId + "]");
                            if (adDiv != null && !adDiv.hasClass("AdPhoto_wrapper__skeleton__rHjT7")) {
                                wrapper = adDiv.selectFirst("div.AdPhoto_wrapper__gAOIH:not(.AdPhoto_wrapper__skeleton__rHjT7)");
                                if (wrapper == null && !adDiv.hasClass("AdPhoto_wrapper__skeleton__rHjT7")) {
                                    wrapper = adDiv;
                                }
                            }
                            
                            // Try to find via data-id
                            if (wrapper == null) {
                                Element favoriteSpan = doc.selectFirst("span[data-id=" + productId + "]");
                                if (favoriteSpan != null) {
                                    Element tempWrapper = favoriteSpan.parent();
                                    while (tempWrapper != null && !tempWrapper.equals(doc)) {
                                        if (tempWrapper.hasClass("AdPhoto_wrapper__gAOIH") && 
                                            !tempWrapper.hasClass("AdPhoto_wrapper__skeleton__rHjT7")) {
                                            wrapper = tempWrapper;
                                            break;
                                        }
                                        tempWrapper = tempWrapper.parent();
                                    }
                                }
                            }
                            
                            // Try to find wrapper containing this product ID in links
                            if (wrapper == null) {
                                Element linkWithId = doc.selectFirst("a[href*='/ro/" + productId + "']");
                                if (linkWithId != null) {
                                    Element tempWrapper = linkWithId.parent();
                                    while (tempWrapper != null && !tempWrapper.equals(doc)) {
                                        if (tempWrapper.hasClass("AdPhoto_wrapper__gAOIH") && 
                                            !tempWrapper.hasClass("AdPhoto_wrapper__skeleton__rHjT7")) {
                                            wrapper = tempWrapper;
                                            break;
                                        }
                                        tempWrapper = tempWrapper.parent();
                                    }
                                }
                            }
                            
                            if (wrapper != null && !wrapper.hasClass("AdPhoto_wrapper__skeleton__rHjT7")) {
                                ProductInfo productInfo = extractProductInfoFromListing(wrapper, fullUrl);
                                products.put(fullUrl, productInfo);
                                log.debug("Extracted product via regex: {} - {}", fullUrl, productInfo.getTitle());
                            } else {
                                // If we found the ID but can't extract details, try to get basic info from the individual page
                                // But only if we haven't found many products yet (to avoid too many requests)
                                if (products.size() < 100) {
                                    try {
                                        log.debug("Attempting to extract details from individual product page: {}", fullUrl);
                                        ProductInfo productInfo = extractProductInfo(fullUrl);
                                        if (productInfo.getTitle() != null && !productInfo.getTitle().isEmpty()) {
                                            products.put(fullUrl, productInfo);
                                            log.debug("Extracted product from individual page: {} - {}", fullUrl, productInfo.getTitle());
                                        } else {
                                            // Create minimal product info - at least we have the URL
                                            productInfo = ProductInfo.builder()
                                                    .title("Product " + productId)
                                                    .build();
                                            products.put(fullUrl, productInfo);
                                        }
                                    } catch (Exception e) {
                                        log.debug("Could not extract from individual page {}: {}", fullUrl, e.getMessage());
                                        // Create minimal product info - at least we have the URL
                                        ProductInfo productInfo = ProductInfo.builder()
                                                .title("Product " + productId)
                                                .build();
                                        products.put(fullUrl, productInfo);
                                    }
                                } else {
                                    // Create minimal product info - at least we have the URL
                                    ProductInfo productInfo = ProductInfo.builder()
                                            .title("Product " + productId)
                                            .build();
                                    products.put(fullUrl, productInfo);
                                    log.debug("Extracted product ID via regex (no wrapper found, too many to visit individually): {}", fullUrl);
                                }
                            }
                        }
                    }
                    log.info("Regex found {} unique product IDs in HTML, successfully extracted {} products with full details", 
                            regexFound, products.values().stream().filter(p -> p.getTitle() != null && !p.getTitle().startsWith("Product ")).count());
                }
                
                log.info("Extracted {} products from listing page: {}", products.size(), currentUrl);
                
                // Extract pagination links
                doc.select("nav.paginator > ul > li > a[href], a[href*='page='], a[href*='/page/']").forEach(link -> {
                    String href = link.attr("href");
                    if (href != null && !href.isEmpty() && !href.startsWith("#") && !href.startsWith("javascript:")) {
                        String fullUrl = href.startsWith("http") ? href : BASE_URL + href;
                        if (!href.matches(".*/ro/\\d+.*") && !pagesToVisit.contains(fullUrl)) {
                            pagesToVisit.add(fullUrl);
                        }
                    }
                });
                
                pagesVisited++;
                Thread.sleep(1000);

            } catch (IOException | InterruptedException e) {
                log.error("Error scraping listing page {}: {}", currentUrl, e.getMessage(), e);
                pagesVisited++;
            }
        }
        
        log.info("Total products extracted: {}", products.size());
        return products;
    }
    
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
                        .header("Accept-Language", "en-US,en;q=0.9,ro;q=0.8")
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
                // Domain-specific handling: eBay listings (URLs only)
                String host = "";
                try { host = new URI(currentUrl).getHost(); } catch (Exception ignore) {}
                if (host != null && host.toLowerCase().contains("ebay")) {
                    int before = productUrls.size();
                    // Prefer direct /itm/ links; fallback to s-item__link
                    Elements itmLinks = doc.select("a[href*='/itm/']");
                    if (itmLinks.isEmpty()) {
                        itmLinks = doc.select("li.s-item a.s-item__link[href], a.s-item__link[href]");
                    }
                    for (Element a : itmLinks) {
                        String href = a.attr("href");
                        if (href == null || href.isEmpty()) continue;
                        String fullUrl = a.absUrl("href");
                        if (fullUrl == null || fullUrl.isEmpty()) {
                            if (href.startsWith("/")) fullUrl = "https://www.ebay.com" + href; else fullUrl = href;
                        }
                        if (!fullUrl.contains("/itm/")) continue;
                        productUrls.add(fullUrl);
                    }
                    log.info("[eBay] Collected {} product URLs on this page", productUrls.size() - before);

                    // eBay pagination
                    Elements nextLinks = doc.select("a[rel=next][href], a[aria-label='Next page'][href], a.pagination__next[href]");
                    for (Element next : nextLinks) {
                        String href = next.attr("href");
                        if (href == null || href.isEmpty()) continue;
                        String nextUrl = next.hasAttr("href") ? next.absUrl("href") : href;
                        if (!nextUrl.startsWith("http")) continue;
                        if (!pagesToVisit.contains(nextUrl)) {
                            pagesToVisit.add(nextUrl);
                        }
                    }

                    pagesVisited++;
                    Thread.sleep(1000);
                    continue;
                }
                int totalLinks = doc.select("a[href]").size();
                log.info("Total links found on page: {}", totalLinks);
                
                // Count AdPhoto wrapper divs - these contain the product listings
                int adPhotoCount = doc.select("div.AdPhoto_wrapper__gAOIH").size();
                log.info("Found {} AdPhoto wrapper divs", adPhotoCount);
                
                // DEBUG: Log actual HTML content from first wrapper to see structure
                if (adPhotoCount > 0) {
                    Element firstWrapper = doc.select("div.AdPhoto_wrapper__gAOIH").first();
                    if (firstWrapper != null) {
                        String wrapperHtml = firstWrapper.outerHtml();
                        log.info("Sample wrapper HTML (first 500 chars): {}", 
                                wrapperHtml.length() > 500 ? wrapperHtml.substring(0, 500) : wrapperHtml);
                    }
                }
                
                // Count data-adid divs
                int dataAdidCount = doc.select("div[data-adid]").size();
                log.info("Found {} divs with data-adid attribute", dataAdidCount);
                if (dataAdidCount > 0) {
                    doc.select("div[data-adid]").stream().limit(3).forEach(div -> {
                        log.info("  - data-adid: '{}'", div.attr("data-adid"));
                    });
                }
                
                // STRATEGY 1: Regex extraction from raw HTML FIRST (most reliable for JS-rendered content)
                log.info("Starting regex extraction from raw HTML...");
                String html = doc.html();
                // Pattern to match /ro/ followed by 6+ digits (product ID)
                java.util.regex.Pattern regexPattern = java.util.regex.Pattern.compile("/ro/(\\d{6,12})(?!\\d)");
                java.util.regex.Matcher matcher = regexPattern.matcher(html);
                int regexCount = 0;
                Set<String> foundIds = new HashSet<>();
                while (matcher.find()) {
                    String productId = matcher.group(1);
                    if (foundIds.add(productId)) {
                        String fullUrl = BASE_URL + "/ro/" + productId;
                        if (productUrls.add(fullUrl)) {
                            regexCount++;
                            log.debug("Found product URL via regex: {}", fullUrl);
                        }
                    }
                }
                log.info("Regex extraction found {} unique product URLs", regexCount);
                
                // STRATEGY 2: Use data-adid attribute from parent div
                int beforeStrategy2 = productUrls.size();
                doc.select("div[data-adid]").forEach(adDiv -> {
                    String adId = adDiv.attr("data-adid");
                    if (adId != null && !adId.isEmpty() && adId.matches("\\d+")) {
                        String fullUrl = BASE_URL + "/ro/" + adId;
                        
                        // Find wrapper to extract name and price
                        Element wrapper = adDiv.selectFirst("div.AdPhoto_wrapper__gAOIH");
                        if (wrapper == null) wrapper = adDiv;
                        
                        Element linkElement = wrapper.selectFirst("a.AdPhoto_info__link__OwhY6, a.AdPhoto_image__BMixw, a[data-testid=photo-item-title]");
                        String productName = linkElement != null ? linkElement.text().trim() : "N/A";
                        Element priceElement = wrapper.selectFirst("span.AdPrice_price__2L3eA");
                        String price = priceElement != null ? priceElement.text().trim() : "N/A";
                        
                        if (productUrls.add(fullUrl)) {
                            log.debug("Found product via data-adid: {} | {} | {}", productName, fullUrl, price);
                        }
                    }
                });
                log.info("Found {} additional URLs from data-adid attributes", productUrls.size() - beforeStrategy2);
                
                // STRATEGY 3: Extract from links in wrapper divs
                int beforeStrategy3 = productUrls.size();
                
                doc.select("div.AdPhoto_wrapper__gAOIH").forEach(wrapper -> {
                    // Extract product link - try multiple selectors
                    Element linkElement = wrapper.selectFirst("a.AdPhoto_info__link__OwhY6[href]");
                    if (linkElement == null) {
                        linkElement = wrapper.selectFirst("a.AdPhoto_image__BMixw[href]");
                    }
                    if (linkElement == null) {
                        linkElement = wrapper.selectFirst("a[data-testid=photo-item-title][href]");
                    }
                    if (linkElement == null) {
                        // Fallback: find any link with /ro/ pattern in the wrapper
                        linkElement = wrapper.selectFirst("a[href^='/ro/']");
                    }
                    
                    if (linkElement != null) {
                        String href = linkElement.attr("href");
                        if (href != null && !href.isEmpty()) {
                            // Remove query parameters and normalize
                            String cleanHref = href.split("\\?")[0].trim();
                            // Extract product ID: /ro/123456 -> https://999.md/ro/123456
                            if (cleanHref.startsWith("/ro/")) {
                                // Use regex to extract just /ro/[digits] part
                                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("/ro/(\\d+)");
                                java.util.regex.Matcher hrefMatcher = pattern.matcher(cleanHref);
                                if (hrefMatcher.find()) {
                                    String productId = hrefMatcher.group(1);
                                    String fullUrl = BASE_URL + "/ro/" + productId;
                                    
                                    if (productUrls.add(fullUrl)) {
                                        log.debug("Found product URL: {}", fullUrl);
                                    }
                                }
                            }
                        }
                    }
                });
                log.info("Found {} additional URLs from wrapper links", productUrls.size() - beforeStrategy3);
                
                // STRATEGY 4: Extract from data-id attributes on favorite buttons
                int beforeStrategy4 = productUrls.size();
                doc.select("span[data-testid=ad-favorites][data-id], span[data-testid=add-booster-ad-favorites][data-id]").forEach(element -> {
                    String adId = element.attr("data-id");
                    if (adId != null && !adId.isEmpty() && adId.matches("\\d+")) {
                        String fullUrl = BASE_URL + "/ro/" + adId;
                        if (productUrls.add(fullUrl)) {
                            log.debug("Found product URL from data-id: {}", fullUrl);
                        }
                    }
                });
                log.info("Found {} additional URLs from data-id attributes", productUrls.size() - beforeStrategy4);
                
                log.info("Total URLs found on this page: {}", productUrls.size());
                
                // Log sample of found products if any were found
                if (!productUrls.isEmpty()) {
                    log.info("Sample of found product URLs (first 5):");
                    productUrls.stream().limit(5).forEach(url -> log.info("  - {}", url));
                }
                
                // Enhanced debugging: log sample of found URLs and all hrefs if none found
                if (productUrls.isEmpty()) {
                    log.warn("No product URLs found on page!");
                    log.warn("Document HTML length: {} chars", doc.html().length());
                    
                    // Log all hrefs found for debugging (first 20)
                    log.info("Sample of all hrefs found (first 20):");
                    doc.select("a[href]").stream().limit(20).forEach(link -> {
                        String href = link.attr("href");
                        String text = link.text().trim();
                        text = text.length() > 50 ? text.substring(0, 50) + "..." : text;
                        log.info("  - href: '{}', text: '{}'", href, text);
                    });
                    
                    // Check for AdPhoto wrappers
                    log.info("Found {} AdPhoto wrapper divs", adPhotoCount);
                    if (adPhotoCount > 0) {
                        doc.select("div.AdPhoto_wrapper__gAOIH").stream().limit(3).forEach(wrapper -> {
                            String linksInWrapper = wrapper.select("a[href^='/ro/']").stream()
                                    .map(link -> link.attr("href"))
                                    .collect(java.util.stream.Collectors.joining(", "));
                            log.info("  - Links in wrapper: {}", linksInWrapper.isEmpty() ? "NONE" : linksInWrapper);
                        });
                    }
                    
                    // Check for data-id attributes on favorite buttons
                    int dataIdCount = doc.select("span[data-testid=ad-favorites][data-id]").size();
                    log.info("Found {} elements with data-id attribute", dataIdCount);
                    if (dataIdCount > 0) {
                        doc.select("span[data-testid=ad-favorites][data-id]").stream().limit(5).forEach(element -> {
                            log.info("  - data-id: '{}'", element.attr("data-id"));
                        });
                    }
                    
                    // Check if page contains expected classes
                    boolean hasAdPhoto = doc.select(".AdPhoto_wrapper__gAOIH, .AdPhoto_info__link__OwhY6").size() > 0;
                    log.info("Page contains AdPhoto classes: {}", hasAdPhoto);
                    
                    // Log all /ro/ links found
                    int roLinksCount = doc.select("a[href^='/ro/']").size();
                    log.info("Total links starting with /ro/: {}", roLinksCount);
                    if (roLinksCount > 0) {
                        doc.select("a[href^='/ro/']").stream().limit(10).forEach(link -> {
                            log.info("  - href: '{}'", link.attr("href"));
                        });
                    }
                }
                
                // Extract pagination links
                doc.select("nav.paginator > ul > li > a[href], a[href*='page='], a[href*='/page/']").forEach(link -> {
                    String href = link.attr("href");
                    if (href != null && !href.isEmpty() && !href.startsWith("#") && !href.startsWith("javascript:")) {
                        String fullUrl = href.startsWith("http") ? href : BASE_URL + href;
                        // Avoid adding product URLs as pagination links
                        if (!href.matches(".*/ro/\\d+.*") && !pagesToVisit.contains(fullUrl)) {
                            pagesToVisit.add(fullUrl);
                        }
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
    
    /**
     * Extract product information directly from a listing page wrapper div
     */
    public ProductInfo extractProductInfoFromListing(Element wrapper, String productUrl) {
        ProductInfo.ProductInfoBuilder builder = ProductInfo.builder();
        Map<String, String> adInfo = new HashMap<>();
        Map<String, String> generalInfo = new HashMap<>();
        
        // Extract product link and title
        Element linkElement = wrapper.selectFirst("a.AdPhoto_info__link__OwhY6[href], a[data-testid=photo-item-title][href]");
        if (linkElement == null) {
            linkElement = wrapper.selectFirst("a.AdPhoto_image__BMixw[href]");
        }
        
        if (linkElement != null) {
            // Product title/name
            String title = linkElement.text().trim();
            builder.title(title);
        }
        
        // Extract image URL
        Element imgElement = wrapper.selectFirst("img[src]");
        if (imgElement != null) {
            String imageUrl = imgElement.attr("src");
            generalInfo.put("Image URL", imageUrl);
        }
        
        // Extract fuel type - look for label with engine icon
        Element labelWrapper = wrapper.selectFirst("span.AdLabel_label__wrapper__5F4Eh");
        if (labelWrapper != null) {
            labelWrapper.select("span.AdLabel_label__custom__kkxZo").forEach(label -> {
                Element icon = label.selectFirst("i");
                if (icon != null) {
                    String iconClass = icon.className();
                    String labelText = label.text().trim();
                    
                    // Check if it's an engine icon (fuel type)
                    if (iconClass.contains("AdLabel_icon__engine")) {
                        adInfo.put("Fuel Type", labelText);
                    }
                    // Check if it's a transmission icon
                    else if (iconClass.contains("AdLabel_icon__transmission")) {
                        adInfo.put("Transmission Type", labelText);
                    }
                    // Other labels without icons (like 4x4)
                    else if (label.hasClass("AdLabel_only__text__38l4V")) {
                        adInfo.put("Drive Type", labelText);
                    }
                }
            });
        }
        
        // Extract price
        Element priceElement = wrapper.selectFirst("span.AdPrice_price__2L3eA");
        if (priceElement != null) {
            builder.price(priceElement.text().trim());
        }
        
        // Extract odometer/mileage
        Element odometerElement = wrapper.selectFirst("span.AdPrice_info__LYNmc");
        if (odometerElement != null) {
            adInfo.put("Odometer", odometerElement.text().trim());
        }
        
        // Extract first payment info if present
        Element firstPaymentElement = wrapper.selectFirst("span.AdPrice_first__payment__O_ljR");
        if (firstPaymentElement != null) {
            adInfo.put("First Payment", firstPaymentElement.text().trim());
        }
        
        // Extract product ID from data-id attribute if available
        Element favoriteElement = wrapper.selectFirst("span[data-testid=ad-favorites][data-id], span[data-testid=add-booster-ad-favorites][data-id]");
        if (favoriteElement != null) {
            String productId = favoriteElement.attr("data-id");
            if (productId != null && !productId.isEmpty()) {
                adInfo.put("Product ID", productId);
            }
        }
        
        builder.adInfo(adInfo);
        builder.generalInfo(generalInfo);
        
        return builder.build();
    }
    
    public ProductInfo extractProductInfo(String url) {
        try {
            Document doc = Jsoup.connect(url)
                    .timeout(TIMEOUT)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8")
                    .get();

            String host = "";
            try { host = new URI(url).getHost(); } catch (Exception ignore) {}

            ProductInfo.ProductInfoBuilder builder = ProductInfo.builder();

            if (host != null && host.toLowerCase().contains("ebay")) {
                Element titleEl = doc.selectFirst("h1.x-item-title__mainTitle, h1#itemTitle, h1[itemprop=name]");
                if (titleEl != null) builder.title(titleEl.text().trim());

                Element priceEl = doc.selectFirst(".x-price-primary, span#prcIsum, span[itemprop=price]");
                if (priceEl != null) builder.price(priceEl.text().trim());

                Element locEl = doc.selectFirst(".ux-seller-section__itemLocation, #itemLocation");
                if (locEl != null) builder.location(locEl.text().trim());

                Map<String, String> adInfo = new HashMap<>();
                // Try to pull item ID from page
                java.util.regex.Matcher m = java.util.regex.Pattern.compile("itemId\\s*:\\s*\'?(\\d+)\'?").matcher(doc.html());
                if (m.find()) adInfo.put("Item ID", m.group(1));
                builder.adInfo(adInfo);

                return builder.build();
            }

            // Generic minimal fallback
            Element titleEl = doc.selectFirst("title, h1");
            if (titleEl != null) builder.title(titleEl.text().trim());
            Element priceEl = doc.selectFirst("[class*='price'], .price, .amount, .x-price-primary");
            if (priceEl != null) builder.price(priceEl.text().trim());
            return builder.build();

        } catch (IOException e) {
            log.error("Error extracting product info from {}: {}", url, e.getMessage());
            return ProductInfo.builder().build();
        }
    }

    /**
     * Test parsing HTML content directly (for debugging)
     */
    public Map<String, ProductInfo> testParseHtml(String htmlContent) {
        Map<String, ProductInfo> products = new HashMap<>();

        try {
            Document doc = Jsoup.parse(htmlContent, "https://999.md/ro/list/transport/cars");

            // Try to find the AdList container
            Element adListContainer = doc.selectFirst("div[data-sentry-component=AdList]");
            if (adListContainer == null) {
                log.warn("No AdList container found in test HTML");
                return products;
            }

            log.info("Found AdList container in test HTML");

            // Find all product wrappers
            Elements wrapperElements = adListContainer.select("div.AdPhoto_wrapper__gAOIH");
            log.info("Found {} wrapper elements in test HTML", wrapperElements.size());

            for (Element wrapper : wrapperElements) {
                try {
                    ProductInfo productInfo = extractProductInfoFromListing(wrapper, "test-url-" + products.size());
                    if (productInfo.getTitle() != null && !productInfo.getTitle().isEmpty()) {
                        products.put("test-" + products.size(), productInfo);
                        log.info("Extracted product from test HTML: {}", productInfo.getTitle());
                    }
                } catch (Exception e) {
                    log.warn("Error extracting product from wrapper: {}", e.getMessage());
                }
            }

        } catch (Exception e) {
            log.error("Error parsing test HTML: {}", e.getMessage(), e);
        }

        return products;
    }

    
}

