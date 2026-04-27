#!/bin/bash
# Script to launch the Docling container.
# This script requires 'podman' to be installed and running.

# Exit immediately if a command exits with a non-zero status.
set -e

# Define constants for clarity
IMAGE="ghcr.io/docling-project/docling-serve"
CONTAINER_PORT="5001"
CACHE_VOLUME="docling_cache"

echo "Starting Docling container..."

# Run the container with specified parameters
podman run \
  -p "${CONTAINER_PORT}:${CONTAINER_PORT}" \
  -e DOCLING_SERVE_ENABLE_UI=1 \
  -v ${CACHE_VOLUME}:/root/.cache \
  "${IMAGE}"

echo "Docling container process finished."
