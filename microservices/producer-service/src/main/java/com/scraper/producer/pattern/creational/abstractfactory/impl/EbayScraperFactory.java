package com.scraper.producer.pattern.creational.abstractfactory.impl;

import com.scraper.producer.pattern.creational.abstractfactory.AbstractScraperFactory;
import com.scraper.producer.pattern.creational.abstractfactory.ExtractorProduct;
import com.scraper.producer.pattern.creational.abstractfactory.ScraperProduct;

/**
 * Abstract Factory Pattern - Concrete Factory for eBay
 */
public class EbayScraperFactory implements AbstractScraperFactory {
    
    @Override
    public ScraperProduct createScraper() {
        return new EbayScraper();
    }
    
    @Override
    public ExtractorProduct createExtractor() {
        return new EbayExtractor();
    }
}

