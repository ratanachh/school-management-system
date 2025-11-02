#!/bin/bash

# Build all services script
set -e

echo "Building School Management System..."

# Build shared modules first
echo "Building shared modules..."
mvn clean install -pl shared/common,shared/events,shared/security,shared/persistence -am

# Build platform services
echo "Building platform services..."
mvn clean install -pl platform/config-server,platform/discovery-server,platform/api-gateway -am

# Build business services (if any exist)
echo "Building business services..."
if [ -d "services" ]; then
    mvn clean install -pl services -am || echo "No business services to build yet"
fi

echo "Build completed successfully!"

