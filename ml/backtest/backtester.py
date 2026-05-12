"""Simple monthly rebalancing backtester that reads windows files and simulates picks.

Produces per-month JSON summary: windowStart, windowEnd (forward end), avgForwardReturn, picksSelected, picksWithForwardData
Assumes windows files are in ml/data/windows/windows_{window_end}.parquet or .csv
"""
import os
import json
from datetime import datetime
from typing import List, Optional, Dict, Any, Set

import numpy as np
import pandas as pd

# Default windows directory (can be overridden via CLI)
WINDOW_DIR = os.path.join(os.path.dirname(__file__), '..', 'data', 'windows')

# Mapping / raw data paths
MAPPING_CSV = os.path.join(os.path.dirname(__file__), '..', 'data', 'stock_id_to_symbol.csv')
RAW_TOP_PARQUET = os.path.join(os.path.dirname(__file__), '..', 'data', 'raw', 'top_stocks.parquet')
RAW_TOP_CSV = os.path.join(os.path.dirname(__file__), '..', 'data', 'raw', 'top_stocks.csv')


def load_window(path: str) -> pd.DataFrame:
    """Load a window file (parquet or csv)."""
    _, ext = os.path.splitext(path)
    if ext.lower() == '.parquet':
        return pd.read_parquet(path)
    elif ext.lower() == '.csv':
        return pd.read_csv(path)
    else:
        raise ValueError(f'Unsupported window file extension: {ext}')


def parse_window_date_from_name(path: str) -> Optional[datetime.date]:
    fname = os.path.basename(path)
    we_str = fname.replace('windows_', '').replace('.parquet', '').replace('.csv', '')
    try:
        return pd.to_datetime(we_str, errors='coerce').date()
    except Exception:
        return None


def _load_stock_metadata() -> Dict[int, Dict[str, Any]]:
    """Load stock metadata from `stock_id_to_symbol.csv` and optionally `ml/data/raw/top_stocks.*`.

    Returns a dict: {stock_id: {symbol, name_of_company, mkt_cap, sector_id, industry}}
    """
    meta: Dict[int, Dict[str, Any]] = {}
    # load basic mapping
    if os.path.exists(MAPPING_CSV):
        try:
            df = pd.read_csv(MAPPING_CSV)
            for _, r in df.iterrows():
                try:
                    sid = int(r['stock_id'])
                except Exception:
                    continue
                meta.setdefault(sid, {})
                if 'symbol' in r and pd.notna(r['symbol']):
                    meta[sid]['symbol'] = str(r['symbol'])
        except Exception:
            # non-fatal
            pass

    # try to load richer metadata if available
    if os.path.exists(RAW_TOP_PARQUET):
        try:
            df_top = pd.read_parquet(RAW_TOP_PARQUET)
        except Exception:
            df_top = None
    elif os.path.exists(RAW_TOP_CSV):
        try:
            df_top = pd.read_csv(RAW_TOP_CSV)
        except Exception:
            df_top = None
    else:
        df_top = None

    if df_top is not None:
        # prefer columns: id, symbol, name_of_company, mkt_cap, sector_id, industry
        for _, r in df_top.iterrows():
            try:
                sid = int(r.get('id') or r.get('stock_id'))
            except Exception:
                continue
            meta.setdefault(sid, {})
            if 'symbol' in r and pd.notna(r['symbol']):
                meta[sid]['symbol'] = str(r['symbol'])
            if 'name_of_company' in r and pd.notna(r['name_of_company']):
                meta[sid]['name_of_company'] = str(r['name_of_company'])
            # normalize market cap column(s) to MKT_CAP
            for mc_col in ('MKT_CAP', 'mkt_cap', 'market_cap', 'marketCap', 'mktcap'):
                if mc_col in r and pd.notna(r[mc_col]):
                    try:
                        meta[sid]['MKT_CAP'] = float(r[mc_col])
                    except Exception:
                        meta[sid]['MKT_CAP'] = r[mc_col]
                    break
            # optional sector/industry
            if 'sector_id' in r and pd.notna(r['sector_id']):
                meta[sid]['sector_id'] = r['sector_id']
            if 'industry' in r and pd.notna(r['industry']):
                meta[sid]['industry'] = r['industry']

    return meta


