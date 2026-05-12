"""Load top-N stocks and their daily history from DB.

This script expects DB connection via environment variables or edit the DATABASE_URL constant.
It writes raw parquet files under ml/data/raw/
"""
import os
from datetime import datetime, timedelta
from typing import List

import pandas as pd
from sqlalchemy import create_engine, text
from sqlalchemy.exc import OperationalError

# Build DATABASE_URL from environment if not explicitly provided
ENV_DB_URL = os.environ.get('DATABASE_URL')
if ENV_DB_URL:
    DATABASE_URL = ENV_DB_URL
else:
    # Try to construct from common PG env vars
    PGHOST = os.environ.get('PGHOST', 'localhost')
    PGPORT = os.environ.get('PGPORT', '5432')
    PGDATABASE = os.environ.get('PGDATABASE', 'funds-holding-analyser')
    PGUSER = os.environ.get('PGUSER') or os.environ.get('DB_USER') or os.environ.get('USER')
    PGPASSWORD = os.environ.get('PGPASSWORD') or os.environ.get('DB_PASSWORD')
    if PGUSER and PGPASSWORD:
        DATABASE_URL = f'postgresql://{PGUSER}:{PGPASSWORD}@{PGHOST}:{PGPORT}/{PGDATABASE}'
    elif PGUSER:
        # no password provided; rely on .pgpass or peer auth
        DATABASE_URL = f'postgresql://{PGUSER}@{PGHOST}:{PGPORT}/{PGDATABASE}'
    else:
        # fallback to a generic placeholder — caller should override via env
        DATABASE_URL = f'postgresql://{PGHOST}:{PGPORT}/{PGDATABASE}'

OUT_DIR = os.path.join(os.path.dirname(__file__), '..', 'data', 'raw')
os.makedirs(OUT_DIR, exist_ok=True)

DEFAULT_LIMIT = 500

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
       sd.industry_id,
       sd.market_cap_category
FROM stock_details sd
WHERE sd.mkt_cap IS NOT NULL
  AND (:marketCapCategory IS NULL OR sd.market_cap_category = :marketCapCategory)
  AND (:sectorId IS NULL OR sd.sector_id = :sectorId)
  AND (:industryId IS NULL OR sd.industry_id = :industryId)
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
    try:
        engine = create_engine(url)
        # test connect lazily
        return engine
    except Exception as e:
        raise


def fetch_top_n(limit: int = DEFAULT_LIMIT, marketCapCategory=None, sectorId=None, industryId=None, engine=None):
    engine = engine or get_engine()
    params = {"limit": limit, "marketCapCategory": marketCapCategory, "sectorId": sectorId, "industryId": industryId}
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


def main(limit=DEFAULT_LIMIT, start_date=None, end_date=None, marketCapCategory=None, sectorId=None, industryId=None):
    try:
        engine = get_engine()
    except Exception as e:
        print('Failed to construct DB engine. Please set DATABASE_URL or PGHOST/PGPORT/PGDATABASE/PGUSER/PGPASSWORD env vars.')
        print('Constructed DATABASE_URL was:', DATABASE_URL)
        raise

    try:
        top_df = fetch_top_n(limit=limit, marketCapCategory=marketCapCategory, sectorId=sectorId, industryId=industryId, engine=engine)
    except OperationalError as oe:
        print('OperationalError when querying DB. Check credentials and network. DATABASE_URL used:', DATABASE_URL)
        print('Error:', oe)
        return
    except Exception as e:
        print('Failed to fetch top stocks:', e)
        return

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
    hist_df = None
    try:
        hist_df = fetch_daily_history(top_ids, start_date_s, end_date_s, engine=engine)
    except OperationalError as oe:
        print('OperationalError when fetching stock history. Check DB credentials and access to table stock_history. DATABASE_URL used:', DATABASE_URL)
        print('Error:', oe)
    except Exception as e:
        print('Warning: failed to fetch stock history (table may be missing). Continuing with top_stocks only. Error:', e)

    # normalize market cap column to 'MKT_CAP' so downstream code has a consistent column name
    if 'MKT_CAP' in top_df.columns:
        # ensure numeric
        top_df['MKT_CAP'] = pd.to_numeric(top_df['MKT_CAP'], errors='coerce').fillna(0.0).astype(float)
    elif 'mkt_cap' in top_df.columns:
        top_df = top_df.rename(columns={'mkt_cap': 'MKT_CAP'})
        top_df['MKT_CAP'] = pd.to_numeric(top_df['MKT_CAP'], errors='coerce').fillna(0.0).astype(float)
    elif 'market_cap' in top_df.columns:
        top_df = top_df.rename(columns={'market_cap': 'MKT_CAP'})
        top_df['MKT_CAP'] = pd.to_numeric(top_df['MKT_CAP'], errors='coerce').fillna(0.0).astype(float)
    else:
        # ensure column exists to avoid downstream KeyErrors
        top_df['MKT_CAP'] = 0.0

    # write outputs - always write top_stocks
    top_df.to_parquet(os.path.join(OUT_DIR, 'top_stocks.parquet'), index=False)
    if hist_df is not None and not hist_df.empty:
        hist_df.to_parquet(os.path.join(OUT_DIR, 'stock_history.parquet'), index=False)
    else:
        # write an empty dataframe with expected columns to keep downstream code happy
        empty = pd.DataFrame(columns=['stock_id', 'date', 'close_price', 'totalTradingVolume', 'totalTradedValue', 'totalNumberOfTransactionsExecuted'])
        empty.to_parquet(os.path.join(OUT_DIR, 'stock_history.parquet'), index=False)
    print('Wrote top_stocks.parquet and stock_history.parquet (history may be empty)')


if __name__ == '__main__':
    main()
