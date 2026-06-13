package com.rms.funds.holdings.analyser.controller;

import com.rms.funds.holdings.analyser.agent.VectorEmbeddingAgent;
import com.rms.funds.holdings.analyser.entity.*;
import com.rms.funds.holdings.analyser.service.EmbeddingInitializationService;
import com.rms.funds.holdings.analyser.service.EmbeddingGeneratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/embeddings")
@Slf4j
@RequiredArgsConstructor
public class EmbeddingController {

    private final VectorEmbeddingAgent vectorEmbeddingAgent;
    private final EmbeddingInitializationService embeddingInitService;
    private final EmbeddingGeneratorService embeddingGeneratorService;

    /**
     * Initialize all embeddings in the system
     */
    @PostMapping("/initialize")
    public ResponseEntity<Map<String, String>> initializeAllEmbeddings() {
        log.info("Initializing all embeddings...");
        try {
            embeddingInitService.initializeAllEmbeddings();
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "All embeddings initialized successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error initializing embeddings", e);
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Initialize stock embeddings only
     */
    @PostMapping("/initialize/stocks")
    public ResponseEntity<Map<String, String>> initializeStockEmbeddings() {
        log.info("Initializing stock embeddings...");
        try {
            embeddingInitService.initializeStockEmbeddings();
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Stock embeddings initialized");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error initializing stock embeddings", e);
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Initialize fund embeddings only
     */
    @PostMapping("/initialize/funds")
    public ResponseEntity<Map<String, String>> initializeFundEmbeddings() {
        log.info("Initializing fund embeddings...");
        try {
            embeddingInitService.initializeFundEmbeddings();
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Fund embeddings initialized");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error initializing fund embeddings", e);
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Initialize sector embeddings only
     */
    @PostMapping("/initialize/sectors")
    public ResponseEntity<Map<String, String>> initializeSectorEmbeddings() {
        log.info("Initializing sector embeddings...");
        try {
            embeddingInitService.initializeSectorEmbeddings();
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Sector embeddings initialized");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error initializing sector embeddings", e);
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Find similar stocks based on embedding
     */
    @PostMapping("/search/similar-stocks")
    public ResponseEntity<Map<String, Object>> findSimilarStocks(
            @RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Float> embeddingList = (List<Float>) request.get("embedding");
            Integer limit = (Integer) request.getOrDefault("limit", 5);

            if (embeddingList == null || embeddingList.isEmpty()) {
                return ResponseEntity.badRequest().body(
                        Map.of("error", "Embedding array is required")
                );
            }

            float[] embedding = new float[embeddingList.size()];
            for (int i = 0; i < embeddingList.size(); i++) {
                embedding[i] = embeddingList.get(i);
            }

            List<StockEmbeddingEntity> results = vectorEmbeddingAgent.findSimilarStocks(embedding, limit);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("count", results.size());
            response.put("results", results);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error finding similar stocks", e);
            return ResponseEntity.internalServerError().body(
                    Map.of("status", "error", "message", e.getMessage())
            );
        }
    }

    /**
     * Find similar funds based on embedding
     */
    @PostMapping("/search/similar-funds")
    public ResponseEntity<Map<String, Object>> findSimilarFunds(
            @RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Float> embeddingList = (List<Float>) request.get("embedding");
            Integer limit = (Integer) request.getOrDefault("limit", 5);

            if (embeddingList == null || embeddingList.isEmpty()) {
                return ResponseEntity.badRequest().body(
                        Map.of("error", "Embedding array is required")
                );
            }

            float[] embedding = new float[embeddingList.size()];
            for (int i = 0; i < embeddingList.size(); i++) {
                embedding[i] = embeddingList.get(i);
            }

            List<FundEmbeddingEntity> results = vectorEmbeddingAgent.findSimilarFunds(embedding, limit);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("count", results.size());
            response.put("results", results);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error finding similar funds", e);
            return ResponseEntity.internalServerError().body(
                    Map.of("status", "error", "message", e.getMessage())
            );
        }
    }

    /**
     * Get top investment insights for a fund
     */
    @GetMapping("/insights/fund/{fundId}")
    public ResponseEntity<Map<String, Object>> getTopInsights(
            @PathVariable Long fundId,
            @RequestParam(defaultValue = "10") Integer limit) {
        try {
            List<InvestmentInsightEntity> insights = vectorEmbeddingAgent.getTopInsights(fundId, limit);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("fundId", fundId);
            response.put("count", insights.size());
            response.put("insights", insights);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving insights", e);
            return ResponseEntity.internalServerError().body(
                    Map.of("status", "error", "message", e.getMessage())
            );
        }
    }

    /**
     * Get high confidence investment insights
     */
    @GetMapping("/insights/high-confidence")
    public ResponseEntity<Map<String, Object>> getHighConfidenceInsights(
            @RequestParam(defaultValue = "0.8") Double threshold) {
        try {
            List<InvestmentInsightEntity> insights = vectorEmbeddingAgent.getHighConfidenceInsights(threshold);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("threshold", threshold);
            response.put("count", insights.size());
            response.put("insights", insights);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving high confidence insights", e);
            return ResponseEntity.internalServerError().body(
                    Map.of("status", "error", "message", e.getMessage())
            );
        }
    }

    /**
     * Get recent price patterns for a stock
     */
    @GetMapping("/patterns/stock/{stockId}")
    public ResponseEntity<Map<String, Object>> getStockPatterns(
            @PathVariable Long stockId) {
        try {
            List<StockPricePatternEntity> patterns = vectorEmbeddingAgent.getRecentPatterns(stockId);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("stockId", stockId);
            response.put("count", patterns.size());
            response.put("patterns", patterns);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving patterns", e);
            return ResponseEntity.internalServerError().body(
                    Map.of("status", "error", "message", e.getMessage())
            );
        }
    }

    /**
     * Generate embedding for input text
     */
    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateEmbedding(
            @RequestBody Map<String, String> request) {
        try {
            String text = request.get("text");
            if (text == null || text.isEmpty()) {
                return ResponseEntity.badRequest().body(
                        Map.of("error", "Text is required")
                );
            }

            float[] embedding = embeddingGeneratorService.generateEmbedding(text);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("text", text);
            response.put("dimension", embedding.length);
            response.put("embedding", embedding);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error generating embedding", e);
            return ResponseEntity.internalServerError().body(
                    Map.of("status", "error", "message", e.getMessage())
            );
        }
    }

    /**
     * Calculate cosine similarity between two embeddings
     */
    @PostMapping("/similarity")
    public ResponseEntity<Map<String, Object>> calculateSimilarity(
            @RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Float> emb1List = (List<Float>) request.get("embedding1");
            @SuppressWarnings("unchecked")
            List<Float> emb2List = (List<Float>) request.get("embedding2");

            if (emb1List == null || emb2List == null) {
                return ResponseEntity.badRequest().body(
                        Map.of("error", "Both embedding1 and embedding2 are required")
                );
            }

            float[] emb1 = new float[emb1List.size()];
            for (int i = 0; i < emb1List.size(); i++) {
                emb1[i] = emb1List.get(i);
            }

            float[] emb2 = new float[emb2List.size()];
            for (int i = 0; i < emb2List.size(); i++) {
                emb2[i] = emb2List.get(i);
            }

            float similarity = embeddingGeneratorService.cosineSimilarity(emb1, emb2);
            float distance = embeddingGeneratorService.euclideanDistance(emb1, emb2);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("cosineSimilarity", similarity);
            response.put("euclideanDistance", distance);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error calculating similarity", e);
            return ResponseEntity.internalServerError().body(
                    Map.of("status", "error", "message", e.getMessage())
            );
        }
    }
}

