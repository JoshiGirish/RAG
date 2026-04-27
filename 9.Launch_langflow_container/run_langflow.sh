#!/bin/bash
# Script to launch LangFlow container using podman

# Run the container with specified parameters
podman run -p 7860:7860 --network=host -e OPENAI_API_BASE=http://127.0.0.1:8000/v1 -e OPENAI_API_KEY=dummy langflowai/langflow:latest