def _load_top_set(top_n: Optional[int]) -> Optional[Set[int]]:
    """Return set of top stock_ids by mkt_cap from raw top file, limited to top_n.
    If no raw top file available, return None.
    """
    if top_n is None:
        return None
    df_top = None
    if os.path.exists(RAW_TOP_PARQUET):
        try:
            df_top = pd.read_parquet(RAW_TOP_PARQUET)
        except Exception:
            df_top = None
    elif os.path.exists(RAW_TOP_CSV):
        try:
            df_top = pd.read_csv(RAW_TOP_CSV)
        except Exception:
            df_top = None

    if df_top is None:
        # cannot enforce top-N without metadata file
        print('Warning: top_n requested but ml/data/raw/top_stocks.* not found; skipping top-N filtering')
        return None

    # prefer columns id or stock_id
    id_col = None
    for c in ('id', 'stock_id'):
        if c in df_top.columns:
            id_col = c
            break
    # prefer normalized market cap column name
    mkt_col = None
    for c in ('MKT_CAP', 'mkt_cap', 'market_cap', 'marketCap', 'mktcap'):
        if c in df_top.columns:
            mkt_col = c
            break

    if id_col is None or mkt_col is None:
        print('Warning: top_stocks file missing id/mkt_cap columns; skipping top-N filtering')
        return None

    # sort desc by market cap and take first top_n
    try:
        df_top_sorted = df_top.sort_values(by=mkt_col, ascending=False)
        top_ids = df_top_sorted[id_col].head(top_n).astype(int).tolist()
        return set(top_ids)
    except Exception as e:
        print('Warning: failed to compute top-N from top_stocks file:', e)
        return None


def _compute_top_from_windows(window_paths: List[str], top_n: int) -> Optional[Set[int]]:
    """Compute top-N stock_ids by MKT_CAP from the provided window files as a fallback."""
    # Collect best known MKT_CAP per stock_id across windows
    caps = {}
    for p in window_paths:
        try:
            df = load_window(p)
        except Exception:
            continue
        if df is None or 'stock_id' not in df.columns:
            continue
        # normalize columns: try 'MKT_CAP' then variants
        mcol = None
        for c in ('MKT_CAP', 'mkt_cap', 'market_cap', 'marketCap', 'mktcap'):
            if c in df.columns:
                mcol = c
                break
        if mcol is None:
            continue
        for _, r in df.iterrows():
            try:
                sid = int(r['stock_id'])
            except Exception:
                continue
            try:
                val = r.get(mcol)
                if pd.isna(val):
                    continue
                v = float(val)
            except Exception:
                continue
            if sid not in caps or (v is not None and v > caps[sid]):
                caps[sid] = v
    if not caps:
        return None
    sorted_ids = sorted(caps.items(), key=lambda x: x[1], reverse=True)
    top_ids = [sid for sid, _ in sorted_ids[:top_n]]
    return set(top_ids)


