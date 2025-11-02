#!/bin/bash

# Setup script for development environment
set -e

echo "Setting up School Management System development environment..."

# Check prerequisites
echo "Checking prerequisites..."
command -v java >/dev/null 2>&1 || { echo "Java is required but not installed. Aborting." >&2; exit 1; }
command -v mvn >/dev/null 2>&1 || { echo "Maven is required but not installed. Aborting." >&2; exit 1; }
command -v docker >/dev/null 2>&1 || { echo "Docker is required but not installed. Aborting." >&2; exit 1; }
command -v docker-compose >/dev/null 2>&1 || { echo "Docker Compose is required but not installed. Aborting." >&2; exit 1; }

echo "Prerequisites check passed!"

# Make scripts executable
echo "Making scripts executable..."
chmod +x scripts/*.sh

# Create .env file if it doesn't exist
if [ ! -f .env ]; then
    echo "Creating .env file from .env.example..."
    cp .env.example .env
    echo "Please update .env file with your configuration"
fi

# Build shared modules
echo "Building shared modules..."
mvn clean install -pl shared/common,shared/events,shared/security,shared/persistence -am -DskipTests

# Start infrastructure services
echo "Starting infrastructure services..."
docker-compose up -d postgres rabbitmq redis elasticsearch kibana minio keycloak prometheus grafana

echo "Setup completed successfully!"
echo "Infrastructure services are starting. You can check status with: docker-compose ps"

