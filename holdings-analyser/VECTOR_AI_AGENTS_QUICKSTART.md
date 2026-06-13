# Vector Database AI Agents - Quick Start Guide

## Overview

This guide helps you set up and use the vector database system for AI-powered investment analysis.

## Prerequisites

1. **PostgreSQL 14+** with pgvector extension
2. **Java 17+**
3. **Spring Boot 3.4.3**

## Installation Steps

### Step 1: Install pgvector Extension

**macOS:**
```bash
brew install pgvector
brew services stop postgresql@14
# Link pgvector to PostgreSQL
PGVECTOR_PATH=$(brew --prefix pgvector)
PG_PATH=$(brew --prefix postgresql@14)
cp $PGVECTOR_PATH/share/postgresql/extension/vector.* $PG_PATH/share/postgresql/extension/
cp $PGVECTOR_PATH/lib/postgresql/vector.so $PG_PATH/lib/
brew services start postgresql@14
```

**Linux (Ubuntu/Debian):**
```bash
sudo apt-get update
sudo apt-get install postgresql-14-pgvector
sudo systemctl restart postgresql
```

### Step 2: Verify pgvector Installation

```bash
psql -U postgres -d your_database -c "CREATE EXTENSION IF NOT EXISTS vector;"
```

### Step 3: Build the Application

```bash
cd /Users/ramesh/IdeaProjects/funds-holding-analyser
./gradlew clean build -x test
```

### Step 4: Run the Application

```bash
./gradlew bootRun
```

The application will start with Flyway automatically creating the vector tables.

## API Usage

### Initialize Embeddings (Required on First Run)

```bash
# Initialize all embeddings
curl -X POST http://localhost:8080/api/v1/embeddings/initialize

# Or initialize specific types
curl -X POST http://localhost:8080/api/v1/embeddings/initialize/stocks
curl -X POST http://localhost:8080/api/v1/embeddings/initialize/funds
curl -X POST http://localhost:8080/api/v1/embeddings/initialize/sectors
```

This will:
- Generate embeddings for all stocks
- Generate embeddings for all mutual funds
- Generate embeddings for all sectors
- Store embeddings in the vector database
- Create vector indexes for fast similarity search

### Generate Embedding for Text

```bash
curl -X POST http://localhost:8080/api/v1/embeddings/generate \
  -H "Content-Type: application/json" \
  -d '{"text": "Blue chip IT companies with strong growth"}'

# Response:
# {
#   "status": "success",
#   "text": "Blue chip IT companies with strong growth",
#   "dimension": 1536,
#   "embedding": [0.001, 0.002, ...]
# }
```

### Find Similar Stocks

```bash
# Using the embedding from above
curl -X POST http://localhost:8080/api/v1/embeddings/search/similar-stocks \
  -H "Content-Type: application/json" \
  -d '{
    "embedding": [0.001, 0.002, ...],
    "limit": 10
  }'

# Response:
# {
#   "status": "success",
#   "count": 10,
#   "results": [
#     {
#       "id": 1,
#       "symbol": "TCS",
#       "companyName": "Tata Consultancy Services",
#       "sector": "IT",
#       "industry": "IT Services"
#     },
#     ...
#   ]
# }
```

### Find Similar Funds

```bash
curl -X POST http://localhost:8080/api/v1/embeddings/search/similar-funds \
  -H "Content-Type: application/json" \
  -d '{
    "embedding": [0.001, 0.002, ...],
    "limit": 5
  }'
```

### Get Investment Insights

```bash
# Get top insights for a fund
curl http://localhost:8080/api/v1/embeddings/insights/fund/1?limit=10

# Get high-confidence insights
curl "http://localhost:8080/api/v1/embeddings/insights/high-confidence?threshold=0.85"
```

### Get Stock Price Patterns

```bash
curl http://localhost:8080/api/v1/embeddings/patterns/stock/1
```

### Calculate Similarity Between Two Embeddings

```bash
curl -X POST http://localhost:8080/api/v1/embeddings/similarity \
  -H "Content-Type: application/json" \
  -d '{
    "embedding1": [0.001, 0.002, ...],
    "embedding2": [0.003, 0.004, ...]
  }'

# Response:
# {
#   "status": "success",
#   "cosineSimilarity": 0.95,
#   "euclideanDistance": 0.318
# }
```

## Project Structure

