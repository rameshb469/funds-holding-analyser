#!/usr/bin/env python3
import json
import yfinance as yf
import time
import pandas as pd
import sys

# --------------------------
# 1. Input JSON file path
# --------------------------
input_file = "stocks.json"  # JSON file containing ["INFY", "RELIANCE", "SBIN", ...]
output_file = "stocks_enriched.json"

# --------------------------
# 2. Load symbols
# --------------------------
try:
    with open(input_file, "r") as f:
        symbols = json.load(f)

    if not isinstance(symbols, list):
        raise ValueError("JSON file must contain a list of symbols")

except Exception as e:
    print(f"❌ Failed to read input file: {e}")
    sys.exit(1)

print(f"🚀 Fetching data for {len(symbols)} symbols...\n")

# --------------------------
# 3. Fetch stock details
# --------------------------
results = []

for i, symbol in enumerate(symbols):
    try:
        print(f"[{i+1}/{len(symbols)}] Fetching {symbol} ...")
        ticker = yf.Ticker(f"{symbol}.NS")  # NSE suffix; change to .BO for BSE
        info = ticker.info

        market_cap = info.get("marketCap")
        shares_outstanding = info.get("sharesOutstanding")
        float_shares = info.get("floatShares")

        results.append({
            "symbol": symbol,
            "marketCap": market_cap,
            "sharesOutstanding": shares_outstanding,
            "floatShares": float_shares
        })

        print(f"✅ Done: MarketCap={market_cap}, Shares={shares_outstanding}")
        time.sleep(0.5)

    except Exception as e:
        print(f"❌ Failed for {symbol}: {e}")
        results.append({
            "symbol": symbol,
            "marketCap": None,
            "sharesOutstanding": None,
            "floatShares": None
        })

# --------------------------
# 4. Rank & marketCapCategory
# --------------------------
df = pd.DataFrame(results)
df = df.sort_values(by="marketCap", ascending=False, na_position='last').reset_index(drop=True)
df['rank'] = df.index + 1

def classify(rank):
    if rank <= 100:
        return "LargeCap"
    elif rank <= 250:
        return "MidCap"
    else:
        return "SmallCap"

df['marketCapCategory'] = df['rank'].apply(classify)

# --------------------------
# 5. Save enriched JSON
# --------------------------
enriched_data = df.to_dict(orient="records")

try:
    with open(output_file, "w") as f:
        json.dump(enriched_data, f, indent=2)
    print(f"\n✅ Finished! Enriched data saved to {output_file}")

except Exception as e:
    print(f"❌ Failed to save output: {e}")
