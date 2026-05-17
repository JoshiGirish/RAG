package com.dev;

import com.dev.ws.stapi.client.EmbeddingsGeneratorClient;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static io.qdrant.client.PointIdFactory.id;
import static io.qdrant.client.ValueFactory.value;
import static io.qdrant.client.VectorsFactory.vectors;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import io.qdrant.client.grpc.Collections.Distance;
import io.qdrant.client.grpc.Points.PointStruct;
import io.qdrant.client.grpc.Points.UpdateResult;

public class EmbeddingVectorIngestor {

    private static String collectionName =  "docDB";
    
        public static void main(String[] args) {
        EmbeddingsGeneratorClient embeddingGeneratorClient = new EmbeddingsGeneratorClient();
        String input = "Shinigamis like apple.";
        float[] vectorEmbedding = embeddingGeneratorClient.generateEmbedding(input);

        // Now build the point
        PointStruct point = PointStruct.newBuilder()
        .setId(id(6))               // or uuid(), or num(42L), ...
        .setVectors(vectors(vectorEmbedding))        // ← this is the key mapping
        .putAllPayload(Map.of(
            "text",     value(input),
            "model",    value("nomic-embed-text"),
            "source",   value("api"),
            "timestamp", value(System.currentTimeMillis())
        ))
        .build();

        try {
            try (QdrantClient vectorDBClient = new QdrantClient(QdrantGrpcClient.newBuilder("localhost", 6334, false).build())) {
                // Check if the collection exists
                    // Upsert to existing collection
                    UpdateResult result = vectorDBClient.upsertAsync(
                        collectionName,
                        List.of(point)
                    ).get();
                    
                    System.out.println("Upsert result: " + result);
            }
        }  catch (ExecutionException e) {
            try (QdrantClient vectorDBClient = new QdrantClient(QdrantGrpcClient.newBuilder("localhost", 6334, false).build())) {
                System.out.println("Collection not found, creating it...");
                
                // Create the collection with default settings
                QdrantClientHelper.createCollectionAsync(vectorDBClient, collectionName, 768, Distance.Cosine);
                
                System.out.println("Collection created successfully");
                
                // Now upsert to the newly created collection
                UpdateResult result = vectorDBClient.upsertAsync(
                    collectionName,
                    List.of(point)
                ).get();
                
                System.out.println("Upsert result after creation: " + result);
                } catch (InterruptedException | ExecutionException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
        } catch (Exception e) {
            System.out.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }

    }

}
