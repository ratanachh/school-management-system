#!/bin/bash
# School Management System - Build All Services Script
# Purpose: Build all microservices from the parent POM

set -e

echo "=========================================="
echo "School Management System - Build All Services"
echo "=========================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Get script directory and navigate to project root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

cd "$PROJECT_ROOT"

# Check if pom.xml exists
if [ ! -f "pom.xml" ]; then
    echo -e "${RED}Error: pom.xml not found in project root${NC}"
    exit 1
fi

echo -e "${YELLOW}Building all services from parent POM...${NC}"
echo ""

# Build with Maven
if mvn clean install -DskipTests; then
    echo ""
    echo -e "${GREEN}=========================================="
    echo "Build successful!"
    echo "==========================================${NC}"
    echo ""
    echo "All services have been built. You can now:"
    echo "  1. Start individual services: cd services/user-service && mvn spring-boot:run"
    echo "  2. Run tests: mvn test"
    echo "  3. Check service health: curl http://localhost:8081/actuator/health"
    echo ""
else
    echo ""
    echo -e "${RED}=========================================="
    echo "Build failed!"
    echo "==========================================${NC}"
    echo ""
    echo "Please check the error messages above and fix any issues."
    exit 1
fi

