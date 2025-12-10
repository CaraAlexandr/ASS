# Frontend Service Setup Guide

## Quick Start (Development)

### Option 1: Run with npm (Recommended for Development)

1. **Install dependencies:**
   ```bash
   cd frontend-service
   npm install
   ```

2. **Start the development server:**
   ```bash
   npm start
   ```

3. **Access the application:**
   - Open http://localhost:3000 in your browser
   - Make sure the producer service is running on http://localhost:8081

### Option 2: Run with Docker

1. **Build and run with docker-compose:**
   ```bash
   cd microservices
   docker-compose up frontend-service
   ```

2. **Access the application:**
   - Open http://localhost:3000 in your browser

## Configuration

### API Endpoint

The frontend connects to the producer service API. By default, it uses:
- Development: `http://localhost:8081` (configured in package.json proxy)
- Production: Set `REACT_APP_API_URL` environment variable

### Environment Variables

Create a `.env` file in the frontend-service directory:

```env
REACT_APP_API_URL=http://localhost:8081
```

## Troubleshooting

### CORS Issues

If you encounter CORS errors, make sure:
1. The producer service is running
2. The producer service allows CORS from the frontend origin
3. You're using the correct API URL

### API Connection Issues

1. Verify the producer service is running:
   ```bash
   curl http://localhost:8081/api/producer/health
   ```

2. Check the browser console for error messages

3. Verify the API URL in the browser's Network tab

## Features

- ✅ Interactive dashboard showing all 9 patterns
- ✅ Individual pattern demonstrations
- ✅ Full pattern integration demo
- ✅ Real-time results and metrics
- ✅ Pattern explanations and documentation
- ✅ Responsive design

## Development Notes

- The app uses React Router for navigation
- API calls are made using Axios
- All patterns are demonstrated through the producer service API
- The frontend is a Single Page Application (SPA)

