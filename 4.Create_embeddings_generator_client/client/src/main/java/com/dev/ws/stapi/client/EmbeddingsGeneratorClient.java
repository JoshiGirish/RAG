package com.dev.ws.stapi.client;
import org.codehaus.jettison.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class EmbeddingsGeneratorClient {

    public static void main(String[] args) {
        try {
            // 1. Prepare the JSON payload using Jettison
            JSONObject jsonPayload = new JSONObject();
            jsonPayload.put("input", "substratus.ai provides the best LLM tools");
            jsonPayload.put("model", "all-MiniLM-L6-v2");

            // 2. Create connection
            URL url = new URL("http://localhost:8080/v1/embeddings");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // 3. Configure connection for POST
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // 4. Write JSON payload
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonPayload.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // 5. Get response code
            int responseCode = conn.getResponseCode();
            System.out.println("Response Code: " + responseCode);

            // 6. Read response (success case)
            if (responseCode >= 200 && responseCode < 300) {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                    
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    
                    System.out.println("Response:");
                    System.out.println(response.toString());
                    
                    JSONObject jsonResponse = new JSONObject(response.toString());
                    // ... work with jsonResponse ...
                }
            } 
            else {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getErrorStream(), "utf-8"))) {
                    StringBuilder error = new StringBuilder();
                    String errorLine;
                    while ((errorLine = br.readLine()) != null) {
                        error.append(errorLine.trim());
                    }
                    System.out.println("Error response: " + error);
                }
            }

            conn.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}