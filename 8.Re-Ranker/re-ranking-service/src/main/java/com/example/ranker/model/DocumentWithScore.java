package com.example.ranker.model;

import java.io.Serializable;
import java.util.List;

/**
 * Enhanced document with relevance scores from different sources
 */
public class DocumentWithScore implements Serializable {
    
    private String id;
    private String content;
    private String source;
    private float vectorSimilarity;
    private float crossEncoderScore;
    private float combinedScore;
    private List<String> metadata;
    
    // Constructors
    public DocumentWithScore() {}
    
    public DocumentWithScore(String id, String content, String source, 
                            float vectorSimilarity, float crossEncoderScore) {
        this.id = id;
        this.content = content;
        this.source = source;
        this.vectorSimilarity = vectorSimilarity;
        this.crossEncoderScore = crossEncoderScore;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    
    public float getVectorSimilarity() { return vectorSimilarity; }
    public void setVectorSimilarity(float vectorSimilarity) { this.vectorSimilarity = vectorSimilarity; }
    
    public float getCrossEncoderScore() { return crossEncoderScore; }
    public void setCrossEncoderScore(float crossEncoderScore) { 
        this.crossEncoderScore = crossEncoderScore; 
    }
    
    public float getCombinedScore() { return combinedScore; }
    public void setCombinedScore(float combinedScore) { this.combinedScore = combinedScore; }
    
    public List<String> getMetadata() { return metadata; }
    public void setMetadata(List<String> metadata) { this.metadata = metadata; }
    
    @Override
    public String toString() {
        return "DocumentWithScore{" +
                "id='" + id + '\'' +
                ", vectorSimilarity=" + vectorSimilarity +
                ", crossEncoderScore=" + crossEncoderScore +
                ", combinedScore=" + String.format("%.4f", combinedScore) +
                '}';
    }
}