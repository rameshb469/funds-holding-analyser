INSERT INTO stock_details(
	symbol, name_of_company, industry_id, sector_id, mkt_cap, series, date_of_listing,
	isin_number, created_at, updated_at)
	VALUES ('TENNIND', 'Tenneco Clean Air India Ltd' ,
	(select id from industry where name = 'Auto Ancillaries - Engine Parts'),
	(select sector_id from industry where name = 'Auto Ancillaries - Engine Parts'), 1983, 'EQ',
	date('2025-11-21'), 'INE19RI01016', current_timestamp, current_timestamp);