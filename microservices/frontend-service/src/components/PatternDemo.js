import React, { useState } from 'react';
import axios from 'axios';
import './PatternDemo.css';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8081';

const PatternDemo = ({ patternType, patternName }) => {
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState(null);
  const [error, setError] = useState(null);
  const [formData, setFormData] = useState({
    url: 'https://www.ebay.com/sch/i.html?_nkw=cell+phones',
    maxPages: '5',
    strategy: 'balanced'
  });

  const handleInputChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
  };

  const executePattern = async () => {
    setLoading(true);
    setError(null);
    setResult(null);

    try {
      let response;
      
      switch (patternType) {
        case 'full':
          // Full demo with all patterns
          response = await axios.post(`${API_BASE_URL}/api/producer/start-enhanced`, null, {
            params: {
              startingUrl: formData.url,
              maxPages: parseInt(formData.maxPages),
              strategy: formData.strategy
            }
          });
          break;
        
        case 'factory':
          // Factory Method demo
          response = await axios.post(`${API_BASE_URL}/api/producer/start`, null, {
            params: {
              startingUrl: formData.url,
              maxPages: parseInt(formData.maxPages)
            }
          });
          break;
        
        default:
          // For other patterns, use the enhanced endpoint
          response = await axios.post(`${API_BASE_URL}/api/producer/start-enhanced`, null, {
            params: {
              startingUrl: formData.url,
              maxPages: parseInt(formData.maxPages),
              strategy: formData.strategy
            }
          });
      }

      setResult(response.data);
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'An error occurred');
    } finally {
      setLoading(false);
    }
  };

  const renderPatternSpecificUI = () => {
    switch (patternType) {
      case 'strategy':
        return (
          <div className="form-group">
            <label>Scraping Strategy:</label>
            <select name="strategy" value={formData.strategy} onChange={handleInputChange}>
              <option value="balanced">Balanced (Standard)</option>
              <option value="aggressive">Aggressive (Fast, More Pages)</option>
              <option value="conservative">Conservative (Slow, Rate Limited)</option>
            </select>
            <p className="help-text">
              The Strategy pattern allows you to switch between different scraping algorithms at runtime.
            </p>
          </div>
        );
      
      case 'builder':
        return (
          <div className="form-group">
            <label>Configuration Options:</label>
            <p className="help-text">
              The Builder pattern constructs the ScrapingConfig object with various optional parameters.
              Try different maxPages values to see the builder in action.
            </p>
          </div>
        );
      
      case 'factory':
        return (
          <div className="form-group">
            <label>URL (try different domains):</label>
            <p className="help-text">
              The Factory Method pattern automatically creates the appropriate scraper (eBay or Generic)
              based on the URL domain.
            </p>
          </div>
        );
      
      default:
        return null;
    }
  };

  return (
    <div className="pattern-demo-container">
      <div className="demo-form">
        <div className="form-group">
          <label>Starting URL:</label>
          <input
            type="text"
            name="url"
            value={formData.url}
            onChange={handleInputChange}
            placeholder="https://www.ebay.com/sch/i.html?_nkw=cell+phones"
          />
        </div>

        <div className="form-group">
          <label>Max Pages:</label>
          <input
            type="number"
            name="maxPages"
            value={formData.maxPages}
            onChange={handleInputChange}
            min="1"
            max="20"
          />
        </div>

        {renderPatternSpecificUI()}

        <button
          className="btn btn-primary"
          onClick={executePattern}
          disabled={loading}
        >
          {loading ? 'Executing...' : `Execute ${patternName}`}
        </button>
      </div>

      {loading && (
        <div className="loading">
          <div className="spinner"></div>
          <p>Executing pattern... This may take a moment.</p>
        </div>
      )}

      {error && (
        <div className="error">
          <strong>Error:</strong> {error}
        </div>
      )}

      {result && (
        <div className="result-box">
          <h4>Execution Results</h4>
          
          {patternType === 'full' && result.metrics && (
            <div className="metrics-grid">
              <div className="metric-card">
                <h4>Total Operations</h4>
                <div className="value">{result.metrics.totalOperations || 0}</div>
              </div>
              <div className="metric-card">
                <h4>URLs Scraped</h4>
                <div className="value">{result.metrics.totalUrlsScraped || 0}</div>
              </div>
              <div className="metric-card">
                <h4>Errors</h4>
                <div className="value">{result.metrics.totalErrors || 0}</div>
              </div>
            </div>
          )}

          <div className="result-details">
            <p><strong>Status:</strong> <span className={result.status === 'completed' ? 'success-text' : 'error-text'}>{result.status}</span></p>
            <p><strong>URLs Found:</strong> {result.urlsFound || 0}</p>
            <p><strong>URLs Published:</strong> {result.urlsPublished || 0}</p>
            {result.message && <p><strong>Message:</strong> {result.message}</p>}
          </div>

          <details className="result-json">
            <summary>View Full Response (JSON)</summary>
            <pre>{JSON.stringify(result, null, 2)}</pre>
          </details>
        </div>
      )}

      <div className="pattern-explanation">
        <h4>How {patternName} Works:</h4>
        {getPatternExplanation(patternType)}
      </div>
    </div>
  );
};

