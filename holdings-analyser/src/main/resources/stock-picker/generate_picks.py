"""
generate_picks.py — Step 8: train on all available history, then score the
*current* month (the one where fwd_ret_3m is still NULL because the future
hasn't happened yet) and output a ranked candidate list.

Run:
    python generate_picks.py
"""
import datetime
import joblib
import pandas as pd
from lightgbm import LGBMClassifier

from db import get_engine, FEATURES, clean_features, restrict_to_universe

TOP_N_OUTPUT = 5                # final number of stocks to actually invest in each month
LABEL_QUANTILE = 0.8
MIN_AVG_DAILY_VOLUME = 10_000   # liquidity floor — tune to your market segment
MODEL_PATH = "stock_picker_model.pkl"
OUTPUT_CSV = "top_picks.csv"


def next_trading_day(from_date=None):
    """Next weekday after from_date. Only skips Sat/Sun — does NOT know about
    NSE market holidays (Diwali, Republic Day, etc). Cross-check the NSE holiday
    calendar before placing orders, especially around festival periods.
    """
    d = (from_date or datetime.date.today()) + datetime.timedelta(days=1)
    while d.weekday() >= 5:  # 5=Saturday, 6=Sunday
        d += datetime.timedelta(days=1)
    return d


def load_full_dataset():
    engine = get_engine()
    # Pull training_data (historical, has fwd_ret_3m) and a small lookup
    # table for symbols/liquidity in one go.
    df = pd.read_sql("SELECT * FROM training_data", engine)
    df = restrict_to_universe(df, engine)   # top-200 universe, before training — adds 'rank' column
    stock_info = pd.read_sql(
        "SELECT id AS stock_id, symbol, name_of_company FROM stock_details",
        engine,
    )
    liquidity = pd.read_sql(
        "SELECT stock_id, month, avg_daily_volume FROM stock_monthly",
        engine,
    )

    # Fund data publishes ~10 days after month-end, so it always lags price data.
    # The month we can actually SCORE with real (non-zero-filled) fund features is
    # whichever is earlier of "latest price month" / "latest fund month" — not
    # just the overall latest month in training_data, which would silently include
    # a brand-new price month with no fund match yet (scored with fund features
    # forced to 0 by clean_features, defeating the point of having them).
    coverage = pd.read_sql(
        "SELECT (SELECT MAX(month) FROM features_price) AS latest_price_month, "
        "       (SELECT MAX(month) FROM features_funds) AS latest_fund_month",
        engine,
    )
    latest_price_month = coverage["latest_price_month"].iloc[0]
    latest_fund_month = coverage["latest_fund_month"].iloc[0]
    scorable_month = min(latest_price_month, latest_fund_month)

    print(f"Latest price month in DB: {latest_price_month}")
    print(f"Latest fund-holding month in DB: {latest_fund_month}")
    if latest_fund_month < latest_price_month:
        print(
            f"Fund data lags price data by design (publishes ~10 days after month-end). "
            f"Using {scorable_month} as the scoring month — the most recent month with "
            f"BOTH real price and fund features available.\n"
        )

    return df, stock_info, liquidity, scorable_month


def train_final_model(df):
    hist = df[df["fwd_ret_3m"].notna()].copy()
    if hist.empty:
        raise RuntimeError(
            "No historical rows with a known fwd_ret_3m — nothing to train on. "
            "Check the UPDATE step that populates fwd_ret_3m in training_data."
        )
    hist = clean_features(hist)
    hist["label"] = hist.groupby("month")["fwd_ret_3m"].transform(
        lambda x: (x >= x.quantile(LABEL_QUANTILE)).astype(int)
    )
    model = LGBMClassifier(n_estimators=300, max_depth=5, learning_rate=0.05)
    model.fit(hist[FEATURES], hist["label"])
    joblib.dump(model, MODEL_PATH)
    print(f"Model trained on {len(hist)} historical stock-months. Saved to {MODEL_PATH}.")
    return model


