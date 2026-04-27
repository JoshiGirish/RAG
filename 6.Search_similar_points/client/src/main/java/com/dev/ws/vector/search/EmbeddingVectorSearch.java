package com.dev.ws.vector.search;

import com.dev.ws.stapi.client.EmbeddingsGeneratorClient;
import java.util.List;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import io.qdrant.client.grpc.Points.QueryPoints;
import io.qdrant.client.grpc.Points.ScoredPoint;

import static io.qdrant.client.QueryFactory.nearest;

public class EmbeddingVectorSearch {
    
    public static void main(String[] args) {
        EmbeddingsGeneratorClient embeddingGeneratorClient = new EmbeddingsGeneratorClient();
        String input = "What do you know about mathematics?";
        float[] vectorEmbedding = embeddingGeneratorClient.generateEmbedding(input);

        try{
            try (QdrantClient vectorDBclient = new QdrantClient(QdrantGrpcClient.newBuilder("localhost", 6334, false).build())) {
                List<ScoredPoint> searchResult =
                vectorDBclient.queryAsync(QueryPoints.newBuilder()
                                .setCollectionName("sectorDB")
                                .setLimit(3)
                                .setQuery(nearest(vectorEmbedding))
                                .build()).get();
                    
                System.out.println(searchResult);
            }
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
    }


}

