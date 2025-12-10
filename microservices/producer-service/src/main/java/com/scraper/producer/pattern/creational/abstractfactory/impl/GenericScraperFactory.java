package com.scraper.producer.pattern.creational.abstractfactory.impl;

import com.scraper.producer.pattern.creational.abstractfactory.AbstractScraperFactory;
import com.scraper.producer.pattern.creational.abstractfactory.ExtractorProduct;
import com.scraper.producer.pattern.creational.abstractfactory.ScraperProduct;

/**
 * Abstract Factory Pattern - Concrete Factory for Generic websites
 */
public class GenericScraperFactory implements AbstractScraperFactory {
    
    @Override
    public ScraperProduct createScraper() {
        return new GenericScraper();
    }
    
    @Override
    public ExtractorProduct createExtractor() {
        return new GenericExtractor();
    }
}

