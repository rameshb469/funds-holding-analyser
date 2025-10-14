import requests
from bs4 import BeautifulSoup
import re

def get_moneycontrol_sector_industry(symbol):
    try:
        # 1️⃣ Search for the company on Moneycontrol
        search_url = f"https://www.moneycontrol.com/mccode/common/autosuggest-get.php?classic=true&query={symbol}"
        resp = requests.get(search_url, headers={'User-Agent': 'Mozilla/5.0'})
        data = resp.text.strip()

        if not data:
            print(f"No search result for symbol: {symbol}")
            return None

        # 2️⃣ Extract the company URL (it appears as part of the response string)
        # Example response: RELIANCE|Reliance Industries|Reliance Industries Ltd.|500325|...|https://www.moneycontrol.com/india/stockpricequote/refineries/relianceindustries/RI
        parts = data.split('|')
        if len(parts) < 6:
            print(f"Invalid data format for {symbol}")
            return None

        company_url = parts[-1]
        print(f"Fetching: {company_url}")

        # 3️⃣ Fetch company page
        resp = requests.get(company_url, headers={'User-Agent': 'Mozilla/5.0'})
        soup = BeautifulSoup(resp.text, 'html.parser')

        # 4️⃣ Locate Sector / Industry
        details = {}
        info_section = soup.find('div', class_='bcrumb')
        if info_section:
            crumbs = [a.text.strip() for a in info_section.find_all('a')]
            if len(crumbs) >= 2:
                details['sector'] = crumbs[-2]
                details['industry'] = crumbs[-1]

        # 5️⃣ If breadcrumbs not found, try meta tags or text search
        if not details:
            meta_desc = soup.find('meta', {'name': 'description'})
            if meta_desc and meta_desc.get('content'):
                text = meta_desc['content']
                match = re.search(r'Industry\s*:\s*([\w\s&-]+)', text)
                if match:
                    details['industry'] = match.group(1).strip()

        return details or {"sector": None, "industry": None}

    except Exception as e:
        print(f"Error for {symbol}: {e}")
        return None


# 🔍 Example usage
symbols = ["INFY", "RELIANCE", "TCS"]
for s in symbols:
    info = get_moneycontrol_sector_industry(s)
    print(f"{s} → {info}")
