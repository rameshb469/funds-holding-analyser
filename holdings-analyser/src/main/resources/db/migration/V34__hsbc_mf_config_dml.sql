INSERT INTO mutual_fund_config (
    mutual_fund_id, download_url, is_zipped, sheet_name, content_type, extension,
    isin_col_mapper, stock_col_mapper, industry_col_mapper, quantity_col_mapper,
    market_value_col_mapper, net_asset_perc_col_mapper, is_pick_by_system,
    date_mapper_1, date_mapper_2, date_mapper_3, date_mapper_4,
    date_mapper_1_config, date_mapper_2_config, date_mapper_3_config, date_mapper_4_config
)
VALUES
(
    (SELECT id FROM mutual_fund WHERE name = 'HSBC Large Cap Fund'),
    'https://www.assetmanagement.hsbc.co.in/-/media/files/attachments/india/mutual-funds/portfolios/document-${DATE_MAPPER_1}/hsbc-large-cap-fund-${DATE_MAPPER_2}.xlsx',
    false, '', 'excel', 'xlsx',
    1,0,2,3,4,5, false,
    'ddMMyyyy', 'dd-MMMM-yyyy', '', '',
    '', '', '', ''
),
(
    (SELECT id FROM mutual_fund WHERE name = 'HSBC Large & Mid Cap Fund'),
    'https://www.assetmanagement.hsbc.co.in/-/media/files/attachments/india/mutual-funds/portfolios/document-${DATE_MAPPER_1}/hsbc-large-mid-cap-fund-${DATE_MAPPER_2}.xlsx',
    false, '', 'excel', 'xlsx',
    1,0,2,3,4,5, false,
    'ddMMyyyy', 'dd-MMMM-yyyy', '', '',
    '', '', '', ''
),
(
    (SELECT id FROM mutual_fund WHERE name = 'HSBC Multi Cap Fund'),
    'https://www.assetmanagement.hsbc.co.in/-/media/files/attachments/india/mutual-funds/portfolios/document-${DATE_MAPPER_1}/hsbc-multi-cap-fund-${DATE_MAPPER_2}.xlsx',
    false, '', 'excel', 'xlsx',
    1,0,2,3,4,5, false,
    'ddMMyyyy', 'dd-MMMM-yyyy', '', '',
    '', '', '', ''
),
(
    (SELECT id FROM mutual_fund WHERE name = 'HSBC Nifty 50 Index Fund'),
    'https://www.assetmanagement.hsbc.co.in/-/media/files/attachments/india/mutual-funds/portfolios/document-${DATE_MAPPER_1}/hsbc-nifty-50-index-fund-${DATE_MAPPER_2}.xlsx',
    false, '', 'excel', 'xlsx',
    1,0,2,3,4,5, false,
    'ddMMyyyy', 'dd-MMMM-yyyy', '', '',
    '', '', '', ''
),
(
    (SELECT id FROM mutual_fund WHERE name = 'HSBC Consumption Fund'),
    'https://www.assetmanagement.hsbc.co.in/-/media/files/attachments/india/mutual-funds/portfolios/document-${DATE_MAPPER_1}/hsbc-consumption-fund-${DATE_MAPPER_2}.xlsx',
    false, '', 'excel', 'xlsx',
    1,0,2,3,4,5, false,
    'ddMMyyyy', 'dd-MMMM-yyyy', '', '',
    '', '', '', ''
),
(
    (SELECT id FROM mutual_fund WHERE name = 'HSBC Mid Cap Fund'),
    'https://www.assetmanagement.hsbc.co.in/-/media/files/attachments/india/mutual-funds/portfolios/document-${DATE_MAPPER_1}/hsbc-mid-cap-fund-${DATE_MAPPER_2}.xlsx',
    false, '', 'excel', 'xlsx',
    1,0,2,3,4,5, false,
    'ddMMyyyy', 'dd-MMMM-yyyy', '', '',
    '', '', '', ''
),
(
    (SELECT id FROM mutual_fund WHERE name = 'HSBC Small Cap Fund'),
    'https://www.assetmanagement.hsbc.co.in/-/media/files/attachments/india/mutual-funds/portfolios/document-${DATE_MAPPER_1}/hsbc-small-cap-fund-${DATE_MAPPER_2}.xlsx',
    false, '', 'excel', 'xlsx',
    1,0,2,3,4,5, false,
    'ddMMyyyy', 'dd-MMMM-yyyy', '', '',
    '', '', '', ''
),
(
    (SELECT id FROM mutual_fund WHERE name = 'HSBC Flexi Cap Fund'),
    'https://www.assetmanagement.hsbc.co.in/-/media/files/attachments/india/mutual-funds/portfolios/document-${DATE_MAPPER_1}/hsbc-flexi-cap-fund-${DATE_MAPPER_2}.xlsx',
    false, '', 'excel', 'xlsx',
    1,0,2,3,4,5, false,
    'ddMMyyyy', 'dd-MMMM-yyyy', '', '',
    '', '', '', ''
),
(
    (SELECT id FROM mutual_fund WHERE name = 'HSBC Focused Equity Fund'),
    'https://www.assetmanagement.hsbc.co.in/-/media/files/attachments/india/mutual-funds/portfolios/document-${DATE_MAPPER_1}/hsbc-focused-equity-fund-${DATE_MAPPER_2}.xlsx',
    false, '', 'excel', 'xlsx',
    1,0,2,3,4,5, false,
    'ddMMyyyy', 'dd-MMMM-yyyy', '', '',
    '', '', '', ''
),
(
    (SELECT id FROM mutual_fund WHERE name = 'HSBC ELSS Tax Saver Fund'),
    'https://www.assetmanagement.hsbc.co.in/-/media/files/attachments/india/mutual-funds/portfolios/document-${DATE_MAPPER_1}/hsbc-elss-tax-saver-fund-${DATE_MAPPER_2}.xlsx',
    false, '', 'excel', 'xlsx',
    1,0,2,3,4,5, false,
    'ddMMyyyy', 'dd-MMMM-yyyy', '', '',
    '', '', '', ''
),
(
    (SELECT id FROM mutual_fund WHERE name = 'HSBC Infrastructure Fund'),
    'https://www.assetmanagement.hsbc.co.in/-/media/files/attachments/india/mutual-funds/portfolios/document-${DATE_MAPPER_1}/hsbc-infrastructure-fund-${DATE_MAPPER_2}.xlsx',
    false, '', 'excel', 'xlsx',
    1,0,2,3,4,5, false,
    'ddMMyyyy', 'dd-MMMM-yyyy', '', '',
    '', '', '', ''
),
(
    (SELECT id FROM mutual_fund WHERE name = 'HSBC Value Fund'),
    'https://www.assetmanagement.hsbc.co.in/-/media/files/attachments/india/mutual-funds/portfolios/document-${DATE_MAPPER_1}/hsbc-value-fund-${DATE_MAPPER_2}.xlsx',
    false, '', 'excel', 'xlsx',
    1,0,2,3,4,5, false,
    'ddMMyyyy', 'dd-MMMM-yyyy', '', '',
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
    (SELECT id FROM mutual_fund WHERE name = 'HSBC Large Cap Fund'),
    'https://www.assetmanagement.hsbc.co.in/en/mutual-funds/investor-resources/-/media/files/attachments/india/mutual-funds/portfolios/document-${DATE_MAPPER_1}/hsbc-large-cap-fund-${DATE_MAPPER_2}.xlsx',
    false, '', 'excel', 'xlsx',
    1, 0, 2, 3, 4, 5, false,
    'ddMMyyyy', 'dd-MMMM-yyyy', '', '',
    '', '', '', ''
),
(
    (SELECT id FROM mutual_fund WHERE name = 'HSBC Large & Mid Cap Fund'),
    'https://www.assetmanagement.hsbc.co.in/en/mutual-funds/investor-resources/-/media/files/attachments/india/mutual-funds/portfolios/document-${DATE_MAPPER_1}/hsbc-large-mid-cap-fund-${DATE_MAPPER_2}.xlsx',
    false, '', 'excel', 'xlsx',
    1, 0, 2, 3, 4, 5, false,
    'ddMMyyyy', 'dd-MMMM-yyyy', '', '',
    '', '', '', ''
),
(
    (SELECT id FROM mutual_fund WHERE name = 'HSBC Multi Cap Fund'),
    'https://www.assetmanagement.hsbc.co.in/en/mutual-funds/investor-resources/-/media/files/attachments/india/mutual-funds/portfolios/document-${DATE_MAPPER_1}/hsbc-multi-cap-fund-${DATE_MAPPER_2}.xlsx',
    false, '', 'excel', 'xlsx',
    1, 0, 2, 3, 4, 5, false,
    'ddMMyyyy', 'dd-MMMM-yyyy', '', '',
    '', '', '', ''
),
(
    (SELECT id FROM mutual_fund WHERE name = 'HSBC Nifty 50 Index Fund'),
    'https://www.assetmanagement.hsbc.co.in/en/mutual-funds/investor-resources/-/media/files/attachments/india/mutual-funds/portfolios/document-${DATE_MAPPER_1}/hsbc-nifty-50-index-fund-${DATE_MAPPER_2}.xlsx',
    false, '', 'excel', 'xlsx',
    1, 0, 2, 3, 4, 5, false,
    'ddMMyyyy', 'dd-MMMM-yyyy', '', '',
    '', '', '', ''
),
(
    (SELECT id FROM mutual_fund WHERE name = 'HSBC Consumption Fund'),
    'https://www.assetmanagement.hsbc.co.in/en/mutual-funds/investor-resources/-/media/files/attachments/india/mutual-funds/portfolios/document-${DATE_MAPPER_1}/hsbc-consumption-fund-${DATE_MAPPER_2}.xlsx',
    false, '', 'excel', 'xlsx',
    1, 0, 2, 3, 4, 5, false,
    'ddMMyyyy', 'dd-MMMM-yyyy', '', '',
    '', '', '', ''
),
(
    (SELECT id FROM mutual_fund WHERE name = 'HSBC Mid Cap Fund'),
    'https://www.assetmanagement.hsbc.co.in/en/mutual-funds/investor-resources/-/media/files/attachments/india/mutual-funds/portfolios/document-${DATE_MAPPER_1}/hsbc-mid-cap-fund-${DATE_MAPPER_2}.xlsx',
    false, '', 'excel', 'xlsx',
    1, 0, 2, 3, 4, 5, false,
    'ddMMyyyy', 'dd-MMMM-yyyy', '', '',
    '', '', '', ''
),
(
    (SELECT id FROM mutual_fund WHERE name = 'HSBC Small Cap Fund'),
    'https://www.assetmanagement.hsbc.co.in/en/mutual-funds/investor-resources/-/media/files/attachments/india/mutual-funds/portfolios/document-${DATE_MAPPER_1}/hsbc-small-cap-fund-${DATE_MAPPER_2}.xlsx',
    false, '', 'excel', 'xlsx',
    1, 0, 2, 3, 4, 5, false,
    'ddMMyyyy', 'dd-MMMM-yyyy', '', '',
    '', '', '', ''
),
(
    (SELECT id FROM mutual_fund WHERE name = 'HSBC Flexi Cap Fund'),
    'https://www.assetmanagement.hsbc.co.in/en/mutual-funds/investor-resources/-/media/files/attachments/india/mutual-funds/portfolios/document-${DATE_MAPPER_1}/hsbc-flexi-cap-fund-${DATE_MAPPER_2}.xlsx',
    false, '', 'excel', 'xlsx',
    1, 0, 2, 3, 4, 5, false,
    'ddMMyyyy', 'dd-MMMM-yyyy', '', '',
    '', '', '', ''
),
(
    (SELECT id FROM mutual_fund WHERE name = 'HSBC Focused Equity Fund'),
    'https://www.assetmanagement.hsbc.co.in/en/mutual-funds/investor-resources/-/media/files/attachments/india/mutual-funds/portfolios/document-${DATE_MAPPER_1}/hsbc-focused-equity-fund-${DATE_MAPPER_2}.xlsx',
    false, '', 'excel', 'xlsx',
    1, 0, 2, 3, 4, 5, false,
    'ddMMyyyy', 'dd-MMMM-yyyy', '', '',
    '', '', '', ''
),
(
    (SELECT id FROM mutual_fund WHERE name = 'HSBC ELSS Tax Saver Fund'),
    'https://www.assetmanagement.hsbc.co.in/en/mutual-funds/investor-resources/-/media/files/attachments/india/mutual-funds/portfolios/document-${DATE_MAPPER_1}/hsbc-elss-tax-saver-fund-${DATE_MAPPER_2}.xlsx',
    false, '', 'excel', 'xlsx',
    1, 0, 2, 3, 4, 5, false,
    'ddMMyyyy', 'dd-MMMM-yyyy', '', '',
    '', '', '', ''
),
(
    (SELECT id FROM mutual_fund WHERE name = 'HSBC Infrastructure Fund'),
    'https://www.assetmanagement.hsbc.co.in/en/mutual-funds/investor-resources/-/media/files/attachments/india/mutual-funds/portfolios/document-${DATE_MAPPER_1}/hsbc-infrastructure-fund-${DATE_MAPPER_2}.xlsx',
    false, '', 'excel', 'xlsx',
    1, 0, 2, 3, 4, 5, false,
    'ddMMyyyy', 'dd-MMMM-yyyy', '', '',
    '', '', '', ''
),
(
    (SELECT id FROM mutual_fund WHERE name = 'HSBC Value Fund'),
    'https://www.assetmanagement.hsbc.co.in/en/mutual-funds/investor-resources/-/media/files/attachments/india/mutual-funds/portfolios/document-${DATE_MAPPER_1}/hsbc-value-fund-${DATE_MAPPER_2}.xlsx',
    false, '', 'excel', 'xlsx',
    1, 0, 2, 3, 4, 5, false,
    'ddMMyyyy', 'dd-MMMM-yyyy', '', '',
    '', '', '', ''
);