const getPatternExplanation = (patternType) => {
  const explanations = {
    'factory': (
      <ul>
        <li>The Factory Method pattern creates the appropriate scraper (EbayScraper or GenericScraper) based on the URL domain.</li>
        <li>This hides the object creation logic from the client code.</li>
        <li>Try different URLs to see different scrapers being created automatically.</li>
      </ul>
    ),
    'builder': (
      <ul>
        <li>The Builder pattern constructs ScrapingConfig objects with optional parameters.</li>
        <li>It provides a fluent API: <code>ScrapingConfig.builder().startingUrl(url).maxPages(10).build()</code></li>
        <li>This makes complex object construction more readable and flexible.</li>
      </ul>
    ),
    'abstract-factory': (
      <ul>
        <li>The Abstract Factory creates families of related objects (scraper + extractor pairs).</li>
        <li>It ensures that related objects are compatible (e.g., eBay scraper with eBay extractor).</li>
        <li>This pattern is used internally when creating scraper-extractor pairs.</li>
      </ul>
    ),
    'adapter': (
      <ul>
        <li>The Adapter pattern converts URLs between different formats (plain string ↔ JSON).</li>
        <li>It allows incompatible interfaces to work together seamlessly.</li>
        <li>In this demo, URLs are adapted to JSON format for enhanced messaging.</li>
      </ul>
    ),
    'decorator': (
      <ul>
        <li>The Decorator pattern adds functionality (caching, retry, logging) to scrapers dynamically.</li>
        <li>Decorators can be chained: <code>new LoggingDecorator(new RetryDecorator(new CachingDecorator(scraper)))</code></li>
        <li>This allows adding features without modifying the base scraper class.</li>
      </ul>
    ),
    'facade': (
      <ul>
        <li>The Facade pattern provides a simple interface to the complex scraping subsystem.</li>
        <li>It hides the complexity of factory creation, decoration, and message publishing.</li>
        <li>Clients interact with a single, simplified interface instead of multiple classes.</li>
      </ul>
    ),
    'strategy': (
      <ul>
        <li>The Strategy pattern defines interchangeable scraping algorithms (Aggressive, Conservative, Balanced).</li>
        <li>You can switch strategies at runtime without changing the client code.</li>
        <li>Each strategy implements the same interface but with different behavior.</li>
      </ul>
    ),
    'observer': (
      <ul>
        <li>The Observer pattern notifies multiple objects (LoggingObserver, MetricsObserver) about scraping events.</li>
        <li>When scraping starts/progresses/completes, all observers are automatically notified.</li>
        <li>This enables loose coupling between the scraping logic and logging/metrics collection.</li>
      </ul>
    ),
    'template-method': (
      <ul>
        <li>The Template Method pattern defines the scraping algorithm skeleton with customizable steps.</li>
        <li>The abstract class defines the overall flow, while subclasses implement specific steps.</li>
        <li>This ensures consistent algorithm structure while allowing customization.</li>
      </ul>
    ),
    'full': (
      <ul>
        <li><strong>Builder:</strong> Creates configuration object</li>
        <li><strong>Factory Method:</strong> Creates appropriate scraper</li>
        <li><strong>Strategy:</strong> Executes scraping with selected strategy</li>
        <li><strong>Observer:</strong> Notifies about scraping events</li>
        <li><strong>Adapter:</strong> Adapts URLs to JSON format</li>
        <li><strong>Template Method:</strong> Defines scraping algorithm structure</li>
        <li><strong>Decorator:</strong> Adds caching, retry, logging (used internally)</li>
        <li><strong>Facade:</strong> Simplifies the overall workflow</li>
        <li><strong>Abstract Factory:</strong> Creates related object families (used internally)</li>
      </ul>
    )
  };

  return explanations[patternType] || <p>Pattern explanation not available.</p>;
};

export default PatternDemo;

