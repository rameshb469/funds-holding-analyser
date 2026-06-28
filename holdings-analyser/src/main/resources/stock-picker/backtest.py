"""
backtest.py — Step 7: walk-forward backtest of the stock-picking model.

How it works:
- Pulls training_data from Postgres (built per the SQL steps already discussed).
- For each rebalance point, trains only on months STRICTLY BEFORE the test month
  (no lookahead), then scores the test month and picks the top-K stocks.
- Rebalances every 3 months (matching the 3-month forward-return horizon) so
  holding periods don't overlap — this keeps the portfolio math simple and honest.
- Compares the strategy's average return to an equal-weighted "buy everything"
  benchmark from the same universe, each period.

Run:
    python backtest.py
"""
import numpy as np
import pandas as pd
from lightgbm import LGBMClassifier

from db import get_engine, FEATURES, clean_features, restrict_to_universe

TOP_K = 5                 # how many stocks to actually pick each rebalance
TOP_K_SWEEP = [5, 10, 20, 30, 50]     # additional basket sizes to compare in one run
MIN_TRAIN_MONTHS = 12     # require at least 1 year of history before first prediction
REBALANCE_EVERY = 3       # months; matches the 3-month forward return horizon
LABEL_QUANTILE = 0.8      # top 20% of returns each month = positive label


def load_data():
    engine = get_engine()
    df = pd.read_sql(
        "SELECT * FROM training_data WHERE fwd_ret_3m IS NOT NULL",
        engine,
    )
    if df.empty:
        raise RuntimeError(
            "training_data returned 0 rows with fwd_ret_3m IS NOT NULL. "
            "Check that the UPDATE step that fills fwd_ret_3m actually ran, "
            "and that you have at least 3+ months of forward price history."
        )
    df = restrict_to_universe(df, engine)   # top-200 universe, before labeling/training
    df = clean_features(df)
    df = df.dropna(subset=["fwd_ret_3m"]).sort_values("month")
    # cross-sectional label: was this stock in the top quintile of returns THAT month,
    # relative to its peers WITHIN THE TOP-200 UNIVERSE — not the whole market
    df["label"] = df.groupby("month")["fwd_ret_3m"].transform(
        lambda x: (x >= x.quantile(LABEL_QUANTILE)).astype(int)
    )
    return df


def annualize_return(nav_series, periods_per_year):
    n_periods = len(nav_series)
    if n_periods == 0:
        return np.nan
    total_ret = nav_series.iloc[-1]
    return total_ret ** (periods_per_year / n_periods) - 1


def max_drawdown(nav_series):
    cummax = nav_series.cummax()
    drawdown = (nav_series - cummax) / cummax
    return drawdown.min()


def sharpe_ratio(returns, periods_per_year):
    std = returns.std()
    if std == 0 or np.isnan(std):
        return np.nan
    return (returns.mean() / std) * np.sqrt(periods_per_year)


