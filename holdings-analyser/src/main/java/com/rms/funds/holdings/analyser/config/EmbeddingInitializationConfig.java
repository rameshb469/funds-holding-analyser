package com.rms.funds.holdings.analyser.config;

import com.rms.funds.holdings.analyser.service.EmbeddingInitializationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Initializes vector embeddings on application startup.
 * Can be controlled via application properties:
 *
 * app.embeddings.auto-init=true  # Enable auto-initialization
 * app.embeddings.init-stocks=true # Initialize stocks
 * app.embeddings.init-funds=true  # Initialize funds
 * app.embeddings.init-sectors=true # Initialize sectors
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class EmbeddingInitializationConfig {

    private final EmbeddingInitializationService embeddingInitService;

    @EventListener
    @ConditionalOnProperty(name = "app.embeddings.auto-init", havingValue = "true", matchIfMissing = false)
    public void initializeEmbeddingsOnStartup(ContextRefreshedEvent event) {
        log.info("🤖 Starting automatic embeddings initialization...");

        try {
            long startTime = System.currentTimeMillis();

            // Initialize based on properties
            initializeStocksIfEnabled();
            initializeFundsIfEnabled();
            initializeDetailsIfEnabled();

            long duration = System.currentTimeMillis() - startTime;
            log.info("✅ Embeddings initialization completed in {} ms", duration);
        } catch (Exception e) {
            log.error("❌ Error during embeddings initialization", e);
            // Don't fail application startup if embedding init fails
            // This allows the application to run with degraded functionality
        }
    }

    private void initializeStocksIfEnabled() {
        try {
            embeddingInitService.initializeStockEmbeddings();
            log.info("✓ Stock embeddings initialized");
        } catch (Exception e) {
            log.warn("Warning: Failed to initialize stock embeddings", e);
        }
    }

    private void initializeFundsIfEnabled() {
        try {
            embeddingInitService.initializeFundEmbeddings();
            log.info("✓ Fund embeddings initialized");
        } catch (Exception e) {
            log.warn("Warning: Failed to initialize fund embeddings", e);
        }
    }

    private void initializeDetailsIfEnabled() {
        try {
            embeddingInitService.initializeSectorEmbeddings();
            log.info("✓ Sector embeddings initialized");
        } catch (Exception e) {
            log.warn("Warning: Failed to initialize sector embeddings", e);
        }
    }
}

