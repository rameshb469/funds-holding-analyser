INSERT INTO mutual_fund_config (
    mutual_fund_id, download_url, is_zipped, sheet_name, content_type, extension,
    isin_col_mapper, stock_col_mapper, industry_col_mapper, quantity_col_mapper,
    market_value_col_mapper, net_asset_perc_col_mapper, is_pick_by_system,
    date_mapper_1, date_mapper_2, date_mapper_3, date_mapper_4,
    date_mapper_1_config, date_mapper_2_config, date_mapper_3_config, date_mapper_4_config
)
VALUES
(
    (SELECT id FROM mutual_fund WHERE name = 'Axis Bluechip Fund'),
    'https://www.axismf.com/cms/sites/default/files/Statutory/Monthly Portfolio-${DATE_MAPPER_1}.xlsx',
    False, 'AXISEQF', 'excel', 'xlsx',
    1, 2, 3, 4, 5, 6, False,
    'dd MMM yyyy', '', '', '',
    '', '', '', ''
),
(
    (SELECT id FROM mutual_fund WHERE name = 'Axis Midcap Fund'),
    'https://www.axismf.com/cms/sites/default/files/Statutory/Monthly Portfolio-${DATE_MAPPER_1}.xlsx',
    False, 'AXISMCF', 'excel', 'xlsx',
    1, 2, 3, 4, 5, 6, False,
    'dd MMM yyyy', '', '', '',
    '', '', '', ''
),
(
    (SELECT id FROM mutual_fund WHERE name = 'Axis Small Cap Fund'),
    'https://www.axismf.com/cms/sites/default/files/Statutory/Monthly Portfolio-${DATE_MAPPER_1}.xlsx',
    False, 'AXISSCF', 'excel', 'xlsx',
    1, 2, 3, 4, 5, 6, False,
    'dd MMM yyyy', '', '', '',
    '', '', '', ''
),
(
    (SELECT id FROM mutual_fund WHERE name = 'Axis Long Term Equity Fund'),
    'https://www.axismf.com/cms/sites/default/files/Statutory/Monthly Portfolio-${DATE_MAPPER_1}.xlsx',
    False, 'AXISTSF', 'excel', 'xlsx',
    1, 2, 3, 4, 5, 6, False,
    'dd MMM yyyy', '', '', '',
    '', '', '', ''
),
(
    (SELECT id FROM mutual_fund WHERE name = 'Axis Growth Opportunities Fund'),
    'https://www.axismf.com/cms/sites/default/files/Statutory/Monthly Portfolio-${DATE_MAPPER_1}.xlsx',
    False, 'AXISGOF', 'excel', 'xlsx',
    1, 2, 3, 4, 5, 6, False,
    'dd MMM yyyy', '', '', '',
    '', '', '', ''
),
(
    (SELECT id FROM mutual_fund WHERE name = 'Axis Nifty 100 Index Fund'),
    'https://www.axismf.com/cms/sites/default/files/Statutory/Monthly Portfolio-${DATE_MAPPER_1}.xlsx',
    False, 'AXISNIF', 'excel', 'xlsx',
    1, 2, 3, 4, 5, 6, False,
    'dd MMM yyyy', '', '', '',
    '', '', '', ''
),
(
    (SELECT id FROM mutual_fund WHERE name = 'Axis Value Fund'),
    'https://www.axismf.com/cms/sites/default/files/Statutory/Monthly Portfolio-${DATE_MAPPER_1}.xlsx',
    False, 'AXISVAL', 'excel', 'xlsx',
    1, 2, 3, 4, 5, 6, False,
    'dd MMM yyyy', '', '', '',
    '', '', '', ''
),
(
    (SELECT id FROM mutual_fund WHERE name = 'Axis Focused 25 Fund'),
    'https://www.axismf.com/cms/sites/default/files/Statutory/Monthly Portfolio-${DATE_MAPPER_1}.xlsx',
    False, 'AXISF25', 'excel', 'xlsx',
    1, 2, 3, 4, 5, 6, False,
    'dd MMM yyyy', '', '', '',
    '', '', '', ''
);


