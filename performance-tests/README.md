# Performance Tests

This directory contains performance test scripts for comparing the Distributed Monolith and Microservices architectures.

## Prerequisites

- `curl` installed
- `bc` (basic calculator) installed: `sudo apt-get install bc` or `brew install bc`
- Both applications running:
  - Distributed Monolith: `docker-compose -f distributed-monolith/docker-compose-monolith.yml up`
  - Microservices: `docker-compose -f microservices/docker-compose-microservices.yml up`

## Running Tests

### Test Distributed Monolith

```bash
chmod +x test-monolith.sh
./test-monolith.sh
```

### Test Microservices

```bash
chmod +x test-microservices.sh
./test-microservices.sh
```

## Metrics Measured

### Distributed Monolith
- Response time for single product extraction
- Scraping performance (small and medium workloads)
- Throughput (products/second)
- Concurrent request handling
- Database query performance

### Microservices
- Producer service response time
- Consumer service response time
- Message queue latency (publish to consume)
- End-to-end throughput (Producer -> Queue -> Consumer)
- Concurrent request handling
- Database query performance

## Notes

- Tests should be run when services are fully started
- Some tests may take longer depending on network conditions
- Results are printed to console and can be redirected to files for analysis

