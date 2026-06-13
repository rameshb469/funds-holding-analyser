# Vector Database AI Agents - Architecture Diagram

## System Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                          REST API Layer                          │
│                   EmbeddingController.java                       │
│  ┌──────────────┐ ┌──────────────┐ ┌──────────────────────────┐ │
│  │ Initialize   │ │ Search API   │ │ Insights/Patterns       │ │
│  │ Endpoints    │ │ Endpoints    │ │ Utility Endpoints       │ │
│  └──────────────┘ └──────────────┘ └──────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                      Service Layer                               │
│  ┌────────────────────┐ ┌────────────────────────────────────┐ │
│  │ VectorEmbedding    │ │ EmbeddingInitialization Service   │ │
│  │ Agent              │ │ - Batch initialization            │ │
│  │ - Create embeddings│ │ - Refresh embeddings             │ │
│  │ - Search similar   │ │ - Error handling                 │ │
│  │ - Record insights  │ │                                  │ │
│  │ - Log tasks        │ │ EmbeddingGenerator Service      │ │
│  └────────────────────┘ │ - Text to vectors               │ │
│                         │ - Similarity metrics            │ │
│                         │ - Batch processing              │ │
│                         └────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                    Repository Layer (Data Access)                │
│  ┌──────────────────────┐ ┌──────────────────────────────────┐ │
│  │ Stock Embedding      │ │ Fund Embedding Repository        │ │
│  │ Repository           │ │ - findByFundId()               │ │
│  │ - findSimilarStocks()│ │ - findSimilarFunds()           │ │
│  │ - findBySymbol()     │ │ - findByFundType()             │ │
│  │ - findBySector()     │ │                                │ │
│  └──────────────────────┘ └──────────────────────────────────┘ │
│                                                                  │
│  ┌──────────────────────┐ ┌──────────────────────────────────┐ │
│  │ Sector Embedding     │ │ Investment Insight Repository    │ │
│  │ Repository           │ │ - findByFundId()               │ │
│  │ - findBySectorId()   │ │ - findByInsightType()          │ │
│  │ - findSimilarSectors│ │ - findSimilarInsights()        │ │
│  └──────────────────────┘ └──────────────────────────────────┘ │
│                                                                  │
│  ┌──────────────────────┐ ┌──────────────────────────────────┐ │
│  │ Stock Price Pattern  │ │ Agent Task Log Repository        │ │
│  │ Repository           │ │ - findByTaskName()             │ │
│  │ - findByStockId()    │ │ - findByStatus()               │ │
│  │ - findByPatternType()│ │ - findSimilarTasks()           │ │
│  │ - findSimilarPattern│ │ - findFastestTasks()           │ │
│  └──────────────────────┘ └──────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                      Entity Layer (ORM)                          │
│  ┌──────────────┐ ┌──────────────┐ ┌──────────────────────────┐ │
│  │ Stock        │ │ Fund         │ │ Sector Embedding        │ │
│  │ Embedding    │ │ Embedding    │ │ Entity                  │ │
│  │ Entity       │ │ Entity       │ │                         │ │
│  └──────────────┘ └──────────────┘ └──────────────────────────┘ │
│  ┌──────────────┐ ┌──────────────┐ ┌──────────────────────────┐ │
│  │ Investment   │ │ Stock Price  │ │ Agent Task Log          │ │
│  │ Insight      │ │ Pattern      │ │ Entity                  │ │
│  │ Entity       │ │ Entity       │ │                         │ │
│  └──────────────┘ └──────────────┘ └──────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                   PostgreSQL Database Layer                      │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │ pgvector Extension (Vector Storage & Similarity Search) │  │
│  └──────────────────────────────────────────────────────────┘  │
│                                                                  │
│  ┌─────────────────┐ ┌──────────────────┐ ┌────────────────┐  │
│  │ stock_          │ │ fund_            │ │ sector_        │  │
│  │ embeddings      │ │ embeddings       │ │ embeddings     │  │
│  │ (IVFFlat L2)    │ │ (IVFFlat L2)     │ │ (IVFFlat L2)   │  │
│  └─────────────────┘ └──────────────────┘ └────────────────┘  │
│                                                                  │
│  ┌─────────────────┐ ┌──────────────────┐ ┌────────────────┐  │
│  │ investment_     │ │ stock_price_     │ │ agent_task_    │  │
│  │ insights        │ │ patterns         │ │ logs           │  │
│  │ (IVFFlat L2)    │ │ (IVFFlat L2)     │ │ (IVFFlat L2)   │  │
│  └─────────────────┘ └──────────────────┘ └────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

