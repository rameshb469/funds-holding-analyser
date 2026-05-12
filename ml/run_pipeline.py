"""Quick runnable pipeline to sanity-check the ML artifacts.

This script creates synthetic datasets, builds the Keras model from ml.models.tf_model if available,
trains for 1 epoch (sanity), writes window parquet files and runs the backtester to produce JSON output.

Usage:
  python ml/run_pipeline.py [--n-stocks N] [--n-picks M] [--use-existing-windows] [--windows-dir DIR] [--tc TC] [--top-n K]

This is intended for local smoke tests when the DB is not available.
"""
import os
import json
import numpy as np
import pandas as pd
import sys
import importlib
from datetime import date

# ensure project root on path so ml modules import cleanly
ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), '..'))
if ROOT not in sys.path:
    sys.path.insert(0, ROOT)

# Import backtester eagerly (pure-Python)
from ml.backtest.backtester import run_backtest

DATA_DIR = os.path.join(ROOT, 'ml', 'data')
RAW_DIR = os.path.join(DATA_DIR, 'raw')
WINDOW_DIR = os.path.join(DATA_DIR, 'windows')
os.makedirs(RAW_DIR, exist_ok=True)
os.makedirs(WINDOW_DIR, exist_ok=True)


def _assign_marketcap_categories(sorted_caps):
    """Given a list of (stock_id, market_cap) sorted desc, return dict stock_id->category."""
    cats = {}
    for idx, (sid, _) in enumerate(sorted_caps, start=1):
        if idx <= 100:
            cats[sid] = 'LARGE_CAP'
        elif idx <= 250:
            cats[sid] = 'MID_CAP'
        elif idx <= 550:
            cats[sid] = 'SMALL_CAP'
        else:
            cats[sid] = 'MICRO_CAP'
    return cats


def create_synthetic_windows(n_stocks=20, window_ends=None):
    """Create one or more synthetic window parquet files for testing.

    Returns list of paths written.
    The generated rows include more realistic columns used by the app:
      - stock_id, symbol, market_cap, marketCapCategory, sector, industry
      - totalTradingVolume, totalTradedValue, totalNumberOfTransactionsExecuted
      - window_start, window_end, median_vol_63, has_forward, forward_ret_3m
    """
    if window_ends is None:
        # default: last 3 month-ends
        today = pd.Timestamp(date.today())
        window_ends = [
            (today - pd.DateOffset(months=2)).to_period('M').end_time.date().isoformat(),
            (today - pd.DateOffset(months=1)).to_period('M').end_time.date().isoformat(),
            today.to_period('M').end_time.date().isoformat()
        ]

    # Pre-generate market caps so categories are consistent across windows
    caps = []
    for sid in range(1, n_stocks + 1):
        # sample lognormal market caps (in arbitrary units)
        caps.append((sid, float(np.random.lognormal(mean=12, sigma=1.2))))
    # sort desc and assign categories
    caps_sorted = sorted(caps, key=lambda x: x[1], reverse=True)
    cap_cats = _assign_marketcap_categories(caps_sorted)
    # make a lookup for market cap by sid
    cap_lookup = {sid: mc for sid, mc in caps_sorted}

    paths = []
    for we in window_ends:
        rows = []
        for sid in range(1, n_stocks + 1):
            median_vol = float(np.random.lognormal(mean=10, sigma=1.0))
            # use has_forward to indicate if we have forward data for this stock-window
            has_forward = np.random.rand() > 0.15
            forward_ret = float(np.random.normal(loc=0.02, scale=0.12)) if has_forward else None

            # generate some realistic trading aggregates
            totalTradingVolume = int(max(1, median_vol * np.random.uniform(0.5, 3.0)))
            totalTradedValue = float(totalTradingVolume * np.random.uniform(10.0, 200.0))
            totalNumberOfTransactionsExecuted = int(max(1, totalTradingVolume / np.random.uniform(1, 10)))

            # symbol and company name
            symbol = f'SYM{sid}'
            company = f'Company {sid}'

            market_cap = cap_lookup.get(sid)
            market_cat = cap_cats.get(sid)

            # random sector/industry from small lists
            sectors = ['Technology', 'Financial', 'Consumer', 'Healthcare', 'Industrials']
            industries = ['Software', 'Banking', 'Retail', 'Pharma', 'Machinery']
            sector = np.random.choice(sectors)
            industry = np.random.choice(industries)

            rows.append({
                'stock_id': sid,
                'symbol': symbol,
                'name': company,
                'MKT_CAP': market_cap,
                'marketCapCategory': market_cat,
                'sector': sector,
                'industry': industry,
                'window_start': str((pd.to_datetime(we) - pd.DateOffset(months=3)).date()),
                'window_end': str(we),
                'median_vol_63': median_vol,
                'has_forward': bool(has_forward),
                'forward_ret_3m': forward_ret,
                'totalTradingVolume': totalTradingVolume,
                'totalTradedValue': totalTradedValue,
                'totalNumberOfTransactionsExecuted': totalNumberOfTransactionsExecuted
            })
        df = pd.DataFrame(rows)
        # ensure types are friendly
        df['stock_id'] = df['stock_id'].astype(int)

        out_path = os.path.join(WINDOW_DIR, f'windows_{we}.parquet')
        try:
            df.to_parquet(out_path, index=False)
            print(f'Wrote synthetic windows to {out_path} ({len(df)} rows)')
            paths.append(out_path)
        except Exception as e:
            # fallback to CSV if pyarrow/fastparquet not installed
            csv_path = out_path.replace('.parquet', '.csv')
            df.to_csv(csv_path, index=False)
            print(f'Parquet not available ({e}); wrote CSV to {csv_path} ({len(df)} rows)')
            paths.append(csv_path)
    return paths


