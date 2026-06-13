package com.rms.funds.holdings.analyser.agent;

import com.rms.funds.holdings.analyser.entity.*;
import com.rms.funds.holdings.analyser.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class VectorEmbeddingAgent {

    private final StockEmbeddingRepository stockEmbeddingRepository;
    private final FundEmbeddingRepository fundEmbeddingRepository;
    private final SectorEmbeddingRepository sectorEmbeddingRepository;
    private final InvestmentInsightRepository insightRepository;
    private final StockPricePatternRepository patternRepository;
    private final AgentTaskLogRepository taskLogRepository;
    private final StockInfoRepository stockInfoRepository;
    private final MutualFundRepository mutualFundRepository;
    private final SectorRepository sectorRepository;
    private final ObjectMapper objectMapper;

    /**
     * Creates or updates stock embedding for AI analysis
     */
    @Transactional
    public StockEmbeddingEntity createOrUpdateStockEmbedding(Long stockId, String symbol, String companyName,
                                                              String description, String sector, String industry,
                                                              float[] embedding) {
        log.info("Creating/updating stock embedding for stock: {} ({})", symbol, stockId);

        return stockEmbeddingRepository.findByStockId(stockId)
                .map(existing -> existing.toBuilder()
                        .description(description)
                        .sector(sector)
                        .industry(industry)
                        .embedding(embedding)
                        .updatedAt(LocalDateTime.now())
                        .build())
                .orElseGet(() -> StockEmbeddingEntity.builder()
                        .stockId(stockId)
                        .symbol(symbol)
                        .companyName(companyName)
                        .description(description)
                        .sector(sector)
                        .industry(industry)
                        .embedding(embedding)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build());
    }

    /**
     * Creates or updates fund embedding for similarity matching
     */
    @Transactional
    public FundEmbeddingEntity createOrUpdateFundEmbedding(Long fundId, String fundName,
                                                            String description, String fundType,
                                                            String holdingsSummary, float[] embedding) {
        log.info("Creating/updating fund embedding for fund: {} ({})", fundName, fundId);

        return fundEmbeddingRepository.findByFundId(fundId)
                .map(existing -> existing.toBuilder()
                        .description(description)
                        .fundType(fundType)
                        .holdingsSummary(holdingsSummary)
                        .embedding(embedding)
                        .updatedAt(LocalDateTime.now())
                        .build())
                .orElseGet(() -> FundEmbeddingEntity.builder()
                        .fundId(fundId)
                        .fundName(fundName)
                        .description(description)
                        .fundType(fundType)
                        .holdingsSummary(holdingsSummary)
                        .embedding(embedding)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build());
    }

    /**
     * Creates or updates sector embedding for sector analysis
     */
    @Transactional
    public SectorEmbeddingEntity createOrUpdateSectorEmbedding(Long sectorId, String sectorName,
                                                               String description, String characteristics,
                                                               float[] embedding) {
        log.info("Creating/updating sector embedding for sector: {} ({})", sectorName, sectorId);

        return sectorEmbeddingRepository.findBySectorId(sectorId)
                .map(existing -> existing.toBuilder()
                        .description(description)
                        .characteristics(characteristics)
                        .embedding(embedding)
                        .updatedAt(LocalDateTime.now())
                        .build())
                .orElseGet(() -> SectorEmbeddingEntity.builder()
                        .sectorId(sectorId)
                        .sectorName(sectorName)
                        .description(description)
                        .characteristics(characteristics)
                        .embedding(embedding)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build());
    }

    /**
     * Records an investment insight with embedding
     */
    @Transactional
    public InvestmentInsightEntity recordInsight(Long fundId, String insightType, String insightText,
                                                  float[] embedding, Map<String, Object> metadata,
                                                  Double confidenceScore) {
        log.info("Recording investment insight for fund: {} - type: {}", fundId, insightType);

        InvestmentInsightEntity entity = InvestmentInsightEntity.builder()
                .fundId(fundId)
                .insightType(insightType)
                .insightText(insightText)
                .embedding(embedding)
                .metadata(metadata != null ? convertToJson(metadata) : null)
                .confidenceScore(confidenceScore)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return insightRepository.save(entity);
    }

    /**
     * Records a stock price pattern with embedding
     */
    @Transactional
    public StockPricePatternEntity recordPattern(Long stockId, String patternType, String patternDescription,
                                                  Map<String, Object> patternData, float[] patternEmbedding,
                                                  java.time.LocalDate dateFrom, java.time.LocalDate dateTo,
                                                  Double confidenceScore) {
        log.info("Recording price pattern for stock: {} - type: {}", stockId, patternType);

        StockPricePatternEntity entity = StockPricePatternEntity.builder()
                .stockId(stockId)
                .patternType(patternType)
                .patternDescription(patternDescription)
                .patternData(patternData != null ? convertToJson(patternData) : null)
                .patternEmbedding(patternEmbedding)
                .dateFrom(dateFrom)
                .dateTo(dateTo)
                .confidenceScore(confidenceScore)
                .build();

        return patternRepository.save(entity);
    }

    /**
     * Logs agent task execution with embedding
     */
    @Transactional
    public AgentTaskLogEntity logTask(String taskName, String taskDescription, float[] taskEmbedding,
                                      Map<String, Object> inputData, Map<String, Object> outputData,
                                      String status, Long executionTimeMs) {
        log.info("Logging agent task: {} - status: {}", taskName, status);

        AgentTaskLogEntity entity = AgentTaskLogEntity.builder()
                .taskName(taskName)
                .taskDescription(taskDescription)
                .taskEmbedding(taskEmbedding)
                .inputData(inputData != null ? convertToJson(inputData) : null)
                .outputData(outputData != null ? convertToJson(outputData) : null)
                .status(status)
                .executionTimeMs(executionTimeMs)
                .build();

        return taskLogRepository.save(entity);
    }

    /**
     * Find similar stocks using vector similarity
     */
    public List<StockEmbeddingEntity> findSimilarStocks(float[] embedding, int limit) {
        log.info("Finding {} similar stocks using vector search", limit);
        try {
            String vectorString = convertFloatArrayToString(embedding);
            return stockEmbeddingRepository.findSimilarStocks(vectorString, limit);
        } catch (Exception e) {
            log.error("Error finding similar stocks", e);
            return Collections.emptyList();
        }
    }

    /**
     * Find similar funds using vector similarity
     */
    public List<FundEmbeddingEntity> findSimilarFunds(float[] embedding, int limit) {
        log.info("Finding {} similar funds using vector search", limit);
        try {
            String vectorString = convertFloatArrayToString(embedding);
            return fundEmbeddingRepository.findSimilarFunds(vectorString, limit);
        } catch (Exception e) {
            log.error("Error finding similar funds", e);
            return Collections.emptyList();
        }
    }

    /**
     * Find similar sectors using vector similarity
     */
    public List<SectorEmbeddingEntity> findSimilarSectors(float[] embedding, int limit) {
        log.info("Finding {} similar sectors using vector search", limit);
        try {
            String vectorString = convertFloatArrayToString(embedding);
            return sectorEmbeddingRepository.findSimilarSectors(vectorString, limit);
        } catch (Exception e) {
            log.error("Error finding similar sectors", e);
            return Collections.emptyList();
        }
    }

    /**
     * Get investment insights for a fund with highest confidence scores
     */
    public List<InvestmentInsightEntity> getTopInsights(Long fundId, int limit) {
        log.info("Retrieving top {} insights for fund: {}", limit, fundId);
        return insightRepository.findLatestInsights(fundId, org.springframework.data.domain.PageRequest.of(0, limit));
    }

    /**
     * Get high-confidence insights across all funds
     */
    public List<InvestmentInsightEntity> getHighConfidenceInsights(Double threshold) {
        log.info("Retrieving insights with confidence > {}", threshold);
        return insightRepository.findByConfidenceScoreGreaterThanOrderByConfidenceScoreDesc(threshold);
    }

    /**
     * Get recent stock price patterns
     */
    public List<StockPricePatternEntity> getRecentPatterns(Long stockId) {
        log.info("Retrieving recent patterns for stock: {}", stockId);
        return patternRepository.findByStockIdOrderByDateFromDesc(stockId);
    }

    /**
     * Get task execution history
     */
    public List<AgentTaskLogEntity> getTaskHistory(String taskName) {
        log.info("Retrieving execution history for task: {}", taskName);
        return taskLogRepository.findLatestTasksByName(taskName);
    }

    /**
     * Get fastest tasks for performance analysis
     */
    public List<AgentTaskLogEntity> getFastestTasks() {
        log.info("Retrieving fastest task executions");
        return taskLogRepository.findFastestTasks();
    }

    /**
     * Helper to convert Map to JSON string
     */
    private String convertToJson(Map<String, Object> data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            log.warn("Error converting map to JSON", e);
            return "{}";
        }
    }

    /**
     * Helper to convert float array to vector string format
     */
    private String convertFloatArrayToString(float[] embedding) {
        if (embedding == null || embedding.length == 0) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < embedding.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(embedding[i]);
        }
        sb.append("]");
        return sb.toString();
    }
}

