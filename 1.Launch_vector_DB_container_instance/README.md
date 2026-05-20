# Lab 1: Launch Vector DB Container Instance

## Overview
This lab demonstrates how to launch a Qdrant vector database container instance for storing and querying embeddings in a RAG system.

## Components
- **Qdrant Storage**: Persistent storage for vector embeddings
- **Container Runtime**: Docker/Podman container management
- **Collection Management**: Database schema and collection setup

## Files
- `run_qdrant_container.sh` - Script to launch the Qdrant container
- `qdrant_storage/` - Persistent storage directory for Qdrant data

## Quick Start
```bash
# Launch the container
./run_qdrant_container.sh

# Verify container is running
docker ps | grep qdrant
```

## Architecture
```
┌─────────────────┐
│   Application   │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│   Qdrant        │
│   Container     │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│   qdrant_storage │
│   (Persistent)   │
└─────────────────┘
```

## Key Concepts
- **Collections**: Logical groupings of vectors (e.g., "docDB")
- **Segments**: Internal storage units for efficient vector retrieval
- **Payloads**: Metadata associated with each vector

## Next Steps
- Connect to the Qdrant instance from your application
- Create collections for different data types
- Upload embeddings for retrieval