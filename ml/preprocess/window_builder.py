"""Build month-end windows and compute simple features and labels.

This module reads raw parquet files produced by load_data.py and creates per-month window features
and 3-month forward returns labels. It implements missing-day rules and basic aggregation.
"""
import os
from datetime import datetime, timedelta
from typing import List

import numpy as np
import pandas as pd

DATA_DIR = os.path.join(os.path.dirname(__file__), '..', 'data')
RAW_DIR = os.path.join(DATA_DIR, 'raw')
WINDOW_DIR = os.path.join(DATA_DIR, 'windows')
os.makedirs(WINDOW_DIR, exist_ok=True)

FORWARD_DAYS = 63  # approx 3 months in trading days
MISSING_DAY_SKIP_THRESHOLD = 0.3  # if >30% missing, skip window


def business_days(start: pd.Timestamp, end: pd.Timestamp) -> pd.DatetimeIndex:
    return pd.bdate_range(start=start, end=end)


def build_windows(window_end_dates: List[pd.Timestamp] = None):
    top_path = os.path.join(RAW_DIR, 'top_stocks.parquet')
    hist_path = os.path.join(RAW_DIR, 'stock_history.parquet')
    if not os.path.exists(top_path) or not os.path.exists(hist_path):
        raise FileNotFoundError('Run load_data.py first to populate ml/data/raw')

    top_df = pd.read_parquet(top_path)
    hist_df = pd.read_parquet(hist_path)
    hist_df['date'] = pd.to_datetime(hist_df['date']).dt.date

    # default window_end_dates = month ends for the last 12 months found in hist
    if window_end_dates is None:
        max_date = pd.to_datetime(hist_df['date']).max().date()
        window_end_dates = []
        for i in range(9, -1, -1):
            # month end n months back
            d = (pd.Timestamp(max_date) - pd.DateOffset(months=i)).to_period('M').end_time.date()
            window_end_dates.append(pd.to_datetime(d))

    results = []

    # Precompute history per stock for speed
    grouped = hist_df.groupby('stock_id')

    for we in window_end_dates:
        we = pd.to_datetime(we).date()
        window_start = (pd.Timestamp(we) - pd.DateOffset(months=3)).date()
        forward_end = (pd.Timestamp(we) + pd.Timedelta(days=FORWARD_DAYS)).date()

        # For each stock, slice history from window_start to forward_end
        rows = []
        for sid in top_df['id'].tolist():
            df = grouped.get_group(sid) if sid in grouped.groups else pd.DataFrame()
            if df.empty:
                continue
            df = df.copy()
            df = df[(pd.to_datetime(df['date']).dt.date >= window_start) & (pd.to_datetime(df['date']).dt.date <= forward_end)]
            # build calendar for window period only (window_start to we)
            cal = pd.bdate_range(start=window_start, end=we)
            cal_dates = pd.to_datetime(cal).date
            hist_window = df[(pd.to_datetime(df['date']).dt.date >= window_start) & (pd.to_datetime(df['date']).dt.date <= we)]
            # merge to calendar
            cal_df = pd.DataFrame({'date': cal_dates})
            merged = cal_df.merge(hist_window, on='date', how='left')
            # price imputation rule: forward-fill last close for short gaps
            merged['close_price'] = merged['close_price'].ffill()
            # volume/turnover/trades -> fill 0 for missing days
            merged['totalTradingVolume'] = merged['totalTradingVolume'].fillna(0)
            merged['totalTradedValue'] = merged['totalTradedValue'].fillna(0)
            merged['totalNumberOfTransactionsExecuted'] = merged['totalNumberOfTransactionsExecuted'].fillna(0)

            # skip windows with too many missing days (close still NaN after ffill)
            missing_close_fraction = merged['close_price'].isna().mean()
            if missing_close_fraction > MISSING_DAY_SKIP_THRESHOLD:
                continue

            # compute simple features for the window (last 63 trading days or available days)
            lookback = merged.tail(63)
            avg_vol = lookback['totalTradingVolume'].mean()
            median_vol = lookback['totalTradingVolume'].median()
            avg_turnover = lookback['totalTradedValue'].mean()
            avg_trades = lookback['totalNumberOfTransactionsExecuted'].mean()
            pct_zero_vol_days = (lookback['totalTradingVolume'] == 0).mean()
            close_now = merged['close_price'].iloc[-1]
            close_63 = merged['close_price'].iloc[0]
            ret_3m = None
            if pd.notna(close_63) and close_63 > 0:
                ret_3m = (close_now / close_63) - 1

            # compute forward return at we + FORWARD_DAYS (if available)
            forward_df = df[(pd.to_datetime(df['date']).dt.date > we) & (pd.to_datetime(df['date']).dt.date <= forward_end)]
            forward_price = None
            if not forward_df.empty:
                # take last available price in forward window
                forward_price = forward_df['close_price'].dropna().iloc[-1] if forward_df['close_price'].dropna().any() else None
            has_forward = forward_price is not None
            forward_ret = None
            if has_forward and close_now and close_now > 0:
                forward_ret = (forward_price / close_now) - 1

            rows.append({
                'stock_id': sid,
                'window_end': we,
                'window_start': window_start,
                'avg_vol_63': float(avg_vol) if not np.isnan(avg_vol) else 0.0,
                'median_vol_63': float(median_vol) if not np.isnan(median_vol) else 0.0,
                'avg_turnover_63': float(avg_turnover) if not np.isnan(avg_turnover) else 0.0,
                'avg_trades_63': float(avg_trades) if not np.isnan(avg_trades) else 0.0,
                'pct_zero_vol_days_63': float(pct_zero_vol_days),
                'ret_3m': float(ret_3m) if ret_3m is not None else None,
                'has_forward': bool(has_forward),
                'forward_ret_3m': float(forward_ret) if forward_ret is not None else None
            })
        if not rows:
            continue
        month_df = pd.DataFrame(rows)
        out_path = os.path.join(WINDOW_DIR, f'windows_{we}.parquet')
        month_df.to_parquet(out_path, index=False)
        print(f'Wrote {out_path} with {len(month_df)} rows')
        results.append(out_path)

    return results


if __name__ == '__main__':
    build_windows()

