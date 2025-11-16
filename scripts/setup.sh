#!/bin/bash
# School Management System - Setup Script
# Purpose: Initialize project and start infrastructure services

set -e

echo "=========================================="
echo "School Management System - Setup"
echo "=========================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check prerequisites
echo -e "${YELLOW}Checking prerequisites...${NC}"

# Check Java
if ! command -v java &> /dev/null; then
    echo -e "${RED}Error: Java not found. Please install OpenJDK 25${NC}"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | sed '/^1\./s///' | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 25 ]; then
    echo -e "${RED}Error: Java 25 or higher required. Found: $JAVA_VERSION${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Java $JAVA_VERSION found${NC}"

# Check Maven
if ! command -v mvn &> /dev/null; then
    echo -e "${RED}Error: Maven not found. Please install Maven 3.9.11${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Maven found${NC}"

# Check Docker
if ! command -v docker &> /dev/null; then
    echo -e "${RED}Error: Docker not found. Please install Docker${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Docker found${NC}"

# Check Docker Compose
if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
    echo -e "${RED}Error: Docker Compose not found${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Docker Compose found${NC}"

echo ""
echo -e "${YELLOW}Starting infrastructure services...${NC}"

# Start infrastructure services
cd "$(dirname "$0")/.."
if docker compose version &> /dev/null; then
    docker compose -f docker/docker-compose.yml up -d
else
    docker-compose -f docker/docker-compose.yml up -d
fi

echo ""
echo -e "${YELLOW}Waiting for services to be healthy...${NC}"
sleep 10

# Check service health
echo -e "${YELLOW}Checking service health...${NC}"

# PostgreSQL
if docker exec school-postgres pg_isready -U school_user &> /dev/null; then
    echo -e "${GREEN}✓ PostgreSQL is ready${NC}"
else
    echo -e "${RED}✗ PostgreSQL is not ready${NC}"
fi

# RabbitMQ
if docker exec school-rabbitmq rabbitmq-diagnostics ping &> /dev/null; then
    echo -e "${GREEN}✓ RabbitMQ is ready${NC}"
else
    echo -e "${RED}✗ RabbitMQ is not ready${NC}"
fi

# Elasticsearch
if curl -f http://localhost:9200/_cluster/health &> /dev/null; then
    echo -e "${GREEN}✓ Elasticsearch is ready${NC}"
else
    echo -e "${RED}✗ Elasticsearch is not ready${NC}"
fi

# Keycloak
if curl -f http://localhost:8080/health/ready &> /dev/null; then
    echo -e "${GREEN}✓ Keycloak is ready${NC}"
else
    echo -e "${YELLOW}⚠ Keycloak may still be starting (check http://localhost:8080)${NC}"
fi

# MinIO
if curl -f http://localhost:9000/minio/health/live &> /dev/null; then
    echo -e "${GREEN}✓ MinIO is ready${NC}"
else
    echo -e "${RED}✗ MinIO is not ready${NC}"
fi

echo ""
echo -e "${GREEN}=========================================="
echo "Setup complete!"
echo "==========================================${NC}"
echo ""
echo "Infrastructure services are running:"
echo "  - PostgreSQL:     localhost:5432"
echo "  - RabbitMQ:       localhost:5672 (Management UI: http://localhost:15672)"
echo "  - Elasticsearch:  http://localhost:9200"
echo "  - Keycloak:       http://localhost:8080"
echo "  - MinIO:          http://localhost:9000 (Console: http://localhost:9001)"
echo ""
echo "Next steps:"
echo "  1. Build all services: ./scripts/build-all.sh"
echo "  2. Start individual services: cd services/user-service && mvn spring-boot:run"
echo ""

