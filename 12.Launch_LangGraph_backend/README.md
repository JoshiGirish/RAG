# FastAPI LangGraph Chatbot

A lightweight chatbot application built with FastAPI and LangGraph that provides a simple API for interacting with an LLM model.

## Features

- 🚀 **FastAPI Backend**: Modern, async web framework
- 🔄 **LangGraph Integration**: Graph-based workflow orchestration
- 💬 **REST API**: Simple endpoints for chat interactions
- 📦 **Docker Ready**: Easy deployment with containerization
- 🔌 **LLM Compatible**: Works with various LLM backends

## Quick Start

### Local Development

```bash
# Install dependencies
pip install -r requirements.txt

# Run the application
uvicorn app:app --reload --host 0.0.0.0 --port 8000
```

### Using Docker

```bash
# Build and run with Docker
docker build -t langgraph-chatbot .

# Or use docker-compose
docker-compose up -d
```

### API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/` | GET | Health check - returns `{"status": "running"}` |
| `/chat` | GET | Process a chat message through the graph |

## API Usage

### Health Check

```bash
curl http://localhost:8000/

# Response:
# {"status": "running"}
```

### Chat Endpoint

```bash
curl "http://localhost:8000/chat?msg=Hello, how are you?"

# Response:
# {"output": "LLM response here..."}
```

### Python Example

```python
import requests

response = requests.get(
    "http://localhost:8000/chat",
    params={"msg": "What is LangGraph?"}
)
print(response.json())
```

## Architecture

```
┌─────────────────────────────────────────┐
│           Client / API Caller            │
└──────────────┬──────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────┐
│           FastAPI Server                 │
│  ┌───────────────────────────────────┐  │
│  │  /  - Health Check                │  │
│  │  /chat - Graph Processing         │  │
│  └───────────────────────────────────┘  │
└──────────────┬──────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────┐
│           LangGraph Workflow             │
│  ┌───────────────────────────────────┐  │
│  │  Entry Point                      │  │
│  │  └─> call_model (LLM Node)       │  │
│  │         └─> END                   │  │
│  └───────────────────────────────────┘  │
└─────────────────────────────────────────┘
```

## Configuration

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `PORT` | 8000 | Server port |
| `HOST` | 0.0.0.0 | Host to bind to |
| `LLM_MODEL` | Qwen3.5-9B.Q4_K_M.gguf | Model to use |
| `LLM_BASE_URL` | http://localhost:8080/v1 | LLM API URL |
| `LLM_TEMPERATURE` | 0 | Creativity parameter |

### Docker Configuration

```bash
# Override environment variables
docker run -p 8000:8000 \
  -e PORT=8000 \
  -e HOST=0.0.0.0 \
  langgraph-chatbot
```

## Project Structure

```
.
├── app.py              # FastAPI application with LangGraph
├── requirements.txt    # Python dependencies
├── Dockerfile          # Container build instructions
├── docker-compose.yml  # Orchestration configuration
└── README.md          # This file
```

## Dependencies

- **FastAPI**: Modern web framework
- **LangGraph**: Graph-based workflow orchestration
- **LangChain**: LLM integration
- **Uvicorn**: ASGI server
- **DebugPy**: Debugging support

## Development Workflow

1. **Install dependencies**
   ```bash
   pip install -r requirements.txt
   ```

2. **Run locally**
   ```bash
   uvicorn app:app --reload
   ```

3. **Test the API**
   ```bash
   curl http://localhost:8000/
   curl "http://localhost:8000/chat?msg=Hello"
   ```

4. **Debug**
   ```bash
   # Set breakpoints in app.py
   # Connect with a debugger
   ```

## Troubleshooting

### Port Already in Use

```bash
# Find process using port 8000
lsof -i :8000

# Kill the process
kill <PID>
```

### Model Connection Issues

Ensure your LLM server is running:
```bash
# Check if the LLM server is accessible
curl http://localhost:8080/v1
```

### View Logs

```bash
# Docker logs
docker logs <container_name>

# Uvicorn logs
uvicorn app:app --log-level debug
```

## Resources

- [FastAPI Documentation](https://fastapi.tiangolo.com/)
- [LangGraph Documentation](https://langchain-ai.github.io/langgraph/)
- [LangChain Documentation](https://python.langchain.com/)
- [Docker Documentation](https://docs.docker.com/)

*Last updated: $(date +%Y-%m-%d)*
