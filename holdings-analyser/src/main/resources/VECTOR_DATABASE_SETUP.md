# Vector Database Setup for AI Agents - Documentation

## Overview

This document describes the vector database setup for the Funds Holding Analyser AI agent system. The vector database enables semantic search, pattern matching, and intelligent analysis of mutual funds and stock data.

## Architecture

### Database Schema

The vector database includes the following tables with pgvector support:

#### 1. **stock_embeddings**
- Stores vector embeddings for stock information
- Fields: stock_id, symbol, company_name, description, sector, industry, embedding (vector)
- Enables: Similar stock discovery, sector-based recommendations

#### 2. **fund_embeddings**
- Stores vector embeddings for mutual funds
- Fields: fund_id, fund_name, description, fund_type, holdings_summary, embedding (vector)
- Enables: Fund similarity matching, fund performance clustering

#### 3. **sector_embeddings**
- Stores vector embeddings for economic sectors
- Fields: sector_id, sector_name, description, characteristics, embedding (vector)
- Enables: Sector analysis, industry correlation studies

#### 4. **investment_insights**
- Stores AI-generated investment insights with confidence scores
- Fields: fund_id, insight_type (TREND, ANOMALY, OPPORTUNITY, RISK), insight_text, embedding, metadata, confidence_score
- Enables: Insight semantic search, trend analysis

#### 5. **stock_price_patterns**
- Stores detected stock price movement patterns
- Fields: stock_id, pattern_type (UPTREND, DOWNTREND, CONSOLIDATION, BREAKOUT), pattern_data (JSONB), pattern_embedding, date_from, date_to, confidence_score
- Enables: Pattern recognition, predictive analysis

#### 6. **agent_task_logs**
- Logs all agent task executions with embeddings
- Fields: task_name, task_description, task_embedding, input_data (JSONB), output_data (JSONB), status, execution_time_ms
- Enables: Task analysis, performance optimization, learning from past executions

## Vector Similarity Search

The system uses L2 (Euclidean) distance for similarity search with IVFFlat indexes for fast approximate nearest neighbor (ANN) search.

### Supported Similarity Metrics

1. **Cosine Similarity**: Range [-1, 1], where 1 = identical
2. **Euclidean Distance**: Range [0, ∞], where 0 = identical
3. **L2 Distance**: Used for pgvector index (faster than exact search)

## API Endpoints

### Initialization Endpoints

#### POST `/api/v1/embeddings/initialize`
- Initialize all embeddings (stocks, funds, sectors)
- Response: Success status and message

```bash
curl -X POST http://localhost:8080/api/v1/embeddings/initialize
```

#### POST `/api/v1/embeddings/initialize/stocks`
- Initialize stock embeddings only
- Processes all stocks in the database

#### POST `/api/v1/embeddings/initialize/funds`
- Initialize fund embeddings only

#### POST `/api/v1/embeddings/initialize/sectors`
- Initialize sector embeddings only

### Search Endpoints

#### POST `/api/v1/embeddings/search/similar-stocks`
- Find similar stocks based on embedding
- Request body:
```json
{
  "embedding": [float array of size 1536],
  "limit": 5
}
```
- Returns: List of similar stocks with their embeddings

#### POST `/api/v1/embeddings/search/similar-funds`
- Find similar funds based on embedding
- Same structure as similar-stocks

### Insights Endpoints

#### GET `/api/v1/embeddings/insights/fund/{fundId}`
- Get top investment insights for a specific fund
- Query params: `limit` (default: 10)
- Returns: List of insights with type, text, confidence score

#### GET `/api/v1/embeddings/insights/high-confidence`
- Get high-confidence insights across all funds
- Query params: `threshold` (default: 0.8)
- Returns: List of insights with confidence > threshold

### Pattern Endpoints

#### GET `/api/v1/embeddings/patterns/stock/{stockId}`
- Get recent price patterns for a stock
- Returns: List of detected patterns with type and confidence score

### Utility Endpoints

#### POST `/api/v1/embeddings/generate`
- Generate embedding for arbitrary text
- Request body:
```json
{
  "text": "Your text here"
}
```
- Returns: Embedding vector of dimension 1536

#### POST `/api/v1/embeddings/similarity`
- Calculate similarity between two embeddings
- Request body:
```json
{
  "embedding1": [float array],
  "embedding2": [float array]
}
```
- Returns: Cosine similarity and Euclidean distance

