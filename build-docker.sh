#!/bin/bash
set -e

# Configuration
IMAGE_NAME="mini-htmx"
IMAGE_TAG="latest"
FULL_IMAGE_NAME="${IMAGE_NAME}:${IMAGE_TAG}"

echo "========================================="
echo "Building Mini HTMX Docker Image"
echo "========================================="

# Step 1: Build the uberjar
echo ""
echo "Step 1: Building uberjar..."
clojure -T:build uber

# Check if uberjar was created
if [ ! -f "target/mini-htmx-0.1.0-standalone.jar" ]; then
    echo "Error: Uberjar not found at target/mini-htmx-0.1.0-standalone.jar"
    exit 1
fi

echo "✓ Uberjar built successfully"

# Step 2: Build the Docker image
echo ""
echo "Step 2: Building Docker image..."
docker build -t "${FULL_IMAGE_NAME}" .

echo "✓ Docker image built successfully"

# Step 3: Display information
echo ""
echo "========================================="
echo "Build Complete!"
echo "========================================="
echo ""
echo "Docker image: ${FULL_IMAGE_NAME}"
echo ""
echo "To run the container:"
echo "  docker run -p 8181:8181 ${FULL_IMAGE_NAME}"
echo ""
echo "To run on a different port (e.g., 3000):"
echo "  docker run -p 3000:8181 ${FULL_IMAGE_NAME}"
echo ""
echo "To run in detached mode:"
echo "  docker run -d -p 8181:8181 --name mini-htmx ${FULL_IMAGE_NAME}"
echo ""
