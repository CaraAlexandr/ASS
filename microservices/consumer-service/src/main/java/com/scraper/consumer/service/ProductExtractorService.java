package com.scraper.consumer.service;

import com.scraper.consumer.dto.ProductInfo;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class ProductExtractorService {
    
    private static final int TIMEOUT = 30000;
    
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
                // Title
                Element titleEl = doc.selectFirst("h1.x-item-title__mainTitle, h1#itemTitle, h1[itemprop=name], h1.ux-textspans");
                if (titleEl == null) {
                    titleEl = doc.selectFirst("h1");
                }
                if (titleEl != null) {
                    builder.title(titleEl.text().trim());
                }

                // Price
                Element priceEl = doc.selectFirst(".x-price-primary, span#prcIsum, span[itemprop=price], .notranslate");
                if (priceEl == null) {
                    priceEl = doc.selectFirst("[class*='price'], .price-primary");
                }
                if (priceEl != null) {
                    builder.price(priceEl.text().trim());
                }

                // Description
                Element descEl = doc.selectFirst("#viTabs_0_is, .vi-VR-cvipContent, .vim x-item-description");
                if (descEl != null) {
                    builder.description(descEl.text().trim());
                }

                // Location
                Element locEl = doc.selectFirst(".ux-seller-section__itemLocation, #itemLocation, .ux-labels-values__values-content");
                if (locEl != null) {
                    builder.location(locEl.text().trim());
                }

                // Extract additional information
                Map<String, String> adInfo = new HashMap<>();
                
                // Item ID from URL or page
                try {
                    java.util.regex.Matcher itmMatcher = java.util.regex.Pattern.compile("/(?:itm|i|p)/(\\d+)").matcher(url);
                    if (itmMatcher.find()) {
                        adInfo.put("Item ID", itmMatcher.group(1));
                    }
                    // Also check page source
                    java.util.regex.Matcher itemIdMatcher = java.util.regex.Pattern.compile("itemId\\s*:\\s*['\"]?([0-9]+)['\"]?").matcher(doc.html());
                    if (itemIdMatcher.find()) {
                        adInfo.put("Item ID", itemIdMatcher.group(1));
                    }
                } catch (Exception ignoreId) {}
                
                // Condition
                Element conditionEl = doc.selectFirst("#viTabs_0_is .u-flL.condText, .x-item-condition-label, .u-flL");
                if (conditionEl != null) {
                    String condition = conditionEl.text().trim();
                    if (!condition.isEmpty()) {
                        adInfo.put("Condition", condition);
                    }
                }
                
                // Shipping
                Element shippingEl = doc.selectFirst("#fshippingCost, .shipping-section, .u-flL.shipping3rd");
                if (shippingEl != null) {
                    adInfo.put("Shipping", shippingEl.text().trim());
                }
                
                // Seller info
                Element sellerEl = doc.selectFirst("#mbgLink, .seller-info__name");
                if (sellerEl != null) {
                    adInfo.put("Seller", sellerEl.text().trim());
                }
                
                // Quantity available
                Element qtyEl = doc.selectFirst("#qtySubTxt, .qtyAvailable");
                if (qtyEl != null) {
                    adInfo.put("Quantity", qtyEl.text().trim());
                }
                
                // Brand
                Element brandEl = doc.selectFirst("[itemprop=brand], .ux-labels-values__labels[aria-label*='Brand'] + .ux-labels-values__values");
                if (brandEl != null) {
                    adInfo.put("Brand", brandEl.text().trim());
                }

                builder.adInfo(adInfo);

                // General Info
                Map<String, String> generalInfo = new HashMap<>();
                
                // Extract image
                Element imgEl = doc.selectFirst("#icImg, img[itemprop=image], .img-wrapper img");
                if (imgEl != null) {
                    String imgSrc = imgEl.attr("src");
                    if (imgSrc == null || imgSrc.isEmpty()) {
                        imgSrc = imgEl.attr("data-src");
                    }
                    if (imgSrc != null && !imgSrc.isEmpty()) {
                        generalInfo.put("Image URL", imgSrc);
                    }
                }
                
                // Extract specifications/features
                Elements specs = doc.select(".ux-labels-values__labels, .itemAttr");
                for (Element spec : specs) {
                    String label = spec.text().trim();
                    Element valueEl = spec.nextElementSibling();
                    if (valueEl == null) {
                        valueEl = spec.parent().selectFirst(".ux-labels-values__values");
                    }
                    if (valueEl != null && !label.isEmpty()) {
                        String value = valueEl.text().trim();
                        if (!value.isEmpty()) {
                            generalInfo.put(label, value);
                        }
                    }
                }

                builder.generalInfo(generalInfo);

                return builder.build();
            }

            // Generic fallback for non-eBay sites
            Element titleEl = doc.selectFirst("title, h1");
            if (titleEl != null) {
                builder.title(titleEl.text().trim());
            }
            Element priceEl = doc.selectFirst("[class*='price'], .price, .amount");
            if (priceEl != null) {
                builder.price(priceEl.text().trim());
            }
            
            return builder.build();

        } catch (IOException e) {
            log.error("Error extracting product info from {}: {}", url, e.getMessage());
            throw new RuntimeException("Failed to extract product info: " + e.getMessage(), e);
        }
    }
}