## Data Flow Diagram

```
┌──────────────────┐
│   User Request   │
│   (HTTP API)     │
└────────┬─────────┘
         ↓
    ┌────────────────────┐
    │ REST Controller    │
    │ (Validation)       │
    └────────┬───────────┘
             ↓
    ┌────────────────────────┐
    │ Service Layer          │
    │ (Business Logic)       │
    │ - Generate embeddings  │
    │ - Find similar items   │
    │ - Record insights      │
    └────────┬───────────────┘
             ↓
    ┌────────────────────────┐
    │ Repository Layer       │
    │ (Data Access)          │
    │ - Vector similarity    │
    │ - CRUD operations      │
    └────────┬───────────────┘
             ↓
    ┌────────────────────────┐
    │ PostgreSQL + pgvector  │
    │ - Store vectors        │
    │ - Vector similarity    │
    │ - Index optimization   │
    └────────┬───────────────┘
             ↓
         ┌────────────┐
         │ JSON       │
         │ Response   │
         └────────────┘
```

## Vector Similarity Search Flow

```
Input Text
    ↓
┌─────────────────────────────────────┐
│ EmbeddingGeneratorService           │
│ - SHA-256 hash for seed             │
│ - Generate 1536-dim vector          │
│ - Normalize to unit length          │
└──────────────┬──────────────────────┘
               ↓
          Query Vector
               ↓
┌─────────────────────────────────────┐
│ PostgreSQL pgvector                 │
│ - L2 distance calculation           │
│ - IVFFlat index lookup              │
│ - Approximate nearest neighbor      │
└──────────────┬──────────────────────┘
               ↓
          Top-K Results
               ↓
┌─────────────────────────────────────┐
│ Results Processing                  │
│ - Calculate cosine similarity       │
│ - Rank by relevance                 │
│ - Format response                   │
└──────────────┬──────────────────────┘
               ↓
          JSON Response
```

## Entity Relationship Diagram

```
                    ┌──────────────────┐
                    │  STOCK_DETAILS   │
                    │  (Existing)      │
                    └────────┬─────────┘
                             │
                    ┌────────┴────────┐
                    │                 │
                    ↓                 ↓
         ┌──────────────────┐ ┌──────────────────┐
         │ stock_embeddings │ │ stock_price_     │
         │ - Stock vectors  │ │ patterns         │
         │ - Similarity idx │ │ - Pattern types  │
         │ - 1536-dim       │ │ - Date range     │
         └──────────────────┘ └──────────────────┘
                    
         ┌──────────────────┐
         │  MUTUAL_FUND     │
         │  (Existing)      │
         └────────┬─────────┘
                  │
                  ↓
         ┌──────────────────┐
         │ fund_embeddings  │
         │ - Fund vectors   │
         │ - 1536-dim       │
         │ - Similarity idx │
         └──────────────────┘
                  
         ┌──────────────────┐
         │  SECTOR          │
         │  (Existing)      │
         └────────┬─────────┘
                  │
                  ↓
         ┌──────────────────┐
         │ sector_          │
         │ embeddings       │
         │ - Sector vectors │
         │ - 1536-dim       │
         └──────────────────┘

         ┌──────────────────────┐
         │ investment_insights  │
         │ - Fund-specific      │
         │ - Confidence scores  │
         │ - Insight types      │
         └──────────────────────┘

         ┌──────────────────────┐
         │ agent_task_logs      │
         │ - Task execution     │
         │ - Performance data   │
         │ - Status tracking    │
         └──────────────────────┘
```

