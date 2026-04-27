package com.example.ranker.model;

import java.io.Serializable;
import java.util.List;

/**
 * Response model for re-ranking API
 */
public class SearchResult implements Serializable {
    
    private String query;
    private List<DocumentWithScore> results;
    private int totalResults;
    private float averageRelevance;
    
    // Constructors
    public SearchResult() {}
    
    public SearchResult(String query, List<DocumentWithScore> results, 
                       int totalResults, float averageRelevance) {
        this.query = query;
        this.results = results;
        this.totalResults = totalResults;
        this.averageRelevance = averageRelevance;
    }
    
    // Getters and Setters
    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }
    
    public List<DocumentWithScore> getResults() { return results; }
    public void setResults(List<DocumentWithScore> results) { this.results = results; }
    
    public int getTotalResults() { return totalResults; }
    public void setTotalResults(int totalResults) { this.totalResults = totalResults; }
    
    public float getAverageRelevance() { return averageRelevance; }
    public void setAverageRelevance(float averageRelevance) { this.averageRelevance = averageRelevance; }
}