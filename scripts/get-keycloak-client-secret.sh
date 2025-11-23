#!/bin/bash
# Script to retrieve Keycloak client secret and update .env file
set -euo pipefail

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Load environment variables
if [ ! -f .env ]; then
    echo -e "${RED}Error: .env file not found. Please run 'make setup-env' first.${NC}"
    exit 1
fi

# Source .env file to get variables
set -a
source .env
set +a

# Default values if not set
KEYCLOAK_URL="${KEYCLOAK_URL:-http://localhost:8070}"
KEYCLOAK_ADMIN="${KEYCLOAK_ADMIN:-admin}"
KEYCLOAK_ADMIN_PASSWORD="${KEYCLOAK_ADMIN_PASSWORD:-}"
KEYCLOAK_ADMIN_CLIENT_ID="${KEYCLOAK_ADMIN_CLIENT_ID:-admin-cli}"
KEYCLOAK_REALM="${KEYCLOAK_REALM:-school-management}"
KEYCLOAK_SERVICE_CLIENT_ID="${KEYCLOAK_SERVICE_CLIENT_ID:-user-profile}"

# Validate required variables
if [ -z "$KEYCLOAK_ADMIN_PASSWORD" ]; then
    echo -e "${RED}Error: KEYCLOAK_ADMIN_PASSWORD is not set in .env file${NC}"
    exit 1
fi

echo -e "${BLUE}Retrieving client secret from Keycloak...${NC}"

# Wait for Keycloak to be ready
echo -e "${YELLOW}Waiting for Keycloak to be ready...${NC}"
MAX_RETRIES=30
RETRY_COUNT=0
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

# Get admin access token
echo -e "${YELLOW}Authenticating with Keycloak admin...${NC}"
TOKEN_RESPONSE=$(curl -s -X POST "${KEYCLOAK_URL}/realms/master/protocol/openid-connect/token" \
    -H "Content-Type: application/x-www-form-urlencoded" \
    -d "username=${KEYCLOAK_ADMIN}" \
    -d "password=${KEYCLOAK_ADMIN_PASSWORD}" \
    -d "grant_type=password" \
    -d "client_id=${KEYCLOAK_ADMIN_CLIENT_ID}")

ACCESS_TOKEN=$(echo "$TOKEN_RESPONSE" | grep -o '"access_token":"[^"]*' | cut -d'"' -f4)

if [ -z "$ACCESS_TOKEN" ]; then
    echo -e "${RED}Error: Failed to get admin access token${NC}"
    echo "Response: $TOKEN_RESPONSE"
    exit 1
fi

echo -e "${GREEN}Admin token obtained${NC}"

# Get realm clients
echo -e "${YELLOW}Finding client '${KEYCLOAK_SERVICE_CLIENT_ID}' in realm '${KEYCLOAK_REALM}'...${NC}"
CLIENTS_RESPONSE=$(curl -s -X GET "${KEYCLOAK_URL}/admin/realms/${KEYCLOAK_REALM}/clients" \
    -H "Authorization: Bearer ${ACCESS_TOKEN}" \
    -H "Content-Type: application/json")

# Check if response is valid JSON and contains clients
if ! echo "$CLIENTS_RESPONSE" | grep -q "\"clientId\""; then
    echo -e "${RED}Error: Invalid response from Keycloak API${NC}"
    echo "Response: $CLIENTS_RESPONSE"
    exit 1
fi

# Find the client by clientId and extract its UUID
# Try using jq if available (most reliable)
if command -v jq > /dev/null 2>&1; then
    CLIENT_UUID=$(echo "$CLIENTS_RESPONSE" | jq -r ".[] | select(.clientId == \"${KEYCLOAK_SERVICE_CLIENT_ID}\") | .id" 2>/dev/null || echo "")
fi

