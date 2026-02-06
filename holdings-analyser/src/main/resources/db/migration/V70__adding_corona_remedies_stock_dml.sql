INSERT INTO stock_details(
	symbol, name_of_company, industry_id, sector_id, mkt_cap, series, date_of_listing,
	isin_number, created_at, updated_at)
	VALUES ('CORONA', 'CORONA Remedies Limited' ,
	(select id from industry where name = 'Pharmaceuticals & Drugs'),
	(select sector_id from industry where name = 'Pharmaceuticals & Drugs'), 874, 'EQ',
	date('2025-12-15'), 'INE02ZQ01018', current_timestamp, current_timestamp);