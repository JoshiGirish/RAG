#!/bin/bash

podman run -p 8080:8080 ghcr.io/substratusai/stapi
# podman run -e MODEL=all-MiniLM-L6-v2 -p 8080:8080 -d ghcr.io/substratusai/stapi
# podman run -e MODEL=multi-qa-MiniLM-L6-cos-v1  -p 8080:8080 -d \
  # ghcr.io/substratusai/stapi