#!/bin/bash

# Performance test script for Microservices Implementation
# Measures: Response time, Throughput, Resource usage
# Tests producer service (port 8081) and consumer service (port 8082)

# Script initialization

# Check for required tools
command -v curl >/dev/null 2>&1 || { echo "ERROR: curl is required but not installed. Aborting."; exit 1; }
command -v bc >/dev/null 2>&1 && HAS_BC=true || { echo "WARNING: bc not available, using integer arithmetic only."; HAS_BC=false; }

PRODUCER_URL="http://localhost:8081/api/producer"
CONSUMER_URL="http://localhost:8082/api/consumer"
RESULTS_FILE="microservices-results.txt"

# Function to log output both to console and file
log() {
    echo "$@" | tee -a "$RESULTS_FILE"
}

# Function to wait for consumer to process messages
wait_for_processing() {
    local timeout=$1
    local initial_count=$2
    local start_time=$(date +%s)

    log "Waiting for message processing (timeout: ${timeout}s)..."

    while [ $(($(date +%s) - start_time)) -lt $timeout ]; do
        current_count=$(curl -s "$CONSUMER_URL/products" 2>/dev/null | grep -o '"id"' | wc -l)
        if [ "$current_count" -gt "$initial_count" ]; then
            log "Processing completed. Products count: $current_count"
            return 0
        fi
        sleep 2
    done

    log "Timeout waiting for processing. Initial count: $initial_count, Current count: $current_count"
    return 1
}

# Clean results file
> "$RESULTS_FILE"

log "=========================================="
log "Microservices Implementation Performance Tests"
log "Started: $(date)"
log "=========================================="
log ""

# Check if services are available
log "Checking service availability..."
producer_health=$(curl -s -f "$PRODUCER_URL/health" > /dev/null 2>&1 && echo "UP" || echo "DOWN")
consumer_health=$(curl -s -f "$CONSUMER_URL/health" > /dev/null 2>&1 && echo "UP" || echo "DOWN")

if [ "$producer_health" != "UP" ]; then
    log "ERROR: Producer service is not available at $PRODUCER_URL"
    log "Please start the producer service first!"
    exit 1
fi

if [ "$consumer_health" != "UP" ]; then
    log "ERROR: Consumer service is not available at $CONSUMER_URL"
    log "Please start the consumer service first!"
    exit 1
fi

log "Producer service: $producer_health"
log "Consumer service: $consumer_health"
log "Services are available. Starting tests..."
log ""

log "Test 1: Health Checks (Warm-up)"
log "-------------------------------"
for service in "producer" "consumer"; do
    if [ "$service" = "producer" ]; then
        URL="$PRODUCER_URL/health"
        PORT="8081"
    else
        URL="$CONSUMER_URL/health"
        PORT="8082"
    fi

    for i in {1..3}; do
        TIME=$(curl -s -o /dev/null -w "%{time_total}" "$URL" 2>/dev/null)
        if [ ! -z "$TIME" ]; then
            log "$service service (port $PORT) - Request $i: ${TIME}s"
        else
            log "$service service (port $PORT) - Request $i: Health endpoint not available"
        fi
    done
done
log ""

log "Test 2: Single URL Publish and Consume"
log "---------------------------------------"
# Get initial product count
INITIAL_COUNT=$(curl -s "$CONSUMER_URL/products" 2>/dev/null | grep -o '"id"' | wc -l || echo "0")

TEST_URL="https://www.ebay.com/itm/123456789"
START_TIME=$(date +%s.%N)

# Publish URL to producer
RESPONSE=$(curl -s -X POST -w "\n%{http_code}\n%{time_total}" --data-urlencode "url=$TEST_URL" "$PRODUCER_URL/publish" 2>&1)
END_TIME=$(date +%s.%N)
if $HAS_BC; then
    if $HAS_BC; then
        ELAPSED=$(echo "$END_TIME - $START_TIME" | bc 2>/dev/null || echo "0")
    else
        ELAPSED="0"  # Fallback for environments without bc
    fi
else
    ELAPSED="0"  # Fallback for environments without bc
fi
HTTP_CODE=$(echo "$RESPONSE" | tail -2 | head -1)
TIME_TOTAL=$(echo "$RESPONSE" | tail -1)

log "Publish Response Time: ${TIME_TOTAL}s"
log "Publish HTTP Code: $HTTP_CODE"
log "Publish Elapsed Time: ${ELAPSED}s"

