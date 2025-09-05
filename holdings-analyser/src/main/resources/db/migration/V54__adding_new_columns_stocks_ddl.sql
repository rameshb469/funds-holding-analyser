ALTER TABLE stock_details
ADD COLUMN market_cap NUMERIC,
ADD COLUMN shares_outstanding NUMERIC,
ADD COLUMN rank INT,
ADD COLUMN market_cap_category VARCHAR(50);
