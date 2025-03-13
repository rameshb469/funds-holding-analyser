INSERT INTO mutual_fund_config (
    mutual_fund_id, download_url, is_zipped, sheet_name, content_type, extension,
    isin_col_mapper, stock_col_mapper, industry_col_mapper, quantity_col_mapper,
    market_value_col_mapper, net_asset_perc_col_mapper, is_pick_by_system,
    date_mapper_1, date_mapper_3, date_mapper_4,
    date_mapper_1_config, date_mapper_3_config, date_mapper_4_config
)
VALUES
(
    (SELECT id FROM mutual_fund WHERE name = 'Bank of India Large & Mid Cap Equity Fund'),
    'https://www.boimf.in/docs/default-source/investorcorner/monthly-portfolio/monthly-portfolio---${DATE_MAPPER_1}.xlsx',
    false, 'YB04', 'excel', 'xlsx',
    2, 1, 3, 4, 5, 6, false,
    'dd-MMMM-yyyy', '', '',
    '', '', ''
),
(
    (SELECT id FROM mutual_fund WHERE name = 'Bank of India Bluechip Fund'),
    'https://www.boimf.in/docs/default-source/investorcorner/monthly-portfolio/monthly-portfolio---${DATE_MAPPER_1}.xlsx',
    false, 'YB37', 'excel', 'xlsx',
    2, 1, 3, 4, 5, 6, false,
    'dd-MMMM-yyyy', '', '',
    '', '', ''
),
(
    (SELECT id FROM mutual_fund WHERE name = 'Bank of India ELSS Tax Saver Fund'),
    'https://www.boimf.in/docs/default-source/investorcorner/monthly-portfolio/monthly-portfolio---${DATE_MAPPER_1}.xlsx',
    false, 'YB08', 'excel', 'xlsx',
    2, 1, 3, 4, 5, 6, false,
    'dd-MMMM-yyyy', '', '',
    '', '', ''
),
(
    (SELECT id FROM mutual_fund WHERE name = 'Bank Of India small cap fund'),
    'https://www.boimf.in/docs/default-source/investorcorner/monthly-portfolio/monthly-portfolio---${DATE_MAPPER_1}.xlsx',
    false, 'YB34', 'excel', 'xlsx',
    2, 1, 3, 4, 5, 6, false,
    'dd-MMMM-yyyy', '', '',
    '', '', ''
),
(
    (SELECT id FROM mutual_fund WHERE name = 'Bank of India Flexi Cap Fund'),
    'https://www.boimf.in/docs/default-source/investorcorner/monthly-portfolio/monthly-portfolio---${DATE_MAPPER_1}.xlsx',
    false, 'YB36', 'excel', 'xlsx',
    2, 1, 3, 4, 5, 6, false,
    'dd-MMMM-yyyy', '', '',
    '', '', ''
),
(
    (SELECT id FROM mutual_fund WHERE name = 'Bank of India Mid & Small Cap Equity & Debt Fund'),
    'https://www.boimf.in/docs/default-source/investorcorner/monthly-portfolio/monthly-portfolio---${DATE_MAPPER_1}.xlsx',
    false, 'YB30', 'excel', 'xlsx',
    2, 1, 3, 4, 5, 6, false,
    'dd-MMMM-yyyy', '', '',
    '', '', ''
);
