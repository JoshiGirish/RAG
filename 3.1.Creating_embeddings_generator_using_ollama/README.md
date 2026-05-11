# Build the lightweight image
podman build --no-cache -t ollama-base .

# Run the container with a persistent volume and pull models after it starts
podman run -d \
    -p 11434:11434 \
    --name ollama-container \
    -e OLLAMA_HOST=0.0.0.0:11434 \
    -v ollama-data:/root/.ollama \
    ollama-base

# Pull the models manually (this is much more reliable because you can retry easily):

  ## Pull LLM (better for tool calling)
podman exec ollama-container ollama pull qwen2.5:1.5b

  ## Pull embedding model
podman exec ollama-container ollama pull nomic-embed-text

# Test Ollama
curl http://localhost:11434/api/generate \
  -d '{
    "model": "gemma-4-E4b-it.Q4_K_M.gguf",
    "prompt": "Who discovered gravity?",
    "stream": false
  }'
  
