package com.dev;

import com.dev.ws.stapi.client.EmbeddingsGeneratorClient;
import java.util.List;
import java.util.Map;

import static io.qdrant.client.PointIdFactory.id;
import static io.qdrant.client.ValueFactory.value;
import static io.qdrant.client.VectorsFactory.vectors;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import io.qdrant.client.grpc.Points.PointStruct;
import io.qdrant.client.grpc.Points.UpdateResult;

public class EmbeddingVectorIngestor {
    
    public static void main(String[] args) {
        EmbeddingsGeneratorClient embeddingGeneratorClient = new EmbeddingsGeneratorClient();
        String input = "An apple a day, keeps the doctor away!";
        float[] vectorEmbedding = embeddingGeneratorClient.generateEmbedding(input);

        // Now build the point
        PointStruct point = PointStruct.newBuilder()
        .setId(id(1))               // or uuid(), or num(42L), ...
        .setVectors(vectors(vectorEmbedding))        // ← this is the key mapping
        .putAllPayload(Map.of(
            "text",     value(input),
            "model",    value("all-MiniLM-L6-v2"),
            "source",   value("api"),
            "timestamp", value(System.currentTimeMillis())
        ))
        .build();

        try{
            try (QdrantClient vectorDBclient = new QdrantClient(QdrantGrpcClient.newBuilder("localhost", 6334, false).build())) {
                // Upsert
                UpdateResult result = vectorDBclient.upsertAsync(
                "test-collection",
                List.of(point)
                ).get();
        
                System.out.println("Upsert result: " + result);
            }
        } catch (Exception e){
            System.out.println(e.getMessage());
        }

    }


}
