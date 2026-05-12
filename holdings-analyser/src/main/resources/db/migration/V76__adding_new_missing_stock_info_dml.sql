INSERT INTO stock_details(
    symbol, name_of_company, industry_id, sector_id, mkt_cap, series,
    date_of_listing, isin_number, created_at, updated_at)
VALUES (
    'CMPDI', 'Central Mine Planning & Design Institute Ltd"',
    (SELECT id FROM industry WHERE name = 'Misc. Commercial Services'),
    (SELECT sector_id FROM industry WHERE name = 'Misc. Commercial Services'),
    1568, 'EQ',
    date('2026-05-11'), 'INE05HV01027', current_timestamp, current_timestamp
);
