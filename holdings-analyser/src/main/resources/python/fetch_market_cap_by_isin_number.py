import json
import requests
import time
import yfinance as yf
import pandas as pd

def get_symbol_from_isin(isin):
    url = 'https://query1.finance.yahoo.com/v1/finance/search'
    params = {'q': isin, 'quotesCount': 1, 'newsCount': 0, 'listsCount': 0, 'quotesQueryId': 'tss_match_phrase_query'}
    headers = {'User-Agent': 'Mozilla/5.0'}
    resp = requests.get(url, params=params, headers=headers)
    data = resp.json()
    if 'quotes' in data and data['quotes']:
        return data['quotes'][0]['symbol']
    return None

# Example input
isin_input = "INE301O01023"
isin_list = [isin.strip() for isin in isin_input.split(",")]

all_details = []
failed = []

for i, isin in enumerate(isin_list, start=1):
    print(f"[{i}/{len(isin_list)}] Resolving ISIN {isin} ...", end=" ")
    symbol = get_symbol_from_isin(isin)
    if not symbol:
        print("❌ Symbol not found")
        failed.append(isin)
        all_details.append({"isin": isin, "error": "Symbol mapping failed"})
        continue

    print(f"Found symbol: {symbol}. Fetching data...", end=" ")
    try:
        ticker = yf.Ticker(symbol)
        info = ticker.info
        stock_data = {"isin": isin, "symbol": symbol, **info}
        all_details.append(stock_data)
        print("✅ Done")
    except Exception as e:
        print(f"❌ Error fetching data: {e}")
        failed.append(isin)
        all_details.append({"isin": isin, "symbol": symbol, "error": str(e)})

    time.sleep(0.5)

# Ranking & categorization
df = pd.DataFrame(all_details)
if 'marketCap' in df.columns:
    df = df.sort_values(by='marketCap', ascending=False, na_position='last').reset_index(drop=True)
    df['rank'] = df.index + 1
    df['marketCapCategory'] = df['rank'].apply(lambda r: 'LargeCap' if r <= 100 else ('MidCap' if r <= 250 else 'SmallCap'))

output = df.to_dict(orient='records')
with open('isin_report.json', 'w') as f:
    json.dump(output, f, indent=2)

print("\nReport saved to isin_report.json")
if failed:
    print("Issues with ISINs:", failed)
