"""Train a LightGBM (or fallback RandomForest) regressor on window files and write model scores back to the window files.

Usage:
  python ml/models/lgbm_trainer.py --window-dir ml/data/windows --model-out ml/models/lgbm.json

Notes:
- Expects window files (windows_*.parquet or windows_*.csv) containing a numeric target column `forward_ret_3m`.
- The script will overwrite the window files to include a new column `model_score` used by the backtester.
"""
import os
import argparse
import json
from typing import List

import pandas as pd
import numpy as np

# Try to import LightGBM, otherwise fallback to RandomForest
try:
    import lightgbm as lgb
    LGB_AVAILABLE = True
except Exception:
    LGB_AVAILABLE = False

try:
    from sklearn.model_selection import train_test_split
    from sklearn.preprocessing import LabelEncoder
    import joblib
    SKLEARN_AVAILABLE = True
except Exception:
    SKLEARN_AVAILABLE = False


def _gather_window_files(window_dir: str) -> List[str]:
    files = [os.path.join(window_dir, f) for f in os.listdir(window_dir) if f.startswith('windows_') and (f.endswith('.parquet') or f.endswith('.csv'))]
    return sorted(files)


def load_table(path: str) -> pd.DataFrame:
    _, ext = os.path.splitext(path)
    if ext.lower() == '.parquet':
        return pd.read_parquet(path)
    else:
        return pd.read_csv(path)


def save_table(df: pd.DataFrame, path: str):
    _, ext = os.path.splitext(path)
    if ext.lower() == '.parquet':
        df.to_parquet(path, index=False)
    else:
        df.to_csv(path, index=False)


def prepare_dataset(paths: List[str]) -> pd.DataFrame:
    parts = []
    for p in paths:
        df = load_table(p)
        df = df.copy()
        df['_source_file'] = p
        parts.append(df)
    if not parts:
        return pd.DataFrame()
    all_df = pd.concat(parts, ignore_index=True, sort=False)
    # only keep rows with forward target
    all_df = all_df[all_df['forward_ret_3m'].notna()].copy()
    return all_df


def featurize(df: pd.DataFrame):
    # Build richer feature set from available window columns
    feats = pd.DataFrame(index=df.index)

    def _col_series(col, default=0.0):
        if col in df.columns:
            try:
                return df[col].astype(float).fillna(default)
            except Exception:
                # if conversion fails, coerce with to_numeric
                return pd.to_numeric(df[col], errors='coerce').fillna(default)
        else:
            return pd.Series([default] * len(df), index=df.index)

    # Volume features
    feats['median_vol_63'] = _col_series('median_vol_63', default=0.0)
    if feats['median_vol_63'].isna().all():
        feats['median_vol_63'] = _col_series('avg_vol_63', default=0.0)
    feats['log_median_vol'] = np.log1p(feats['median_vol_63'].clip(lower=0))

    # Turnover / traded value
    feats['avg_turnover_63'] = _col_series('avg_turnover_63', default=0.0)
    if feats['avg_turnover_63'].isna().all():
        feats['avg_turnover_63'] = _col_series('totalTradedValue_63', default=0.0)
    feats['log_turnover_63'] = np.log1p(feats['avg_turnover_63'].clip(lower=0))

    # Trades
    feats['avg_trades_63'] = _col_series('avg_trades_63', default=0.0)
    if feats['avg_trades_63'].isna().all():
        feats['avg_trades_63'] = _col_series('totalNumberOfTransactionsExecuted_63', default=0.0)

    # Percent zero-volume days
    feats['pct_zero_vol_days_63'] = _col_series('pct_zero_vol_days_63', default=0.0)

    # Price returns and momentum
    feats['ret_3m'] = _col_series('ret_3m', default=0.0)
    feats['ret_1m'] = _col_series('ret_1m', default=0.0)

    # Volatility (if provided), fallback to 0
    feats['volatility_30'] = _col_series('volatility_30', default=0.0)
    if feats['volatility_30'].isna().all():
        feats['volatility_30'] = _col_series('stddev_logreturns_30', default=0.0)

    # Simple liquidity score: normalized combination of volume, turnover and trades
    eps = 1e-9
    # compute robust scales (median and MAD-like std)
    v_med = feats['median_vol_63'].median() if not feats['median_vol_63'].empty else 0.0
    v_std = feats['median_vol_63'].std() if not feats['median_vol_63'].empty else 0.0
    t_med = feats['avg_turnover_63'].median() if not feats['avg_turnover_63'].empty else 0.0
    t_std = feats['avg_turnover_63'].std() if not feats['avg_turnover_63'].empty else 0.0
    tr_med = feats['avg_trades_63'].median() if not feats['avg_trades_63'].empty else 0.0
    tr_std = feats['avg_trades_63'].std() if not feats['avg_trades_63'].empty else 0.0

    vol_norm = (feats['median_vol_63'] - v_med) / (v_std + eps)
    turn_norm = (feats['avg_turnover_63'] - t_med) / (t_std + eps)
    trades_norm = (feats['avg_trades_63'] - tr_med) / (tr_std + eps)

    feats['liquidity_score'] = (0.5 * vol_norm.fillna(0)) + (0.3 * turn_norm.fillna(0)) + (0.2 * trades_norm.fillna(0))

    # Market-of-interest features (if available)
    if 'mf_total_quantity' in df.columns:
        feats['mf_total_quantity'] = _col_series('mf_total_quantity', default=0.0)
    if 'mf_num_funds' in df.columns:
        feats['mf_num_funds'] = _col_series('mf_num_funds', default=0.0)

    # encode stock_id as categorical label (safe fallback)
    if 'stock_id' in df.columns:
        try:
            from sklearn.preprocessing import LabelEncoder
            le = LabelEncoder()
            feats['stock_id_le'] = le.fit_transform(df['stock_id'].astype(str))
        except Exception:
            uniq = {v: i for i, v in enumerate(sorted(df['stock_id'].astype(str).unique()))}
            feats['stock_id_le'] = df['stock_id'].astype(str).map(uniq)
    else:
        feats['stock_id_le'] = 0

    # Fill any remaining NaNs with 0
    feats = feats.fillna(0.0)
    return feats


