package com.scraper.producer.pattern.creational.abstractfactory;

import java.util.Map;

/**
 * Abstract Factory Pattern - Abstract Product B
 */
public interface ExtractorProduct {
    Map<String, String> extractData(String url);
}

