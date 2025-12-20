INSERT INTO stock_details(
	symbol, name_of_company, industry_id, sector_id, mkt_cap, series, date_of_listing,
	isin_number, created_at, updated_at)
	VALUES ('LENSKART', 'Lenskart Solutions Ltd' ,
	(select id from industry where name = 'Retailing'),
	(select sector_id from industry where name = 'Retailing'), 75678, 'EQ',
	date('2025-11-10'), 'INE956O01016', current_timestamp, current_timestamp);