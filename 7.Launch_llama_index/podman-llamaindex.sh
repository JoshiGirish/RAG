#!/bin/bash
#
# podman-llamaindex.sh
# Pulls the official llamaIndex container image and runs it with default configuration
#
# Usage: ./podman-llamaindex.sh [command]
#
# Commands:
#   pull    - Pull the latest llamaIndex image (default)
#   run     - Pull if needed and run the container
#   stop    - Stop the running container
#   cleanup - Stop and remove all llamaIndex containers and volumes
#
# Default Configuration:
#   - Image: quay.io/llamaindex/llamaindex:latest
#   - Container name: llamaindex
#   - Port: 8000 (host) -> 8000 (container)
#   - Working directory: /app
#   - Environment: LLM_API_BASE=http://localhost:8080 (llama.cpp default)
#   - llama.cpp serves models on port 8080
#
set -e

# ============================================================================
# Configuration
# ============================================================================
readonly IMAGE_NAME="quay.io/llamaindex/llamaindex"
readonly IMAGE_TAG="latest"
readonly CONTAINER_NAME="llamaindex"
readonly DEFAULT_PORT="8000"
readonly LLM_API_BASE="http://localhost:8080"

# ============================================================================
# Functions
# ============================================================================

# Print usage information
usage() {
    cat << EOF
Usage: $0 [command]

Commands:
  pull    - Pull the latest llamaIndex image (default)
  run     - Pull if needed and run the container
  stop    - Stop the running container
  cleanup - Stop and remove all llamaIndex containers and volumes

Default Configuration:
  Image:     $IMAGE_NAME:$IMAGE_TAG
  Container: $CONTAINER_NAME
  Port:      $DEFAULT_PORT
  LLM API:   $LLM_API_BASE
  
  llama.cpp Compatibility:
  - llama.cpp serves models on port 8080
  - Set LLM_API_BASE=http://host:8080 to connect
  - Example: ./podman-llamaindex.sh run --env LLM_API_BASE=http://localhost:8080
EOF
    exit 1
}

# Pull the llamaIndex image
pull_image() {
    echo "Pulling $IMAGE_NAME:$IMAGE_TAG..."
    podman pull "$IMAGE_NAME:$IMAGE_TAG"
    echo "Image pulled successfully."
}

# Run the llamaIndex container
run_container() {
    # Pull image if not present
    if ! podman image inspect "$IMAGE_NAME:$IMAGE_TAG" &>/dev/null; then
        echo "Image not found. Pulling..."
        pull_image
    fi

    # Check if container is already running
    if podman ps --format "{{.Names}}" | grep -q "^${CONTAINER_NAME}$"; then
        echo "Container '$CONTAINER_NAME' is already running."
        podman ps -a --filter "name=$CONTAINER_NAME"
        exit 0
    fi

    # Add llama.cpp environment variables
    env_vars="--env LLM_API_BASE=\"\$LLM_API_BASE\" --env LLM_API_KEY=\"\" --env LLM_MODEL_PATH=\"\""

    # Build the run command
    podman run -d \
        --name "$CONTAINER_NAME" \
        -p "$DEFAULT_PORT:$DEFAULT_PORT" \
        $env_vars \
        --restart unless-stopped \
        "$IMAGE_NAME:$IMAGE_TAG"

    echo "Container '$CONTAINER_NAME' started successfully."
    echo "Access the service at: http://localhost:$DEFAULT_PORT"
    echo ""
    echo "llama.cpp Connection Info:"
    echo "  - API Base: $LLM_API_BASE"
    echo "  - To test: curl -X POST \$LLM_API_BASE/v1/chat/completions -H 'Content-Type: application/json' -d '{\"model\":\"default\",\"messages\":[{\"role\":\"user\",\"content\":\"Hello!\"}]}'"

}

# Stop the container
stop_container() {
    if podman ps --format "{{.Names}}" | grep -q "^${CONTAINER_NAME}$"; then
        podman stop "$CONTAINER_NAME" 2>/dev/null || true
        podman rm "$CONTAINER_NAME" 2>/dev/null || true
        echo "Container stopped and removed."
    else
        echo "Container '$CONTAINER_NAME' is not running."
    fi
}

# Cleanup all llamaIndex containers and volumes
cleanup() {
    echo "Cleaning up llamaIndex resources..."
    
    # Stop and remove containers
    podman rm "$CONTAINER_NAME" 2>/dev/null || true
    
    # Remove associated volumes
    podman volume rm llamaindex_* 2>/dev/null || true
    
    echo "Cleanup complete."
}

# ============================================================================
# Main
# ============================================================================

# Default command is 'pull'
COMMAND="${1:-pull}"

case "$COMMAND" in
    pull)
        pull_image
        ;;
    run)
        run_container
        ;;
    stop)
        stop_container
        ;;
    cleanup)
        cleanup
        ;;
    *)
        usage
        ;;
esac