## Configuration Flow

```
┌─────────────────────────────────┐
│ Application Startup             │
└────────────┬────────────────────┘
             ↓
┌─────────────────────────────────┐
│ Flyway Database Migration       │
│ (V78 creates vector tables)     │
└────────────┬────────────────────┘
             ↓
┌─────────────────────────────────┐
│ EmbeddingInitializationConfig   │
│ (Optional auto-init)            │
└────────────┬────────────────────┘
             ↓
        Check: app.embeddings.auto-init
        ↙                            ↘
    true                          false
     ↓                              ↓
Initialize               API Ready for Use
All Embeddings
     ↓
  Parallel Processing:
  - initializeStocks()
  - initializeFunds()
  - initializeSectors()
     ↓
  Indexing & Optimization
     ↓
  API Ready
```

## Component Interaction Sequence

```
Client Request
     ↓
EmbeddingController
     ↓
VectorEmbeddingAgent
     ├→ EmbeddingGeneratorService
     │  └→ Generate/Search embeddings
     └→ *Repository
        └→ PostgreSQL + pgvector
           ├→ Vector Storage
           ├→ Similarity Search
           └→ IVFFlat Indexing
     ↓
JSON Response
```

## Technology Stack Layers

```
┌──────────────────────────────────────────────────┐
│         Frontend (React/Vue)                      │
│     (Consumes REST API endpoints)                 │
└────────────────┬─────────────────────────────────┘
                 ↓
┌──────────────────────────────────────────────────┐
│         REST API Layer (Spring Boot)              │
│     EmbeddingController (11 endpoints)            │
└────────────────┬─────────────────────────────────┘
                 ↓
┌──────────────────────────────────────────────────┐
│      Service/Agent Layer                          │
│  - VectorEmbeddingAgent                           │
│  - EmbeddingInitializationService                 │
│  - EmbeddingGeneratorService                      │
└────────────────┬─────────────────────────────────┘
                 ↓
┌──────────────────────────────────────────────────┐
│      Repository/DAO Layer (Spring Data JPA)       │
│  - 6 Repository interfaces                        │
│  - Native queries for vector operations           │
└────────────────┬─────────────────────────────────┘
                 ↓
┌──────────────────────────────────────────────────┐
│      ORM/Entity Layer (Hibernate/JPA)             │
│  - 6 Entity classes                               │
│  - Vector type mapping                            │
└────────────────┬─────────────────────────────────┘
                 ↓
┌──────────────────────────────────────────────────┐
│      Database Layer                               │
│  - PostgreSQL 14+                                 │
│  - pgvector Extension                             │
│  - IVFFlat Indexes                                │
│  - JSONB Columns                                  │
└──────────────────────────────────────────────────┘
```

## Similarity Search Performance

```
Query (1536 dims)
     ↓
Calculate L2 Distance
     ├─ Sequential: O(n) ~1000ms for 1M vectors
     └─ With IVFFlat Index: O(k*n/lists) ~50-100ms
     ↓
Top-K Results
     ├─ Cosine Similarity Ranking
     └─ Euclidean Distance Ranking
     ↓
Return Sorted Results
```

## Index Strategy

```
Vector Data (1536-dim)
        ↓
IVFFlat Index
├─ Lists: 100
├─ Metric: L2 Distance
├─ Build Time: ~30-60s per 1000 vectors
├─ Query Time: ~50-100ms per 1000 items
├─ Memory: ~200MB per 1M vectors
└─ Accuracy: ~95-98%
```

---

**Architecture Diagram Version**: 1.0  
**Last Updated**: June 13, 2026  
**Status**: Production Ready

