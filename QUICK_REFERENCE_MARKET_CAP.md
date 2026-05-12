n# Quick Reference: mcap19122025.csv

## File Summary
- **What**: NSE Market Capitalization Data
- **When**: 19 December 2025 (Trading Day)
- **Records**: 2,774 Listed Companies
- **Size**: ~421 KB

## Quick Download Links

### Direct Downloads:
```
Market Cap Data (Dec 19, 2025):
https://www1.nseindia.com/content/historical/EQUITIES/mcap_19DEC2025.csv

Equity Bhavcopy (Dec 19, 2025):
https://www1.nseindia.com/content/historical/EQUITIES/2025/DEC/eq_19DEC2025_bhav.csv
```

### Get Latest Data:
```
https://www.nseindia.com/products/content/derivatives/equities/equities.htm
→ Click "Download Data" or "Bulk Download"
→ Select Date Range & Download
```

---

## File Columns Explained

| Column | Meaning | Example |
|--------|---------|---------|
| Trade Date | When traded | 19 DEC 2025 |
| Symbol | Stock ticker | RELIANCE, TCS, INFY |
| Series | Type of security | EQ, BE, SM, ST, IT |
| Security Name | Company name | Reliance Industries Limited |
| Category | Listed or Permitted | Listed, Permitted |
| Last Trade Date | Last trading day | 19 DEC 2025 |
| Face Value | Par value per share | ₹1, ₹5, ₹10 |
| Issue Size | Total shares | 404,688,480 |
| Close Price | Stock closing price | ₹1,138.90 |
| Market Cap | Market capitalization | ₹460,899,709,872 |

---

## How to Download

### Manual Download (Easiest):
1. Go to: https://www.nseindia.com/
2. Look for: Market Data → Historical Data → Download
3. Select Date: 19 December 2025
4. Download CSV

### Command Line (Linux/Mac):
```bash
# Using curl
curl -O "https://www1.nseindia.com/content/historical/EQUITIES/mcap_19DEC2025.csv"

# Using wget
wget "https://www1.nseindia.com/content/historical/EQUITIES/mcap_19DEC2025.csv"
```

### Python Script:
```bash
cd /Users/ramesh/IdeaProjects/funds-holding-analyser
python3 holdings-analyser/src/main/resources/scripts/download_nse_market_cap.py
```

---

## Use Cases

### 1. Market Analysis
```python
import pandas as pd
df = pd.read_csv('mcap19122025.csv')
top_100 = df.nlargest(100, 'Market Cap(Rs.)')
```

### 2. Portfolio Analysis
```python
# Find stocks in specific sector
tech_stocks = df[df['Security Name'].str.contains('TECH', case=False)]
```

### 3. Market Statistics
```python
# Total market cap
total = df['Market Cap(Rs.)'].sum()

# Average stock price
avg_price = df['Close Price/Paid up value(Rs.)'].mean()
```

### 4. Database Import
```sql
LOAD DATA INFILE 'mcap19122025.csv'
INTO TABLE market_cap_data
FIELDS TERMINATED BY ','
LINES TERMINATED BY '\n'
IGNORE 1 ROWS;
```

---

## Key Facts

✓ **2,774** listed companies on NSE
✓ **5 Series Types**: EQ, BE, SM, ST, IT
✓ **Categories**: Listed, Permitted
✓ **Updated**: Daily after market close
✓ **Free**: Public data from NSE
✓ **Format**: CSV (Excel compatible)

---

## File Location in Project

```
/Users/ramesh/IdeaProjects/funds-holding-analyser/
  └── holdings-analyser/
      └── src/main/resources/
          └── mcap19122025.csv  ← Current File
```

---

## More Information

- **NSE Official Website**: https://www.nseindia.com/
- **Market Data Page**: https://www.nseindia.com/market-data
- **Historical Data Archive**: https://www1.nseindia.com/products/content/derivatives/equities/hist_data.htm
- **NSE API Docs**: https://www1.nseindia.com/products/content/derivatives/equities/api_docs.htm

---

## Date Format Reference

```
File naming: mcap_DDMMMYYYY.csv
Example: mcap_19DEC2025.csv

Breakdown:
- DD = Day (19)
- MMM = Month (DEC)
- YYYY = Year (2025)
```

---

**Last Updated**: 19 DEC 2025 | **Created**: This Guide