def score_current_month(df, model, stock_info, liquidity, scorable_month):
    latest_month = scorable_month
    current = df[(df["month"] == latest_month)].copy()
    if current.empty:
        raise RuntimeError(
            f"No rows for the scorable month {latest_month}. This month exists in "
            f"features_price/features_funds per the coverage check, but didn't make it "
            f"into training_data — check that training_data was rebuilt after the most "
            f"recent SQL refresh."
        )
    current = clean_features(current)

    current["score"] = model.predict_proba(current[FEATURES])[:, 1]

    current = current.merge(stock_info, on="stock_id", how="left")
    current = current.merge(
        liquidity[liquidity["month"] == latest_month][["stock_id", "avg_daily_volume"]],
        on="stock_id", how="left",
    )
    # psycopg2 + a left join can leave this as object dtype (mix of Decimal/None) —
    # force it to numeric explicitly rather than relying on fillna to infer the type.
    current["avg_daily_volume"] = pd.to_numeric(current["avg_daily_volume"], errors="coerce")

    before = len(current)

    # Diagnose before filtering, so a bad column (rather than genuinely picky
    # filters) doesn't silently wipe the whole candidate list.
    vol_all_zero = (current["avg_daily_volume"].fillna(0) == 0).all()
    cap_coverage = current["market_cap_category"].notna().mean()
    print(f"Before filtering: {before} candidates. "
          f"avg_daily_volume all zero/NaN: {vol_all_zero}. "
          f"market_cap_category populated for {cap_coverage:.0%} of rows.")

    if vol_all_zero:
        print("WARNING: avg_daily_volume is 0/NaN for every candidate. That's almost "
              "certainly a merge mismatch (e.g. 'month' values not lining up between "
              "training_data and stock_monthly) rather than every stock genuinely being "
              "illiquid. Skipping the liquidity filter rather than wiping the list.")
    else:
        current = current[current["avg_daily_volume"].fillna(0) >= MIN_AVG_DAILY_VOLUME]
        print(f"  After liquidity filter (>= {MIN_AVG_DAILY_VOLUME}): {len(current)} remain.")

    if cap_coverage < 0.5:
        print(f"WARNING: market_cap_category is populated for only {cap_coverage:.0%} of "
              f"candidates. Skipping this filter rather than wiping the list — populate "
              f"stock_details.market_cap_category if you want this filter to be meaningful.")
    else:
        current = current[current["market_cap_category"].notna()]
        print(f"  After market_cap_category filter: {len(current)} remain.")

    print(f"Final candidate count: {len(current)} (started from {before}).")

    ranked = current.sort_values("score", ascending=False)
    if ranked.empty:
        print("\nNo candidates survived filtering — see the warnings above for why. "
              "Nothing written to top_picks.csv.")
        return ranked

    out_cols = [
        "symbol", "name_of_company", "rank", "score", "ret_3m", "ret_12m",
        "num_funds_holding", "net_fund_adds", "fund_value_change_pct",
        "market_cap_category", "avg_daily_volume",
    ]
    top = ranked[out_cols].head(TOP_N_OUTPUT)
    top.to_csv(OUTPUT_CSV, index=False)
    print(f"\nLatest scored month: {latest_month}")
    print(f"Top {TOP_N_OUTPUT} picks (from top-{200} universe by rank) saved to {OUTPUT_CSV}\n")
    print(top.to_string(index=False))
    if len(top) < TOP_N_OUTPUT:
        print(f"\nNote: only {len(top)} candidates survived filtering — fewer than the "
              f"requested {TOP_N_OUTPUT}. See the filter counts above for why.")
    return top


if __name__ == "__main__":
    df, stock_info, liquidity, scorable_month = load_full_dataset()
    model = train_final_model(df)
    picks = score_current_month(df, model, stock_info, liquidity, scorable_month)

    if not picks.empty:
        trade_date = next_trading_day()
        print(
            f"\n--- Action plan ---\n"
            f"Picks generated using {scorable_month} data (price + fund holdings).\n"
            f"Next trading day from today ({datetime.date.today()}): {trade_date}.\n"
            f"NOTE: this only skips weekends, not NSE market holidays — double check "
            f"the NSE holiday calendar if {trade_date} falls near a festival/holiday period."
        )