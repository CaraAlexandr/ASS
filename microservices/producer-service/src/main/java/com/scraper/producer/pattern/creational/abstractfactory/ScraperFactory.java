package com.scraper.producer.pattern.creational.abstractfactory;

import com.scraper.producer.pattern.creational.abstractfactory.impl.EbayScraperFactory;
import com.scraper.producer.pattern.creational.abstractfactory.impl.GenericScraperFactory;

import java.net.URI;

/**
 * Abstract Factory Pattern
 * Creates families of related objects (scraper + extractor pairs)
 */
public class ScraperFactory {
    
    public static AbstractScraperFactory getFactory(String url) {
        try {
            String host = new URI(url).getHost();
            if (host != null && host.toLowerCase().contains("ebay")) {
                return new EbayScraperFactory();
            } else {
                return new GenericScraperFactory();
            }
        } catch (Exception e) {
            return new GenericScraperFactory();
        }
    }
}

