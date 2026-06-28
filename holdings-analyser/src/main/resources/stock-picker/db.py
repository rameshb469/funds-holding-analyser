"""
db.py — shared Postgres connection helper.
Reads credentials from a .env file (copy .env.example to .env and fill in your values).
"""
import os
import pandas as pd
from dotenv import load_dotenv
from sqlalchemy import create_engine

load_dotenv()

def get_engine():
    host = os.environ.get("DB_HOST", "localhost")
    port = os.environ.get("DB_PORT", "5432")
    name = os.environ.get("DB_NAME", "funds-holding-analyser")
    user = os.environ.get("DB_USER", "postgres")
    password = os.environ.get("DB_PASSWORD", "root")

    if not all([name, user, password]):
        raise RuntimeError(
            "Missing DB credentials. Copy .env.example to .env and fill in DB_NAME, DB_USER, DB_PASSWORD."
        )

    url = f"postgresql+psycopg2://{user}:{password}@{host}:{port}/{name}"
    return create_engine(url)

# Feature columns used by both the backtest and the live-picks scripts.
# Keep this list in one place so both scripts always train/score on the same features.
#
# Split into two groups because they need different NULL handling:
# - PRICE_FEATURES: should always exist if a stock has price history. A NULL here
#   means real missing data -> drop the row.
# - FUND_FEATURES: come from a LEFT JOIN against mutual fund holdings. A NULL here
#   usually means "no fund held this stock that month" -> fill with 0, don't drop.
PRICE_FEATURES = [
    "ret_1m", "ret_3m", "ret_6m", "ret_12m",
    "volatility", "volume_trend_3m",
    "turnover_trend_3m", "trades_trend_3m",
]
FUND_FEATURES = [
    "num_funds_holding", "net_fund_adds", "net_shares_change",
    "fund_value_change_pct", "avg_net_asset_pct",
]
FEATURES = PRICE_FEATURES + FUND_FEATURES

# Investable universe: only the top N stocks by stock_details.rank (lower rank =
# larger/more prominent). Applied BEFORE labeling and training — not just at the
# final output stage — so the model learns "best of the top 200" rather than
# "best of the whole market." Those are genuinely different patterns: a stock
# can be top-quintile among 3000+ names but mediocre within the top 200, or
# vice versa. Training and deployment should see the same population.
TOP_N_UNIVERSE = 200


def restrict_to_universe(df, engine, top_n=TOP_N_UNIVERSE):
    """Filter df down to stocks within the top-N by stock_details.rank.
    Prints coverage so a sparsely-populated rank column doesn't silently
    produce a near-empty or wrong universe without anyone noticing.
    """
    ranks = pd.read_sql('SELECT id AS stock_id, "rank" FROM stock_details', engine)
    before = len(df)
    df = df.merge(ranks, on="stock_id", how="left")
    coverage = df["rank"].notna().mean()
    print(f"stock_details.rank populated for {coverage:.0%} of rows.")
    if coverage < 0.5:
        print(
            f"WARNING: rank is populated for only {coverage:.0%} of rows. The top-{top_n} "
            f"universe filter will only ever capture whatever subset has a rank assigned — "
            f"if that's a much smaller set than you expect, populate stock_details.rank "
            f"more completely before relying on this filter."
        )
    df = df[df["rank"].notna() & (df["rank"] <= top_n)]
    print(f"Restricted to top-{top_n} universe by rank: {len(df)} rows remain (from {before}).")
    if df.empty:
        raise RuntimeError(
            f"restrict_to_universe() produced 0 rows. Check stock_details.rank actually "
            f"has values <= {top_n} for stocks present in your training data."
        )
    return df


def clean_features(df):
    """Fill fund-feature NULLs with 0 (no fund holding = a real zero, not missing
    data). For price features, only require ret_1m (the shortest-lookback signal)
    to be present — ret_3m/ret_6m/ret_12m/volatility/volume_trend_3m are allowed
    to stay NaN. LightGBM handles missing values natively (it learns a default
    split direction for them), so a stock that doesn't yet have 12 months of
    history can still train/score using whatever shorter-window features it has,
    instead of being dropped entirely.
    """
    df = df.copy()

    # Postgres NUMERIC columns come back via psycopg2 as Decimal objects. Once a
    # column has any NULLs mixed in (guaranteed for ret_12m etc. with short
    # history), pandas stores the whole column as `object` dtype rather than
    # float64 — and LightGBM rejects `object` columns outright. Coerce every
    # feature column to numeric explicitly rather than trusting read_sql's
    # inferred dtype.
    all_feature_cols = PRICE_FEATURES + FUND_FEATURES
    df[all_feature_cols] = df[all_feature_cols].apply(pd.to_numeric, errors="coerce")

    df[FUND_FEATURES] = df[FUND_FEATURES].fillna(0)
    before = len(df)
    df = df.dropna(subset=["ret_1m"])
    dropped = before - len(df)
    if dropped:
        print(f"Dropped {dropped} rows missing ret_1m (out of {before}).")
    if df.empty:
        raise RuntimeError(
            "clean_features() produced an empty dataframe even requiring only "
            "ret_1m. This means features_price itself is empty or nearly empty — "
            "check stock_ohlcv_equity / stock_monthly row counts. The equity "
            "filter from Step 1 (financial_instrument_type / segment) may be "
            "excluding everything; re-run the Step 0 diagnostic query."
        )
    return df