# Wait for consumer to process
if wait_for_processing 6 $INITIAL_COUNT; then
    FINAL_COUNT=$(curl -s "$CONSUMER_URL/products" 2>/dev/null | grep -o '"id"' | wc -l || echo "0")
    PRODUCTS_ADDED=$((FINAL_COUNT - INITIAL_COUNT))
    log "Products added to database: $PRODUCTS_ADDED"
else
    log "Processing timeout or failed"
fi
log ""

log "Test 3: Bulk Scraping Performance (Small - 2 pages)"
log "---------------------------------------------------"
# Get initial product count
INITIAL_COUNT=$(curl -s "$CONSUMER_URL/products" 2>/dev/null | grep -o '"id"' | wc -l || echo "0")

EBAY_STARTING_URL="https://www.ebay.com/b/Cell-Phones-Smartphones/9355/bn_320094"
START_TIME=$(date +%s.%N)

# Start scraping via producer
RESPONSE=$(curl -s -X POST -w "\n%{http_code}\n%{time_total}" --data-urlencode "startingUrl=$EBAY_STARTING_URL" --data-urlencode "maxPages=2" "$PRODUCER_URL/start" 2>&1)
END_TIME=$(date +%s.%N)
if $HAS_BC; then
    PUBLISH_ELAPSED=$(echo "$END_TIME - $START_TIME" | bc 2>/dev/null || echo "0")
else
    PUBLISH_ELAPSED="0"  # Fallback for environments without bc
fi

HTTP_CODE=$(echo "$RESPONSE" | tail -2 | head -1)
TIME_TOTAL=$(echo "$RESPONSE" | tail -1)
URLS_FOUND=$(echo "$RESPONSE" | grep -o '"urlsFound":[0-9]*' | cut -d: -f2 || echo "0")

log "Producer Response Time: ${TIME_TOTAL}s"
log "Producer HTTP Code: $HTTP_CODE"
log "URLs Found and Published: ${URLS_FOUND:-0}"
log "Producer Processing Time: ${PUBLISH_ELAPSED}s"

# Wait for consumer to process all URLs (longer timeout for bulk processing)
if wait_for_processing 24 $INITIAL_COUNT; then
    FINAL_COUNT=$(curl -s "$CONSUMER_URL/products" 2>/dev/null | grep -o '"id"' | wc -l || echo "0")
    PRODUCTS_ADDED=$((FINAL_COUNT - INITIAL_COUNT))
    if $HAS_BC; then
        TOTAL_ELAPSED=$(echo "$(date +%s.%N) - $START_TIME" | bc 2>/dev/null || echo "0")
        if [ "$PRODUCTS_ADDED" -gt 0 ] && [ ! -z "$TOTAL_ELAPSED" ] && [ "$TOTAL_ELAPSED" != "0" ]; then
            THROUGHPUT=$(echo "scale=2; $PRODUCTS_ADDED / $TOTAL_ELAPSED" | bc 2>/dev/null || echo "0")
        else
            THROUGHPUT="0"
        fi
    else
        TOTAL_ELAPSED="0"  # Fallback for environments without bc
        THROUGHPUT="0"
    fi
    log "Total Elapsed Time (end-to-end): ${TOTAL_ELAPSED}s"
    log "Products Added to Database: $PRODUCTS_ADDED"
    if [ "$THROUGHPUT" != "0" ]; then
        log "End-to-End Throughput: ${THROUGHPUT} products/second"
    fi
else
    log "Bulk processing timeout"
fi
log ""

log "Test 4: Bulk Scraping Performance (Medium - 5 pages)"
log "----------------------------------------------------"
# Get initial product count
INITIAL_COUNT=$(curl -s "$CONSUMER_URL/products" 2>/dev/null | grep -o '"id"' | wc -l || echo "0")

EBAY_STARTING_URL="https://www.ebay.com/b/Cell-Phones-Smartphones/9355/bn_320094"
START_TIME=$(date +%s.%N)

# Start scraping via producer
RESPONSE=$(curl -s -X POST -w "\n%{http_code}\n%{time_total}" --data-urlencode "startingUrl=$EBAY_STARTING_URL" --data-urlencode "maxPages=5" "$PRODUCER_URL/start" 2>&1)
END_TIME=$(date +%s.%N)
if $HAS_BC; then
    PUBLISH_ELAPSED=$(echo "$END_TIME - $START_TIME" | bc 2>/dev/null || echo "0")
