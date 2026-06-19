-- Kite session: single row (id=1) tracking the most recent Zerodha Kite Connect login
CREATE TABLE IF NOT EXISTS kite_session (
    id BIGINT PRIMARY KEY,
    user_id VARCHAR(64),
    user_name VARCHAR(128),
    access_token VARCHAR(2000),
    api_key VARCHAR(64),
    login_at TIMESTAMP,
    expires_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Kite order audit: every placeOrder attempt, with request/response payloads and Kite status
CREATE TABLE IF NOT EXISTS kite_order_audit (
    id BIGSERIAL PRIMARY KEY,
    symbol VARCHAR(32) NOT NULL,
    exchange VARCHAR(16),
    transaction_type VARCHAR(8) NOT NULL,
    order_type VARCHAR(16) NOT NULL,
    product VARCHAR(8) NOT NULL,
    quantity INTEGER NOT NULL,
    price DOUBLE PRECISION,
    trigger_price DOUBLE PRECISION,
    tag VARCHAR(64),
    validity VARCHAR(16),
    kite_order_id VARCHAR(64),
    status VARCHAR(2000),
    request_json VARCHAR(4000),
    response_json VARCHAR(4000),
    sandbox BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_kite_order_audit_created_at ON kite_order_audit (created_at);
CREATE INDEX IF NOT EXISTS idx_kite_order_audit_symbol ON kite_order_audit (symbol);
CREATE INDEX IF NOT EXISTS idx_kite_order_audit_transaction_type ON kite_order_audit (transaction_type);
