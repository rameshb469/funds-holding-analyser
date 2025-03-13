INSERT INTO mutual_fund_config (
    mutual_fund_id, download_url, is_zipped, sheet_name, content_type, extension,
    isin_col_mapper, stock_col_mapper, industry_col_mapper, quantity_col_mapper,
    market_value_col_mapper, net_asset_perc_col_mapper, is_pick_by_system,
    date_mapper_1, date_mapper_2, date_mapper_3, date_mapper_4,
    date_mapper_1_config, date_mapper_2_config, date_mapper_3_config, date_mapper_4_config
)
VALUES
(
    (SELECT id FROM mutual_fund WHERE name = 'Mirae Asset Large Cap Fund'),
    'https://www.miraeassetmf.co.in/docs/default-source/portfolios/miiof_${DATE_MAPPER_1}.xlsx',
    false, '', 'excel', 'xlsx',
    2, 1, 3, 4, 5, 6, false,
    'MMMyyyy', '', '', '',
    '', '', '', ''
),
(
    (SELECT id FROM mutual_fund WHERE name = 'Mirae Asset ELSS Tax Saver Fund'),
    'https://www.miraeassetmf.co.in/docs/default-source/portfolios/matsf_${DATE_MAPPER_1}.xlsx',
    false, '', 'excel', 'xlsx',
    2, 1, 3, 4, 5, 6, false,
    'MMMyyyy', '', '', '',
    '', '', '', ''
),
(
    (SELECT id FROM mutual_fund WHERE name = 'Mirae Asset Large and Midcap Fund'),
    'https://www.miraeassetmf.co.in/docs/default-source/portfolios/maebf_${DATE_MAPPER_1}.xlsx',
    false, '', 'excel', 'xlsx',
    2, 1, 3, 4, 5, 6, false,
    'MMMyyyy', '', '', '',
    '', '', '', ''
),
(
    (SELECT id FROM mutual_fund WHERE name = 'Mirae Asset Great Consumer Fund'),
    'https://www.miraeassetmf.co.in/docs/default-source/portfolios/miicf_${DATE_MAPPER_1}.xlsx',
    false, '', 'excel', 'xlsx',
    2, 1, 3, 4, 5, 6, false,
    'MMMyyyy', '', '', '',
    '', '', '', ''
),
(
    (SELECT id FROM mutual_fund WHERE name = 'Mirae Asset Healthcare Fund'),
    'https://www.miraeassetmf.co.in/docs/default-source/portfolios/mahcf_${DATE_MAPPER_1}.xlsx',
    false, '', 'excel', 'xlsx',
    2, 1, 3, 4, 5, 6, false,
    'MMMyyyy', '', '', '',
    '', '', '', ''
),
(
    (SELECT id FROM mutual_fund WHERE name = 'Mirae Asset Midcap Fund'),
    'https://www.miraeassetmf.co.in/docs/default-source/portfolios/mamcf_${DATE_MAPPER_1}.xlsx',
    false, '', 'excel', 'xlsx',
    2, 1, 3, 4, 5, 6, false,
    'MMMyyyy', '', '', '',
    '', '', '', ''
),
(
    (SELECT id FROM mutual_fund WHERE name = 'Mirae Asset Banking and Financial Services Fund'),
    'https://www.miraeassetmf.co.in/docs/default-source/portfolios/mabff_${DATE_MAPPER_1}.xlsx',
    false, '', 'excel', 'xlsx',
    2, 1, 3, 4, 5, 6, false,
    'MMMyyyy', '', '', '',
    '', '', '', ''
),
(
    (SELECT id FROM mutual_fund WHERE name = 'Mirae Asset Flexi Cap Fund'),
    'https://www.miraeassetmf.co.in/docs/default-source/portfolios/mafcf_${DATE_MAPPER_1}.xlsx',
    false, '', 'excel', 'xlsx',
    2, 1, 3, 4, 5, 6, false,
    'MMMyyyy', '', '', '',
    '', '', '', ''
),
(
    (SELECT id FROM mutual_fund WHERE name = 'Mirae Asset Multicap Fund'),
    'https://www.miraeassetmf.co.in/docs/default-source/portfolios/mampf_${DATE_MAPPER_1}.xlsx',
    false, '', 'excel', 'xlsx',
    2, 1, 3, 4, 5, 6, false,
    'MMMyyyy', '', '', '',
    '', '', '', ''
),
(
    (SELECT id FROM mutual_fund WHERE name = 'Mirae Asset Smallcap Fund'),
    'https://www.miraeassetmf.co.in/docs/default-source/portfolios/mascf_${DATE_MAPPER_1}.xlsx',
    false, '', 'excel', 'xlsx',
    2, 1, 3, 4, 5, 6, false,
    'MMMyyyy', '', '', '',
    '', '', '', ''
),
(
    (SELECT id FROM mutual_fund WHERE name = 'Mirae Asset Focused Fund'),
    'https://www.miraeassetmf.co.in/docs/default-source/portfolios/maff_${DATE_MAPPER_1}.xlsx',
    false, '', 'excel', 'xlsx',
    2, 1, 3, 4, 5, 6, false,
    'MMMyyyy', '', '', '',
    '', '', '', ''
);