else
    PUBLISH_ELAPSED="0"  # Fallback for environments without bc
fi

HTTP_CODE=$(echo "$RESPONSE" | tail -2 | head -1)
TIME_TOTAL=$(echo "$RESPONSE" | tail -1)
URLS_FOUND=$(echo "$RESPONSE" | grep -o '"urlsFound":[0-9]*' | cut -d: -f2 || echo "0")

log "Producer Response Time: ${TIME_TOTAL}s"
log "Producer HTTP Code: $HTTP_CODE"
log "URLs Found and Published: ${URLS_FOUND:-0}"
log "Producer Processing Time: ${PUBLISH_ELAPSED}s"

# Wait for consumer to process all URLs
if wait_for_processing 60 $INITIAL_COUNT; then
    FINAL_COUNT=$(curl -s "$CONSUMER_URL/products" 2>/dev/null | grep -o '"id"' | wc -l || echo "0")
    PRODUCTS_ADDED=$((FINAL_COUNT - INITIAL_COUNT))
    if $HAS_BC; then
        TOTAL_ELAPSED=$(echo "$(date +%s.%N) - $START_TIME" | bc 2>/dev/null || echo "0")
        if [ "$PRODUCTS_ADDED" -gt 0 ] && [ ! -z "$TOTAL_ELAPSED" ] && [ "$TOTAL_ELAPSED" != "0" ]; then
            THROUGHPUT=$(echo "scale=2; $PRODUCTS_ADDED / $TOTAL_ELAPSED" | bc 2>/dev/null || echo "0")
        else
            THROUGHPUT="0"
        fi
    else
        TOTAL_ELAPSED="0"  # Fallback for environments without bc
        THROUGHPUT="0"
    fi
    log "Total Elapsed Time (end-to-end): ${TOTAL_ELAPSED}s"
    log "Products Added to Database: $PRODUCTS_ADDED"
    if [ "$THROUGHPUT" != "0" ]; then
        log "End-to-End Throughput: ${THROUGHPUT} products/second"
    fi
else
    log "Bulk processing timeout"
fi
log ""

log "Test 5: Concurrent Requests (10 parallel requests)"
log "---------------------------------------------------"
START_TIME=$(date +%s.%N)

# Test both producer and consumer concurrently
for i in {1..5}; do
    curl -s -o /dev/null -w "%{time_total}\n" "$PRODUCER_URL/health" 2>/dev/null &
    curl -s -o /dev/null -w "%{time_total}\n" "$CONSUMER_URL/products" 2>/dev/null &
done

wait
END_TIME=$(date +%s.%N)
if $HAS_BC; then
    ELAPSED=$(echo "$END_TIME - $START_TIME" | bc 2>/dev/null || echo "0")
    AVG_TIME=$(echo "scale=3; $ELAPSED / 10" | bc 2>/dev/null || echo "0")
else
    ELAPSED="0"  # Fallback for environments without bc
    AVG_TIME="0"
fi

log "Total Time for 10 concurrent requests (5 producer + 5 consumer): ${ELAPSED}s"
log "Average per request: ${AVG_TIME}s"
log ""

log "Test 6: Database Query Performance"
log "-----------------------------------"
START_TIME=$(date +%s.%N)
RESPONSE=$(curl -s -w "\n%{http_code}\n%{time_total}" "$CONSUMER_URL/products" 2>&1)
END_TIME=$(date +%s.%N)
if $HAS_BC; then
    ELAPSED=$(echo "$END_TIME - $START_TIME" | bc 2>/dev/null || echo "0")
else
    ELAPSED="0"  # Fallback for environments without bc
fi
HTTP_CODE=$(echo "$RESPONSE" | tail -2 | head -1)
TIME_TOTAL=$(echo "$RESPONSE" | tail -1)
PRODUCT_COUNT=$(echo "$RESPONSE" | grep -o '"id"' | wc -l)

log "Response Time: ${TIME_TOTAL}s"
log "HTTP Code: $HTTP_CODE"
log "Products Retrieved: $PRODUCT_COUNT"
log "Query Elapsed Time: ${ELAPSED}s"
log ""

log "=========================================="
log "Tests completed at $(date)"
log "Results saved to: $RESULTS_FILE"
log "=========================================="