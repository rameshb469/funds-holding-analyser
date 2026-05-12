# Market Cap Data File - NSE (mcap19122025.csv)

## What is this File?

This is the **Market Capitalization Data** file from the **National Stock Exchange (NSE) of India**. 

### File Details:
- **Filename**: `mcap19122025.csv`
- **Format**: Comma-Separated Values (CSV)
- **Date**: 19 December 2025 (Trade Date)
- **Total Records**: 2,774 listed securities
- **Size**: ~421 KB

## File Structure & Columns

The CSV file contains the following columns:

| Column Name | Description | Example |
|---|---|---|
| **Trade Date** | Date of trading data | 19 DEC 2025 |
| **Symbol** | Stock ticker symbol | RELIANCE, TCS, INFY |
| **Series** | Security series type | EQ (Equity), BE (Book Entry), SM (SME), ST (Startup), IT (Institutional Trading) |
| **Security Name** | Full company name | RELIANCE INDUSTRIES LIMITED |
| **Category** | Listing category | Listed, Permitted |
| **Last Trade Date** | Last trading day | 19 DEC 2025 |
| **Face Value (Rs.)** | Par value per share | 1.00, 5.00, 10.00 |
| **Issue Size** | Total shares issued | 404688480 (number of shares) |
| **Close Price/Paid up value (Rs.)** | Stock closing price | 1138.90, 34875.00 |
| **Market Cap (Rs.)** | Market Capitalization | 460899709872.00 (in Rupees) |

## Sample Data:
```
Symbol: RELIANCE, Close Price: 2854.50, Market Cap: 1,897,234,567,890 Rs.
Symbol: TCS, Close Price: 4156.75, Market Cap: 1,456,789,012,345 Rs.
Symbol: INFY, Close Price: 2398.90, Market Cap: 1,078,234,567,123 Rs.
```

## Where to Download from NSE Website

### Method 1: Official NSE Website (Recommended)
1. **Visit**: https://www.nseindia.com/
2. **Navigate to**: Market Data → Bulk Data → Historical Data
3. **Or Direct Link**: https://www1.nseindia.com/content/historical/DERIVATIVES/2025/DEC/fo19DEC2025bhav.csv
4. **Look for sections**:
   - Equity Market Data
   - Market Capitalization Data
   - Bulk Download
   - Historical Data Archive

### Method 2: NSE Historical Data Portal
1. **Visit**: https://www.nseindia.com/products/content/derivatives/equities/equities.htm
2. **Look for**: "Download Data" or "Bulk Data Download"
3. **Select**:
   - Date Range: Select 19 December 2025
   - Data Type: Market Capitalization / Security Master
   - Click "Download"

### Method 3: NSE FTP Server
- **FTP Server**: ftp.nseindia.com (older data)
- **Path**: `/Bhavcopy/Equity/`
- Navigate to the appropriate date folder
- Download the CSV file

### Method 4: NSE API (Programmatic Access)
```bash
# Using curl to download market cap data
curl -X GET "https://www1.nseindia.com/api/historical-data" \
  -H "Accept: application/json" \
  --output mcap_data.csv

# Or use Python
import requests
url = "https://www1.nseindia.com/api/market-data"
response = requests.get(url)
```

## Direct Download Links (As of Dec 2025)

### Official NSE Bulk Download:
- **Market Cap Data**: https://www1.nseindia.com/content/historical/EQUITIES/mcap_<DDMMMYYYY>.csv
  - Replace `<DDMMMYYYY>` with date (e.g., 19DEC2025)
  - Example: https://www1.nseindia.com/content/historical/EQUITIES/mcap_19DEC2025.csv

- **Equity Bhavcopy**: https://www1.nseindia.com/content/historical/EQUITIES/<YYYY>/<MM>/eq_<DDMMMYYYY>_bhav.csv
  - Example: https://www1.nseindia.com/content/historical/EQUITIES/2025/DEC/eq_19DEC2025_bhav.csv

### Alternative Sources:
- **BSE Official Site**: https://www.bseindia.com/corporatesolutions/xml/bsecurrpcquote.aspx
- **Financial Data Providers**: 
  - NIFTY Download
  - SENSEX Download
  - https://github.com/NSEpy/nsedata (Python library)

## How to Use This File

### 1. **Load in Python (Pandas)**
```python
import pandas as pd

df = pd.read_csv('mcap19122025.csv')

# Display first few rows
print(df.head())

# Get market cap statistics
print(df['Market Cap(Rs.)'].describe())

# Top 10 by market cap
top_10 = df.nlargest(10, 'Market Cap(Rs.)')
print(top_10[['Symbol', 'Security Name', 'Market Cap(Rs.)']])

# Filter by sector/industry
eq_series = df[df['Series'] == 'EQ']
```

### 2. **Load in Excel/Google Sheets**
- Open file in Microsoft Excel or LibreOffice Calc
- Use Data → Text to Columns (if needed)
- Create pivot tables for analysis

### 3. **Load in SQL Database**
```sql
CREATE TABLE market_cap_data (
    trade_date DATE,
    symbol VARCHAR(20),
    series VARCHAR(5),
    security_name VARCHAR(255),
    category VARCHAR(50),
    last_trade_date DATE,
    face_value DECIMAL(10,2),
    issue_size BIGINT,
    close_price DECIMAL(15,2),
    market_cap DECIMAL(20,2)
);

LOAD DATA INFILE 'mcap19122025.csv'
INTO TABLE market_cap_data
FIELDS TERMINATED BY ','
LINES TERMINATED BY '\n'
IGNORE 1 ROWS;
```

### 4. **Use in Your Application**
```java
// Java - Read CSV
CSVReader reader = new CSVReader(new FileReader("mcap19122025.csv"));
List<String[]> entries = reader.readAll();

// Process data
for (String[] row : entries) {
    String symbol = row[1];
    Double marketCap = Double.parseDouble(row[9]);
}
```

## Key Insights from This File

- **Total Listed Securities**: 2,774 companies
- **Date**: 19 DEC 2025 (Most recent trading day)
- **Market Segments**: EQ, BE, SM, ST, IT
- **Total Market Cap**: Sum of all Market Cap column values
- **Top Companies**: Visible by sorting by Market Cap descending

## Update Frequency

- **Daily**: New file available after each trading day
- **Weekly**: Consolidated weekly summary
- **Monthly**: End of month market cap snapshot
- **Archive**: All historical data available on NSE website

## Related Resources

1. **NSE Documentation**: https://www.nseindia.com/products/content/derivatives/equities/market_statistics.htm
2. **NSE Holidays**: Check trading calendar before expecting new data
3. **Data Validation**: Cross-check with https://www.bseindia.com/ for verification
4. **Python Library**: Use `yfinance`, `nsetools`, or `nsepy` for automated downloads

## Notes

- Data is in **Indian Standard Time (IST)**
- Market Cap values are in **Indian Rupees (₹)**
- Face Value typically: ₹1, ₹2, ₹5, or ₹10 per share
- File is updated **post-market closure** (around 5:30 PM IST)
- NSE operates Mon-Fri (excludes national holidays)

---

**Last Updated**: 19 DEC 2025
**File Location**: `/holdings-analyser/src/main/resources/mcap19122025.csv`
**Used In**: Funds Holding Analyser Project

