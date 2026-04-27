package com.example.ranker;

import com.example.ranker.model.DocumentWithScore;
import com.example.ranker.model.SearchResult;
import com.example.ranker.crossencoder.CrossEncoderService;
import io.quarkus.arc.lookup.LookupIfProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Main re-ranker service that orchestrates the re-ranking process
 * 
 * This service:
 * 1. Takes top-k results from vector search
 * 2. Applies cross-encoder scoring
 * 3. Reorders results by combined relevance score
 */
@LookupIfProperty(name = "re-ranker.enabled", stringValue = "true")
public class ReRankerService {
    
    private static final Logger LOG = LoggerFactory.getLogger(ReRankerService.class);
    
    private final ReRankerConfig config;
    private final CrossEncoderService crossEncoder;
    
    public ReRankerService(ReRankerConfig config) {
        this.config = config;
        this.crossEncoder = new CrossEncoderService();
    }
    
    /**
     * Main re-ranking method
     * 
     * @param query The search query
     * @param initialResults Top-k results from vector search
     * @return Re-ranked results with combined scores
     */
    public SearchResult reRank(String query, List<DocumentWithScore> initialResults) {
        LOG.info("Starting re-ranking process for query: {}", query);
        
        if (initialResults == null || initialResults.isEmpty()) {
            LOG.warn("No initial results to re-rank");
            return new SearchResult(query, new ArrayList<>(), 0, 0.0f);
        }
        
        // Step 1: Limit to top-k results
        List<DocumentWithScore> topK = initialResults.stream()
                .limit(config.topK())
                .collect(Collectors.toList());
        
        LOG.debug("Processing {} documents for re-ranking (top-k: {})", 
                   topK.size(), config.topK());
        
        // Step 2: Apply cross-encoder scoring
        List<DocumentWithScore> reRanked = crossEncoder.reRank(query, topK);
        
        // Step 3: Sort by combined score (descending)
        reRanked.sort(Comparator.comparingDouble(
            d -> Double.valueOf(String.format("%.10f", d.getCombinedScore()))
        ).reversed());
        
        // Step 4: Calculate statistics
        float avgRelevance = reRanked.stream()
                .mapToDouble(DocumentWithScore::getCombinedScore)
                .average()
                .orElse(0.0f);
        
        // Step 5: Build result
        SearchResult result = new SearchResult(
                query,
                reRanked,
                initialResults.size(),
                avgRelevance
        );
        
        LOG.info("Re-ranking complete. Average relevance: {}", 
                String.format("%.4f", avgRelevance));
        
        return result;
    }
    
    /**
     * Re-rank with custom top-k value
     */
    public SearchResult reRank(String query, List<DocumentWithScore> initialResults, int customTopK) {
        ReRankerConfig modifiedConfig = new ReRankerConfig() {
            @Override
            public int topK() { return customTopK; }
            
            @Override
            public int maxResults() { return config.maxResults(); }
            
            @Override
            public String model() { return config.model(); }
            
            @Override
            public int maxTokens() { return config.maxTokens(); }
        };
        
        ReRankerService tempService = new ReRankerService(modifiedConfig);
        return tempService.reRank(query, initialResults);
    }
    
    /**
     * Dry run - calculate scores without full re-ranking
     */
    public List<DocumentWithScore> scoreOnly(String query, List<DocumentWithScore> documents) {
        if (documents == null || documents.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<DocumentWithScore> scored = new ArrayList<>(documents);
        
        for (DocumentWithScore doc : scored) {
            float crossEncoderScore = calculateQuickScore(query, doc.getContent());
            doc.setCrossEncoderScore(crossEncoderScore);
            doc.setCombinedScore(calculateCombinedScore(
                    doc.getVectorSimilarity(),
                    crossEncoderScore,
                    0.4f
            ));
        }
        
        return scored.stream()
                .sorted(Comparator.comparingDouble(d -> 
                    Double.valueOf(String.format("%.10f", d.getCombinedScore()))
                ).reversed())
                .collect(Collectors.toList());
    }
    
    /**
     * Quick scoring without full index operations
     */
    private float calculateQuickScore(String query, String content) {
        // Simple TF-IDF-like scoring for quick estimation
        int queryTermCount = 0;
        int contentTermCount = 0;
        int totalContentTerms = 0;
        
        String queryLower = query.toLowerCase();
        String contentLower = content.toLowerCase();
        
        for (char c : queryLower.toCharArray()) {
            int count = contentLower.chars()
                    .filter(ch -> Character.toLowerCase(ch) == c)
                    .count();
            queryTermCount += count;
            totalContentTerms += count;
        }
        
        if (totalContentTerms == 0) {
            return 0.0f;
        }
        
        // Simple term frequency score
        float tfScore = queryTermCount / (float)totalContentTerms;
        return Math.min(1.0f, tfScore * 10.0f);
    }
    
    /**
     * Get statistics about the re-ranking process
     */
    public class ReRankStatistics {
        private int totalDocuments;
        private int documentsProcessed;
        private float avgVectorScore;
        private float avgCrossEncoderScore;
        private float avgCombinedScore;
        
        public ReRankStatistics(int totalDocuments, int documentsProcessed, 
                               float avgVectorScore, float avgCrossEncoderScore, 
                               float avgCombinedScore) {
            this.totalDocuments = totalDocuments;
            this.documentsProcessed = documentsProcessed;
            this.avgVectorScore = avgVectorScore;
            this.avgCrossEncoderScore = avgCrossEncoderScore;
            this.avgCombinedScore = avgCombinedScore;
        }
        
        public int getTotalDocuments() { return totalDocuments; }
        public int getDocumentsProcessed() { return documentsProcessed; }
        public float getAvgVectorScore() { return avgVectorScore; }
        public float getAvgCrossEncoderScore() { return avgCrossEncoderScore; }
        public float getAvgCombinedScore() { return avgCombinedScore; }
        
        @Override
        public String toString() {
            return "ReRankStatistics{" +
                    "totalDocuments=" + totalDocuments +
                    ", documentsProcessed=" + documentsProcessed +
                    ", avgVectorScore=" + String.format("%.4f", avgVectorScore) +
                    ", avgCrossEncoderScore=" + String.format("%.4f", avgCrossEncoderScore) +
                    ", avgCombinedScore=" + String.format("%.4f", avgCombinedScore) +
                    '}';
        }
    }
}