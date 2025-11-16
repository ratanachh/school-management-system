#!/bin/bash
# Script to verify all services meet >80% test coverage requirement

set -e

echo "=== Verifying Test Coverage ==="
echo ""

COVERAGE_THRESHOLD=80
FAILED_SERVICES=()

SERVICES=(
    "user-service"
    "academic-service"
    "attendance-service"
    "academic-assessment-service"
    "search-service"
    "audit-service"
    "notification-service"
)

for service in "${SERVICES[@]}"; do
    echo "Checking $service..."
    
    if [ -d "services/$service" ]; then
        cd "services/$service"
        
        if [ -f "pom.xml" ]; then
            # Run tests with coverage
            mvn clean test jacoco:report jacoco:check || {
                echo "⚠️  $service: Tests failed or coverage check failed"
                FAILED_SERVICES+=("$service")
            }
        else
            echo "⚠️  $service: No pom.xml found"
        fi
        
        cd ../..
    else
        echo "⚠️  $service: Directory not found"
    fi
    
    echo ""
done

echo "=== Coverage Verification Summary ==="
if [ ${#FAILED_SERVICES[@]} -eq 0 ]; then
    echo "✅ All services meet coverage requirements (>80%)"
    exit 0
else
    echo "❌ The following services failed coverage checks:"
    for service in "${FAILED_SERVICES[@]}"; do
        echo "  - $service"
    done
    exit 1
fi

