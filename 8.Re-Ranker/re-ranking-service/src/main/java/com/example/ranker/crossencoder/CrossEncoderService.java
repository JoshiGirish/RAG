package com.example.ranker.crossencoder;

import com.example.ranker.model.DocumentWithScore;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Cross-encoder service using Lucene for similarity-based re-ranking
 * 
 * This implements a cross-encoder approach where documents are re-ranked
 * based on their semantic similarity to the query using BM25 scoring
 */
public class CrossEncoderService {
    
    private static final Logger LOG = LoggerFactory.getLogger(CrossEncoderService.class);
    private static final String INDEX_NAME = "re-ranker-index";
    private static final int MAX_RESULTS = 100;
    
    private Path indexDir;
    
    public CrossEncoderService() {
        this.indexDir = Files.createTempDirectory("re-ranker-index-");
        LOG.info("CrossEncoderService initialized with index directory: {}", indexDir);
    }
    
    /**
     * Re-rank documents using cross-encoder scoring
     * 
     * @param query The search query
     * @param documents List of documents to re-rank
     * @return Re-ranked list of documents with scores
     */
    public List<DocumentWithScore> reRank(String query, List<DocumentWithScore> documents) {
        LOG.debug("Starting cross-encoder re-ranking for query: {}", query);
        
        try {
            // Index all documents
            indexDocuments(documents);
            
            // Perform similarity search
            TopDocs topDocs = searchSimilarDocuments(query, MAX_RESULTS);
            
            // Build re-ranked results
            List<DocumentWithScore> reRanked = new ArrayList<>();
            
            for (int i = 0; i < topDocs.scoreDocs.length; i++) {
                ScoreDoc scoreDoc = topDocs.scoreDocs[i];
                String docId = getDocIdFromStoredFields(scoreDoc.doc);
                
                // Find original document
                DocumentWithScore originalDoc = documents.stream()
                        .filter(d -> d.getId().equals(docId))
                        .findFirst()
                        .orElse(new DocumentWithScore());
                
                // Calculate combined score (weighted average)
                float crossEncoderScore = scoreDoc.score;
                float combinedScore = calculateCombinedScore(
                        originalDoc.getVectorSimilarity(),
                        crossEncoderScore,
                        0.4f // weight for cross-encoder
                );
                
                originalDoc.setCrossEncoderScore(crossEncoderScore);
                originalDoc.setCombinedScore(combinedScore);
                reRanked.add(originalDoc);
            }
            
            LOG.info("Cross-encoder re-ranking complete. Processed {} documents", reRanked.size());
            return reRanked;
            
        } catch (IOException e) {
            LOG.error("Error during cross-encoder re-ranking: {}", e.getMessage(), e);
            throw new RuntimeException("Re-ranking failed", e);
        }
    }
    
    /**
     * Index documents for cross-encoder search
     */
    private void indexDocuments(List<DocumentWithScore> documents) throws IOException {
        IndexWriterConfig config = new IndexWriterConfig(
                new WhitespaceAnalyzer()
        );
        IndexWriter writer = new IndexWriter(indexDir.toFile(), config);
        
        for (DocumentWithScore doc : documents) {
            Document luceneDoc = new Document();
            
            luceneDoc.add(new TextField("id", doc.getId(), Field.Store.YES));
            luceneDoc.add(new TextField("content", doc.getContent(), Field.Store.NO));
            luceneDoc.add(new TextField("source", doc.getSource(), Field.Store.NO));
            
            if (doc.getMetadata() != null) {
                for (String meta : doc.getMetadata()) {
                    luceneDoc.add(new TextField("metadata", meta, Field.Store.NO));
                }
            }
            
            writer.addDocument(luceneDoc);
        }
        
        writer.commit();
        writer.close();
        LOG.debug("Indexed {} documents for cross-encoder", documents.size());
    }
    
    /**
     * Search for similar documents using cross-encoder
     */
    private TopDocs searchSimilarDocuments(String query, int numResults) throws IOException {
        IndexReader reader = DirectoryReader.open(IndexWriter.getIndexWriter(indexDir));
        QueryParser parser = new QueryParser("content", new WhitespaceAnalyzer());
        parser.setOperator(Operator.OR);
        
        String queryString = String.format("\"%s\"", query);
        org.apache.lucene.search.Query query = parser.parse(queryString);
        
        BM25Similarity similarity = new BM25Similarity();
        similarity.setZeroTermFrequencySaturation(1.0f);
        similarity.setQueryNorm(1.0f);
        
        org.apache.lucene.search.Searcher searcher = new org.apache.lucene.search.Searcher(reader);
        searcher.setSimilarity(similarity);
        
        TopDocs topDocs = searcher.search(query, numResults);
        searcher.close();
        reader.close();
        
        return topDocs;
    }
    
    /**
     * Extract document ID from stored fields
     */
    private String getDocIdFromStoredFields(int docId) throws IOException {
        IndexReader reader = DirectoryReader.open(IndexWriter.getIndexWriter(indexDir));
        FieldInfo idField = reader.getFieldInfo("id");
        
        if (idField == null) {
            return String.valueOf(docId);
        }
        
        try {
            Object value = reader.getStoredFields().get(docId);
            return value != null ? value.toString() : String.valueOf(docId);
        } finally {
            reader.close();
        }
    }
    
    /**
     * Calculate combined score from vector similarity and cross-encoder score
     */
    private float calculateCombinedScore(float vectorScore, float crossEncoderScore, float crossEncoderWeight) {
        // Normalize scores to [0, 1] range
        float normalizedVector = Math.min(1.0f, Math.max(0.0f, vectorScore));
        float normalizedCrossEncoder = Math.min(1.0f, Math.max(0.0f, crossEncoderScore));
        
        return (normalizedVector * (1 - crossEncoderWeight)) + 
               (normalizedCrossEncoder * crossEncoderWeight);
    }
    
    /**
     * Cleanup method - remove index directory
     */
    public void cleanup() {
        try {
            if (indexDir != null && indexDir.toFile().exists()) {
                Files.walk(indexDir)
                    .sorted(java.util.Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            LOG.warn("Failed to delete {}", path);
                        }
                    });
                LOG.info("Index directory cleaned up");
            }
        } catch (IOException e) {
            LOG.error("Error cleaning up index directory", e);
        }
    }
    
    public Path getIndexDir() {
        return indexDir;
    }
}