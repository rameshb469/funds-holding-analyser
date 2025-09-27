import requests
import zipfile
import io
import pandas as pd
import json
import sys

def fetch_nse_bhavcopy(date_str):
    """
    date_str: DDMMYYYY (e.g., 12092025)
    NSE URL requires DDMMMYYYY (12SEP2025).
    """

    # Parse date
    day = date_str[:2]
    month = date_str[2:4]
    year = date_str[4:]

    # Month map
    month_map = {
        "01": "JAN", "02": "FEB", "03": "MAR", "04": "APR",
        "05": "MAY", "06": "JUN", "07": "JUL", "08": "AUG",
        "09": "SEP", "10": "OCT", "11": "NOV", "12": "DEC"
    }

    mmm = month_map[month]
    url = f"https://archives.nseindia.com/content/historical/EQUITIES/{year}/{mmm}/cm{day}{mmm}{year}bhav.csv.zip"

    headers = {
        "User-Agent": "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) "
                      "AppleWebKit/537.36 (KHTML, like Gecko) "
                      "Chrome/118.0.0.0 Safari/537.36",
        "Accept": "application/zip"
    }

    try:
        resp = requests.get(url, headers=headers, timeout=30)
        resp.raise_for_status()
    except Exception as e:
        sys.stderr.write(f"❌ Error fetching NSE bhavcopy: {e}\n")
        return []

    # unzip
    z = zipfile.ZipFile(io.BytesIO(resp.content))
    csv_name = z.namelist()[0]
    df = pd.read_csv(z.open(csv_name))

    # Normalize columns
    df = df.rename(columns={
        "SYMBOL": "symbol",
        "SERIES": "series",
        "OPEN": "open",
        "HIGH": "high",
        "LOW": "low",
        "CLOSE": "close",
        "TOTTRDQTY": "volume",
        "TOTTRDVAL": "turnover",
        "TIMESTAMP": "date"
    })

    # Only EQ series (ignore BE, BL, etc.)
    df = df[df['series'] == "EQ"]

    records = df.to_dict(orient="records")
    print(json.dumps(records))  # stdout → Java

    return records


if __name__ == "__main__":
    if len(sys.argv) < 2:
        sys.stderr.write("Usage: python fetch_nse_bhavcopy.py DDMMYYYY\n")
        sys.exit(1)

    fetch_nse_bhavcopy(sys.argv[1])
