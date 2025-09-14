#!/bin/bash

# School Management System Deployment Script
# Usage: ./deploy.sh [environment] [version]
# Example: ./deploy.sh development v1.0.0

set -e

ENVIRONMENT=${1:-development}
VERSION=${2:-latest}
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../../.." && pwd)"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Validate environment
validate_environment() {
    case $ENVIRONMENT in
        local|development|staging|production)
            log_info "Deploying to environment: $ENVIRONMENT"
            ;;
        *)
            log_error "Invalid environment: $ENVIRONMENT"
            log_info "Valid environments: local, development, staging, production"
            exit 1
            ;;
    esac
}

# Build all services
build_services() {
    log_info "Building all services..."
    cd "$PROJECT_ROOT"
    
    if ./mvnw clean package -DskipTests; then
        log_success "All services built successfully"
    else
        log_error "Build failed"
        exit 1
    fi
}

# Deploy to local environment
deploy_local() {
    log_info "Deploying to local environment using Docker Compose..."
    cd "$PROJECT_ROOT/deployment/local"
    
    # Stop existing containers
    docker-compose down --remove-orphans
    
    # Build and start services
    docker-compose up --build -d
    
    log_success "Local deployment completed"
    log_info "Services are starting up. Check status with: docker-compose ps"
    log_info "Gateway available at: http://localhost:8080"
    log_info "Discovery Server available at: http://localhost:8761"
}

# Deploy to cloud environments
deploy_cloud() {
    log_info "Deploying to $ENVIRONMENT environment..."
    
    case $ENVIRONMENT in
        development|staging|production)
            if command -v kubectl &> /dev/null; then
                deploy_kubernetes
            else
                log_warning "kubectl not found, falling back to Docker Compose"
                deploy_docker_compose
            fi
            ;;
    esac
}

# Deploy using Kubernetes
deploy_kubernetes() {
    log_info "Deploying using Kubernetes..."
    
    KUBE_DIR="$PROJECT_ROOT/deployment/environments/$ENVIRONMENT/kubernetes"
    
    if [ ! -d "$KUBE_DIR" ]; then
        log_error "Kubernetes manifests not found for environment: $ENVIRONMENT"
        exit 1
    fi
    
    # Apply namespace
    kubectl apply -f "$KUBE_DIR/namespace.yml"
    
    # Deploy infrastructure services first
    kubectl apply -f "$KUBE_DIR/infrastructure/"
    
    # Wait for infrastructure services to be ready
    log_info "Waiting for infrastructure services to be ready..."
    sleep 30
    
    # Deploy business services
    kubectl apply -f "$KUBE_DIR/business-services/"
    
    # Deploy platform services
    kubectl apply -f "$KUBE_DIR/platform-services/"
    
    # Apply ingress
    kubectl apply -f "$KUBE_DIR/ingress/"
    
    log_success "Kubernetes deployment completed"
}

# Deploy using Docker Compose for cloud environments
deploy_docker_compose() {
    log_info "Deploying using Docker Compose..."
    
    COMPOSE_FILE="$PROJECT_ROOT/deployment/environments/$ENVIRONMENT/docker-compose.$ENVIRONMENT.yml"
    
    if [ ! -f "$COMPOSE_FILE" ]; then
        log_error "Docker Compose file not found: $COMPOSE_FILE"
        exit 1
    fi
    
    docker-compose -f "$COMPOSE_FILE" down --remove-orphans
    docker-compose -f "$COMPOSE_FILE" up --build -d
    
    log_success "Docker Compose deployment completed"
}

# Health check
health_check() {
    log_info "Performing health check..."
    
    local gateway_url
    case $ENVIRONMENT in
        local)
            gateway_url="http://localhost:8080"
            ;;
        development)
            gateway_url="http://dev-gateway.schoolmanagement.com"
            ;;
        staging)
            gateway_url="http://staging-gateway.schoolmanagement.com"
            ;;
        production)
            gateway_url="http://gateway.schoolmanagement.com"
            ;;
    esac
    
    # Wait for services to start
    sleep 60
    
    if curl -f "$gateway_url/actuator/health" &> /dev/null; then
        log_success "Health check passed - Gateway is responding"
    else
        log_warning "Health check failed - Gateway may still be starting up"
        log_info "Check service logs for more information"
    fi
}

# Main deployment flow
main() {
    log_info "Starting deployment process..."
    log_info "Environment: $ENVIRONMENT"
    log_info "Version: $VERSION"
    
    validate_environment
    build_services
    
    if [ "$ENVIRONMENT" = "local" ]; then
        deploy_local
    else
        deploy_cloud
    fi
    
    health_check
    
    log_success "Deployment completed successfully!"
}

# Run main function
main "$@"
