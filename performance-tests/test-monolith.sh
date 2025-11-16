#!/bin/bash

# Performance test script for Distributed Monolith
# Measures: Response time, Throughput, Resource usage

BASE_URL="http://localhost:8080/api/scraper"
RESULTS_FILE="monolith-results.txt"

# Function to log output both to console and file
log() {
    echo "$@" | tee -a "$RESULTS_FILE"
}

# Clean results file
> "$RESULTS_FILE"

log "=========================================="
log "Distributed Monolith Performance Tests"
log "Started: $(date)"
log "=========================================="
log ""

# Check if service is available
log "Checking service availability..."
if ! curl -s -f "$BASE_URL/../actuator/health" > /dev/null 2>&1 && ! curl -s -f "$BASE_URL/products" > /dev/null 2>&1; then
    log "ERROR: Service is not available at $BASE_URL"
    log "Please start the service first!"
    exit 1
fi

log "Service is available. Starting tests..."
log ""

log "Test 1: Health Check (Warm-up)"
log "-------------------------------"
for i in {1..5}; do
    TIME=$(curl -s -o /dev/null -w "%{time_total}" "$BASE_URL/../actuator/health" 2>/dev/null || curl -s -o /dev/null -w "%{time_total}" "$BASE_URL/products" 2>/dev/null)
    if [ ! -z "$TIME" ]; then
        log "Request $i: ${TIME}s"
    else
        log "Request $i: Health endpoint not available"
    fi
done
log ""

log "Test 2: Single Product Extraction"
log "----------------------------------"
TEST_URL="https://www.ebay.com/itm/123456789"
START_TIME=$(date +%s.%N)
# URL encode the eBay URL for the path parameter
ENCODED_URL=$(echo "$TEST_URL" | sed 's|:|%3A|g' | sed 's|/|%2F|g' | sed 's|&|%26|g' | sed 's|?|%3F|g' | sed 's|=|%3D|g')
RESPONSE=$(curl -s -w "\n%{http_code}\n%{time_total}" "$BASE_URL/extract/$ENCODED_URL" 2>&1)
END_TIME=$(date +%s.%N)
ELAPSED=$(echo "$END_TIME - $START_TIME" | bc 2>/dev/null || echo "0")
HTTP_CODE=$(echo "$RESPONSE" | tail -2 | head -1)
TIME_TOTAL=$(echo "$RESPONSE" | tail -1)
log "Response Time: ${TIME_TOTAL}s"
log "HTTP Code: $HTTP_CODE"
log "Elapsed Time: ${ELAPSED}s"
log ""

log "Test 3: Scraping Performance (Small - 2 pages)"
log "-----------------------------------------------"
EBAY_STARTING_URL="https://www.ebay.com/b/Cell-Phones-Smartphones/9355/bn_320094"
START_TIME=$(date +%s.%N)
RESPONSE=$(curl -s -X POST -w "\n%{http_code}\n%{time_total}" --data-urlencode "startingUrl=$EBAY_STARTING_URL" --data-urlencode "maxPages=2" "$BASE_URL/start" 2>&1)
END_TIME=$(date +%s.%N)
ELAPSED=$(echo "$END_TIME - $START_TIME" | bc 2>/dev/null || echo "0")
HTTP_CODE=$(echo "$RESPONSE" | tail -2 | head -1)
TIME_TOTAL=$(echo "$RESPONSE" | tail -1)
PRODUCTS_FOUND=$(echo "$RESPONSE" | grep -o '"productsFound":[0-9]*' | cut -d: -f2)
log "Response Time: ${TIME_TOTAL}s"
log "Total Elapsed: ${ELAPSED}s"
log "HTTP Code: $HTTP_CODE"
log "Products Found: ${PRODUCTS_FOUND:-0}"
if [ ! -z "$PRODUCTS_FOUND" ] && [ "$PRODUCTS_FOUND" -gt 0 ] && [ ! -z "$ELAPSED" ] && [ "$ELAPSED" != "0" ]; then
    THROUGHPUT=$(echo "scale=2; $PRODUCTS_FOUND / $ELAPSED" | bc 2>/dev/null || echo "0")
    log "Throughput: ${THROUGHPUT} products/second"
fi
log ""

log "Test 4: Scraping Performance (Medium - 5 pages)"
log "------------------------------------------------"
EBAY_STARTING_URL="https://www.ebay.com/b/Cell-Phones-Smartphones/9355/bn_320094"
START_TIME=$(date +%s.%N)
RESPONSE=$(curl -s -X POST -w "\n%{http_code}\n%{time_total}" --data-urlencode "startingUrl=$EBAY_STARTING_URL" --data-urlencode "maxPages=5" "$BASE_URL/start" 2>&1)
END_TIME=$(date +%s.%N)
ELAPSED=$(echo "$END_TIME - $START_TIME" | bc 2>/dev/null || echo "0")
HTTP_CODE=$(echo "$RESPONSE" | tail -2 | head -1)
TIME_TOTAL=$(echo "$RESPONSE" | tail -1)
PRODUCTS_FOUND=$(echo "$RESPONSE" | grep -o '"productsFound":[0-9]*' | cut -d: -f2)
log "Response Time: ${TIME_TOTAL}s"
log "Total Elapsed: ${ELAPSED}s"
log "HTTP Code: $HTTP_CODE"
log "Products Found: ${PRODUCTS_FOUND:-0}"
if [ ! -z "$PRODUCTS_FOUND" ] && [ "$PRODUCTS_FOUND" -gt 0 ] && [ ! -z "$ELAPSED" ] && [ "$ELAPSED" != "0" ]; then
    THROUGHPUT=$(echo "scale=2; $PRODUCTS_FOUND / $ELAPSED" | bc 2>/dev/null || echo "0")
    log "Throughput: ${THROUGHPUT} products/second"
fi
log ""

log "Test 5: Concurrent Requests (10 parallel requests)"
log "---------------------------------------------------"
START_TIME=$(date +%s.%N)
for i in {1..10}; do
    curl -s -o /dev/null -w "%{time_total}\n" "$BASE_URL/products" 2>/dev/null &
done
wait
END_TIME=$(date +%s.%N)
ELAPSED=$(echo "$END_TIME - $START_TIME" | bc 2>/dev/null || echo "0")
AVG_TIME=$(echo "scale=3; $ELAPSED / 10" | bc 2>/dev/null || echo "0")
log "Total Time for 10 concurrent requests: ${ELAPSED}s"
log "Average per request: ${AVG_TIME}s"
log ""

log "Test 6: Database Query Performance"
log "-----------------------------------"
START_TIME=$(date +%s.%N)
RESPONSE=$(curl -s -w "\n%{http_code}\n%{time_total}" "$BASE_URL/products" 2>&1)
END_TIME=$(date +%s.%N)
ELAPSED=$(echo "$END_TIME - $START_TIME" | bc 2>/dev/null || echo "0")
HTTP_CODE=$(echo "$RESPONSE" | tail -2 | head -1)
TIME_TOTAL=$(echo "$RESPONSE" | tail -1)
PRODUCT_COUNT=$(echo "$RESPONSE" | grep -o '"id"' | wc -l)
log "Response Time: ${TIME_TOTAL}s"
log "HTTP Code: $HTTP_CODE"
log "Products Retrieved: $PRODUCT_COUNT"
log ""

log "=========================================="
log "Tests completed at $(date)"
log "Results saved to: $RESULTS_FILE"
log "=========================================="
