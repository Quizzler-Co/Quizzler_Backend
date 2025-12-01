#!/bin/bash

echo "=========================================="
echo "Starting Quizzler Backend Services"
echo "=========================================="
echo ""

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker Desktop."
    exit 1
fi

echo "✅ Docker is running"
echo ""

# Start all services
echo "🚀 Building and starting all services..."
echo "This may take a few minutes on first run..."
echo ""

docker-compose up --build

