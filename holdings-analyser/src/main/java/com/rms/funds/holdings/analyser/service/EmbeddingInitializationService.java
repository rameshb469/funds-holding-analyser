package com.rms.funds.holdings.analyser.service;

import com.rms.funds.holdings.analyser.agent.VectorEmbeddingAgent;
import com.rms.funds.holdings.analyser.entity.*;
import com.rms.funds.holdings.analyser.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmbeddingInitializationService {

    private final VectorEmbeddingAgent vectorEmbeddingAgent;
    private final StockInfoRepository stockInfoRepository;
    private final MutualFundRepository mutualFundRepository;
    private final SectorRepository sectorRepository;
    private final IndustryRepository industryRepository;
    private final EmbeddingGeneratorService embeddingGeneratorService;

    /**
     * Initialize embeddings for all stocks in the database
     */
    @Transactional
    public void initializeStockEmbeddings() {
        log.info("Starting stock embeddings initialization...");
        try {
            List<StockInfoEntity> stocks = stockInfoRepository.findAll();
            int count = 0;

            for (StockInfoEntity stock : stocks) {
                try {
                    String description = stock.getCompany() != null ?
                            String.format("Stock: %s (%s)", stock.getCompany(), stock.getSymbol()) :
                            stock.getSymbol();

                    float[] embedding = embeddingGeneratorService.generateEmbedding(description);

                    String sector = stock.getSector() != null ? stock.getSector().getName() : "Unknown";
                    String industry = stock.getIndustry() != null ? stock.getIndustry().getName() : "Unknown";

                    vectorEmbeddingAgent.createOrUpdateStockEmbedding(
                            stock.getId(),
                            stock.getSymbol(),
                            stock.getCompany(),
                            description,
                            sector,
                            industry,
                            embedding
                    );
                    count++;
                } catch (Exception e) {
                    log.warn("Failed to create embedding for stock: {}", stock.getSymbol(), e);
                }
            }

            log.info("Completed stock embeddings initialization. {} stocks processed", count);
        } catch (Exception e) {
            log.error("Error during stock embeddings initialization", e);
            throw new RuntimeException("Stock embeddings initialization failed", e);
        }
    }

    /**
     * Initialize embeddings for all mutual funds
     */
    @Transactional
    public void initializeFundEmbeddings() {
        log.info("Starting fund embeddings initialization...");
        try {
            List<MutualFundEntity> funds = mutualFundRepository.findAll();
            int count = 0;

            for (MutualFundEntity fund : funds) {
                try {
                    String description = fund.getDescription() != null ?
                            fund.getDescription() :
                            String.format("Fund: %s", fund.getName());

                    float[] embedding = embeddingGeneratorService.generateEmbedding(description);

                    String fundType = fund.getTypeEntity() != null ?
                            fund.getTypeEntity().getName() : "Unknown";

                    vectorEmbeddingAgent.createOrUpdateFundEmbedding(
                            fund.getId(),
                            fund.getName(),
                            description,
                            fundType,
                            "",
                            embedding
                    );
                    count++;
                } catch (Exception e) {
                    log.warn("Failed to create embedding for fund: {}", fund.getName(), e);
                }
            }

            log.info("Completed fund embeddings initialization. {} funds processed", count);
        } catch (Exception e) {
            log.error("Error during fund embeddings initialization", e);
            throw new RuntimeException("Fund embeddings initialization failed", e);
        }
    }

    /**
     * Initialize embeddings for all sectors
     */
    @Transactional
    public void initializeSectorEmbeddings() {
        log.info("Starting sector embeddings initialization...");
        try {
            List<SectorEntity> sectors = sectorRepository.findAll();
            int count = 0;

            for (SectorEntity sector : sectors) {
                try {
                    String description = sector.getDescription() != null ?
                            sector.getDescription() :
                            String.format("Sector: %s", sector.getName());

                    float[] embedding = embeddingGeneratorService.generateEmbedding(description);

                    vectorEmbeddingAgent.createOrUpdateSectorEmbedding(
                            sector.getId(),
                            sector.getName(),
                            description,
                            "",
                            embedding
                    );
                    count++;
                } catch (Exception e) {
                    log.warn("Failed to create embedding for sector: {}", sector.getName(), e);
                }
            }

            log.info("Completed sector embeddings initialization. {} sectors processed", count);
        } catch (Exception e) {
            log.error("Error during sector embeddings initialization", e);
            throw new RuntimeException("Sector embeddings initialization failed", e);
        }
    }

    /**
     * Initialize all embeddings
     */
    @Transactional
    public void initializeAllEmbeddings() {
        log.info("Starting comprehensive embeddings initialization...");
        long startTime = System.currentTimeMillis();

        try {
            initializeStockEmbeddings();
            initializeFundEmbeddings();
            initializeSectorEmbeddings();

            long duration = System.currentTimeMillis() - startTime;
            log.info("All embeddings initialized successfully in {} ms", duration);
        } catch (Exception e) {
            log.error("Error during comprehensive embeddings initialization", e);
            throw new RuntimeException("Embeddings initialization failed", e);
        }
    }

    /**
     * Refresh embeddings for a specific stock
     */
    @Transactional
    public void refreshStockEmbedding(Long stockId) {
        log.info("Refreshing embedding for stock ID: {}", stockId);
        try {
            StockInfoEntity stock = stockInfoRepository.findById(stockId)
                    .orElseThrow(() -> new IllegalArgumentException("Stock not found: " + stockId));

            String description = stock.getCompany() != null ?
                    String.format("Stock: %s (%s)", stock.getCompany(), stock.getSymbol()) :
                    stock.getSymbol();

            float[] embedding = embeddingGeneratorService.generateEmbedding(description);

            String sector = stock.getSector() != null ? stock.getSector().getName() : "Unknown";
            String industry = stock.getIndustry() != null ? stock.getIndustry().getName() : "Unknown";

            vectorEmbeddingAgent.createOrUpdateStockEmbedding(
                    stock.getId(),
                    stock.getSymbol(),
                    stock.getCompany(),
                    description,
                    sector,
                    industry,
                    embedding
            );

            log.info("Stock embedding refreshed for: {}", stock.getSymbol());
        } catch (Exception e) {
            log.error("Error refreshing stock embedding", e);
            throw new RuntimeException("Embedding refresh failed", e);
        }
    }

    /**
     * Refresh embeddings for a specific fund
     */
    @Transactional
    public void refreshFundEmbedding(Long fundId) {
        log.info("Refreshing embedding for fund ID: {}", fundId);
        try {
            MutualFundEntity fund = mutualFundRepository.findById(fundId)
                    .orElseThrow(() -> new IllegalArgumentException("Fund not found: " + fundId));

            String description = fund.getDescription() != null ?
                    fund.getDescription() :
                    String.format("Fund: %s", fund.getName());

            float[] embedding = embeddingGeneratorService.generateEmbedding(description);

            String fundType = fund.getTypeEntity() != null ?
                    fund.getTypeEntity().getName() : "Unknown";

            vectorEmbeddingAgent.createOrUpdateFundEmbedding(
                    fund.getId(),
                    fund.getName(),
                    description,
                    fundType,
                    "",
                    embedding
            );

            log.info("Fund embedding refreshed for: {}", fund.getName());
        } catch (Exception e) {
            log.error("Error refreshing fund embedding", e);
            throw new RuntimeException("Embedding refresh failed", e);
        }
    }
}

