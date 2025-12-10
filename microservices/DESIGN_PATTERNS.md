# Design Patterns Implementation

This document describes the 9 design patterns implemented across the microservices.

## Creational Patterns (3)

### 1. Factory Method Pattern
**Location**: `producer-service/src/main/java/com/scraper/producer/pattern/creational/factory/`

**Purpose**: Creates appropriate scraper instances based on URL domain without exposing the creation logic.

**Implementation**:
- `ScraperFactory`: Factory class that creates scrapers
- `Scraper`: Product interface
- `EbayScraper`: Concrete product for eBay
- `GenericScraper`: Concrete product for generic websites

**Usage**: `Scraper scraper = ScraperFactory.createScraper(url);`

### 2. Builder Pattern
**Location**: `producer-service/src/main/java/com/scraper/producer/pattern/creational/builder/`

**Purpose**: Constructs complex `ScrapingConfig` objects step by step with optional parameters.

**Implementation**:
- `ScrapingConfig`: Product class with nested `Builder` class
- Supports fluent API for configuration

**Usage**:
```java
ScrapingConfig config = ScrapingConfig.builder()
    .startingUrl(url)
    .maxPages(10)
    .timeout(30000)
    .enableCaching(true)
    .build();
```

### 3. Abstract Factory Pattern
**Location**: `producer-service/src/main/java/com/scraper/producer/pattern/creational/abstractfactory/`

**Purpose**: Creates families of related objects (scraper + extractor pairs) without specifying their concrete classes.

**Implementation**:
- `AbstractScraperFactory`: Abstract factory interface
- `ScraperProduct`: Abstract product A (scraper)
- `ExtractorProduct`: Abstract product B (extractor)
- `EbayScraperFactory`: Concrete factory for eBay products
- `GenericScraperFactory`: Concrete factory for generic products

**Usage**: `AbstractScraperFactory factory = ScraperFactory.getFactory(url);`

## Structural Patterns (3)

### 4. Adapter Pattern
**Location**: `producer-service/src/main/java/com/scraper/producer/pattern/structural/adapter/`

**Purpose**: Adapts different message formats (plain string, JSON) to work together.

**Implementation**:
- `MessageAdapter`: Adapts between string URLs and JSON message formats
- Methods: `adaptToJson()`, `adaptFromJson()`, `adaptBatchToJson()`

**Usage**: `String jsonMessage = messageAdapter.adaptToJson(url);`

### 5. Decorator Pattern
**Location**: `producer-service/src/main/java/com/scraper/producer/pattern/structural/decorator/`

**Purpose**: Dynamically adds responsibilities (caching, retry, logging) to scrapers without modifying their structure.

**Implementation**:
- `ScraperDecorator`: Component interface
- `BaseScraperDecorator`: Base decorator
- `CachingScraperDecorator`: Adds caching functionality
- `RetryScraperDecorator`: Adds retry logic
- `LoggingScraperDecorator`: Adds enhanced logging

**Usage**: Decorators can be chained to add multiple features.

### 6. Facade Pattern
**Location**: `producer-service/src/main/java/com/scraper/producer/pattern/structural/facade/`

**Purpose**: Provides a simplified interface to the complex subsystem of scraping, decorating, and messaging.

**Implementation**:
- `ScrapingFacade`: Simplifies the entire scraping workflow
- Hides complexity of factory creation, decoration, and message publishing

**Usage**: `ScrapingResult result = scrapingFacade.scrapeAndPublish(config);`

## Behavioral Patterns (3)

### 7. Strategy Pattern
**Location**: `producer-service/src/main/java/com/scraper/producer/pattern/behavioral/strategy/`

**Purpose**: Defines a family of algorithms (scraping strategies) and makes them interchangeable.

**Implementation**:
- `ScrapingStrategy`: Strategy interface
- `AggressiveScrapingStrategy`: Aggressive scraping with no delays
- `ConservativeScrapingStrategy`: Conservative scraping with rate limiting
- `BalancedScrapingStrategy`: Balanced approach
- `ScrapingContext`: Context that uses a strategy

**Usage**: `context.setStrategy(new AggressiveScrapingStrategy());`

### 8. Observer Pattern
**Location**: `producer-service/src/main/java/com/scraper/producer/pattern/behavioral/observer/`

**Purpose**: Defines a one-to-many dependency between objects so that when one object changes state, all dependents are notified.

**Implementation**:
- `ScrapingObserver`: Observer interface
- `ScrapingSubject`: Subject that manages observers
- `LoggingObserver`: Logs all scraping events
- `MetricsObserver`: Tracks metrics for scraping operations

**Usage**: Observers are attached to the subject and notified of events.

### 9. Template Method Pattern
**Location**: `producer-service/src/main/java/com/scraper/producer/pattern/behavioral/templatemethod/`

**Purpose**: Defines the skeleton of an algorithm in a method, deferring some steps to subclasses.

**Implementation**:
- `AbstractScrapingTemplate`: Abstract class with template method
- `EbayScrapingTemplate`: Concrete implementation for eBay
- `GenericScrapingTemplate`: Concrete implementation for generic sites

**Usage**: `AbstractScrapingTemplate template = new EbayScrapingTemplate(); List<String> urls = template.scrape(url, maxPages);`

## Integration

All patterns are integrated in:
- `EnhancedScrapingService`: Demonstrates all patterns working together
- `ProducerController`: New endpoint `/api/producer/start-enhanced` that uses all patterns
- `PatternConfig`: Spring configuration for pattern beans

## API Endpoint

**POST** `/api/producer/start-enhanced`
- Parameters:
  - `startingUrl` (default: eBay cell phones search)
  - `maxPages` (default: 10)
  - `strategy` (options: "aggressive", "conservative", "balanced")

This endpoint demonstrates all 9 design patterns working together in a real-world scenario.

