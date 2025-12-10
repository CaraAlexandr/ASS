package com.scraper.producer.pattern.creational.factory;

import java.util.List;

/**
 * Factory Method Pattern - Product interface
 */
public interface Scraper {
    List<String> scrapeUrls(String url, int maxPages);
    String getSupportedDomain();
}

