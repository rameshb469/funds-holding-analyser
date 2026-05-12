"""Create a mapping file stock_id -> symbol (CSV + JSON).

Strategy:
- Read unique `stock_id` values from window files (ml/data/windows/*.csv or parquet).
- Try to read `ml/data/raw/top_stocks.parquet` or CSV to map ids -> symbol.
- If not available, try to connect to DB using DATABASE_URL env and query stock_details for matching ids.
- For any unmapped ids, set symbol to 'UNKNOWN_<id>'.
- Output: ml/data/stock_id_to_symbol.csv and .json

Usage:
    python3 ml/utils/save_stock_id_symbol_map.py

"""
import argparse
import os
import glob
import json
from pathlib import Path
from typing import Dict, Set

import pandas as pd

try:
    from sqlalchemy import create_engine, text
    SQLALCHEMY_AVAILABLE = True
except Exception:
    SQLALCHEMY_AVAILABLE = False

ROOT = Path(__file__).resolve().parents[1]
WINDOWS_DIR = ROOT / 'data' / 'windows'
RAW_DIR = ROOT / 'data' / 'raw'
OUT_CSV = ROOT / 'data' / 'stock_id_to_symbol.csv'
OUT_JSON = ROOT / 'data' / 'stock_id_to_symbol.json'

DATABASE_URL = os.environ.get('DATABASE_URL', None)


def gather_stock_ids_from_windows(window_dir: Path) -> Set[int]:
    ids = set()
    patterns = [str(window_dir / '*')]
    for p in glob.glob(str(window_dir / '*')):
        try:
            if p.endswith('.csv'):
                df = pd.read_csv(p, usecols=['stock_id'])
            else:
                df = pd.read_parquet(p, columns=['stock_id'])
            ids.update(int(x) for x in df['stock_id'].dropna().unique())
        except Exception:
            # ignore files that aren't windows or unreadable
            continue
    return ids


def load_mapping_from_top_stocks(raw_dir: Path) -> Dict[int, str]:
    candidates = [raw_dir / 'top_stocks.parquet', raw_dir / 'top_stocks.csv']
    for f in candidates:
        if f.exists():
            try:
                if f.suffix == '.parquet':
                    df = pd.read_parquet(f)
                else:
                    df = pd.read_csv(f)
                if 'id' in df.columns and 'symbol' in df.columns:
                    return {int(r['id']): str(r['symbol']) for _, r in df[['id', 'symbol']].iterrows()}
            except Exception:
                continue
    return {}


def query_db_for_symbols(stock_ids: Set[int], database_url: str) -> Dict[int, str]:
    if not SQLALCHEMY_AVAILABLE or not database_url:
        return {}
    try:
        engine = create_engine(database_url)
        ids_tuple = tuple(sorted(stock_ids))
        if not ids_tuple:
            return {}
        q = text('SELECT id, symbol FROM stock_details WHERE id IN :ids')
        with engine.connect() as conn:
            res = conn.execute(q, {'ids': ids_tuple})
            rows = res.fetchall()
        return {int(r[0]): str(r[1]) for r in rows}
    except Exception as e:
        print('DB query failed:', e)
        return {}


def query_top_n_symbols(database_url: str, n: int) -> Dict[int, str]:
    """Query the DB for top-n stocks ordered by MKT_CAP DESC and return id->symbol mapping."""
    if not SQLALCHEMY_AVAILABLE or not database_url:
        return {}
    try:
        engine = create_engine(database_url)
        q = text('SELECT id, symbol FROM stock_details WHERE mkt_cap IS NOT NULL ORDER BY mkt_cap DESC LIMIT :n')
        with engine.connect() as conn:
            res = conn.execute(q, {'n': int(n)})
            rows = res.fetchall()
        return {int(r[0]): str(r[1]) for r in rows}
    except Exception as e:
        print('DB top-n query failed:', e)
        return {}


def main():
    parser = argparse.ArgumentParser(description='Create stock_id -> symbol mapping')
    parser.add_argument('--top-n', type=int, default=None, help='If provided, fetch top-N stocks by MKT_CAP from DB and generate mapping for them')
    args = parser.parse_args()

    if args.top_n is not None:
        # fetch top N directly from DB
        print(f'Querying top {args.top_n} stocks by MKT_CAP from DB')
        mapping = {}
        if DATABASE_URL:
            mapping = query_top_n_symbols(DATABASE_URL, args.top_n)
            print('DB returned', len(mapping), 'mappings')
        else:
            print('DATABASE_URL not provided; cannot query DB for top-n')
        # fill missing ids up to top-n with UNKNOWN if any (unlikely)
        if mapping:
            # write mapping as-is
            os.makedirs(OUT_CSV.parent, exist_ok=True)
            df_out = pd.DataFrame([{'stock_id': k, 'symbol': v} for k, v in sorted(mapping.items())])
            df_out.to_csv(OUT_CSV, index=False)
            with open(OUT_JSON, 'w') as f:
                json.dump(mapping, f, indent=2)
            print('Wrote', OUT_CSV, 'and', OUT_JSON)
        else:
            print('No mapping obtained for top-n request')
        return

    # default behavior: infer ids from window files and try raw/DB lookups
    print('Gathering stock_ids from windows in', WINDOWS_DIR)
    ids = gather_stock_ids_from_windows(WINDOWS_DIR)
    print('Found', len(ids), 'unique stock_ids in windows')

    mapping = {}
    # try raw top_stocks first
    print('Trying to load mapping from raw/top_stocks...')
    mapping.update(load_mapping_from_top_stocks(RAW_DIR))
    print('Loaded', len(mapping), 'mappings from top_stocks if available')

    missing = ids - set(mapping.keys())
    print('Still missing', len(missing), 'ids')

    if missing and DATABASE_URL:
        print('Attempting DB lookup using DATABASE_URL...')
        db_map = query_db_for_symbols(missing, DATABASE_URL)
        print('DB returned', len(db_map), 'mappings')
        mapping.update(db_map)

    # Fill unknowns
    for sid in sorted(ids):
        if int(sid) not in mapping:
            mapping[int(sid)] = f'UNKNOWN_{sid}'

    # Save CSV
    df_out = pd.DataFrame([{'stock_id': k, 'symbol': v} for k, v in sorted(mapping.items())])
    os.makedirs(OUT_CSV.parent, exist_ok=True)
    df_out.to_csv(OUT_CSV, index=False)
    with open(OUT_JSON, 'w') as f:
        json.dump(mapping, f, indent=2)

    print('Wrote', OUT_CSV, 'and', OUT_JSON)


if __name__ == '__main__':
    main()