def find_existing_windows(windows_dir):
    """Find parquet/csv window files in the given directory and return sorted paths."""
    exts = ('.parquet', '.parq', '.csv')
    files = []
    for fn in os.listdir(windows_dir):
        if fn.lower().endswith(exts):
            files.append(os.path.join(windows_dir, fn))
    files.sort()
    return files


def save_stock_id_symbol_map(paths, out_path=None):
    """Persist a mapping stock_id -> symbol (if symbol exists) or UNKNOWN_{id}.

    Reads the provided window files (parquet or csv) to collect stock_id and optional symbol.
    """
    mapping = {}
    for p in paths:
        try:
            if p.lower().endswith('.csv'):
                df = pd.read_csv(p)
            else:
                df = pd.read_parquet(p)
        except Exception:
            continue
        if 'stock_id' not in df.columns:
            continue
        for sid in df['stock_id'].unique():
            if int(sid) in mapping:
                continue
            # try to find symbol column
            sym = None
            if 'symbol' in df.columns:
                row = df[df['stock_id'] == sid]
                if not row.empty and pd.notna(row.iloc[0].get('symbol')):
                    sym = str(row.iloc[0].get('symbol'))
            mapping[int(sid)] = sym if sym is not None else f'UNKNOWN_{int(sid)}'
    if out_path is None:
        out_path = os.path.join(DATA_DIR, 'stock_id_symbol_map.json')
    with open(out_path, 'w') as f:
        json.dump(mapping, f, indent=2)
    print(f'Wrote stock_id->symbol map to {out_path} ({len(mapping)} entries)')
    return out_path


def generate_red_flags(paths, out_path=None):
    """Generate simple rule-based red flags from window files.

    Flags include:
      - low_volume: median_vol_63 below threshold
      - missing_marketcap: market_cap is null or <= 0
      - insufficient_forward: has_forward is False
      - extreme_forward_return: abs(forward_ret_3m) > 0.25

    Writes a JSON mapping stock_id -> {symbol, flags}
    """
    flags_map = {}
    for p in paths:
        try:
            if p.lower().endswith('.csv'):
                df = pd.read_csv(p)
            else:
                df = pd.read_parquet(p)
        except Exception:
            continue
        # normalize columns if necessary
        if 'stock_id' not in df.columns:
            continue
        # thresholds
        low_vol_threshold = df['median_vol_63'].quantile(0.10) if 'median_vol_63' in df.columns else 0
        for _, row in df.iterrows():
            sid = int(row.get('stock_id'))
            symbol = row.get('symbol') if 'symbol' in row.index else f'UNKNOWN_{sid}'
            sflags = flags_map.setdefault(sid, {'symbol': symbol, 'flags': []})
            # low volume
            try:
                if 'median_vol_63' in row.index and pd.notna(row['median_vol_63']):
                    if float(row['median_vol_63']) < low_vol_threshold:
                        if 'low_volume' not in sflags['flags']:
                            sflags['flags'].append('low_volume')
            except Exception:
                pass
            # missing market cap (use MKT_CAP)
            try:
                if 'MKT_CAP' in row.index and (pd.isna(row['MKT_CAP']) or float(row['MKT_CAP']) <= 0):
                    if 'missing_marketcap' not in sflags['flags']:
                        sflags['flags'].append('missing_marketcap')
            except Exception:
                pass
            # insufficient forward data
            if 'has_forward' in row.index and (not bool(row['has_forward'])):
                if 'insufficient_forward' not in sflags['flags']:
                    sflags['flags'].append('insufficient_forward')
            # extreme forward return
            try:
                if 'forward_ret_3m' in row.index and pd.notna(row['forward_ret_3m']):
                    if abs(float(row['forward_ret_3m'])) > 0.25:
                        if 'extreme_forward_return' not in sflags['flags']:
                            sflags['flags'].append('extreme_forward_return')
            except Exception:
                pass
    if out_path is None:
        out_path = os.path.join(DATA_DIR, 'red_flags.json')
    with open(out_path, 'w') as f:
        json.dump(flags_map, f, indent=2)
    print(f'Wrote red flags to {out_path} ({len(flags_map)} stocks)')
    return out_path


