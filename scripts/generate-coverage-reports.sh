#!/bin/bash
# Script to generate test coverage reports for all services

set -e

echo "=== Generating Test Coverage Reports ==="
echo ""

SERVICES=(
    "user-service"
    "academic-service"
    "attendance-service"
    "academic-assessment-service"
    "search-service"
    "audit-service"
    "notification-service"
)

REPORTS_DIR="reports/coverage"
mkdir -p "$REPORTS_DIR"

for service in "${SERVICES[@]}"; do
    echo "Generating coverage report for $service..."
    
    if [ -d "services/$service" ]; then
        cd "services/$service"
        
        if [ -f "pom.xml" ]; then
            # Generate coverage report
            mvn clean test jacoco:report || {
                echo "⚠️  $service: Failed to generate coverage report"
                cd ../..
                continue
            }
            
            # Copy report to reports directory
            if [ -d "target/site/jacoco" ]; then
                mkdir -p "../../$REPORTS_DIR/$service"
                cp -r target/site/jacoco/* "../../$REPORTS_DIR/$service/" 2>/dev/null || true
                echo "✅ Coverage report generated for $service"
            fi
        fi
        
        cd ../..
    fi
    
    echo ""
done

echo "=== Coverage Reports Generated ==="
echo "Reports available in: $REPORTS_DIR"
echo ""

# Generate summary
echo "=== Coverage Summary ==="
for service in "${SERVICES[@]}"; do
    if [ -f "$REPORTS_DIR/$service/index.html" ]; then
        echo "✅ $service: Report available"
    else
        echo "⚠️  $service: Report not found"
    fi
done