def run_backtest():
    df = load_data()
    months = sorted(df["month"].unique())
    n_months = len(months)
    print(f"Loaded {len(df)} rows across {n_months} distinct months "
          f"({months[0]} to {months[-1]}).\n")

    min_train = MIN_TRAIN_MONTHS
    smoke_test_only = False
    if n_months <= MIN_TRAIN_MONTHS:
        smoke_test_only = True
        min_train = max(1, n_months - 1)   # leave at least 1 month to test on
        print(
            f"WARNING: only {n_months} months of history available — well below the "
            f"{MIN_TRAIN_MONTHS} months a real walk-forward test needs. Lowering "
            f"MIN_TRAIN_MONTHS to {min_train} so the pipeline can run at all, which "
            f"gives at most 1-2 test periods.\n"
            f"Treat whatever metrics come out of this run as a MECHANICAL SMOKE TEST "
            f"of the code, not a validated strategy — a single holdout period tells you "
            f"the pipeline works, not whether the model has any real edge. Revisit this "
            f"once you have 12+ months of history.\n"
        )

    records = []
    for i in range(min_train, n_months, REBALANCE_EVERY):
        test_month = months[i]
        train_months = months[:i]   # strictly before test_month -> no lookahead

        train_df = df[df["month"].isin(train_months)]
        test_df = df[df["month"] == test_month]

        if train_df.empty or test_df.empty:
            continue
        if len(train_df) < 30:   # arbitrary floor — too few rows makes the model meaningless
            print(f"Skipping {test_month}: only {len(train_df)} training rows available.")
            continue

        model = LGBMClassifier(n_estimators=200, max_depth=4, learning_rate=0.05)
        model.fit(train_df[FEATURES], train_df["label"])

        test_df = test_df.copy()
        test_df["score"] = model.predict_proba(test_df[FEATURES])[:, 1]

        ranked_test_df = test_df.sort_values("score", ascending=False)
        top_picks = ranked_test_df.head(TOP_K)

        strategy_ret = top_picks["fwd_ret_3m"].mean()      # equal-weighted top-K
        benchmark_ret = test_df["fwd_ret_3m"].mean()        # equal-weighted universe

        # Same ranking, just sliced at different basket sizes — no retraining needed.
        sweep_returns = {
            k: ranked_test_df.head(min(k, len(ranked_test_df)))["fwd_ret_3m"].mean()
            for k in TOP_K_SWEEP
        }

        records.append({
            "month": test_month,
            "strategy_ret": strategy_ret,
            "benchmark_ret": benchmark_ret,
            "universe_median_ret": test_df["fwd_ret_3m"].median(),
            "universe_top_decile_ret": test_df["fwd_ret_3m"].quantile(0.9),
            "n_candidates": len(test_df),
            "picks": ", ".join(top_picks["stock_id"].astype(str)),
            "pick_returns": top_picks.set_index("stock_id")["fwd_ret_3m"].round(4).to_dict(),
            "sweep_returns": sweep_returns,
        })

    results = pd.DataFrame(records)
    if results.empty:
        print("No backtest periods produced — check that training_data has enough history.")
        return results

    results["strategy_nav"] = (1 + results["strategy_ret"]).cumprod()
    results["benchmark_nav"] = (1 + results["benchmark_ret"]).cumprod()

    periods_per_year = 12 / REBALANCE_EVERY  # e.g. 4 rebalances/year if REBALANCE_EVERY=3

    if smoke_test_only:
        print("*** SMOKE TEST RESULTS — NOT A VALIDATED BACKTEST (see warning above) ***\n")

    print("Period-by-period results (this is the number that actually matters right now):")
    print(results[["month", "strategy_ret", "benchmark_ret", "universe_median_ret",
                    "universe_top_decile_ret", "n_candidates"]]
          .to_string(index=False))
    print()

    # Per-stock breakdown for the most recent period — with only 1-2 periods total,
    # this tells you more than the aggregate strategy_ret does: is underperformance
    # spread across most picks, or dragged down by one or two big losers?
    last_period = results.iloc[-1]
    print(f"Pick-by-pick breakdown for {last_period['month']} "
          f"(universe median was {last_period['universe_median_ret']:.2%}, "
          f"top decile was {last_period['universe_top_decile_ret']:.2%}):")
    for stock_id, ret in sorted(last_period["pick_returns"].items(), key=lambda x: -x[1]):
        flag = "OK" if ret >= last_period["universe_median_ret"] else "below median"
        print(f"  stock_id {stock_id}: {ret:+.2%}  ({flag})")
    print()

    # Basket-size sweep — does the model's ranking hold up as you take more of it,
    # or was the top-10 result just noise from a small sample?
    print("Strategy return by basket size, per period (same ranking, different cutoffs):")
    sweep_rows = []
    for _, row in results.iterrows():
        sweep_row = {"month": row["month"], **{f"top{k}": row["sweep_returns"][k] for k in TOP_K_SWEEP}}
        sweep_row["benchmark_mean"] = row["benchmark_ret"]
        sweep_row["universe_median"] = row["universe_median_ret"]
        sweep_rows.append(sweep_row)
    print(pd.DataFrame(sweep_rows).to_string(index=False, float_format=lambda x: f"{x:.2%}"))
    print()

    # Feature importances from the last model trained — sanity-check that the model
    # is actually leaning on sensible signals rather than noise.
    importances = sorted(zip(FEATURES, model.feature_importances_), key=lambda x: -x[1])
    print("Feature importances (last trained model):")
    for feat, imp in importances:
        print(f"  {feat:<22s} {imp}")
    print()

    print(f"Backtest periods: {len(results)}  (rebalance every {REBALANCE_EVERY} months)\n")
    print(f"Strategy CAGR:   {annualize_return(results['strategy_nav'], periods_per_year):.2%}")
    print(f"Benchmark CAGR:  {annualize_return(results['benchmark_nav'], periods_per_year):.2%}")
    print(f"Strategy MaxDD:  {max_drawdown(results['strategy_nav']):.2%}")
    print(f"Strategy Sharpe: {sharpe_ratio(results['strategy_ret'], periods_per_year):.2f}")
    hit_rate = (results["strategy_ret"] > results["benchmark_ret"]).mean()
    print(f"Hit rate vs benchmark (periods strategy beat the universe avg): {hit_rate:.2%}")

    if len(results) < 3:
        print(
            f"\nNote: CAGR/Sharpe above are annualized from only {len(results)} period(s) "
            f"— mathematically computed correctly, but raising a single {REBALANCE_EVERY}-month "
            f"return to the 4th power exaggerates it into a misleading-looking annual number. "
            f"The period-by-period table above (raw strategy_ret vs benchmark_ret) is the only "
            f"real signal you have at this data volume — ignore CAGR/Sharpe until you have "
            f"several more periods."
        )

    results.to_csv("backtest_results.csv", index=False)
    print("\nFull period-by-period results saved to backtest_results.csv")
    return results


if __name__ == "__main__":
    run_backtest()