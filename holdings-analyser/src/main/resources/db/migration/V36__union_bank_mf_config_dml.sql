INSERT INTO mutual_fund_config (
    mutual_fund_id, download_url, is_zipped, sheet_name, content_type, extension,
    isin_col_mapper, stock_col_mapper, industry_col_mapper, quantity_col_mapper,
    market_value_col_mapper, net_asset_perc_col_mapper, is_pick_by_system,
    date_mapper_1, date_mapper_2, date_mapper_3, date_mapper_4,
    date_mapper_1_config, date_mapper_2_config, date_mapper_3_config, date_mapper_4_config
)
VALUES
(
    (SELECT id FROM mutual_fund WHERE name = 'Union Small Cap Fund'),
    'https://unionmf.com/docs/default-source/funddetail-downloads/fund-portfolio/${DATE_MAPPER_1}/monthly-portfolio-report-union-small-cap-fund-${DATE_MAPPER_2}.xlsx',
    false, '', 'excel', 'xlsx',
    2, 3, 4, 5,6,7 , false,
    'MMMM-yyyy', 'dd-MM-yyyy', '', '',
    '', '', '', ''
),
(
    (SELECT id FROM mutual_fund WHERE name = 'Union Flexi Cap Fund'),
    'https://unionmf.com/docs/default-source/funddetail-downloads/fund-portfolio/${DATE_MAPPER_1}/monthly-portfolio-report-union-flexi-cap-fund-${DATE_MAPPER_2}.xlsx',
    false, '', 'excel', 'xlsx',
    2, 3, 4, 5,6,7 , false,
    'MMMM-yyyy', 'dd-MM-yyyy', '', '',
    '', '', '', ''
),
(
    (SELECT id FROM mutual_fund WHERE name = 'Union Focused Fund'),
    'https://unionmf.com/docs/default-source/funddetail-downloads/fund-portfolio/${DATE_MAPPER_1}/monthly-portfolio-report-union-focused-fund-${DATE_MAPPER_2}.xlsx',
    false, '', 'excel', 'xlsx',
    2, 3, 4, 5,6,7 , false,
    'MMMM-yyyy', 'dd-MM-yyyy', '', '',
    '', '', '', ''
),
(
    (SELECT id FROM mutual_fund WHERE name = 'Union Value Fund'),
    'https://unionmf.com/docs/default-source/funddetail-downloads/fund-portfolio/${DATE_MAPPER_1}/monthly-portfolio-report-union-value-fund-${DATE_MAPPER_2}.xlsx',
    false, '', 'excel', 'xlsx',
    2, 3, 4, 5,6,7 , false,
    'MMMM-yyyy', 'dd-MM-yyyy', '', '',
    '', '', '', ''
),
(
    (SELECT id FROM mutual_fund WHERE name = 'Union ELSS Tax Saver Fund'),
    'https://unionmf.com/docs/default-source/funddetail-downloads/fund-portfolio/${DATE_MAPPER_1}/monthly-portfolio-report-union-elss-tax-saver-fund-${DATE_MAPPER_2}.xlsx',
    false, '', 'excel', 'xlsx',
    2, 3, 4, 5,6,7 , false,
    'MMMM-yyyy', 'dd-MM-yyyy', '', '',
    '', '', '', ''
),
(
    (SELECT id FROM mutual_fund WHERE name = 'Union Largecap Fund'),
    'https://unionmf.com/docs/default-source/funddetail-downloads/fund-portfolio/${DATE_MAPPER_1}/monthly-portfolio-report-union-largecap-fund-${DATE_MAPPER_2}.xlsx',
    false, '', 'excel', 'xlsx',
    2, 3, 4, 5,6,7 , false,
    'MMMM-yyyy', 'dd-MM-yyyy', '', '',
    '', '', '', ''
),
(
    (SELECT id FROM mutual_fund WHERE name = 'Union Midcap Fund'),
    'https://unionmf.com/docs/default-source/funddetail-downloads/fund-portfolio/${DATE_MAPPER_1}/monthly-portfolio-report-union-midcap-fund-${DATE_MAPPER_2}.xlsx',
    false, '', 'excel', 'xlsx',
    2, 3, 4, 5,6,7 , false,
    'MMMM-yyyy', 'dd-MM-yyyy', '', '',
    '', '', '', ''
),
(
    (SELECT id FROM mutual_fund WHERE name = 'Union Large and Midcap Fund'),
    'https://unionmf.com/docs/default-source/funddetail-downloads/fund-portfolio/${DATE_MAPPER_1}/monthly-portfolio-report-union-large-and-midcap-fund-${DATE_MAPPER_2}.xlsx',
    false, '', 'excel', 'xlsx',
    2, 3, 4, 5,6,7 , false,
    'MMMM-yyyy', 'dd-MM-yyyy', '', '',
    '', '', '', ''
);
