setup-env: ## Copy .env.example to .env in the project root and docker/.env
	@if [ -f .env.example ]; then \
		cp .env.example .env; \
		cp .env.example docker/.env; \
		echo "Copied .env.example to .env and docker/.env. Please edit these files with your actual secrets and configuration."; \
	else \
		echo "No .env.example file found. Please create one with your required environment variables."; \
	fi
.PHONY: help docker-up docker-down docker-restart docker-logs docker-ps docker-clean \
        build build-clean test coverage \
        run-config run-discovery run-gateway \
        run-user run-academic run-attendance run-assessment run-search run-audit run-notification \
        run-all-services stop-all \
        setup setup-keycloak \
        clean clean-all \
        quick-start quick-start-user

# Variables
DOCKER_COMPOSE = docker compose -f docker/docker-compose.yml
MVN = mvn
SERVICES_DIR = services
PLATFORM_DIR = platform

# Colors for output
RED = \033[0;31m
GREEN = \033[0;32m
YELLOW = \033[1;33m
BLUE = \033[0;34m
NC = \033[0m # No Color

##@ Help
help: ## Display this help
	@echo "$(BLUE)School Management System - Makefile Commands$(NC)"
	@echo ""
	@awk 'BEGIN {FS = ":.*##"; printf "\nUsage:\n  make $(YELLOW)<target>$(NC)\n"} /^[a-zA-Z_-]+:.*?##/ { printf "  $(YELLOW)%-25s$(NC) %s\n", $$1, $$2 } /^##@/ { printf "\n$(BLUE)%s$(NC)\n", substr($$0, 5) } ' $(MAKEFILE_LIST)

##@ Docker Infrastructure
docker-up: ## Start all Docker infrastructure services (Postgres, RabbitMQ, Elasticsearch, Keycloak, MinIO)
	@if [ ! -f .env ] || [ ! -f docker/.env ]; then \
    echo "Missing .env or docker/.env. Running setup-env..."; \
    make setup-env; \
  fi
	@echo "$(GREEN)Starting Docker infrastructure...$(NC)"
	$(DOCKER_COMPOSE) up -d
	@echo "$(GREEN)Docker infrastructure started successfully!$(NC)"
	@echo "$(YELLOW)Waiting for services to be healthy...$(NC)"
	@sleep 5
	@make docker-ps

docker-down: ## Stop all Docker infrastructure services
	@echo "$(RED)Stopping Docker infrastructure...$(NC)"
	$(DOCKER_COMPOSE) down

docker-restart: ## Restart all Docker infrastructure services
	@echo "$(YELLOW)Restarting Docker infrastructure...$(NC)"
	$(DOCKER_COMPOSE) restart

docker-logs: ## Show logs from Docker services
	$(DOCKER_COMPOSE) logs -f

docker-ps: ## Show status of Docker services
	@echo "$(BLUE)Docker Services Status:$(NC)"
	$(DOCKER_COMPOSE) ps

docker-clean: ## Stop and remove all Docker containers, volumes, and networks
	@echo "$(RED)Cleaning up Docker infrastructure...$(NC)"
	$(DOCKER_COMPOSE) down -v --remove-orphans
	@echo "$(GREEN)Docker cleanup completed!$(NC)"

##@ Build
build: ## Build all services using Maven
	@echo "$(GREEN)Building all services (skipping test compilation)...$(NC)"
	$(MVN) clean install -Dmaven.test.skip=true
	@echo "$(GREEN)Build completed successfully!$(NC)"

build-clean: ## Clean build all services
	@echo "$(GREEN)Clean building all services...$(NC)"
	$(MVN) clean install
	@echo "$(GREEN)Clean build completed successfully!$(NC)"

test: ## Run all tests
	@echo "$(GREEN)Running all tests...$(NC)"
	$(MVN) test

coverage: ## Generate test coverage reports
	@echo "$(GREEN)Generating coverage reports...$(NC)"
	@bash scripts/generate-coverage-reports.sh

##@ Platform Services
run-config: ## Run Config Server
	@echo "$(GREEN)Starting Config Server...$(NC)"
	cd $(PLATFORM_DIR)/config-server && bash -c 'source ../../.env 2>/dev/null || true; mvn spring-boot:run'

run-discovery: ## Run Discovery Server (Eureka)
	@echo "$(GREEN)Starting Discovery Server...$(NC)"
	cd $(PLATFORM_DIR)/discovery-server && bash -c 'source ../../.env 2>/dev/null || true; mvn spring-boot:run'

run-gateway: ## Run API Gateway
	@echo "$(GREEN)Starting API Gateway...$(NC)"
	cd $(PLATFORM_DIR)/api-gateway && bash -c 'source ../../.env 2>/dev/null || true; mvn spring-boot:run'

##@ Business Services
run-user: ## Run User Service
	@echo "$(GREEN)Starting User Service...$(NC)"
	cd $(SERVICES_DIR)/user-service && bash -c 'source ../../.env 2>/dev/null || true; mvn spring-boot:run'

run-academic: ## Run Academic Service
	@echo "$(GREEN)Starting Academic Service...$(NC)"
	cd $(SERVICES_DIR)/academic-service && bash -c 'source ../../.env 2>/dev/null || true; mvn spring-boot:run'

run-attendance: ## Run Attendance Service
	@echo "$(GREEN)Starting Attendance Service...$(NC)"
	cd $(SERVICES_DIR)/attendance-service && bash -c 'source ../../.env 2>/dev/null || true; mvn spring-boot:run'

run-assessment: ## Run Academic Assessment Service
	@echo "$(GREEN)Starting Academic Assessment Service...$(NC)"
	cd $(SERVICES_DIR)/academic-assessment-service && bash -c 'source ../../.env 2>/dev/null || true; mvn spring-boot:run'

run-search: ## Run Search Service
	@echo "$(GREEN)Starting Search Service...$(NC)"
	cd $(SERVICES_DIR)/search-service && bash -c 'source ../../.env 2>/dev/null || true; mvn spring-boot:run'

run-audit: ## Run Audit Service
	@echo "$(GREEN)Starting Audit Service...$(NC)"
	cd $(SERVICES_DIR)/audit-service && bash -c 'source ../../.env 2>/dev/null || true; mvn spring-boot:run'

run-notification: ## Run Notification Service
	@echo "$(GREEN)Starting Notification Service...$(NC)"
	cd $(SERVICES_DIR)/notification-service && bash -c 'source ../../.env 2>/dev/null || true; mvn spring-boot:run'

##@ Run All
run-all-services: ## Run all services (Config -> Discovery -> Gateway -> Business Services) - Use separate terminals
	@echo "$(YELLOW)Note: This will attempt to run all services sequentially.$(NC)"
	@echo "$(YELLOW)For production use, run services in separate terminals or use a process manager.$(NC)"
	@echo ""
	@echo "$(GREEN)Recommended startup order:$(NC)"
	@echo "  1. make docker-up"
	@echo "  2. make run-config (in terminal 1)"
	@echo "  3. make run-discovery (in terminal 2)"
	@echo "  4. make run-gateway (in terminal 3)"
	@echo "  5. make run-<service> (in separate terminals)"
	@echo ""
	@echo "$(RED)Press Ctrl+C to cancel or any key to continue...$(NC)"
	@read -n 1

##@ Setup
setup: ## Initial setup - Install dependencies and prepare environment
	@echo "$(GREEN)Running initial setup...$(NC)"
	@bash scripts/setup.sh
	@echo "$(GREEN)Setup completed!$(NC)"

setup-keycloak: ## Setup Keycloak realm and clients
	@echo "$(GREEN)Setting up Keycloak...$(NC)"
	@bash scripts/setup-keycloak.sh
	@echo "$(GREEN)Keycloak setup completed!$(NC)"

##@ Clean
clean: ## Clean build artifacts
	@echo "$(YELLOW)Cleaning build artifacts...$(NC)"
	$(MVN) clean
	@echo "$(GREEN)Clean completed!$(NC)"

clean-all: docker-clean clean ## Clean everything (Docker + build artifacts)
	@echo "$(GREEN)Full cleanup completed!$(NC)"

##@ Quick Start

quick-start: docker-up
	@echo "$(GREEN)Building all services (skipping test compilation)...$(NC)"
	mvn clean install -Dmaven.test.skip=true
	@echo ""
	@echo "$(GREEN)======================================$(NC)"
	@echo "$(GREEN)Quick start completed!$(NC)"
	@echo "$(GREEN)======================================$(NC)"
	@echo ""
	@echo "$(BLUE)Next steps:$(NC)"
	@echo "  1. Run Config Server:    $(YELLOW)make run-config$(NC)"
	@echo "  2. Run Discovery Server: $(YELLOW)make run-discovery$(NC)"
	@echo "  3. Run API Gateway:      $(YELLOW)make run-gateway$(NC)"
	@echo "  4. Run other services:   $(YELLOW)make run-<service>$(NC)"
	@echo ""
	@echo "$(BLUE)Access points:$(NC)"
	@echo "  - Postgres:       localhost:6432"
	@echo "  - RabbitMQ:       localhost:15672 (admin/admin)"
	@echo "  - Elasticsearch:  localhost:9200"
	@echo "  - Keycloak:       localhost:8070"
	@echo "  - MinIO:          localhost:9001"
	@echo ""

quick-start-user: ## Quick start: Start infra, init Keycloak, get client secret, and restart user-service
	@bash scripts/quick-start-with-keycloak.sh

##@ Development
dev-config: docker-up ## Start infrastructure and Config Server
	@make run-config

dev-full: docker-up ## Start infrastructure and platform services (requires tmux or multiple terminals)
	@echo "$(YELLOW)Starting development environment...$(NC)"
	@echo "$(YELLOW)Please run these commands in separate terminals:$(NC)"
	@echo "  Terminal 1: $(GREEN)make run-config$(NC)"
	@echo "  Terminal 2: $(GREEN)make run-discovery$(NC)"
	@echo "  Terminal 3: $(GREEN)make run-gateway$(NC)"