def simulate_month(window_path: str, n: int = 10, score_col: str = 'median_vol_63', transaction_cost_rate: float = 0.001, top_set: Optional[Set[int]] = None):
    """Simulate a single month rebalancing using top-n by score_col.

    Net return per pick is computed as: net = (1 + gross) * (1 - tc)^2 - 1  (account buy+sell costs multiplicatively)
    The output now includes a `picks` list with enriched metadata where available.
    If `top_set` is provided, only stocks whose `stock_id` is in top_set are considered.
    """
    df = load_window(window_path)
    df = df.copy()

    # if top_set supplied, filter candidates
    if top_set is not None:
        if 'stock_id' in df.columns:
            df = df[df['stock_id'].isin(top_set)]
        else:
            # cannot filter without stock_id column
            pass

    # load metadata mapping once per invocation
    stock_meta = _load_stock_metadata()

    # normalize score column fallback
    if score_col not in df.columns:
        if 'median_vol_63' in df.columns:
            df['score'] = df['median_vol_63'].fillna(0)
        elif 'avg_vol_63' in df.columns:
            df['score'] = df['avg_vol_63'].fillna(0)
        else:
            # fallback to forward_ret_3m as proxy (descending)
            df['score'] = df.get('forward_ret_3m', 0).fillna(0)
    else:
        df['score'] = df[score_col].fillna(0)

    df = df.sort_values('score', ascending=False)
    selected = df.head(n)
    picksSelected = int(len(selected))

    # determine picksWithForwardData
    if 'has_forward' in selected.columns:
        picksWithForwardData = int(selected['has_forward'].astype(bool).sum())
    else:
        picksWithForwardData = int(selected['forward_ret_3m'].notna().sum())

    # compute net forward returns per pick after transaction costs
    net_returns = []
    positive_count = 0

    picks: List[Dict[str, Any]] = []

    for _, row in selected.iterrows():
        sid = int(row.get('stock_id')) if 'stock_id' in row.index and pd.notna(row.get('stock_id')) else None
        has_forward = bool(row.get('has_forward', False)) if 'has_forward' in row.index else pd.notna(row.get('forward_ret_3m'))
        forward_ret = row.get('forward_ret_3m') if 'forward_ret_3m' in row.index else None

        # compute net if forward exists
        net = None
        if has_forward and pd.notna(forward_ret):
            try:
                gross = float(forward_ret)
                net = (1.0 + gross) * (1.0 - transaction_cost_rate) * (1.0 - transaction_cost_rate) - 1.0
                net_returns.append(net)
                if net > 0:
                    positive_count += 1
            except Exception:
                net = None

        # enrich with metadata if available
        meta = stock_meta.get(sid, {}) if sid is not None else {}
        symbol = meta.get('symbol') if meta.get('symbol') else (f'UNKNOWN_{sid}' if sid is not None else None)
        name = meta.get('name_of_company') if 'name_of_company' in meta else None
        mkt_cap = meta.get('MKT_CAP') if 'MKT_CAP' in meta else None

        picks.append({
            'stock_id': sid,
            'symbol': symbol,
            'name_of_company': name,
            'MKT_CAP': mkt_cap,
            'marketCap': mkt_cap,
            'score': float(row.get('score')) if pd.notna(row.get('score')) else None,
            'forward_ret_3m': float(forward_ret) if (forward_ret is not None and pd.notna(forward_ret)) else None,
            'net_return_after_tc': float(net) if net is not None else None,
            'has_forward': bool(has_forward)
        })

    # try to attach red flags per stock if available
    red_flags_path = os.path.join(os.path.dirname(__file__), '..', 'data', 'red_flags.json')
    red_map = {}
    try:
        if os.path.exists(red_flags_path):
            with open(red_flags_path, 'r') as rf:
                raw = json.load(rf)
                # keys may be strings; normalize to int keys
                for k, v in raw.items():
                    try:
                        red_map[int(k)] = v.get('flags', []) if isinstance(v, dict) else v
                    except Exception:
                        continue
    except Exception:
        red_map = {}

    # attach flags to picks
    for p in picks:
        sid = p.get('stock_id')
        p['red_flags'] = red_map.get(sid, [])

    avgForwardReturn = float(sum(net_returns) / len(net_returns)) if net_returns else 0.0
    hit_rate = (positive_count / len(net_returns)) if net_returns else 0.0

    # parse dates
    window_start_val = None
    if 'window_start' in df.columns and not df['window_start'].isna().all():
        window_start_val = str(df['window_start'].iloc[0])
    we = parse_window_date_from_name(window_path)
    forward_end = None
    if we:
        forward_end = (pd.to_datetime(we) + pd.Timedelta(days=63)).date()

    return {
        'windowStart': window_start_val,
        'windowEnd': str(forward_end) if forward_end else None,
        'avgForwardReturn': avgForwardReturn,
        'picksSelected': picksSelected,
        'picksWithForwardData': picksWithForwardData,
        'hitRate': hit_rate,
        'picks': picks
    }


def run_backtest(window_paths: List[str], n: int = 10, score_col: str = 'median_vol_63', transaction_cost_rate: float = 0.001, top_n: Optional[int] = None, tc: Optional[float] = None):
    """Run backtest across window files.

    Accepts either `transaction_cost_rate` or `tc` (keyword) for convenience. If `top_n` is provided,
    attempts to load ml/data/raw/top_stocks.* to compute the set of top stock_ids and filters candidates to that set.
    """
    results = []
    # compute tc effective value
    eff_tc = tc if (tc is not None) else transaction_cost_rate

    # compute top_set if requested
    top_set = None
    if top_n is not None:
        top_set = _load_top_set(top_n)
        if top_set is None:
            # fallback: compute top set from windows using MKT_CAP
            top_set = _compute_top_from_windows(window_paths, top_n)

    # Sort by window date parsed from filename for chronological order
    sorted_paths = sorted(window_paths, key=lambda p: (parse_window_date_from_name(p) or pd.Timestamp('1970-01-01').date()))
    for p in sorted_paths:
        res = simulate_month(p, n=n, score_col=score_col, transaction_cost_rate=eff_tc, top_set=top_set)
        results.append(res)
    return results


