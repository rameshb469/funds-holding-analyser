# Vector Embeddings: Quick Visual Guide

## The 8 Tables - What's Embedded and Why

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    VECTOR DATABASE ARCHITECTURE                             │
└─────────────────────────────────────────────────────────────────────────────┘

✅ VECTORIZED (6 tables) - Semantic Search Enabled
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

┌─────────────────────┐
│  stock_embeddings   │  📈 Core entity
│  (1536-dim vector)  │  Stable characteristics
│  ✅ VECTORIZED      │  Semantic: "Tech stocks with dividend"
└─────────────────────┘
                
┌─────────────────────┐
│  fund_embeddings    │  💼 Core entity  
│  (1536-dim vector)  │  Fund strategy metadata
│  ✅ VECTORIZED      │  Semantic: "Aggressive growth funds"
└─────────────────────┘

┌─────────────────────┐
│ sector_embeddings   │  🏭 Core entity
│  (1536-dim vector)  │  Sector classification
│  ✅ VECTORIZED      │  Semantic: "IT sector characteristics"
└─────────────────────┘

┌─────────────────────┐
│investment_insights  │  💡 Generated insights
│  (1536-dim vector)  │  AI patterns & anomalies
│  ✅ VECTORIZED      │  Semantic: "Fund X shows uptrend"
└─────────────────────┘

┌─────────────────────┐
│ stock_price_        │  📊 Aggregated patterns
│   patterns          │  UPTREND/DOWNTREND/etc
│  (1536-dim vector)  │  ✅ VECTORIZED
│  ✅ VECTORIZED      │  Semantic: "Similar trends"
└─────────────────────┘

┌─────────────────────┐
│ agent_task_logs     │  🤖 Performance tracking
│  (1536-dim vector)  │  Agent learning
│  ✅ VECTORIZED      │  Semantic: "Successful patterns"
└─────────────────────┘


❌ NOT VECTORIZED (2 tables) - Relational/TimeSeries Approach
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

┌──────────────────────────────────┐
│  mutual_fund_holding             │  ❌ NO VECTORS
│  ├─ fund_id (FK)                 │  
│  ├─ stock_id (FK)                │  Why not?
│  ├─ quantity                      │  • Join table
│  ├─ market_value                  │  • Transactional (changes daily)
│  ├─ net_asset_pct                 │  • Already covered by:
│  └─ at_date                       │    - stock_embeddings
│                                  │    - fund_embeddings
│  ⚡ Use: Relational queries       │    - investment_insights
│     SELECT * FROM holding        │
│     WHERE fund_id = ? AND date=? │
└──────────────────────────────────┘

                              ↓↓↓

┌──────────────────────────────────┐
│  stock_price_history             │  ❌ NO VECTORS
│  ├─ stock_id (FK)                │  
│  ├─ trade_date                   │  Why not?
│  ├─ open_price                   │  • TimeSeries data (100K+ records)
│  ├─ high_price                   │  • Daily changes ≠ semantic similarity
│  ├─ low_price                    │  • Better via TSDB approach
│  ├─ close_price                  │  • Already covered by:
│  └─ volume                       │    - stock_price_patterns
│                                  │    - technical indicators
│  ⚡ Use: Time-series analysis    │
│     SELECT * FROM history        │
│     WHERE stock_id = ?           │
│     AND date BETWEEN ? AND ?     │
└──────────────────────────────────┘
```

---

## Query Examples

### ✅ Find Similar Stocks
```sql
-- Using stock_embeddings with semantic search
SELECT * FROM stock_embeddings 
WHERE embedding <-> 'query_vector'::vector < 0.5
LIMIT 10;
```

### ✅ Find Similar Funds
```sql
-- Using fund_embeddings  
SELECT * FROM fund_embeddings
WHERE embedding <-> 'query_vector'::vector < 0.5
LIMIT 10;
```

### ✅ Find Stocks with Similar Trends
```sql
-- Using stock_price_patterns (which has embeddings)
SELECT * FROM stock_price_patterns
WHERE pattern_embedding <-> 'query_vector'::vector < 0.5
AND pattern_type = 'UPTREND';
```

### ❌ Can't: Find holdings with vector search
```sql
-- ❌ NOT VECTORIZED - Use relational query instead
SELECT * FROM mutual_fund_holding
WHERE fund_id = 123 AND at_date = '2026-06-13';
```

### ❌ Can't: Find price points with vector search  
```sql
-- ❌ NOT VECTORIZED - Use time-series query instead
SELECT * FROM stock_price_history
WHERE stock_id = 456 
AND trade_date BETWEEN '2026-01-01' AND '2026-06-13'
ORDER BY trade_date DESC;
```

---

## Decision Tree: Should We Add Vector Embeddings?

```
                           Need new data?
                                 |
                    ┌────────────┴────────────┐
                    |                         |
                YES: Is it semantic?     NO: Skip vectors
                    |
        ┌───────────┴───────────┐
        |                       |
     YES: Entity or      NO: Transaction or
     insight?            time-series?
        |                       |
        |                   YES: Skip
    CREATE VECTOR        (use relational/TSDB)
    EMBEDDING ✅             
```

### Applied to our tables:

```
stock_embeddings
├─ Entity? YES
├─ Semantic? YES  
├─ Stable? YES
└─ Result: ✅ CREATE VECTOR

mutual_fund_holding
├─ Join table? YES
├─ Transactional? YES
└─ Result: ❌ SKIP (use relational)

stock_price_history
├─ TimeSeries? YES
├─ Granular? YES (100K+ records)
└─ Result: ❌ SKIP (use TSDB)
```

---

## Storage Comparison

### Current: 6 Vector Tables ✅
```
Embeddings: ~50-200 MB
Indexes:    ~30-100 MB
Total:      ~100-300 MB

Query time: 50-100 ms
Accuracy:   ~95-98%
```

### If we added 2 more tables: ❌
```
Embeddings: +100-500 MB
Indexes:    +50-200 MB
Total:      ~300-1000 MB

Query time: 100-200 ms
Accuracy:   ~90-95%
Maintenance: +3-5% CPU
```

---

## When to Add Vector Embeddings

### ✅ DO vectorize:
- Core **entities** (stocks, funds, sectors)
- **Aggregated insights** (patterns, trends)
- **Semantic concepts** (strategy, characteristics)
- **Stable data** (changes infrequently)

### ❌ DON'T vectorize:
- **Join tables** (relationships, holdings)
- **Transactional data** (changes frequently)
- **Time-series data** (historical records)
- **Granular data** (100K+ records)
- **Redundant** data (already in other tables)

---

## Key Takeaway

```
┌──────────────────────────────────────────────────────────────┐
│                                                              │
│  6 core vector tables → Cover all semantic search needs      │
│                                                              │
│  Holdings + History → Use relational & time-series queries   │
│                                                              │
│  Optimal balance between functionality and efficiency ✅     │
│                                                              │
└──────────────────────────────────────────────────────────────┘
```

For detailed explanation, see:
- `VECTOR_EMBEDDINGS_DESIGN_DECISION.md` (comprehensive)
- `README_VECTOR_AI_AGENTS.md` (full documentation)

