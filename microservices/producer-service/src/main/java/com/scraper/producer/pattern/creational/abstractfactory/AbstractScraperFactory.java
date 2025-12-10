package com.scraper.producer.pattern.creational.abstractfactory;

/**
 * Abstract Factory Pattern - Abstract Factory interface
 */
public interface AbstractScraperFactory {
    ScraperProduct createScraper();
    ExtractorProduct createExtractor();
}