INSERT INTO mutual_fund_config (
    mutual_fund_id, download_url, is_zipped, sheet_name, content_type, extension,
    isin_col_mapper, stock_col_mapper, industry_col_mapper, quantity_col_mapper,
    market_value_col_mapper, net_asset_perc_col_mapper, is_pick_by_system,
    date_mapper_1, date_mapper_2, date_mapper_3, date_mapper_4,
    date_mapper_1_config, date_mapper_2_config, date_mapper_3_config, date_mapper_4_config
)
VALUES
(
    (SELECT id FROM mutual_fund WHERE name = 'Axis Bluechip Fund'),
    'https://www.axismf.com/cms/sites/default/files/Statutory/Monthly Portfolio as on ${DATE_MAPPER_1}.xlsx',
    False, 'AXISEQF', 'excel', 'xlsx',
    1, 2, 3, 4, 5, 6, False,
    'MMM dd yyyy', '', '', '',
    '', '', '', ''
),
(
    (SELECT id FROM mutual_fund WHERE name = 'Axis Midcap Fund'),
    'https://www.axismf.com/cms/sites/default/files/Statutory/Monthly Portfolio as on ${DATE_MAPPER_1}.xlsx',
    False, 'AXISMCF', 'excel', 'xlsx',
    1, 2, 3, 4, 5, 6, False,
    'MMM dd yyyy', '', '', '',
    '', '', '', ''
),
(
    (SELECT id FROM mutual_fund WHERE name = 'Axis Small Cap Fund'),
    'https://www.axismf.com/cms/sites/default/files/Statutory/Monthly Portfolio as on ${DATE_MAPPER_1}.xlsx',
    False, 'AXISSCF', 'excel', 'xlsx',
    1, 2, 3, 4, 5, 6, False,
    'MMM dd yyyy', '', '', '',
    '', '', '', ''
),
(
    (SELECT id FROM mutual_fund WHERE name = 'Axis Long Term Equity Fund'),
    'https://www.axismf.com/cms/sites/default/files/Statutory/Monthly Portfolio as on ${DATE_MAPPER_1}.xlsx',
    False, 'AXISTSF', 'excel', 'xlsx',
    1, 2, 3, 4, 5, 6, False,
    'MMM dd yyyy', '', '', '',
    '', '', '', ''
),
(
    (SELECT id FROM mutual_fund WHERE name = 'Axis Growth Opportunities Fund'),
    'https://www.axismf.com/cms/sites/default/files/Statutory/Monthly Portfolio as on ${DATE_MAPPER_1}.xlsx',
    False, 'AXISGOF', 'excel', 'xlsx',
    1, 2, 3, 4, 5, 6, False,
    'MMM dd yyyy', '', '', '',
    '', '', '', ''
),
(
    (SELECT id FROM mutual_fund WHERE name = 'Axis Nifty 100 Index Fund'),
    'https://www.axismf.com/cms/sites/default/files/Statutory/Monthly Portfolio as on ${DATE_MAPPER_1}.xlsx',
    False, 'AXISNIF', 'excel', 'xlsx',
    1, 2, 3, 4, 5, 6, False,
    'MMM dd yyyy', '', '', '',
    '', '', '', ''
),
(
    (SELECT id FROM mutual_fund WHERE name = 'Axis Value Fund'),
    'https://www.axismf.com/cms/sites/default/files/Statutory/Monthly Portfolio as on ${DATE_MAPPER_1}.xlsx',
    False, 'AXISVAL', 'excel', 'xlsx',
    1, 2, 3, 4, 5, 6, False,
    'MMM dd yyyy', '', '', '',
    '', '', '', ''
),
(
    (SELECT id FROM mutual_fund WHERE name = 'Axis Focused 25 Fund'),
    'https://www.axismf.com/cms/sites/default/files/Statutory/Monthly Portfolio as on ${DATE_MAPPER_1}.xlsx',
    False, 'AXISF25', 'excel', 'xlsx',
    1, 2, 3, 4, 5, 6, False,
    'MMM dd yyyy', '', '', '',
    '', '', '', ''
);
