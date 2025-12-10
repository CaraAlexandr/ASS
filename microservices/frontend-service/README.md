# Design Patterns Frontend

A React-based frontend application to demonstrate and interact with 9 design patterns implemented in the microservices architecture.

## Features

- **Interactive Dashboard**: Overview of all 9 design patterns organized by category
- **Pattern Demonstrations**: Live demos for each pattern with real API integration
- **Visual Feedback**: Real-time results, metrics, and explanations
- **User-Friendly Interface**: Modern, responsive UI with clear explanations

## Design Patterns Demonstrated

### Creational Patterns
1. **Factory Method** - Creates scrapers based on URL domain
2. **Builder** - Constructs configuration objects step by step
3. **Abstract Factory** - Creates families of related objects

### Structural Patterns
4. **Adapter** - Adapts message formats (string ↔ JSON)
5. **Decorator** - Adds functionality dynamically (caching, retry, logging)
6. **Facade** - Simplifies complex subsystem interactions

### Behavioral Patterns
7. **Strategy** - Interchangeable scraping algorithms
8. **Observer** - Event notification system
9. **Template Method** - Algorithm skeleton with customizable steps

## Getting Started

### Prerequisites
- Node.js 16+ and npm
- Producer service running on port 8081

### Installation

```bash
cd frontend-service
npm install
```

### Running the Application

```bash
npm start
```

The application will open at `http://localhost:3000`

### Building for Production

```bash
npm run build
```

## Usage

1. **Dashboard**: Start at the main dashboard to see all pattern categories
2. **Category View**: Click on a category (Creational, Structural, Behavioral) to see individual patterns
3. **Pattern Demo**: Click "Show Demo" on any pattern to see it in action
4. **Full Demo**: Use the "Run Full Demo" button to see all patterns working together

## API Integration

The frontend connects to the producer service API:
- `POST /api/producer/start-enhanced` - Full pattern demonstration
- `POST /api/producer/start` - Basic scraping (Factory Method demo)

## Project Structure

```
frontend-service/
├── public/
│   └── index.html
├── src/
│   ├── components/
│   │   ├── Dashboard.js          # Main dashboard
│   │   ├── PatternCategory.js    # Category view
│   │   └── PatternDemo.js        # Pattern demonstration component
│   ├── App.js                    # Main app component
│   ├── App.css                   # Global styles
│   └── index.js                  # Entry point
├── package.json
└── README.md
```

## Features in Detail

### Interactive Pattern Demos
Each pattern has its own demo interface where you can:
- Configure parameters (URL, max pages, strategy)
- Execute the pattern
- View real-time results
- See pattern-specific explanations

### Metrics Dashboard
The full demo shows:
- Total operations count
- URLs scraped
- Error count
- Detailed JSON response

### Pattern Explanations
Each demo includes:
- How the pattern works
- Code examples
- Real-world usage in the system

## Development

The frontend uses:
- **React 18** - UI framework
- **React Router** - Navigation
- **Axios** - HTTP client
- **CSS3** - Styling with modern features

## Notes

- Make sure the producer service is running before using the frontend
- The frontend proxies API requests to `http://localhost:8081` in development
- CORS may need to be configured on the producer service for production deployments

