import requests
import zipfile
import io
import pandas as pd
import json
import sys
from datetime import datetime

def fetch_bse_bhavcopy(date_str):
    """Fetch BSE Bhavcopy zip for given date (format: DDMMYY)."""
    url = f"https://www.bseindia.com/download/BhavCopy/Equity/EQ{date_str}_CSV.ZIP"
    print(f"📥 Downloading {url} ...")
    r = requests.get(url, timeout=30)
    r.raise_for_status()

    z = zipfile.ZipFile(io.BytesIO(r.content))
    file_name = z.namelist()[0]
    df = pd.read_csv(z.open(file_name))

    # Normalize column names
    df.columns = [c.strip().upper() for c in df.columns]

    records = []
    for _, row in df.iterrows():
        symbol = row["SC_CODE"]
        records.append({
            "symbol": row["SC_CODE"],  # Map this to your stock_details.symbol
            "date": datetime.strptime(row["DATE1"], "%d-%b-%Y").strftime("%Y-%m-%d"),
            "open": float(row["OPEN"]),
            "high": float(row["HIGH"]),
            "low": float(row["LOW"]),
            "close": float(row["CLOSE"]),
            "volume": int(row["NO_OF_SHRS"])
        })

    return records

if __name__ == "__main__":
    # Pass date as argument: e.g. python bse_bhavcopy_fetch.py 120925
    if len(sys.argv) != 2:
        print("❌ Usage: python bse_bhavcopy_fetch.py <DDMMYY>")
        sys.exit(1)

    date_str = sys.argv[1]
    try:
        records = fetch_bse_bhavcopy(date_str)
        print(json.dumps(records, indent=2))
    except Exception as e:
        print(f"❌ Error fetching bhavcopy: {e}")
        sys.exit(1)
