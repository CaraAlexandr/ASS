# Web Scraper Application

A web scraper application for scraping 999.md website, implemented using two different architectures:
1. **Distributed Monolith** - Single Spring Boot application with internal modules
2. **Microservices** - Separate producer and consumer services communicating via RabbitMQ

## Technologies Used

- **Java 21**
- **Spring Boot 3.2.0**
- **Spring Data JPA**
- **RabbitMQ** (for microservices)
- **PostgreSQL**
- **JSoup** (HTML parsing)
- **Swagger/OpenAPI** (API documentation)
- **Docker & Docker Compose**

## Architecture Overview

### Distributed Monolith

A single Spring Boot application that:
- Scrapes product URLs from 999.md
- Extracts product details
- Saves data directly to PostgreSQL
- Provides REST API with Swagger documentation

**Services:**
- Single application running on port 8080
- PostgreSQL database on port 5433

### Microservices

Separated into two services:

1. **Producer Service** (Port 8081)
   - Scrapes 999.md website
   - Finds product URLs
   - Publishes URLs to RabbitMQ queue

2. **Consumer Service** (Port 8082)
   - Consumes URLs from RabbitMQ
   - Extracts product details
   - Saves to PostgreSQL

**Services:**
- Producer service: Port 8081
- Consumer service: Port 8082
- RabbitMQ: Port 5672 (AMQP), Port 15672 (Management UI)
- PostgreSQL: Port 5434

## Prerequisites

- Docker and Docker Compose installed
- Java 21 (optional, for local development)
- Maven (optional, for local development)

## Getting Started

### Running Distributed Monolith

The services will automatically rebuild on each `docker-compose up`:

```bash
cd distributed-monolith
docker-compose up --build
```

**Or use the convenience script:**
```bash
cd distributed-monolith
./docker-compose.up.sh
```

The application will be available at:
- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html

### Running Microservices

The services will automatically rebuild on each `docker-compose up`:

```bash
cd microservices
docker-compose up --build
```

**Or use the convenience script:**
```bash
cd microservices
./docker-compose.up.sh
```

Services will be available at:
- Producer Service API: http://localhost:8081
- Producer Service Swagger: http://localhost:8081/swagger-ui.html
- Consumer Service API: http://localhost:8082
- Consumer Service Swagger: http://localhost:8082/swagger-ui.html
- RabbitMQ Management: http://localhost:15672 (guest/guest)

## API Usage

### Distributed Monolith

#### Start Scraping
```bash
POST http://localhost:8080/api/scraper/start?startingUrl=https://999.md/ru/list/animals-and-plants/the-birds&maxPages=10
```

#### Get All Products
```bash
GET http://localhost:8080/api/scraper/products
```

#### Extract Single Product Info
```bash
GET http://localhost:8080/api/scraper/extract/{url}
```

### Microservices

#### Start Scraping (Producer)
```bash
POST http://localhost:8081/api/producer/start?startingUrl=https://999.md/ru/list/animals-and-plants/the-birds&maxPages=10
```

#### Publish Single URL (Producer)
```bash
POST http://localhost:8081/api/producer/publish?url=https://999.md/ro/12345678
```

#### Get All Products (Consumer)
```bash
GET http://localhost:8082/api/consumer/products
```

#### Health Checks
```bash
GET http://localhost:8081/api/producer/health
GET http://localhost:8082/api/consumer/health
```

## Database Schema

The `product_details` table stores:
- `id` - Primary key
- `url` - Unique product URL
- `title` - Product title
- `description` - Product description
- `price` - Product price
- `location` - Product location
- `ad_info` - JSONB field for ad information
- `general_info` - JSONB field for general information
- `features` - JSONB field for product features
- `created_at` - Timestamp

## Project Structure

```
ASS/
├── distributed-monolith/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/scraper/
│   │       │   ├── controller/
│   │       │   ├── service/
│   │       │   ├── entity/
│   │       │   ├── repository/
│   │       │   ├── dto/
│   │       │   └── config/
│   │       └── resources/
│   ├── Dockerfile
│   └── docker-compose.yml
│
├── microservices/
│   ├── producer-service/
│   │   ├── src/
│   │   │   └── main/
│   │   │       ├── java/com/scraper/producer/
│   │   │       └── resources/
│   │   ├── Dockerfile
│   │   └── pom.xml
│   │
│   ├── consumer-service/
│   │   ├── src/
│   │   │   └── main/
│   │   │       ├── java/com/scraper/consumer/
│   │   │       └── resources/
│   │   ├── Dockerfile
│   │   └── pom.xml
│   │
│   └── docker-compose.yml
│
└── README.md
```

## Configuration

### Environment Variables

#### Distributed Monolith
- `DB_HOST` - Database host (default: db)
- `DB_NAME` - Database name (default: scraperdb)
- `DB_USER` - Database user (default: admin)
- `DB_PASSWORD` - Database password (default: adminpass)

#### Microservices - Producer
- `RABBITMQ_HOST` - RabbitMQ host (default: rabbitmq)
- `RABBITMQ_PORT` - RabbitMQ port (default: 5672)
- `RABBITMQ_USER` - RabbitMQ user (default: guest)
- `RABBITMQ_PASSWORD` - RabbitMQ password (default: guest)

#### Microservices - Consumer
- `RABBITMQ_HOST` - RabbitMQ host (default: rabbitmq)
- `RABBITMQ_PORT` - RabbitMQ port (default: 5672)
- `RABBITMQ_USER` - RabbitMQ user (default: guest)
- `RABBITMQ_PASSWORD` - RabbitMQ password (default: guest)
- `DB_HOST` - Database host (default: db)
- `DB_NAME` - Database name (default: scraperdb)
- `DB_USER` - Database user (default: admin)
- `DB_PASSWORD` - Database password (default: adminpass)

## Features

- ✅ Web scraping with JSoup
- ✅ RabbitMQ message queue (microservices)
- ✅ PostgreSQL database persistence
- ✅ RESTful APIs
- ✅ Swagger/OpenAPI documentation
- ✅ Docker containerization
- ✅ Health check endpoints
- ✅ Concurrent processing
- ✅ Error handling and logging

## Stopping Services

### Distributed Monolith
```bash
cd distributed-monolith
docker-compose down
```

### Microservices
```bash
cd microservices
docker-compose down
```

To also remove volumes:
```bash
docker-compose down -v
```

## Development

### Building Locally

#### Distributed Monolith
```bash
cd distributed-monolith
mvn clean package
java -jar target/distributed-monolith-1.0.0.jar
```

#### Microservices
```bash
# Producer Service
cd microservices/producer-service
mvn clean package
java -jar target/producer-service-1.0.0.jar

# Consumer Service
cd microservices/consumer-service
mvn clean package
java -jar target/consumer-service-1.0.0.jar
```

## Notes

- The scraper includes rate limiting (1 second delay between requests)
- URLs are deduplicated to avoid processing the same product twice
- The consumer service processes messages concurrently (5 consumers by default)
- Both architectures use the same database schema for compatibility

## License

This project is for educational purposes.
