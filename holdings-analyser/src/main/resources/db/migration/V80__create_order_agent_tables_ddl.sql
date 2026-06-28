-- Order-execution agent: cycles, positions, and NSE holiday calendar.

CREATE TABLE IF NOT EXISTS nse_holiday (
    id BIGSERIAL PRIMARY KEY,
    holiday_date DATE NOT NULL UNIQUE,
    description VARCHAR(256),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS order_agent_cycle (
    id BIGSERIAL PRIMARY KEY,
    cycle_anchor_date DATE NOT NULL,
    target_trade_date DATE NOT NULL,
    actual_trade_date DATE,
    status VARCHAR(32) NOT NULL,
    skip_reason VARCHAR(256),
    picks_json VARCHAR(4000),
    broker_order_ids VARCHAR(512),
    closed_at TIMESTAMP,
    realised_pnl DOUBLE PRECISION,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_order_agent_cycle_anchor
    ON order_agent_cycle (cycle_anchor_date);

CREATE INDEX IF NOT EXISTS idx_order_agent_cycle_status
    ON order_agent_cycle (status);

CREATE TABLE IF NOT EXISTS order_agent_position (
    id BIGSERIAL PRIMARY KEY,
    cycle_id BIGINT NOT NULL REFERENCES order_agent_cycle(id),
    stock_id BIGINT NOT NULL,
    symbol VARCHAR(32) NOT NULL,
    exchange VARCHAR(16) NOT NULL DEFAULT 'NSE',
    qty INTEGER NOT NULL,
    entry_price DOUBLE PRECISION NOT NULL,
    entry_order_id VARCHAR(64),
    stop_loss_order_id VARCHAR(64),
    trail_high DOUBLE PRECISION NOT NULL,
    ladder_level SMALLINT NOT NULL DEFAULT 0,
    ladder_exit_order_ids VARCHAR(512),
    status VARCHAR(32) NOT NULL,
    close_price DOUBLE PRECISION,
    realised_pnl DOUBLE PRECISION,
    closed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_order_agent_position_cycle
    ON order_agent_position (cycle_id);

CREATE INDEX IF NOT EXISTS idx_order_agent_position_status
    ON order_agent_position (status);
