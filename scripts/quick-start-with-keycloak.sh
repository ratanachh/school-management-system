#!/bin/bash
# Quick start script: Start infrastructure, initialize Keycloak, get client secret, and restart user-service
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}==========================================${NC}"
echo -e "${BLUE}Quick Start: Infrastructure + User Service${NC}"
echo -e "${BLUE}==========================================${NC}"
echo ""

# Step 1: Start infrastructure
echo -e "${GREEN}Step 1: Starting Docker infrastructure...${NC}"
make docker-up
echo ""

# Step 2: Wait for Keycloak to be ready
echo -e "${GREEN}Step 2: Waiting for Keycloak to be ready...${NC}"
MAX_RETRIES=60
RETRY_COUNT=0
KEYCLOAK_URL="${KEYCLOAK_URL:-http://localhost:8070}"

while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
    if curl -s -f "${KEYCLOAK_URL}/health" > /dev/null 2>&1 || \
       curl -s -f "${KEYCLOAK_URL}/realms/master" > /dev/null 2>&1; then
        echo -e "${GREEN}Keycloak is ready!${NC}"
        break
    fi
    RETRY_COUNT=$((RETRY_COUNT + 1))
    if [ $RETRY_COUNT -eq $MAX_RETRIES ]; then
        echo -e "${RED}Error: Keycloak is not ready after ${MAX_RETRIES} attempts${NC}"
        exit 1
    fi
    sleep 2
done
echo ""

# Step 3: Build user-service
echo -e "${GREEN}Step 3: Building user-service...${NC}"
cd services/user-service
mvn clean package -Dmaven.test.skip=true -q
cd "$PROJECT_ROOT"
echo ""

# Step 4: Run user-service to initialize Keycloak (in background, wait for initialization)
echo -e "${GREEN}Step 4: Starting user-service to initialize Keycloak...${NC}"
echo -e "${YELLOW}This will initialize Keycloak realm and clients...${NC}"

# Start user-service in background
cd services/user-service
USER_SERVICE_PID=""
(
    source ../../.env 2>/dev/null || true
    mvn spring-boot:run > /tmp/user-service-init.log 2>&1 &
    USER_SERVICE_PID=$!
    echo $USER_SERVICE_PID > /tmp/user-service.pid
    echo "User-service PID: $USER_SERVICE_PID"
) || {
    echo -e "${RED}Failed to start user-service${NC}"
    exit 1
}

cd "$PROJECT_ROOT"

# Wait for Keycloak initialization to complete
echo -e "${YELLOW}Waiting for Keycloak initialization to complete...${NC}"
INIT_TIMEOUT=120
INIT_COUNT=0
REALM_READY=false

# Load env vars
set -a
source .env 2>/dev/null || true
set +a

KEYCLOAK_REALM="${KEYCLOAK_REALM:-school-management}"

while [ $INIT_COUNT -lt $INIT_TIMEOUT ]; do
    # Check if realm exists and has clients
    if curl -s -f "${KEYCLOAK_URL}/realms/${KEYCLOAK_REALM}" > /dev/null 2>&1; then
        # Try to get admin token and check for clients
        TOKEN_RESPONSE=$(curl -s -X POST "${KEYCLOAK_URL}/realms/master/protocol/openid-connect/token" \
            -H "Content-Type: application/x-www-form-urlencoded" \
            -d "username=${KEYCLOAK_ADMIN:-admin}" \
            -d "password=${KEYCLOAK_ADMIN_PASSWORD:-}" \
            -d "grant_type=password" \
            -d "client_id=${KEYCLOAK_ADMIN_CLIENT_ID:-admin-cli}") || true
        
        ACCESS_TOKEN=$(echo "$TOKEN_RESPONSE" | grep -o '"access_token":"[^"]*' | cut -d'"' -f4 || echo "")
        
        if [ -n "$ACCESS_TOKEN" ]; then
            CLIENTS_RESPONSE=$(curl -s -X GET "${KEYCLOAK_URL}/admin/realms/${KEYCLOAK_REALM}/clients" \
                -H "Authorization: Bearer ${ACCESS_TOKEN}" \
                -H "Content-Type: application/json" || echo "")
            
            if echo "$CLIENTS_RESPONSE" | grep -q "user-profile" || echo "$CLIENTS_RESPONSE" | grep -q "${KEYCLOAK_SERVICE_CLIENT_ID:-user-profile}"; then
                REALM_READY=true
                break
            fi
        fi
    fi
    
    INIT_COUNT=$((INIT_COUNT + 1))
    sleep 2
done

if [ "$REALM_READY" = false ]; then
    echo -e "${YELLOW}Warning: Keycloak initialization may not be complete, but continuing...${NC}"
fi

# Stop user-service
if [ -f /tmp/user-service.pid ]; then
    USER_SERVICE_PID=$(cat /tmp/user-service.pid 2>/dev/null || echo "")
    if [ -n "$USER_SERVICE_PID" ] && kill -0 "$USER_SERVICE_PID" 2>/dev/null; then
        echo -e "${YELLOW}Stopping user-service...${NC}"
        kill "$USER_SERVICE_PID" 2>/dev/null || true
        sleep 3
        kill -9 "$USER_SERVICE_PID" 2>/dev/null || true
    fi
    rm -f /tmp/user-service.pid
fi

echo -e "${GREEN}Keycloak initialization completed${NC}"
echo ""

# Step 5: Get client secret and update .env
echo -e "${GREEN}Step 5: Retrieving client secret from Keycloak...${NC}"
bash scripts/get-keycloak-client-secret.sh
echo ""

# Step 6: Restart user-service
echo -e "${GREEN}Step 6: Restarting user-service with updated configuration...${NC}"
echo -e "${YELLOW}Starting user-service in background...${NC}"
echo -e "${YELLOW}Note: Service logs will be written to /tmp/user-service.log${NC}"

# Reload environment variables to get the updated secret
set -a
source .env 2>/dev/null || true
set +a

cd services/user-service
(
    source ../../.env 2>/dev/null || true
    nohup mvn spring-boot:run > /tmp/user-service.log 2>&1 &
    NEW_PID=$!
    echo $NEW_PID > /tmp/user-service.pid
    echo -e "${GREEN}User-service started (PID: $NEW_PID)${NC}"
) || {
    echo -e "${RED}Failed to start user-service${NC}"
    exit 1
}

cd "$PROJECT_ROOT"

# Wait a moment for service to start
sleep 5

# Verify service is running
if [ -f /tmp/user-service.pid ]; then
    PID=$(cat /tmp/user-service.pid)
    if kill -0 "$PID" 2>/dev/null; then
        echo -e "${GREEN}User-service is running (PID: $PID)${NC}"
    else
        echo -e "${YELLOW}Warning: User-service process not found. Check logs:${NC} tail -f /tmp/user-service.log"
    fi
fi

echo ""
echo -e "${BLUE}======================================${NC}"
echo -e "${GREEN}Quick start completed!${NC}"
echo -e "${BLUE}======================================${NC}"
echo ""
echo -e "${BLUE}User-service is running in the background${NC}"
echo -e "${YELLOW}To view logs:${NC} tail -f /tmp/user-service.log"
echo -e "${YELLOW}To stop service:${NC} kill \$(cat /tmp/user-service.pid)"
echo ""

