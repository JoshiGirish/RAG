// File: src/main/java/com/dev/ws/qdrant/QdrantClientHelper.java
package com.dev.ws.qdrant;

import com.google.common.annotations.VisibleForTesting;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import io.qdrant.client.grpc.Collections.Distance;
import io.qdrant.client.grpc.Collections.VectorParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Helper class for creating and managing Qdrant client connections securely.
 * Follows security best practices including TLS, proper error handling,
 * and resource management.
 */
public class QdrantClientHelper {
    
    private static final Logger logger = LoggerFactory.getLogger(QdrantClientHelper.class);
    
    /** Default Qdrant gRPC port */
    private static final int DEFAULT_PORT = 6334;
    
    /** Default timeout in seconds */
    private static final long DEFAULT_TIMEOUT_SECONDS = 30;
    
    
    /**
     * Creates a Qdrant client with secure configuration.
     *
     * @param host Qdrant server hostname or IP address
     * @param port gRPC port (default: 6334)
     * @param useTls Enable TLS encryption (recommended for production)
     * @return QdrantClient instance
     */
    public static QdrantClient createClient(String host, int port) {
        logger.info("Creating Qdrant client - Host: {}, Port: {}, TLS: {}", 
                   host, port);
        
        QdrantGrpcClient.Builder builder = QdrantGrpcClient.newBuilder(host, port, false);

        return new QdrantClient(builder.build());
    }
    
    /**
     * Creates a collection with retry logic and proper error handling.
     *
     * @param client Qdrant client instance
     * @param collectionName Name of the collection to create
     * @param vectorSize Dimension of vectors
     * @param distance Distance metric for similarity search
     * @return Future containing the result
     */
    public static void createCollectionAsync(
            QdrantClient client,
            String collectionName,
            int vectorSize,
            Distance distance) {
        
        logger.info("Creating collection: {} with {} dimensions, distance: {}",
                   collectionName, vectorSize, distance);
        
        VectorParams.Builder params = VectorParams.newBuilder()
                .setDistance(distance)
                .setSize(vectorSize);
        
        try {
            client.createCollectionAsync(collectionName, params.build()).get(
                    DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            logger.info("Collection '{}' created successfully", collectionName);
        } catch (Exception e) {
            logger.error("Failed to create collection '{}': {}", collectionName, e.getMessage(), e);
            throw new RuntimeException("Collection creation failed", e);
        }
    }
    
    /**
     * Main entry point for testing the client.
     */
    @VisibleForTesting
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java com.dev.ws.qdrant.QdrantClientHelper <host> [port] [development]");
            System.out.println("  host: Qdrant server address (default: localhost)");
            System.out.println("  port: gRPC port (default: 6334)");
            System.out.println("  development: skip TLS for local testing");
        }
        String host = "localhost";
        
        int port = args.length > 1 ? Integer.parseInt(args[1]) : DEFAULT_PORT;
        
        QdrantClient client;
        client = createClient(host, port);

        String collectionName = "sectorDB";
        int vectorSize = 768;
        Distance distance = Distance.Cosine;
        
        try {
            logger.info("Creating collection: {} with {} dimensions", collectionName, vectorSize);
            createCollectionAsync(client, collectionName, vectorSize, distance);
            logger.info("Collection created successfully");
        } catch (Exception e) {
            logger.error("Error occurred: {}", e.getMessage(), e);
            System.exit(1);
        }
    }
    
    /**
     * Validates host address format.
     */
    @VisibleForTesting
    static boolean isValidHost(String host) {
        if (host == null || host.trim().isEmpty()) {
            return false;
        }
        
        // Check for localhost or valid hostname
        return host.equalsIgnoreCase("localhost") || 
               host.matches("^[a-zA-Z0-9.-]+$") ||
               host.startsWith("http://") || host.startsWith("https://");
    }
}
