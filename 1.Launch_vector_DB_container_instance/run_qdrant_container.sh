#!/bin/bash
#
# run_qdrant_container.sh
# Run Qdrant vector database container with persistent storage
#

set -euo pipefail

# Configuration with environment variable overrides
QDRANT_PORT="${QDRANT_PORT:-6333}"
QDRANT_GRPC_PORT="${QDRANT_GRPC_PORT:-6334}"
QDRANT_STORAGE_DIR="${QDRANT_STORAGE_DIR:-$(pwd)/qdrant_storage}"
QDRANT_CONTAINER_NAME="${QDRANT_CONTAINER_NAME:-qdrant}"

# Print usage information
usage() {
    cat << EOF
Usage: $(basename "$0") [OPTIONS]

Run Qdrant vector database container.

Options:
    -p, --port PORT        HTTP port (default: 6333)
    -g, --grpc-port PORT   gRPC port (default: 6334)
    -s, --storage PATH     Storage directory (default: $(pwd)/qdrant_storage)
    -h, --help             Show this help message

Environment Variables:
    QDRANT_PORT            HTTP port (default: 6333)
    QDRANT_GRPC_PORT       gRPC port (default: 6334)
    QDRANT_STORAGE_DIR     Storage directory (default: $(pwd)/qdrant_storage)

Examples:
    $(basename "$0")                    # Run with defaults
    $(basename "$0") --port 8080        # Run on custom HTTP port
    $(basename "$0") -s /data/qdrant    # Use custom storage path
EOF
    exit 0
}

# Parse command-line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -p|--port)
            QDRANT_PORT="$2"
            shift 2
            ;;
        -g|--grpc-port)
            QDRANT_GRPC_PORT="$2"
            shift 2
            ;;
        -s|--storage)
            QDRANT_STORAGE_DIR="$2"
            shift 2
            ;;
        -h|--help)
            usage
            ;;
        *)
            echo "Error: Unknown option: $1" >&2
            echo "Use --help for usage information" >&2
            exit 1
            ;;
    esac
done

# Verify prerequisites
check_prerequisites() {
    local missing_tools=()
    
    if ! command -v podman &> /dev/null; then
        missing_tools+=("podman")
    fi
    
    if [[ ${#missing_tools[@]} -gt 0 ]]; then
        echo "Error: Missing required tools: ${missing_tools[*]}" >&2
        exit 1
    fi
}

# Check for port conflicts
check_port_conflicts() {
    local port_in_use=false
    local port_desc=""
    
    for port in "$QDRANT_PORT" "$QDRANT_GRPC_PORT"; do
        if ss -tuln 2>/dev/null | grep -q ":$port "; then
            port_in_use=true
            port_desc="${port_desc}port $port, "
        fi
    done
    
    if [[ "$port_in_use" == "true" ]]; then
        echo "Warning: The following ports are already in use: ${port_desc%/}" >&2
        read -p "Do you want to proceed anyway? (y/n) " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            exit 1
        fi
    fi
}

# Main function
main() {
    check_prerequisites
    check_port_conflicts
    
    # Create storage directory if it doesn't exist
    mkdir -p "$QDRANT_STORAGE_DIR"
    
    echo "Starting Qdrant vector database..."
    echo "  HTTP Port:   $QDRANT_PORT"
    echo "  gRPC Port:   $QDRANT_GRPC_PORT"
    echo "  Storage:     $QDRANT_STORAGE_DIR"
    echo ""
    
    # Pull the latest image (silently)
    podman pull docker.io/qdrant/qdrant --quiet 2>/dev/null || true
    
    # Start container with mounted storage
    CONTAINER_ID=$(podman run -d \
        -p "$QDRANT_PORT":6333 \
        -p "$QDRANT_GRPC_PORT":6334 \
        -v "$QDRANT_STORAGE_DIR:/qdrant/storage:z" \
        --name "$QDRANT_CONTAINER_NAME" \
        qdrant/qdrant)
    
    echo "Container started with ID: $CONTAINER_ID"
    echo ""
    echo "Qdrant is now running!"
    echo "  HTTP API:     http://localhost:$QDRANT_PORT"
    echo "  gRPC API:     localhost:$QDRANT_GRPC_PORT"
    echo "  Health Check: curl http://localhost:$QDRANT_PORT/health"
    echo ""
}

main "$@"
