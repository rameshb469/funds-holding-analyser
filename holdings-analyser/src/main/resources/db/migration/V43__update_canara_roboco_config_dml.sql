UPDATE mutual_fund_config
SET download_url = REPLACE(download_url, 'www.canararobeco.com', 'old.canararobeco.com')
WHERE download_url LIKE '%www.canararobeco.com%';