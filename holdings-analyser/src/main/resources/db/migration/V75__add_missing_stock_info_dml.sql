INSERT INTO stock_details(
    symbol, name_of_company, industry_id, sector_id, mkt_cap, series,
    date_of_listing, isin_number, created_at, updated_at)
VALUES (
    'BHARATCOAL', 'Bharat Coking Coal Ltd.',
    (SELECT id FROM industry WHERE name = 'Coal'),
    (SELECT sector_id FROM industry WHERE name = 'Coal'),
    171, 'EQ',
    date('2026-01-19'), 'INE05XR01022', current_timestamp, current_timestamp
);

INSERT INTO stock_details(
    symbol, name_of_company, industry_id, sector_id, mkt_cap, series,
    date_of_listing, isin_number, created_at, updated_at)
VALUES (
    'AMAGI', 'Amagi Media Labs Ltd',
    (SELECT id FROM industry WHERE name = 'Software'),
    (SELECT sector_id FROM industry WHERE name = 'Software'),
    898, 'EQ',
    date('2026-01-21'), 'INE121R01077', current_timestamp, current_timestamp
);

INSERT INTO stock_details(
    symbol, name_of_company, industry_id, sector_id, mkt_cap, series,
    date_of_listing, isin_number, created_at, updated_at)
VALUES (
    'INDIGRID', 'IndiGrid Infrastructure Trust Ltd',
    (SELECT id FROM industry WHERE name = 'Power Generation/Distribution'),
    (SELECT sector_id FROM industry WHERE name = 'Power Generation/Distribution'),
    1568, 'EQ',
    date('2017-06-6'), 'INE219X23014', current_timestamp, current_timestamp
);

