#!/usr/bin/env python3
import sys
import json
import time
import yfinance as yf
import pandas as pd
import requests
from io import StringIO

# 1️⃣ Input: ISIN list from command-line
if len(sys.argv) < 2:
    print("❌ ISIN numbers required", file=sys.stderr)
    sys.exit(1)

isin_csv = sys.argv[1]
isins = [s.strip() for s in isin_csv.split(",") if s.strip()]

print(f"🚀 Fetching NSE symbol master and details for {len(isins)} ISINs...", file=sys.stderr)

# 2️⃣ Download latest NSE symbol master (EQUITY_L.csv)
try:
    NSE_URL = "https://nsearchives.nseindia.com/content/equities/EQUITY_L.csv"
    headers = {
        "User-Agent": "Mozilla/5.0",
        "Accept-Encoding": "gzip, deflate, br",
        "Accept-Language": "en-US,en;q=0.9",
        "Referer": "https://www.nseindia.com/",
        "Connection": "keep-alive"
    }

    session = requests.Session()
    session.get("https://www.nseindia.com", headers=headers, timeout=10)
    response = session.get(NSE_URL, headers=headers, timeout=10)
    response.raise_for_status()

    symbol_master = pd.read_csv(StringIO(response.text))
    print(f"✅ NSE symbol master loaded with {len(symbol_master)} records", file=sys.stderr)
except Exception as e:
    print(f"❌ Failed to fetch NSE symbol master: {e}", file=sys.stderr)
    sys.exit(1)

# 3️⃣ Map ISIN → SYMBOL
symbol_master.columns = [col.strip().upper() for col in symbol_master.columns]
isin_map = dict(zip(symbol_master["ISIN NUMBER"], symbol_master["SYMBOL"]))

results = []

# 4️⃣ Fetch each symbol’s data via Yahoo Finance
for i, isin in enumerate(isins):
    nse_symbol = isin_map.get(isin)
    if not nse_symbol:
        print(f"⚠️ ISIN not found in NSE list: {isin}", file=sys.stderr)
        results.append({
            "symbol": None,
            "company": None,
            "isinNumber": isin,
            "marketCap": None,
            "sharesOutstanding": None,
            "floatShares": None
        })
        continue

    try:
        print(f"[{i+1}/{len(isins)}] 🔍 Fetching {nse_symbol}.NS ({isin})", file=sys.stderr)
        ticker = yf.Ticker(f"{nse_symbol}.NS")
        info = ticker.info

        results.append({
            "symbol": nse_symbol,
            "company": info.get("shortName"),
            "isinNumber": isin,
            "series": "EQ",
            "marketCap": info.get("marketCap"),
            "sharesOutstanding": info.get("sharesOutstanding"),
            "floatShares": info.get("floatShares")
        })

        time.sleep(0.5)  # prevent rate limiting
    except Exception as e:
        print(f"❌ Failed for {isin}: {e}", file=sys.stderr)
        results.append({
            "symbol": nse_symbol,
            "company": None,
            "isinNumber": isin,
            "marketCap": None,
            "sharesOutstanding": None,
            "floatShares": None
        })

# 5️⃣ Rank and classify
df = pd.DataFrame(results)
df = df.sort_values(by="marketCap", ascending=False, na_position="last").reset_index(drop=True)
df["rank"] = df.index + 1

def classify(rank):
    if rank <= 100:
        return "LargeCap"
    elif rank <= 250:
        return "MidCap"
    else:
        return "SmallCap"

df["marketCapCategory"] = df["rank"].apply(classify)

# 6️⃣ Output clean JSON
json.dump(df.to_dict(orient="records"), sys.stdout, indent=2)
print(file=sys.stderr)
