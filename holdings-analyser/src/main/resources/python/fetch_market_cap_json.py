import json
import yfinance as yf
import time
import pandas as pd

# Load input JSON
file_path = "stocks.json"

with open(file_path, "r") as f:
    stocks = json.load(f)

print(f"🚀 Starting fetch for {len(stocks)} stocks...\n")

# Convert to DataFrame for easier sorting
df = pd.DataFrame(stocks)

market_caps = []
shares_outstanding = []

for i, symbol in enumerate(df['symbol']):
    try:
        print(f"[{i+1}/{len(df)}] Fetching {symbol} ...", end=" ")

        ticker = yf.Ticker(f"{symbol}.NS")  # NSE suffix (use .BO for BSE)
        info = ticker.info

        market_cap = info.get("marketCap", None)
        shares = info.get("sharesOutstanding", None)

        market_caps.append(market_cap)
        shares_outstanding.append(shares)

        print(f"✅ Done (MarketCap={market_cap}, Shares={shares})")

        time.sleep(0.5)  # avoid throttling

    except Exception as e:
        print(f"❌ Failed for {symbol}: {e}")
        market_caps.append(None)
        shares_outstanding.append(None)

# Add results to DataFrame
df['marketCap'] = market_caps
df['sharesOutstanding'] = shares_outstanding

# Categorize by rank
df = df.sort_values(by="marketCap", ascending=False).reset_index(drop=True)
df['rank'] = df.index + 1

def classify(rank):
    if rank <= 100:
        return "LargeCap"
    elif rank <= 250:
        return "MidCap"
    else:
        return "SmallCap"

df['marketCapCategory'] = df['rank'].apply(classify)

# Convert back to JSON
enriched_stocks = df.to_dict(orient="records")

# Save enriched JSON
output_file = "stock_details_enriched.json"
with open(output_file, "w") as f:
    json.dump(enriched_stocks, f, indent=2)

print("\n✅ Finished fetching data!")
print(f"📂 Enriched JSON saved to {output_file}")
