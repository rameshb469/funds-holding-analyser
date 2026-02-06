-- Queries for stock-picking pipeline
-- 1) Top-N (default 1000) stocks by market cap with optional filters
-- Parameters: :limit, :marketCapCategory, :sectorId, :industry
-- Use your DB driver's parameter style if different (e.g., %s or ?)

SELECT sd.id,
       sd.symbol,
       sd.name_of_company,
       sd.mkt_cap,
       sd.sector_id,
       sd.industry,
       sd.marketCapCategory
FROM stock_details sd
WHERE sd.mkt_cap IS NOT NULL
  AND (:marketCapCategory IS NULL OR sd.marketCapCategory = :marketCapCategory)
  AND (:sectorId IS NULL OR sd.sector_id = :sectorId)
  AND (:industry IS NULL OR sd.industry = :industry)
ORDER BY sd.mkt_cap DESC
LIMIT :limit;

-- 2) Mutual fund holdings snapshot aggregated by stock for a given month-end date
-- Params: :monthEndDate (DATE)

SELECT h.stock_id,
       SUM(h.quantity) AS mf_total_quantity,
       COUNT(DISTINCT h.mf_id) AS mf_num_funds
FROM mutual_fund_holdings h
WHERE h.date = :monthEndDate
GROUP BY h.stock_id;

-- 3) Daily history for a list of stock ids in given date range
-- Params: :stockIds (list), :startDate, :endDate

SELECT sh.stock_id,
       sh.date,
       sh.close_price,
       sh.totalTradingVolume,
       sh.totalTradedValue,
       sh.totalNumberOfTransactionsExecuted
FROM stock_history sh
WHERE sh.stock_id IN (:stockIds)
  AND sh.date BETWEEN :startDate AND :endDate
ORDER BY sh.stock_id, sh.date;

-- 4) Optional: fetch shares outstanding if available to compute mkt_cap
-- Params: :stockId

-- SELECT s.stock_id, s.shares_outstanding FROM stock_shares s WHERE s.stock_id = :stockId;
