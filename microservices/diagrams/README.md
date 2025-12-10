# Design Patterns Diagrams

This folder contains PlantUML diagrams for all 9 design patterns implemented in the project.

## Diagram Files

1. **01-overview.puml** - Overview of all 9 design patterns organized by category
2. **02-factory-method.puml** - Factory Method pattern implementation
3. **03-builder.puml** - Builder pattern implementation
4. **04-abstract-factory.puml** - Abstract Factory pattern implementation
5. **05-adapter.puml** - Adapter pattern implementation
6. **06-decorator.puml** - Decorator pattern implementation
7. **07-facade.puml** - Facade pattern implementation
8. **08-strategy.puml** - Strategy pattern implementation
9. **09-observer.puml** - Observer pattern implementation
10. **10-template-method.puml** - Template Method pattern implementation
11. **11-pattern-integration.puml** - Integration diagram showing all patterns working together

## How to Generate Diagrams

### Option 1: Online PlantUML Server (Easiest)

1. Visit: http://www.plantuml.com/plantuml/uml/
2. Copy the content from any `.puml` file
3. Paste into the online editor
4. Export as PNG, SVG, or PDF

### Option 2: VS Code Extension

1. Install "PlantUML" extension in VS Code
2. Open any `.puml` file
3. Press `Alt+D` to preview
4. Right-click on preview → "Export Current Diagram"

### Option 3: Command Line

```bash
# Install PlantUML (requires Java)
# Download plantuml.jar from http://plantuml.com/download

# Generate all diagrams
java -jar plantuml.jar *.puml

# Generate specific diagram
java -jar plantuml.jar 01-overview.puml
```

### Option 4: IntelliJ IDEA

1. Install PlantUML plugin
2. Open any `.puml` file
3. Right-click → "PlantUML" → "Preview Diagram"

## Diagram Categories

### Creational Patterns (02-04)
- Factory Method: Object creation based on URL domain
- Builder: Step-by-step configuration building
- Abstract Factory: Creating families of related objects

### Structural Patterns (05-07)
- Adapter: Format conversion between string and JSON
- Decorator: Dynamic feature addition (caching, retry, logging)
- Facade: Simplified interface to complex subsystem

### Behavioral Patterns (08-10)
- Strategy: Interchangeable scraping algorithms
- Observer: Event notification system
- Template Method: Algorithm skeleton with customizable steps

### Integration (11)
- Shows how all patterns work together in the system

## Usage in Documentation

These diagrams can be:
- Included in project documentation
- Used in presentations
- Referenced in code reviews
- Exported as images for reports

## File Naming Convention

Files are numbered for easy ordering:
- `01-` Overview
- `02-04` Creational patterns
- `05-07` Structural patterns
- `08-10` Behavioral patterns
- `11-` Integration

