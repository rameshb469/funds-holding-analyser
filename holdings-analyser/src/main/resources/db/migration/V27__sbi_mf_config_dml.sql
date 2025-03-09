INSERT INTO mutual_fund_config (
    mutual_fund_id, download_url, is_zipped, sheet_name, content_type, extension,
    isin_col_mapper, stock_col_mapper, industry_col_mapper, quantity_col_mapper,
    market_value_col_mapper, net_asset_perc_col_mapper, is_pick_by_system,
    date_mapper_1, date_mapper_2, date_mapper_3, date_mapper_4,
    date_mapper_1_config, date_mapper_2_config, date_mapper_3_config, date_mapper_4_config
)
VALUES
(
    (SELECT id FROM mutual_fund WHERE name = 'SBI Bluechip Fund'),
    'https://www.sbimf.com/docs/default-source/scheme-portfolios/all-schemes-monthly-portfolio---as-on-${DATE_MAPPER_1}.xlsx',
    False, 'SBLUECHIP', 'excel', 'xlsx', 3, 2, 4, 5, 6, 7, False, 'ddN-MMMM-yyyy', '', '', '', '', '', '', ''
),
(
    (SELECT id FROM mutual_fund WHERE name = 'SBI Magnum Midcap Fund'),
    'https://www.sbimf.com/docs/default-source/scheme-portfolios/all-schemes-monthly-portfolio---as-on-${DATE_MAPPER_1}.xlsx',
    False, 'SMIDCAP', 'excel', 'xlsx', 3, 2, 4, 5, 6, 7, False, 'ddN-MMMM-yyyy', '', '', '', '', '', '', ''
),
(
    (SELECT id FROM mutual_fund WHERE name = 'SBI Small Cap Fund'),
    'https://www.sbimf.com/docs/default-source/scheme-portfolios/all-schemes-monthly-portfolio---as-on-${DATE_MAPPER_1}.xlsx',
    False, 'SSCF', 'excel', 'xlsx', 3, 2, 4, 5, 6, 7, False, 'ddN-MMMM-yyyy', '', '', '', '', '', '', ''
),
(
    (SELECT id FROM mutual_fund WHERE name = 'SBI Flexicap Fund'),
    'https://www.sbimf.com/docs/default-source/scheme-portfolios/all-schemes-monthly-portfolio---as-on-${DATE_MAPPER_1}.xlsx',
    False, 'SFLEXI', 'excel', 'xlsx', 3, 2, 4, 5, 6, 7, False, 'ddN-MMMM-yyyy', '', '', '', '', '', '', ''
),
(
    (SELECT id FROM mutual_fund WHERE name = 'SBI Focused Equity Fund'),
    'https://www.sbimf.com/docs/default-source/scheme-portfolios/all-schemes-monthly-portfolio---as-on-${DATE_MAPPER_1}.xlsx',
    False, 'SFEF', 'excel', 'xlsx', 3, 2, 4, 5, 6, 7, False, 'ddN-MMMM-yyyy', '', '', '', '', '', '', ''
),
(
    (SELECT id FROM mutual_fund WHERE name = 'SBI Magnum Taxgain Scheme'),
    'https://www.sbimf.com/docs/default-source/scheme-portfolios/all-schemes-monthly-portfolio---as-on-${DATE_MAPPER_1}.xlsx',
    False, 'SMAGNUMTAXGAIN', 'excel', 'xlsx', 3, 2, 4, 5, 6, 7, False, 'ddN-MMMM-yyyy', '', '', '', '', '', '', ''
),
(
    (SELECT id FROM mutual_fund WHERE name = 'SBI Equity Hybrid Fund'),
    'https://www.sbimf.com/docs/default-source/scheme-portfolios/all-schemes-monthly-portfolio---as-on-${DATE_MAPPER_1}.xlsx',
    False, 'SEHF', 'excel', 'xlsx', 3, 2, 4, 5, 6, 7, False, 'ddN-MMMM-yyyy', '', '', '', '', '', '', ''
),
(
    (SELECT id FROM mutual_fund WHERE name = 'SBI Infrastructure Fund'),
    'https://www.sbimf.com/docs/default-source/scheme-portfolios/all-schemes-monthly-portfolio---as-on-${DATE_MAPPER_1}.xlsx',
    False, 'SIF', 'excel', 'xlsx', 3, 2, 4, 5, 6, 7, False, 'ddN-MMMM-yyyy', '', '', '', '', '', '', ''
),
(
    (SELECT id FROM mutual_fund WHERE name = 'SBI Banking & Financial Services Fund'),
    'https://www.sbimf.com/docs/default-source/scheme-portfolios/all-schemes-monthly-portfolio---as-on-${DATE_MAPPER_1}.xlsx',
    False, 'SBFS', 'excel', 'xlsx', 3, 2, 4, 5, 6, 7, False, 'ddN-MMMM-yyyy', '', '', '', '', '', '', ''
),
(
    (SELECT id FROM mutual_fund WHERE name = 'SBI Consumption Opportunities Fund'),
    'https://www.sbimf.com/docs/default-source/scheme-portfolios/all-schemes-monthly-portfolio---as-on-${DATE_MAPPER_1}.xlsx',
    False, 'SCOF', 'excel', 'xlsx', 3, 2, 4, 5, 6, 7, False, 'ddN-MMMM-yyyy', '', '', '', '', '', '', ''
);