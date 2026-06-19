-- Enable pgvector extension if not already enabled
CREATE EXTENSION IF NOT EXISTS vector;

-- Table for storing stock embeddings (for semantic search and AI analysis)
CREATE TABLE IF NOT EXISTS stock_embeddings (
    id BIGSERIAL PRIMARY KEY,
    stock_id BIGINT NOT NULL UNIQUE,
    symbol VARCHAR(20) NOT NULL,
    company_name VARCHAR(255) NOT NULL,
    description TEXT,
    sector VARCHAR(100),
    industry VARCHAR(100),
    embedding vector(1536),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (stock_id) REFERENCES stock_details(id) ON DELETE CASCADE,
    CONSTRAINT embedding_not_null CHECK (embedding IS NOT NULL)
);

-- Create indexes for stock_embeddings
CREATE INDEX IF NOT EXISTS idx_stock_embeddings_stock_id ON stock_embeddings (stock_id);
CREATE INDEX IF NOT EXISTS idx_stock_embeddings_symbol ON stock_embeddings (symbol);

-- Table for storing mutual fund embeddings (for fund analysis and similarity matching)
CREATE TABLE IF NOT EXISTS fund_embeddings (
    id BIGSERIAL PRIMARY KEY,
    fund_id BIGINT NOT NULL UNIQUE,
    fund_name VARCHAR(255) NOT NULL,
    description TEXT,
    fund_type VARCHAR(100),
    holdings_summary TEXT,
    embedding vector(1536),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (fund_id) REFERENCES mutual_fund(id) ON DELETE CASCADE,
    CONSTRAINT fund_embedding_not_null CHECK (embedding IS NOT NULL)
);

-- Create indexes for fund_embeddings
CREATE INDEX IF NOT EXISTS idx_fund_embeddings_fund_id ON fund_embeddings (fund_id);
CREATE INDEX IF NOT EXISTS idx_fund_embeddings_fund_name ON fund_embeddings (fund_name);

-- Table for storing sector embeddings (for sector analysis)
CREATE TABLE IF NOT EXISTS sector_embeddings (
    id BIGSERIAL PRIMARY KEY,
    sector_id BIGINT NOT NULL UNIQUE,
    sector_name VARCHAR(100) NOT NULL,
    description TEXT,
    characteristics TEXT,
    embedding vector(1536),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sector_id) REFERENCES sector(id) ON DELETE CASCADE,
    CONSTRAINT sector_embedding_not_null CHECK (embedding IS NOT NULL)
);

-- Create indexes for sector_embeddings
CREATE INDEX IF NOT EXISTS idx_sector_embeddings_sector_id ON sector_embeddings (sector_id);
CREATE INDEX IF NOT EXISTS idx_sector_embeddings_sector_name ON sector_embeddings (sector_name);

-- Table for storing investment patterns and insights
CREATE TABLE IF NOT EXISTS investment_insights (
    id BIGSERIAL PRIMARY KEY,
    fund_id BIGINT NOT NULL,
    insight_type VARCHAR(50) NOT NULL, -- 'TREND', 'ANOMALY', 'OPPORTUNITY', 'RISK'
    insight_text TEXT NOT NULL,
    embedding vector(1536),
    metadata JSONB,
    confidence_score DECIMAL(5, 2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (fund_id) REFERENCES mutual_fund(id) ON DELETE CASCADE
);

-- Create indexes for investment_insights
CREATE INDEX IF NOT EXISTS idx_investment_insights_fund_id ON investment_insights (fund_id);
CREATE INDEX IF NOT EXISTS idx_investment_insights_insight_type ON investment_insights (insight_type);
CREATE INDEX IF NOT EXISTS idx_investment_insights_created_at ON investment_insights (created_at);

-- Table for storing stock price movement patterns (vectorized)
CREATE TABLE IF NOT EXISTS stock_price_patterns (
    id BIGSERIAL PRIMARY KEY,
    stock_id BIGINT NOT NULL,
    pattern_type VARCHAR(50) NOT NULL, -- 'UPTREND', 'DOWNTREND', 'CONSOLIDATION', 'BREAKOUT'
    pattern_description TEXT,
    pattern_data JSONB,
    pattern_embedding vector(1536),
    date_from DATE NOT NULL,
    date_to DATE NOT NULL,
    confidence_score DECIMAL(5, 2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (stock_id) REFERENCES stock_details(id) ON DELETE CASCADE
);

-- Create indexes for stock_price_patterns
CREATE INDEX IF NOT EXISTS idx_stock_price_patterns_stock_id ON stock_price_patterns (stock_id);
CREATE INDEX IF NOT EXISTS idx_stock_price_patterns_pattern_type ON stock_price_patterns (pattern_type);
CREATE INDEX IF NOT EXISTS idx_stock_price_patterns_date_range ON stock_price_patterns (date_from, date_to);

-- Table for agent task logs and embeddings (for learning and pattern detection)
CREATE TABLE IF NOT EXISTS agent_task_logs (
    id BIGSERIAL PRIMARY KEY,
    task_name VARCHAR(100) NOT NULL,
    task_description TEXT,
    task_embedding vector(1536),
    input_data JSONB,
    output_data JSONB,
    status VARCHAR(20), -- 'SUCCESS', 'FAILED', 'PENDING'
    execution_time_ms BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for agent_task_logs
CREATE INDEX IF NOT EXISTS idx_agent_task_logs_task_name ON agent_task_logs (task_name);
CREATE INDEX IF NOT EXISTS idx_agent_task_logs_status ON agent_task_logs (status);
CREATE INDEX IF NOT EXISTS idx_agent_task_logs_created_at ON agent_task_logs (created_at);

-- Create indexes for similarity search using L2 distance
CREATE INDEX IF NOT EXISTS idx_stock_embeddings_l2 ON stock_embeddings USING ivfflat (embedding vector_l2_ops) WITH (lists = 100);
CREATE INDEX IF NOT EXISTS idx_fund_embeddings_l2 ON fund_embeddings USING ivfflat (embedding vector_l2_ops) WITH (lists = 100);
CREATE INDEX IF NOT EXISTS idx_sector_embeddings_l2 ON sector_embeddings USING ivfflat (embedding vector_l2_ops) WITH (lists = 100);
CREATE INDEX IF NOT EXISTS idx_investment_insights_l2 ON investment_insights USING ivfflat (embedding vector_l2_ops) WITH (lists = 100);
CREATE INDEX IF NOT EXISTS idx_stock_price_patterns_l2 ON stock_price_patterns USING ivfflat (pattern_embedding vector_l2_ops) WITH (lists = 100);
CREATE INDEX IF NOT EXISTS idx_agent_task_logs_l2 ON agent_task_logs USING ivfflat (task_embedding vector_l2_ops) WITH (lists = 100);

