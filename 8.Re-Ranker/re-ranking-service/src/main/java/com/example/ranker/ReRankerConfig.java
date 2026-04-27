package com.example.ranker;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.smallrye.config.ConfigMapping;

/**
 * Configuration for the re-ranker component
 */
@ConfigMapping(prefix = "re-ranker")
public interface ReRankerConfig {
    
    /**
     * Number of top results to consider for re-ranking
     */
    int topK();
    
    /**
     * Maximum number of results to process
     */
    int maxResults();
    
    /**
     * Cross-encoder model to use for re-ranking
     */
    String model();
    
    /**
     * Maximum tokens for cross-encoder processing
     */
    int maxTokens();
}