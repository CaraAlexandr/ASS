package com.scraper.producer.pattern.creational.abstractfactory;

import java.util.List;

/**
 * Abstract Factory Pattern - Abstract Product A
 */
public interface ScraperProduct {
    List<String> scrapeUrls(String url, int maxPages);
}

