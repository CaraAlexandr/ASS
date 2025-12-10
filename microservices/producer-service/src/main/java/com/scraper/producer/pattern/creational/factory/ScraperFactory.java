package com.scraper.producer.pattern.creational.factory;

import com.scraper.producer.pattern.creational.factory.impl.EbayScraper;
import com.scraper.producer.pattern.creational.factory.impl.GenericScraper;
import com.scraper.producer.pattern.creational.factory.Scraper;

import java.net.URI;

/**
 * Factory Method Pattern
 * Creates appropriate scraper instances based on URL domain
 */
public class ScraperFactory {
    
    public static Scraper createScraper(String url) {
        try {
            String host = new URI(url).getHost();
            if (host != null && host.toLowerCase().contains("ebay")) {
                return new EbayScraper();
            } else {
                return new GenericScraper();
            }
        } catch (Exception e) {
            // Default to generic scraper if URL parsing fails
            return new GenericScraper();
        }
    }
}

