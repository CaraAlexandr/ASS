import React from 'react';
import { Link } from 'react-router-dom';
import './Dashboard.css';

const Dashboard = () => {
  const patterns = {
    creational: {
      title: 'Creational Patterns',
      description: 'Patterns that deal with object creation mechanisms',
      color: '#1976d2',
      patterns: [
        { name: 'Factory Method', description: 'Creates objects without specifying the exact class' },
        { name: 'Builder', description: 'Constructs complex objects step by step' },
        { name: 'Abstract Factory', description: 'Creates families of related objects' }
      ]
    },
    structural: {
      title: 'Structural Patterns',
      description: 'Patterns that deal with object composition',
      color: '#7b1fa2',
      patterns: [
        { name: 'Adapter', description: 'Allows incompatible interfaces to work together' },
        { name: 'Decorator', description: 'Adds behavior to objects dynamically' },
        { name: 'Facade', description: 'Provides a simplified interface to a complex subsystem' }
      ]
    },
    behavioral: {
      title: 'Behavioral Patterns',
      description: 'Patterns that deal with object interaction and responsibility',
      color: '#388e3c',
      patterns: [
        { name: 'Strategy', description: 'Defines a family of algorithms and makes them interchangeable' },
        { name: 'Observer', description: 'Notifies multiple objects about state changes' },
        { name: 'Template Method', description: 'Defines the skeleton of an algorithm' }
      ]
    }
  };

  return (
    <div className="dashboard">
      <div className="container">
        <div className="header">
          <h1>🎨 Design Patterns Demonstration</h1>
          <p>Explore 9 design patterns implemented in the microservices architecture</p>
        </div>

        <div className="pattern-grid">
          {Object.entries(patterns).map(([key, category]) => (
            <Link 
              key={key} 
              to={`/patterns/${key}`}
              className="pattern-card"
            >
              <span className={`category-badge category-${key}`}>
                {category.title}
              </span>
              <h3>{category.title}</h3>
              <p>{category.description}</p>
              <ul className="pattern-list">
                {category.patterns.map((pattern, idx) => (
                  <li key={idx}>{pattern.name}</li>
                ))}
              </ul>
            </Link>
          ))}
        </div>

        <div className="quick-demo">
          <div className="pattern-demo">
            <h2>🚀 Quick Demo: All Patterns Together</h2>
            <p className="description">
              Test all 9 design patterns working together in a real scraping operation.
              This demonstrates how Factory Method, Builder, Strategy, Observer, and other patterns
              collaborate to create a robust, maintainable system.
            </p>
            <Link to="/patterns/demo" className="btn btn-primary">
              Run Full Demo
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;

