package com.dev.ws.qdrant;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import io.qdrant.client.grpc.Collections.Distance;
import io.qdrant.client.grpc.Collections.VectorParams;

public class QdrantClientHelper 
{
    public static void main( String[] args )
    {
        try{
            try (QdrantClient client = new QdrantClient(QdrantGrpcClient.newBuilder("localhost", 6334, false).build())) {
                client.createCollectionAsync("stocksViaAPI", VectorParams.newBuilder().setDistance(Distance.Cosine).setSize(4).build()).get();
            }
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
}
