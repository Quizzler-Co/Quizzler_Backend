#!/bin/bash

# Script to manually build and push all Docker images to GitHub Container Registry
# Usage: ./push-to-ghcr.sh [username] [tag]
# Example: ./push-to-ghcr.sh myusername latest

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Get GitHub username from argument or prompt
GITHUB_USERNAME=${1:-}
if [ -z "$GITHUB_USERNAME" ]; then
    echo -e "${YELLOW}Enter your GitHub username:${NC}"
    read GITHUB_USERNAME
fi

# Get tag from argument or use latest
TAG=${2:-latest}

REGISTRY="ghcr.io"
IMAGE_PREFIX="${GITHUB_USERNAME}/quizzler-backend"

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Building and Pushing to GHCR${NC}"
echo -e "${GREEN}========================================${NC}"
echo -e "Registry: ${REGISTRY}"
echo -e "Image Prefix: ${IMAGE_PREFIX}"
echo -e "Tag: ${TAG}"
echo ""

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo -e "${RED}❌ Docker is not running. Please start Docker Desktop.${NC}"
    exit 1
fi

# Login to GHCR
echo -e "${YELLOW}Logging in to GitHub Container Registry...${NC}"
echo "You need a Personal Access Token (PAT) with 'write:packages' permission"
echo "Create one at: https://github.com/settings/tokens"
echo ""
echo -n "Enter your GitHub Personal Access Token: "
read -s GITHUB_TOKEN
echo ""

echo "$GITHUB_TOKEN" | docker login ghcr.io -u "$GITHUB_USERNAME" --password-stdin

if [ $? -ne 0 ]; then
    echo -e "${RED}❌ Failed to login to GHCR${NC}"
    exit 1
fi

echo -e "${GREEN}✅ Logged in successfully${NC}"
echo ""

# Services to build and push
SERVICES=(
    "service-registry:./service-registry"
    "api-gateway:./api-gateway"
    "user-auth:./user-auth"
    "quiz-service:./Quiz"
    "question-service:./question-service"
    "participation-service:./participation-service"
    "leaderboard:./LeaderBoard"
    "java-judge:./JavaJudge"
    "bugreport-service:./BugReport-service"
)

# Build and push each service
for service_info in "${SERVICES[@]}"; do
    IFS=':' read -r service_name service_path <<< "$service_info"
    IMAGE_NAME="${REGISTRY}/${IMAGE_PREFIX}/${service_name}:${TAG}"
    
    echo -e "${YELLOW}Building ${service_name}...${NC}"
    docker build -t "$IMAGE_NAME" -f "${service_path}/Dockerfile" "${service_path}"
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ Built ${service_name}${NC}"
        echo -e "${YELLOW}Pushing ${service_name}...${NC}"
        docker push "$IMAGE_NAME"
        
        if [ $? -eq 0 ]; then
            echo -e "${GREEN}✅ Pushed ${service_name}${NC}"
        else
            echo -e "${RED}❌ Failed to push ${service_name}${NC}"
        fi
    else
        echo -e "${RED}❌ Failed to build ${service_name}${NC}"
    fi
    echo ""
done

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}All images pushed successfully!${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo "View your images at: https://github.com/${GITHUB_USERNAME}?tab=packages"