# Fallback: use grep to find client block and extract UUID
if [ -z "$CLIENT_UUID" ]; then
    # Find the JSON object containing our clientId
    # Look backwards from clientId to find the id field in the same object
    CLIENT_BLOCK=$(echo "$CLIENTS_RESPONSE" | grep -B50 "\"clientId\":\"${KEYCLOAK_SERVICE_CLIENT_ID}\"" | grep "\"id\":" | tail -1)
    if [ -n "$CLIENT_BLOCK" ]; then
        CLIENT_UUID=$(echo "$CLIENT_BLOCK" | grep -o "\"id\":\"[^\"]*" | cut -d'"' -f4)
    fi
fi

if [ -z "$CLIENT_UUID" ]; then
    echo -e "${RED}Error: Client '${KEYCLOAK_SERVICE_CLIENT_ID}' not found in realm '${KEYCLOAK_REALM}'${NC}"
    echo "Available clients:"
    echo "$CLIENTS_RESPONSE" | grep -o "\"clientId\":\"[^\"]*" | cut -d'"' -f4 | sed 's/^/  - /'
    exit 1
fi

echo -e "${GREEN}Found client with UUID: ${CLIENT_UUID}${NC}"

# Get client secret from credentials
echo -e "${YELLOW}Retrieving client secret...${NC}"
SECRET_RESPONSE=$(curl -s -X GET "${KEYCLOAK_URL}/admin/realms/${KEYCLOAK_REALM}/clients/${CLIENT_UUID}/client-secret" \
    -H "Authorization: Bearer ${ACCESS_TOKEN}" \
    -H "Content-Type: application/json")

CLIENT_SECRET=$(echo "$SECRET_RESPONSE" | grep -o '"value":"[^"]*' | cut -d'"' -f4)

if [ -z "$CLIENT_SECRET" ]; then
    echo -e "${RED}Error: Failed to retrieve client secret${NC}"
    echo "Response: $SECRET_RESPONSE"
    exit 1
fi

echo -e "${GREEN}Client secret retrieved successfully${NC}"

# Update .env file
echo -e "${YELLOW}Updating .env file...${NC}"
ENV_FILE=".env"

# Check if KEYCLOAK_SERVICE_CLIENT_SECRET already exists in .env
if grep -q "^KEYCLOAK_SERVICE_CLIENT_SECRET=" "$ENV_FILE"; then
    # Update existing entry
    if [[ "$OSTYPE" == "darwin"* ]]; then
        # macOS
        sed -i '' "s|^KEYCLOAK_SERVICE_CLIENT_SECRET=.*|KEYCLOAK_SERVICE_CLIENT_SECRET=${CLIENT_SECRET}|" "$ENV_FILE"
    else
        # Linux
        sed -i "s|^KEYCLOAK_SERVICE_CLIENT_SECRET=.*|KEYCLOAK_SERVICE_CLIENT_SECRET=${CLIENT_SECRET}|" "$ENV_FILE"
    fi
else
    # Append new entry
    echo "KEYCLOAK_SERVICE_CLIENT_SECRET=${CLIENT_SECRET}" >> "$ENV_FILE"
fi

# Also update docker/.env if it exists
if [ -f "docker/.env" ]; then
    if grep -q "^KEYCLOAK_SERVICE_CLIENT_SECRET=" "docker/.env"; then
        if [[ "$OSTYPE" == "darwin"* ]]; then
            sed -i '' "s|^KEYCLOAK_SERVICE_CLIENT_SECRET=.*|KEYCLOAK_SERVICE_CLIENT_SECRET=${CLIENT_SECRET}|" "docker/.env"
        else
            sed -i "s|^KEYCLOAK_SERVICE_CLIENT_SECRET=.*|KEYCLOAK_SERVICE_CLIENT_SECRET=${CLIENT_SECRET}|" "docker/.env"
        fi
    else
        echo "KEYCLOAK_SERVICE_CLIENT_SECRET=${CLIENT_SECRET}" >> "docker/.env"
    fi
    echo -e "${GREEN}Updated docker/.env file${NC}"
fi

echo -e "${GREEN}======================================${NC}"
echo -e "${GREEN}Client secret updated successfully!${NC}"
echo -e "${GREEN}======================================${NC}"
echo -e "${BLUE}Client ID: ${KEYCLOAK_SERVICE_CLIENT_ID}${NC}"
echo -e "${BLUE}Secret updated in .env file${NC}"

