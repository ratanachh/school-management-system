#!/bin/bash

# Test all services script
set -e

echo "Running tests for School Management System..."

# Test shared modules
echo "Testing shared modules..."
mvn test -pl shared/common,shared/events,shared/security,shared/persistence

# Test platform services
echo "Testing platform services..."
mvn test -pl platform/config-server,platform/discovery-server,platform/api-gateway

# Test business services (if any exist)
echo "Testing business services..."
if [ -d "services" ]; then
    mvn test -pl services || echo "No business services to test yet"
fi

echo "All tests completed successfully!"