def _gather_window_files(window_dir: str):
    files = [os.path.join(window_dir, f) for f in os.listdir(window_dir) if f.startswith('windows_') and (f.endswith('.parquet') or f.endswith('.csv'))]
    return files


def _read_window(path):
    try:
        if path.lower().endswith('.csv'):
            return pd.read_csv(path)
        else:
            return pd.read_parquet(path)
    except Exception:
        # best-effort fallback
        try:
            return pd.read_csv(path)
        except Exception:
            return None


def run_backtest_fallback(paths, n=10, tc=0.001, top_n=None):
    """
    Minimal backtester fallback for local smoke-tests.

    Args:
      paths: list of parquet/csv window files
      n: number of picks per window
      tc: transaction cost per side (0.001 = 0.1%)
      top_n: optional limit on candidate universe before picking top-n

    Returns a JSON-serializable dict with per-window results and a summary.
    """
    windows = []
    for p in paths:
        df = _read_window(p)
        if df is None or 'stock_id' not in df.columns:
            windows.append({
                'file': os.path.basename(p),
                'error': 'failed to read or missing stock_id',
            })
            continue

        # ensure columns exist
        if 'median_vol_63' not in df.columns:
            # if missing, create a random proxy
            df['median_vol_63'] = np.random.lognormal(mean=10, sigma=1.0, size=len(df))

        # optional universe trimming
        candidates = df
        if top_n is not None and top_n > 0:
            candidates = candidates.nlargest(top_n, 'median_vol_63')

        # pick top-n by median_vol_63
        picked = candidates.nlargest(n, 'median_vol_63').copy()

        # compute naive expected forward return (mean of forward_ret_3m where available)
        if 'forward_ret_3m' in picked.columns:
            vals = pd.to_numeric(picked['forward_ret_3m'], errors='coerce').dropna()
            avg_forward = float(vals.mean()) if not vals.empty else 0.0
        else:
            avg_forward = 0.0

        # simulate net return: mean forward minus round-trip transaction costs
        net_return = avg_forward - 2.0 * float(tc)

        windows.append({
            'file': os.path.basename(p),
            'n_universe': int(len(df)),
            'n_candidates': int(len(candidates)),
            'n_picks': int(len(picked)),
            'avg_forward_return': avg_forward,
            'net_return': net_return,
        })

    # summary
    net_returns = [w['net_return'] for w in windows if 'net_return' in w]
    summary = {
        'n_windows': len(windows),
        'mean_net_return': float(np.mean(net_returns)) if net_returns else 0.0,
        'median_net_return': float(np.median(net_returns)) if net_returns else 0.0,
    }

    return {
        'summary': summary,
        'windows': windows,
    }


if __name__ == '__main__':
    import argparse

    parser = argparse.ArgumentParser(description='Run monthly pick backtest on window files')
    parser.add_argument('--window-dir', type=str, default=WINDOW_DIR, help='Directory containing windows_*.parquet or .csv')
    parser.add_argument('--n', type=int, default=10, help='Number of picks per month')
    parser.add_argument('--score-col', type=str, default='median_vol_63', help='Column name to score/rank picks')
    parser.add_argument('--tc', type=float, default=0.001, help='Transaction cost rate (fraction) applied to buy and sell')
    parser.add_argument('--top-n', type=int, default=None, help='Limit universe to top-N stocks by mkt_cap (requires ml/data/raw/top_stocks.*)')
    parser.add_argument('--out', type=str, default=None, help='Output JSON file path')
    args = parser.parse_args()

    paths = _gather_window_files(args.window_dir)
    if not paths:
        print('No window files found. Run window_builder.py or run_pipeline.py first.')
        raise SystemExit(1)
    out = run_backtest(paths, n=args.n, score_col=args.score_col, transaction_cost_rate=args.tc, top_n=args.top_n)
    # persist results to ml/data/windows/backtest_results.json by default when run from CLI
    out_json = json.dumps(out, indent=2)
    print(out_json)
    out_path = args.out if args.out else os.path.join(os.path.dirname(__file__), '..', 'data', 'windows', 'backtest_results.json')
    try:
        with open(out_path, 'w') as fh:
            fh.write(out_json)
        print(f'Wrote results to {out_path}')
    except Exception as e:
        print('Failed to write results to', out_path, e)