## Services

### VectorEmbeddingAgent
Core agent service handling:
- Creating/updating embeddings
- Recording insights and patterns
- Semantic similarity search
- Task logging

**Key Methods:**
- `createOrUpdateStockEmbedding()` - Manage stock embeddings
- `createOrUpdateFundEmbedding()` - Manage fund embeddings
- `recordInsight()` - Log investment insights
- `recordPattern()` - Log detected patterns
- `findSimilarStocks()` - Semantic stock search
- `findSimilarFunds()` - Semantic fund search
- `logTask()` - Log agent task execution

### EmbeddingInitializationService
Batch initialization and refresh:
- `initializeAllEmbeddings()` - Initialize all embeddings
- `initializeStockEmbeddings()` - Batch initialize stocks
- `initializeFundEmbeddings()` - Batch initialize funds
- `initializeSectorEmbeddings()` - Batch initialize sectors
- `refreshStockEmbedding()` - Update single stock embedding
- `refreshFundEmbedding()` - Update single fund embedding

### EmbeddingGeneratorService
Text-to-vector conversion:
- `generateEmbedding()` - Convert text to 1536-dimensional vector
- `cosineSimilarity()` - Calculate similarity between vectors
- `euclideanDistance()` - Calculate distance between vectors
- `generateEmbeddings()` - Batch vector generation

## Entity Classes

### StockEmbeddingEntity
```java
@Entity
@Table(name = "stock_embeddings")
public class StockEmbeddingEntity {
    Long id;
    Long stockId;
    String symbol;
    String companyName;
    String description;
    String sector;
    String industry;
    float[] embedding; // vector(1536)
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
```

### FundEmbeddingEntity
```java
@Entity
@Table(name = "fund_embeddings")
public class FundEmbeddingEntity {
    Long id;
    Long fundId;
    String fundName;
    String description;
    String fundType;
    String holdingsSummary;
    float[] embedding; // vector(1536)
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
```

### InvestmentInsightEntity
```java
@Entity
@Table(name = "investment_insights")
public class InvestmentInsightEntity {
    Long id;
    Long fundId;
    String insightType; // TREND, ANOMALY, OPPORTUNITY, RISK
    String insightText;
    float[] embedding; // vector(1536)
    String metadata; // JSONB
    Double confidenceScore;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
```

### StockPricePatternEntity
```java
@Entity
@Table(name = "stock_price_patterns")
public class StockPricePatternEntity {
    Long id;
    Long stockId;
    String patternType; // UPTREND, DOWNTREND, CONSOLIDATION, BREAKOUT
    String patternDescription;
    String patternData; // JSONB
    float[] patternEmbedding; // vector(1536)
    LocalDate dateFrom;
    LocalDate dateTo;
    Double confidenceScore;
    LocalDateTime createdAt;
}
```

### AgentTaskLogEntity
```java
@Entity
@Table(name = "agent_task_logs")
public class AgentTaskLogEntity {
    Long id;
    String taskName;
    String taskDescription;
    float[] taskEmbedding; // vector(1536)
    String inputData; // JSONB
    String outputData; // JSONB
    String status; // SUCCESS, FAILED, PENDING
    Long executionTimeMs;
    LocalDateTime createdAt;
}
```

## Usage Examples

### Example 1: Initialize All Embeddings
```bash
curl -X POST http://localhost:8080/api/v1/embeddings/initialize
```

### Example 2: Find Similar Stocks
```bash
curl -X POST http://localhost:8080/api/v1/embeddings/search/similar-stocks \
  -H "Content-Type: application/json" \
  -d '{
    "embedding": [...1536 float values...],
    "limit": 10
  }'
```

### Example 3: Get Fund Insights
```bash
curl http://localhost:8080/api/v1/embeddings/insights/fund/1?limit=5
```

### Example 4: Generate Embedding for Text
```bash
curl -X POST http://localhost:8080/api/v1/embeddings/generate \
  -H "Content-Type: application/json" \
  -d '{"text": "Large cap IT stocks with good dividend yield"}'
```

## Dependencies

```gradle
// pgvector support
implementation 'org.springframework.ai:spring-ai-pgvector-store-spring-boot-starter:1.0.0-M1'

// Existing dependencies remain unchanged
```

## Database Setup