def train_tf_sanity(batch=32):
    """Attempt to import TensorFlow model from ml.models.tf_model and run a 1-epoch sanity train.
    If TensorFlow is not installed, skip gracefully.
    """
    try:
        tf_model_module = importlib.import_module('ml.models.tf_model')
    except ModuleNotFoundError:
        print('TensorFlow not installed; skipping TF sanity training. To enable, `pip install -r ml/requirements.txt`.')
        return None

    build_model = getattr(tf_model_module, 'build_model')
    # create tiny synthetic seq + tab + targets
    seq = np.random.randn(batch, 63, 3).astype(np.float32)
    tab = np.random.randn(batch, 16).astype(np.float32)
    y = np.random.randn(batch, 1).astype(np.float32) * 0.05

    model = build_model()
    print('Training model for 1 epoch (sanity)...')
    model.fit({'seq_input': seq, 'tab_input': tab}, y, epochs=1, batch_size=16, verbose=1)
    print('Model trained (1 epoch)')
    return model


def run(argv=None):
    # use argparse for more flexible CLI
    import argparse
    parser = argparse.ArgumentParser(description='Run ML smoke-test/backtester with synthetic or existing windows')
    parser.add_argument('--n-stocks', type=int, default=50, help='Number of synthetic stocks to generate')
    parser.add_argument('--n-picks', type=int, default=10, help='Number of picks for backtester')
    parser.add_argument('--windows-dir', type=str, default=WINDOW_DIR, help='Directory containing window files')
    parser.add_argument('--use-existing-windows', action='store_true', help='Use existing window files in windows-dir instead of generating synthetic ones')
    parser.add_argument('--tc', type=float, default=0.001, help='Transaction cost per side (e.g. 0.001 = 0.1%)')
    parser.add_argument('--top-n', type=int, default=500, help='Limit candidate universe to top-N stocks by market cap if supported by backtester (default 500)')
    args = parser.parse_args(argv)

    n_stocks = args.n_stocks
    n_picks = args.n_picks
    windows_dir = args.windows_dir
    use_existing = args.use_existing_windows
    tc = args.tc
    top_n = args.top_n

    # 1) create or collect window files
    if use_existing:
        paths = find_existing_windows(windows_dir)
        if not paths:
            print(f'No window files found in {windows_dir}, falling back to synthetic generation')
            paths = create_synthetic_windows(n_stocks=n_stocks)
        else:
            print(f'Found {len(paths)} existing window files in {windows_dir}')
    else:
        paths = create_synthetic_windows(n_stocks=n_stocks)

    # persist stock_id -> symbol mapping for downstream use
    try:
        save_stock_id_symbol_map(paths)
    except Exception as e:
        print('Failed to write stock_id->symbol map:', e)

    # generate simple red flags
    try:
        generate_red_flags(paths)
    except Exception as e:
        print('Failed to generate red flags:', e)

    # 2) train TF model for sanity (if available)
    try:
        _ = train_tf_sanity()
    except Exception as e:
        print('TF training failed during sanity run:', e)

    # 3) run backtester on windows
    print('Running backtest on windows...')
    results = None
    try:
        # try calling run_backtest with optional kwargs if it accepts them
        results = run_backtest(paths, n=n_picks, tc=tc, top_n=top_n)
    except TypeError:
        # fallback if backtester doesn't accept tc/top_n
        try:
            results = run_backtest(paths, n=n_picks, tc=tc)
        except TypeError:
            results = run_backtest(paths, n=n_picks)

    out_json = json.dumps(results, indent=2)
    print(out_json)
    out_file = os.path.join(WINDOW_DIR, 'backtest_results.json')
    with open(out_file, 'w') as f:
        f.write(out_json)
    print(f'Backtest results written to {out_file}')


if __name__ == '__main__':
    # allow optional args via flags
    run(sys.argv[1:])
