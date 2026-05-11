# RAG Project Overview

This repository contains code samples and infrastructure setups for implementing various components of a Retrieval-Augmented Generation (RAG) system. The project is modularized into several distinct services, each addressing a specific part of the RAG pipeline.

## Project Structure

The repository is organized by the major steps of the RAG workflow:

### 1. Data Ingestion and Vector Storage
- **`2.Create_vector_DB_client`**: Contains the Java client for interacting with the vector database (Qdrant).
- **`1.Launch_vector_DB_container_instance`**: Scripts for launching the actual vector database container.
- **`5.Push_embeddings_to_vector_DB`**: Handles the process of generating embeddings and pushing them into the vector database.
- **`6.Search_similar_points`**: Contains the client logic for performing similarity searches on the vector store.

### 2. Embedding Generation
- **`3.Creating_embeddings_generator_using_ollama`**: Setup for using Ollama to generate embeddings.
- **`3.Create_embeddings_generator`**: Scripts and setup for running the embedding generator service.
- **`4.Create_embeddings_generator_client`**: Client implementation for consuming the embedding generation service.

### 3. Core RAG Components & Services
- **`7.Launch_llama_index`**: Setup for using LlamaIndex, including a local LLM declaration example.
- **`7.Launch_PostgreSQL_container`**: Setup for the PostgreSQL database container, including a Java service client.
- **`8.Re-Ranker`**: Implementation for a re-ranking service (e.g., using CrossEncoderService).

### 4. Other Utilities & Launch Scripts
- **`10.Launch_Docling_container`**: Scripts for launching Docling container.
- **`9.Launch_langflow_container`**: Scripts for launching the Langflow UI.

## Getting Started

Please follow the steps corresponding to the component you wish to work on. Generally, the workflow should proceed in this order:

1. **Setup Dependencies**: Launch necessary infrastructure containers (Vector DB, LLM, etc.).
2. **Ingest Data**: Use the Embedding Generator to create embeddings and push them to the Vector DB.
3. **Query/Search**: Use the Search client to retrieve relevant documents and pass them through the Reranker and LLM for final answer generation.

For detailed instructions on specific services, please refer to the respective `README.md` files within each module directory.

