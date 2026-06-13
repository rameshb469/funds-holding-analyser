package com.rms.funds.holdings.analyser.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Random;

/**
 * Service for generating vector embeddings.
 *
 * This is a placeholder implementation using deterministic hashing.
 * In production, you should integrate with real embedding models like:
 * - OpenAI Embeddings API
 * - Hugging Face Transformers
 * - Google Cloud Vertex AI Embeddings
 * - LangChain embedding providers
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EmbeddingGeneratorService {

    private static final int EMBEDDING_DIMENSION = 1536;

    /**
     * Generate a vector embedding from text.
     *
     * Currently uses a deterministic hash-based approach for demo purposes.
     * Replace with actual embedding model for production use.
     *
     * @param text Input text to embed
     * @return float array of size 1536 (OpenAI embedding dimension)
     */
    public float[] generateEmbedding(String text) {
        try {
            if (text == null || text.isEmpty()) {
                return generateZeroEmbedding();
            }

            // Generate deterministic seed from text hash
            long seed = generateDeterministicSeed(text);
            Random random = new Random(seed);

            // Generate embedding vector
            float[] embedding = new float[EMBEDDING_DIMENSION];
            float magnitude = 0.0f;

            for (int i = 0; i < EMBEDDING_DIMENSION; i++) {
                embedding[i] = (random.nextFloat() - 0.5f) * 2.0f; // Values between -1 and 1
                magnitude += embedding[i] * embedding[i];
            }

            // Normalize to unit length (common for embeddings)
            magnitude = (float) Math.sqrt(magnitude);
            if (magnitude > 0) {
                for (int i = 0; i < EMBEDDING_DIMENSION; i++) {
                    embedding[i] /= magnitude;
                }
            }

            log.debug("Generated embedding for text: {}", text.substring(0, Math.min(50, text.length())));
            return embedding;

        } catch (Exception e) {
            log.error("Error generating embedding", e);
            return generateZeroEmbedding();
        }
    }

    /**
     * Generate deterministic seed from text using SHA-256 hash
     */
    private long generateDeterministicSeed(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(text.getBytes(StandardCharsets.UTF_8));

            // Convert first 8 bytes to long
            long seed = 0;
            for (int i = 0; i < Math.min(8, hash.length); i++) {
                seed = (seed << 8) | (hash[i] & 0xFF);
            }
            return seed;
        } catch (Exception e) {
            log.warn("Error generating seed, using default", e);
            return text.hashCode();
        }
    }

    /**
     * Generate a zero embedding (for null/empty inputs)
     */
    private float[] generateZeroEmbedding() {
        float[] embedding = new float[EMBEDDING_DIMENSION];
        // Single non-zero value at first position to distinguish from null
        embedding[0] = 0.001f;
        return embedding;
    }

    /**
     * Calculate cosine similarity between two embeddings
     * Useful for vector similarity search
     *
     * @param embedding1 First embedding vector
     * @param embedding2 Second embedding vector
     * @return Cosine similarity value between -1 and 1
     */
    public float cosineSimilarity(float[] embedding1, float[] embedding2) {
        if (embedding1 == null || embedding2 == null) {
            return 0.0f;
        }

        if (embedding1.length != embedding2.length) {
            throw new IllegalArgumentException("Embeddings must have the same dimension");
        }

        float dotProduct = 0.0f;
        float magnitude1 = 0.0f;
        float magnitude2 = 0.0f;

        for (int i = 0; i < embedding1.length; i++) {
            dotProduct += embedding1[i] * embedding2[i];
            magnitude1 += embedding1[i] * embedding1[i];
            magnitude2 += embedding2[i] * embedding2[i];
        }

        magnitude1 = (float) Math.sqrt(magnitude1);
        magnitude2 = (float) Math.sqrt(magnitude2);

        if (magnitude1 == 0 || magnitude2 == 0) {
            return 0.0f;
        }

        return dotProduct / (magnitude1 * magnitude2);
    }

    /**
     * Calculate Euclidean distance between two embeddings
     * Lower distance = more similar
     *
     * @param embedding1 First embedding vector
     * @param embedding2 Second embedding vector
     * @return Euclidean distance
     */
    public float euclideanDistance(float[] embedding1, float[] embedding2) {
        if (embedding1 == null || embedding2 == null) {
            return Float.MAX_VALUE;
        }

        if (embedding1.length != embedding2.length) {
            throw new IllegalArgumentException("Embeddings must have the same dimension");
        }

        float sumSquaredDifferences = 0.0f;

        for (int i = 0; i < embedding1.length; i++) {
            float diff = embedding1[i] - embedding2[i];
            sumSquaredDifferences += diff * diff;
        }

        return (float) Math.sqrt(sumSquaredDifferences);
    }

    /**
     * Batch generate embeddings for multiple texts
     *
     * @param texts List of input texts
     * @return List of embedding vectors
     */
    public float[][] generateEmbeddings(java.util.List<String> texts) {
        return texts.stream()
                .map(this::generateEmbedding)
                .toArray(float[][]::new);
    }
}