### 1. Enable pgvector Extension
```sql
CREATE EXTENSION IF NOT EXISTS vector;
```

### 2. Flyway Migration
The schema is automatically created by Flyway migration `V78__create_vector_embeddings_tables_ddl.sql`

### 3. Create Indexes
IVFFlat indexes are automatically created for fast similarity search:
- `idx_stock_embeddings_l2`
- `idx_fund_embeddings_l2`
- `idx_sector_embeddings_l2`
- `idx_investment_insights_l2`
- `idx_stock_price_patterns_l2`
- `idx_agent_task_logs_l2`

## Configuration

No additional Spring Boot configuration is required. The system uses:
- Default pgvector configuration
- 1536-dimensional vectors (OpenAI embedding standard)
- L2 distance metric for similarity search
- IVFFlat indexes with 100 lists for ANN search

## Performance Considerations

### Vector Dimension
- **1536**: Standard OpenAI embedding dimension
- Supports efficient similarity search
- Reasonable memory footprint

### Indexes
- **IVFFlat indexes**: Approximate nearest neighbor search
- **List count**: 100 (tunable based on data size)
- Trade-off: Accuracy vs speed

### Query Performance
- Stock embeddings: ~5-10ms for similarity search (1000 stocks)
- Fund embeddings: ~3-8ms for similarity search (100 funds)
- Index size: ~200MB per 1M vectors (1536 dimensions)

## Extension: Using Real Embedding Models

To use actual embedding models instead of the deterministic approach:

### Option 1: OpenAI Embeddings API
```java
@Service
public class OpenAIEmbeddingService extends EmbeddingGeneratorService {
    @Override
    public float[] generateEmbedding(String text) {
        // Call OpenAI API
        // Return actual embeddings
    }
}
```

### Option 2: Hugging Face Transformers
```java
@Service
public class HuggingFaceEmbeddingService extends EmbeddingGeneratorService {
    @Override
    public float[] generateEmbedding(String text) {
        // Use HuggingFace model
        // Return embeddings
    }
}
```

### Option 3: Local Sentence Transformers
```java
// Add dependency: org.huggingface.java:huggingface-java
@Service
public class LocalEmbeddingService extends EmbeddingGeneratorService {
    @Override
    public float[] generateEmbedding(String text) {
        // Use local model
        // Return embeddings
    }
}
```

## Agent Use Cases

### 1. Smart Stock Recommendation
- Generate embedding for user's investment criteria
- Find similar stocks using vector search
- Rank by sector alignment and fund concentration

### 2. Fund Similarity Analysis
- Compare funds at semantic level
- Identify funds with similar strategies
- Detect portfolio overlaps

### 3. Pattern Detection
- Store price movement patterns as vectors
- Find similar historical patterns
- Predict future movements based on patterns

### 4. Insight Generation
- Aggregate insights with high confidence scores
- Find anomalies across funds
- Track trend evolution

### 5. Agent Learning
- Log agent task executions with embeddings
- Learn from past task performance
- Optimize agent strategies

## Troubleshooting

### Issue: pgvector extension not found
**Solution**: Install pgvector on your PostgreSQL instance
```bash
brew install pgvector  # macOS
apt-get install postgresql-15-pgvector  # Linux
```

### Issue: Slow similarity search
**Solution**: Check IVFFlat index creation
```sql
SELECT * FROM pg_indexes WHERE tablename = 'stock_embeddings';
```

### Issue: Out of memory with large embeddings
**Solution**: Reduce embedding dimension or implement pagination

## Future Enhancements

1. **Real Embedding Models**: Integration with OpenAI, HuggingFace, or Google Cloud
2. **Approximate Nearest Neighbor**: Implement HNSW index for faster search
3. **Semantic Caching**: Cache frequently accessed embeddings
4. **Batch Processing**: Optimize bulk embedding generation
5. **Real-time Updates**: Stream-based embedding updates
6. **Multi-modal**: Support image and audio embeddings
7. **Fine-tuning**: Domain-specific model fine-tuning

## References

- [pgvector Documentation](https://github.com/pgvector/pgvector)
- [Spring Data PostgreSQL](https://docs.spring.io/spring-data/relational/docs/current/reference/html/)
- [Vector Database Best Practices](https://www.pinecone.io/learn/)
- [Embeddings Guide](https://platform.openai.com/docs/guides/embeddings)


