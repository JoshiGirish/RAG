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
<<<<<<< Updated upstream
        String input = "Mathematics is the language of the universe.";
=======
        String input = "Shinigamis like apple.";
>>>>>>> Stashed changes
        float[] vectorEmbedding = embeddingGeneratorClient.generateEmbedding(input);

        // Now build the point
        PointStruct point = PointStruct.newBuilder()
<<<<<<< Updated upstream
        .setId(id(5))               // or uuid(), or num(42L), ...
=======
        .setId(id(6))               // or uuid(), or num(42L), ...
>>>>>>> Stashed changes
        .setVectors(vectors(vectorEmbedding))        // ← this is the key mapping
        .putAllPayload(Map.of(
            "text",     value(input),
            "model",    value("nomic-embed-text"),
            "source",   value("api"),
            "timestamp", value(System.currentTimeMillis())
        ))
        .build();

        try{
            try (QdrantClient vectorDBclient = new QdrantClient(QdrantGrpcClient.newBuilder("localhost", 6334, false).build())) {
                // Upsert
                UpdateResult result = vectorDBclient.upsertAsync(
                "sectorDB",
                List.of(point)
                ).get();
        
                System.out.println("Upsert result: " + result);
            }
        } catch (Exception e){
            System.out.println(e.getMessage());
        }

    }


}