def train_and_score(window_dir: str, model_out: str = None):
    # Re-check availability at runtime (in case packages were installed after module import)
    try:
        import importlib
        lgb_spec = importlib.util.find_spec('lightgbm')
        sklearn_spec = importlib.util.find_spec('sklearn')
        joblib_spec = importlib.util.find_spec('joblib')
        runtime_lgb = bool(lgb_spec)
        runtime_sklearn = bool(sklearn_spec)
        runtime_joblib = bool(joblib_spec)
    except Exception:
        runtime_lgb = LGB_AVAILABLE
        runtime_sklearn = SKLEARN_AVAILABLE
        runtime_joblib = False

    print(f'Runtime availability - lightgbm: {runtime_lgb}, sklearn: {runtime_sklearn}, joblib: {runtime_joblib}')
    paths = _gather_window_files(window_dir)
    print(f'Found {len(paths)} window files in {window_dir}')
    if not paths:
        print('No window files found in', window_dir)
        return
    df_all = prepare_dataset(paths)
    print(f'Aggregated dataset rows with forward target: {len(df_all)}')
    if df_all.empty:
        print('No rows with forward_ret_3m available for training')
        return

    # time-based split by window_end if present
    if 'window_end' in df_all.columns:
        df_all['window_end_dt'] = pd.to_datetime(df_all['window_end'], errors='coerce')
        # sort by date and split last 20% as test
        df_all = df_all.sort_values('window_end_dt').reset_index(drop=True)
        cutoff = int(len(df_all) * 0.8)
        # featurize after sorting to keep alignment
        X = featurize(df_all)
        y = df_all['forward_ret_3m'].astype(float)
        X_train = X.iloc[:cutoff]
        y_train = y.iloc[:cutoff].values
        X_test = X.iloc[cutoff:]
        y_test = y.iloc[cutoff:].values
    else:
        X = featurize(df_all)
        y = df_all['forward_ret_3m'].astype(float).values
        if SKLEARN_AVAILABLE:
            X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)
        else:
            # fallback deterministic split when sklearn is not installed
            n = len(X)
            cutoff = int(n * 0.8) if n > 1 else 1
            X_train = X.iloc[:cutoff]
            X_test = X.iloc[cutoff:]
            y_train = y[:cutoff]
            y_test = y[cutoff:]

    model = None
    model_type = None
    # Prefer runtime availability checks
    if runtime_lgb:
        try:
            import lightgbm as lgb_runtime
            print('Training LightGBM regressor (runtime)...')
            lgbm = lgb_runtime.LGBMRegressor(n_estimators=200, learning_rate=0.05, random_state=42)
            lgbm.fit(X_train, y_train, eval_set=[(X_test, y_test)], early_stopping_rounds=10, verbose=False)
            model = lgbm
            model_type = 'lightgbm'
        except Exception as e:
            print('Runtime LightGBM training failed:', e)

    if model is None and runtime_sklearn:
        try:
            print('Training RandomForestRegressor as fallback...')
            from sklearn.ensemble import RandomForestRegressor
            rf = RandomForestRegressor(n_estimators=200, random_state=42)
            rf.fit(X_train, y_train)
            model = rf
            model_type = 'randomforest'
        except Exception as e:
            print('Runtime sklearn training failed:', e)

    if model is None:
        print('Neither LightGBM nor scikit-learn worked; using heuristic scoring (normalized median_vol_63)')
        model = None
        model_type = 'heuristic'

    # evaluate and save model only if we trained one
    if model is not None:
        try:
            preds_test = model.predict(X_test)
            mae = float(np.mean(np.abs(preds_test - y_test)))
            print(f'Test MAE: {mae:.6f} using model {model_type}')
        except Exception as e:
            print('Could not compute evaluation metrics:', e)

        # save model if requested
        if model_out:
            out_dir = os.path.dirname(model_out)
            if out_dir and not os.path.exists(out_dir):
                os.makedirs(out_dir, exist_ok=True)
            # prefer joblib if available at runtime
            try:
                import importlib
                jb_spec = importlib.util.find_spec('joblib')
                if jb_spec:
                    import joblib as _joblib
                    _joblib.dump(model, model_out)
                    print('Saved model via joblib to', model_out)
                else:
                    # attempt LightGBM native save
                    if model_type == 'lightgbm' and hasattr(model, 'booster_'):
                        model.booster_.save_model(model_out)
                        print('Saved LightGBM booster to', model_out)
                    else:
                        print('No joblib available and model type not LightGBM; model not saved')
            except Exception as e:
                print('Model saving failed:', e)
    else:
        print('Skipping evaluation and saving because no ML model was trained')

    # predict and write scores back to each window file (overwrite)
    print('Scoring window files and writing model_score column...')
    # featurize full dataset again to map back
    feats_full = featurize(df_all)
    if model is not None:
        scores = model.predict(feats_full)
    else:
        # fallback heuristic: normalized median_vol_63
        vals = feats_full['median_vol_63'].astype(float).fillna(0.0).values
        if vals.max() - vals.min() == 0:
            scores = np.zeros_like(vals)
        else:
            scores = (vals - vals.min()) / (vals.max() - vals.min())
    df_all['model_score'] = scores

    # ensure keys for merging are strings for reliable matching
    df_all['stock_id'] = df_all['stock_id'].astype(str)
    if 'window_end' in df_all.columns:
        df_all['window_end'] = df_all['window_end'].astype(str)

    # split by source file and write back (for files that had no training rows, still write scores if present)
    for p, group in df_all.groupby('_source_file'):
        print(f'Processing file {p} with {len(group)} scored rows')
        # load original file to preserve rows with no forward target
        orig = load_table(p)
        orig = orig.copy()
        # coerce types for merge
        if 'stock_id' in orig.columns:
            orig['stock_id'] = orig['stock_id'].astype(str)
        if 'window_end' in orig.columns:
            orig['window_end'] = orig['window_end'].astype(str)

        # prefer merging on both stock_id and window_end when available
        if 'window_end' in orig.columns and 'window_end' in group.columns:
            merged = orig.merge(group[['stock_id', 'window_end', 'model_score']], on=['stock_id', 'window_end'], how='left')
        else:
            # merge on stock_id only
            merged = orig.merge(group[['stock_id', 'model_score']], on=['stock_id'], how='left')

        # after merge, consolidate model_score if pandas created _x/_y
        if 'model_score_y' in merged.columns:
            # prefer the newly merged values (y), fallback to existing x
            merged['model_score'] = merged['model_score_y'].fillna(merged.get('model_score_x'))
            # drop helper cols
            merged = merged.drop(columns=[c for c in ['model_score_x', 'model_score_y'] if c in merged.columns])
        elif 'model_score_x' in merged.columns and 'model_score' not in merged.columns:
            merged = merged.rename(columns={'model_score_x': 'model_score'})
        elif 'model_score' not in merged.columns:
            # ensure column exists
            merged['model_score'] = np.nan

        # save back (overwrite same format)
        save_table(merged, p)
        print('Wrote scores to', p, 'columns now:', merged.columns.tolist())
    print('Finished training/scoring')

    return True


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Train a LightGBM baseline on window files and write model_score into window files')
    parser.add_argument('--window-dir', type=str, default=os.path.join('ml', 'data', 'windows'), help='Directory with windows_*.parquet or .csv')
    parser.add_argument('--model-out', type=str, default=os.path.join('ml', 'models', 'lgbm_model.joblib'), help='Path to save trained model')
    args = parser.parse_args()

    train_and_score(args.window_dir, model_out=args.model_out)