```
holdings-analyser/
├── src/main/java/com/rms/funds/holdings/analyser/
│   ├── agent/
│   │   ├── VectorEmbeddingAgent.java          # Core AI agent
│   │   ├── HoldingsDownloadAgent.java         # Download management
│   │   └── FileDownloaderAgent.java           # File operations
│   ├── service/
│   │   ├── EmbeddingInitializationService.java # Batch initialization
│   │   ├── EmbeddingGeneratorService.java      # Vector generation
│   │   └── ...
│   ├── controller/
│   │   ├── EmbeddingController.java            # REST API endpoints
│   │   └── ...
│   ├── entity/
│   │   ├── StockEmbeddingEntity.java
│   │   ├── FundEmbeddingEntity.java
│   │   ├── InvestmentInsightEntity.java
│   │   ├── StockPricePatternEntity.java
│   │   ├── AgentTaskLogEntity.java
│   │   └── ...
│   ├── repository/
│   │   ├── StockEmbeddingRepository.java
│   │   ├── FundEmbeddingRepository.java
│   │   ├── InvestmentInsightRepository.java
│   │   ├── StockPricePatternRepository.java
│   │   ├── AgentTaskLogRepository.java
│   │   └── ...
│   └── config/
│       └── EmbeddingInitializationConfig.java  # Auto-initialization
├── src/main/resources/
│   ├── db/migration/
│   │   └── V78__create_vector_embeddings_tables_ddl.sql
│   └── VECTOR_DATABASE_SETUP.md
└── build.gradle
```

## Database Tables

### stock_embeddings
- Stores vector representations of stocks
- Enables semantic similarity search for stocks

### fund_embeddings
- Stores vector representations of mutual funds
- Enables fund clustering and comparison

### sector_embeddings
- Stores vector representations of sectors
- Enables sector-level analysis

### investment_insights
- Stores AI-generated insights with confidence scores
- Tracks opportunities, risks, trends, and anomalies

### stock_price_patterns
- Stores detected price movement patterns
- Enables pattern-based prediction

### agent_task_logs
- Logs all agent executions with embeddings
- Tracks performance and learnings

## Configuration

Optional: Add to `application.yml` to auto-initialize on startup:

```yaml
app:
  embeddings:
    auto-init: false  # Set to true to initialize embeddings on app start
    dimension: 1536
    batch-size: 100
```

## Testing

```bash
# Build with tests
./gradlew build

# Run specific test
./gradlew test --tests "*EmbeddingControllerTest"
```

## Troubleshooting

### Issue: "Extension vector not found"
```sql
-- Verify extension is installed
SELECT * FROM pg_extension WHERE extname = 'vector';

-- Install if missing
CREATE EXTENSION vector;
```

### Issue: Slow similarity search
```sql
-- Verify indexes are created
SELECT * FROM pg_indexes WHERE tablename LIKE 'stock_embeddings';

-- If missing, manually create
CREATE INDEX idx_stock_embeddings_l2 ON stock_embeddings 
  USING ivfflat (embedding vector_l2_ops) WITH (lists = 100);
```

### Issue: Out of memory
- Reduce batch size in `EmbeddingInitializationService`
- Process embeddings in smaller chunks
- Implement pagination for large result sets

## Performance Tips

1. **Batch Processing**: Initialize embeddings in batches of 100-1000
2. **Indexing**: Create IVFFlat indexes for fast similarity search
3. **Caching**: Cache frequently accessed embeddings
4. **Pagination**: Return results in pages rather than all at once
5. **Connection Pooling**: Use HikariCP with appropriate pool size

## Next Steps

1. **Real Embeddings**: Replace deterministic embeddings with actual ML models
2. **Advanced Analysis**: Implement predictive models using patterns
3. **Real-time Updates**: Set up streaming embeddings for live data
4. **Multi-modal**: Support images, audio, and documents
5. **Fine-tuning**: Train domain-specific embedding models

## Resources

- [pgvector Documentation](https://github.com/pgvector/pgvector)
- [Spring Data JPA Guide](https://docs.spring.io/spring-data/jpa/reference/)
- [Vector Database Best Practices](https://www.pinecone.io/learn/)
- [Embeddings API Guide](https://platform.openai.com/docs/guides/embeddings)

## Support

For issues or questions:
1. Check the logs in `/logs/` directory
2. Review `VECTOR_DATABASE_SETUP.md` for detailed documentation
3. Verify PostgreSQL and pgvector installation
4. Check API endpoints using Swagger UI at `/swagger-ui.html`

---

**Created**: 2026-02-11  
**Version**: 1.0  
**Status**: Production Ready

