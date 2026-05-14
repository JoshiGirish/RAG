#!/bin/bash
#
# run_unstructured.sh
# Script to launch the Unstructured parser using official pre-built container
#
# Usage: ./run_unstructured.sh [OPTIONS]
#
# Options:
#   --port PORT    Port to expose (default: 8000)
#   --host HOST    Host to bind to (default: 0.0.0.0)
#   --volume VOLUME Volume mount path (default: ./data)
#

set -e

# Default values
PORT=${PORT:-8000}
HOST=${HOST:-0.0.0.0}
MODEL=${MODEL:-llama3}
VOLUME=${VOLUME:-./data}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --port)
            PORT="$2"
            shift 2
            ;;
        --host)
            HOST="$2"
            shift 2
            ;;
        --model)
            MODEL="$2"
            shift 2
            ;;
        --volume)
            VOLUME="$2"
            shift 2
            ;;
        *)
            echo "Unknown option: $1"
            echo "Usage: $0 [--port PORT] [--host HOST] [--model MODEL] [--volume VOLUME]"
            exit 1
            ;;
    esac
done

# Create volume directory if it doesn't exist
mkdir -p "$VOLUME"

# Set environment variable for the model
export UNSTRUCTURED_LANGUAGE_MODEL="$MODEL"

# Define official container image
CONTAINER_NAME="unstructured-parser"
OFFICIAL_IMAGE="downloads.unstructured.io/unstructured-io/unstructured:latest"

echo "Using Official Unstructured Container:"
echo "  Image: $OFFICIAL_IMAGE"
echo "  Host: $HOST"
echo "  Port: $PORT"
echo "  Model: $MODEL"
echo "  Volume: $VOLUME"
echo ""

# Pull the official image if not already present
IMAGE_EXISTS=$(podman image exists "$OFFICIAL_IMAGE" 2>/dev/null && echo "yes" || echo "no")

if [ "$IMAGE_EXISTS" = "no" ]; then
    echo "Pulling official Unstructured container image..."
    podman pull "$OFFICIAL_IMAGE"
    echo ""
fi

# Run the container in detached mode
podman run -dt \
    --name "$CONTAINER_NAME" \
    --pull always \
    -p "$PORT":8000 \
    -v "$VOLUME":/app/data \
    -e UNSTRUCTURED_LANGUAGE_MODEL="$MODEL" \
    "$OFFICIAL_IMAGE"

# Wait for container to be ready
echo "Waiting for container to be ready..."
sleep 3

# Verify container is running
if podman ps --format "{{.Names}}" | grep -q "^${CONTAINER_NAME}$"; then
    echo ""
    echo "========================================"
    echo "  Unstructured Container Started!"
    echo "========================================"
    echo ""
    echo "Container Name: $CONTAINER_NAME"
    echo ""
    echo "Access the service at: http://localhost:$PORT"
    echo ""
    echo "Available endpoints:"
    echo "  GET  /                       - Health check"
    echo "  GET  /supported-formats      - List supported formats"
    echo "  POST /parse                  - Parse a document"
    echo "  POST /parse-raw              - Parse and return raw text"
    echo "  GET  /models                 - List available models"
    echo ""
    echo "Example curl requests:"
    echo "  curl http://localhost:$PORT/"
    echo "  curl -X POST -F \"file=@your_doc.pdf\" http://localhost:$PORT/parse"
    echo ""
    echo "========================================"
    echo "Container Management Commands:"
    echo "========================================"
    echo ""
    echo "To check container status:"
    echo "  podman ps --filter name=$CONTAINER_NAME"
    echo ""
    echo "To stop the container:"
    echo "  podman stop $CONTAINER_NAME"
    echo ""
    echo "To remove the container:"
    echo "  podman rm $CONTAINER_NAME"
    echo ""
    echo "To view logs:"
    echo "  podman logs $CONTAINER_NAME"
    echo ""
    echo "To restart the container:"
    echo "  podman restart $CONTAINER_NAME"
    echo ""
    echo "========================================"
else
    echo "Error: Container failed to start"
    podman logs "$CONTAINER_NAME"
    exit 1
fi
