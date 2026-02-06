"""Load top-N stocks and their daily history from DB.

This script expects DB connection via environment variables or edit the DATABASE_URL constant.
It writes raw parquet files under ml/data/raw/
"""
import os
from datetime import datetime, timedelta
from typing import List

import pandas as pd
from sqlalchemy import create_engine, text

# Simple DB config - change or set DATABASE_URL in env
DATABASE_URL = os.environ.get('DATABASE_URL', 'postgresql://user:password@localhost:5432/mydb')
OUT_DIR = os.path.join(os.path.dirname(__file__), '..', 'data', 'raw')
os.makedirs(OUT_DIR, exist_ok=True)

DEFAULT_LIMIT = 1000

QUERIES_PATH = os.path.join(os.path.dirname(__file__), '..', 'sql', 'queries.sql')
with open(QUERIES_PATH, 'r') as f:
    QUERIES_SQL = f.read()

# A helper to read a named query chunk by a comment marker is omitted; we'll embed the query here for clarity.
TOP_N_QUERY = """
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
"""

DAILY_HISTORY_QUERY = """
SELECT sh.stock_id,
       sh.date,
       sh.close_price,
       sh.totalTradingVolume,
       sh.totalTradedValue,
       sh.totalNumberOfTransactionsExecuted
FROM stock_history sh
WHERE sh.stock_id IN :stockIds
  AND sh.date BETWEEN :startDate AND :endDate
ORDER BY sh.stock_id, sh.date;
"""


def get_engine(database_url: str = None):
    url = database_url or DATABASE_URL
    engine = create_engine(url)
    return engine


def fetch_top_n(limit: int = DEFAULT_LIMIT, marketCapCategory=None, sectorId=None, industry=None, engine=None):
    engine = engine or get_engine()
    params = {"limit": limit, "marketCapCategory": marketCapCategory, "sectorId": sectorId, "industry": industry}
    with engine.connect() as conn:
        result = conn.execute(text(TOP_N_QUERY), params)
        df = pd.DataFrame(result.fetchall(), columns=result.keys())
    return df


def fetch_daily_history(stock_ids: List[int], start_date: str, end_date: str, engine=None):
    engine = engine or get_engine()
    # SQLAlchemy needs a tuple for an IN clause
    ids_tuple = tuple(stock_ids)
    params = {"stockIds": ids_tuple, "startDate": start_date, "endDate": end_date}
    with engine.connect() as conn:
        result = conn.execute(text(DAILY_HISTORY_QUERY), params)
        df = pd.DataFrame(result.fetchall(), columns=result.keys())
    return df


def main(limit=DEFAULT_LIMIT, start_date=None, end_date=None, marketCapCategory=None, sectorId=None, industry=None):
    engine = get_engine()
    top_df = fetch_top_n(limit=limit, marketCapCategory=marketCapCategory, sectorId=sectorId, industry=industry, engine=engine)
    if top_df.empty:
        print('No stocks found for given filters')
        return
    top_ids = top_df['id'].tolist()
    # default date range: last 2 years
    if end_date is None:
        end_date = datetime.now().date()
    if start_date is None:
        start_date = end_date - timedelta(days=365 * 2)
    start_date_s = pd.to_datetime(start_date).date().isoformat()
    end_date_s = pd.to_datetime(end_date).date().isoformat()

    print(f'Fetching daily history for {len(top_ids)} stocks from {start_date_s} to {end_date_s}')
    hist_df = fetch_daily_history(top_ids, start_date_s, end_date_s, engine=engine)
    # write outputs
    top_df.to_parquet(os.path.join(OUT_DIR, 'top_stocks.parquet'), index=False)
    hist_df.to_parquet(os.path.join(OUT_DIR, 'stock_history.parquet'), index=False)
    print('Wrote top_stocks.parquet and stock_history.parquet')


if __name__ == '__main__':
    main()

