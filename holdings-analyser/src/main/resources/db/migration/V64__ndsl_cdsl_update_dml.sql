INSERT INTO stock_details(
	symbol, name_of_company, industry_id, sector_id, mkt_cap, series, date_of_listing,
	isin_number, created_at, updated_at)
	VALUES ('NSDL','National Securities Depository Ltd' ,
	(select id from industry where name = 'Finance - Investment'),
	(select sector_id from industry where name = 'Finance - Investment'), 18678, 'EQ',
	date('2025-08-06'), 'INE301O01023', current_timestamp, current_timestamp);

update stock_details
set industry_id = (select id from industry where name = 'Finance - Investment'),
sector_id = (select sector_id from industry where name = 'Finance - Investment')
where symbol = 'CDSL';