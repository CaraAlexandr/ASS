package com.scraper.producer.pattern.behavioral.strategy;

import java.util.List;

/**
 * Strategy Pattern - Strategy interface
 * Defines the algorithm interface for different scraping strategies
 */
public interface ScrapingStrategy {
    List<String> execute(String url, int maxPages);
    String getStrategyName();
}

