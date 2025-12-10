import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import PatternDemo from './PatternDemo';
import './PatternCategory.css';

const PatternCategory = () => {
  const { category } = useParams();
  const [activePattern, setActivePattern] = useState(null);

  const patternData = {
    creational: {
      title: 'Creational Patterns',
      patterns: [
        {
          id: 'factory-method',
          name: 'Factory Method',
          description: 'Creates appropriate scraper instances based on URL domain without exposing creation logic.',
          demo: 'factory'
        },
        {
          id: 'builder',
          name: 'Builder',
          description: 'Constructs complex ScrapingConfig objects step by step with optional parameters using a fluent API.',
          demo: 'builder'
        },
        {
          id: 'abstract-factory',
          name: 'Abstract Factory',
          description: 'Creates families of related objects (scraper + extractor pairs) without specifying their concrete classes.',
          demo: 'abstract-factory'
        }
      ]
    },
    structural: {
      title: 'Structural Patterns',
      patterns: [
        {
          id: 'adapter',
          name: 'Adapter',
          description: 'Adapts different message formats (plain string, JSON) to work together seamlessly.',
          demo: 'adapter'
        },
        {
          id: 'decorator',
          name: 'Decorator',
          description: 'Dynamically adds responsibilities (caching, retry, logging) to scrapers without modifying their structure.',
          demo: 'decorator'
        },
        {
          id: 'facade',
          name: 'Facade',
          description: 'Provides a simplified interface to the complex subsystem of scraping, decorating, and messaging.',
          demo: 'facade'
        }
      ]
    },
    behavioral: {
      title: 'Behavioral Patterns',
      patterns: [
        {
          id: 'strategy',
          name: 'Strategy',
          description: 'Defines a family of algorithms (scraping strategies) and makes them interchangeable at runtime.',
          demo: 'strategy'
        },
        {
          id: 'observer',
          name: 'Observer',
          description: 'Defines a one-to-many dependency so that when one object changes state, all dependents are notified.',
          demo: 'observer'
        },
        {
          id: 'template-method',
          name: 'Template Method',
          description: 'Defines the skeleton of an algorithm in a method, deferring some steps to subclasses.',
          demo: 'template-method'
        }
      ]
    },
    demo: {
      title: 'All Patterns Together',
      patterns: [
        {
          id: 'full-demo',
          name: 'Complete Pattern Demonstration',
          description: 'See all 9 design patterns working together in a real scraping operation.',
          demo: 'full'
        }
      ]
    }
  };

  const categoryData = patternData[category] || patternData.creational;
  
  // Auto-expand demo if it's the demo route
  useEffect(() => {
    if (category === 'demo' && categoryData.patterns.length > 0) {
      setActivePattern(categoryData.patterns[0].id);
    }
  }, [category, categoryData]);

  return (
    <div className="pattern-category">
      <div className="container">
        <Link to="/" className="back-button">← Back to Dashboard</Link>
        
        <div className="header">
          <h1>{categoryData.title}</h1>
          <p>Select a pattern to see it in action</p>
        </div>

        <div className="pattern-list">
          {categoryData.patterns.map((pattern) => (
            <div key={pattern.id} className="pattern-item">
              <div className="pattern-header">
                <h3>{pattern.name}</h3>
                <button
                  className="btn btn-primary"
                  onClick={() => setActivePattern(activePattern === pattern.id ? null : pattern.id)}
                >
                  {activePattern === pattern.id ? 'Hide Demo' : 'Show Demo'}
                </button>
              </div>
              <p className="pattern-description">{pattern.description}</p>
              {activePattern === pattern.id && (
                <PatternDemo patternType={pattern.demo} patternName={pattern.name} />
              )}
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

export default PatternCategory;

