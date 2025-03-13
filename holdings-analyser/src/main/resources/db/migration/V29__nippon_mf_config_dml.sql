INSERT INTO mutual_fund_config (
    mutual_fund_id, download_url, is_zipped, sheet_name, content_type, extension,
    isin_col_mapper, stock_col_mapper, industry_col_mapper, quantity_col_mapper,
    market_value_col_mapper, net_asset_perc_col_mapper, is_pick_by_system,
    date_mapper_1, date_mapper_3, date_mapper_4,
    date_mapper_1_config, date_mapper_3_config, date_mapper_4_config
)
VALUES
(
    (SELECT id FROM mutual_fund WHERE name = 'Nippon India Growth Fund'),
    'https://mf.nipponindiaim.com/InvestorServices/FactsheetsDocuments/NIMF-MONTHLY-PORTFOLIO-${DATE_MAPPER_1}.xlsx',
    false, 'Nippon Growth Fund', 'excel', 'xls',
    1, 2, 3, 4, 5, 6, false,
    'dd-MMM-yy', '', '',
    '', '', ''
),
(
    (SELECT id FROM mutual_fund WHERE name = 'Nippon India Vision Fund'),
    'https://mf.nipponindiaim.com/InvestorServices/FactsheetsDocuments/NIMF-MONTHLY-PORTFOLIO-${DATE_MAPPER_1}.xlsx',
    false, 'Nippon Vision Fund', 'excel', 'xls',
    1, 2, 3, 4, 5, 6, false,
    'dd-MMM-yy', '', '',
    '', '', ''
),
(
    (SELECT id FROM mutual_fund WHERE name = 'Nippon India Large Cap Fund'),
    'https://mf.nipponindiaim.com/InvestorServices/FactsheetsDocuments/NIMF-MONTHLY-PORTFOLIO-${DATE_MAPPER_1}.xlsx',
    false, 'Nippon Large Cap Fund', 'excel', 'xls',
    1, 2, 3, 4, 5, 6, false,
    'dd-MMM-yy', '', '',
    '', '', ''
),
(
    (SELECT id FROM mutual_fund WHERE name = 'Nippon India Index Fund - Nifty 50 Plan'),
    'https://mf.nipponindiaim.com/InvestorServices/FactsheetsDocuments/NIMF-MONTHLY-PORTFOLIO-${DATE_MAPPER_1}.xlsx',
    false, 'Nippon Nifty 50 Plan', 'excel', 'xls',
    1, 2, 3, 4, 5, 6, false,
    'dd-MMM-yy', '', '',
    '', '', ''
),
(
    (SELECT id FROM mutual_fund WHERE name = 'Nippon India Index Fund - BSE Sensex Plan'),
    'https://mf.nipponindiaim.com/InvestorServices/FactsheetsDocuments/NIMF-MONTHLY-PORTFOLIO-${DATE_MAPPER_1}.xlsx',
    false, 'Nippon BSE Sensex Plan', 'excel', 'xls',
    1, 2, 3, 4, 5, 6, false,
    'dd-MMM-yy', '', '',
    '', '', ''
),
(
    (SELECT id FROM mutual_fund WHERE name = 'Nippon India Focused Equity Fund'),
    'https://mf.nipponindiaim.com/InvestorServices/FactsheetsDocuments/NIMF-MONTHLY-PORTFOLIO-${DATE_MAPPER_1}.xlsx',
    false, 'Nippon Focused Equity Fund', 'excel', 'xls',
    1, 2, 3, 4, 5, 6, false,
    'dd-MMM-yy', '', '',
    '', '', ''
),
(
    (SELECT id FROM mutual_fund WHERE name = 'Nippon India Small Cap Fund'),
    'https://mf.nipponindiaim.com/InvestorServices/FactsheetsDocuments/NIMF-MONTHLY-PORTFOLIO-${DATE_MAPPER_1}.xlsx',
    false, 'Nippon Small Cap Fund', 'excel', 'xls',
    1, 2, 3, 4, 5, 6, false,
    'dd-MMM-yy', '', '',
    '', '', ''
),
(
    (SELECT id FROM mutual_fund WHERE name = 'Nippon India Banking & Financial Services Fund'),
    'https://mf.nipponindiaim.com/InvestorServices/FactsheetsDocuments/NIMF-MONTHLY-PORTFOLIO-${DATE_MAPPER_1}.xlsx',
    false, 'Nippon Banking & Financial Services Fund', 'excel', 'xls',
    1, 2, 3, 4, 5, 6, false,
    'dd-MMM-yy', '', '',
    '', '', ''
),
(
    (SELECT id FROM mutual_fund WHERE name = 'Nippon India Power & Infra Fund'),
    'https://mf.nipponindiaim.com/InvestorServices/FactsheetsDocuments/NIMF-MONTHLY-PORTFOLIO-${DATE_MAPPER_1}.xlsx',
    false, 'Nippon Power & Infra Fund', 'excel', 'xls',
    1, 2, 3, 4, 5, 6, false,
    'dd-MMM-yy', '', '',
    '', '', ''
),
(
    (SELECT id FROM mutual_fund WHERE name = 'Nippon India Hybrid Bond Fund'),
    'https://mf.nipponindiaim.com/InvestorServices/FactsheetsDocuments/NIMF-MONTHLY-PORTFOLIO-${DATE_MAPPER_1}.xlsx',
    false, 'Nippon Hybrid Bond Fund', 'excel', 'xls',
    1, 2, 3, 4, 5, 6, false,
    'dd-MMM-yy', '', '',
    '', '', ''
),
(
    (SELECT id FROM mutual_fund WHERE name = 'Nippon India Pharma Fund'),
    'https://mf.nipponindiaim.com/InvestorServices/FactsheetsDocuments/NIMF-MONTHLY-PORTFOLIO-${DATE_MAPPER_1}.xlsx',
    false, 'Nippon Pharma Fund', 'excel', 'xls',
    1, 2, 3, 4, 5, 6, false,
    'dd-MMM-yy', '', '',
    '', '', ''
),
(
    (SELECT id FROM mutual_fund WHERE name = 'Nippon India Consumption Fund'),
    'https://mf.nipponindiaim.com/InvestorServices/FactsheetsDocuments/NIMF-MONTHLY-PORTFOLIO-${DATE_MAPPER_1}.xlsx',
    false, 'Nippon Consumption Fund', 'excel', 'xls',
    1, 2, 3, 4, 5, 6, false,
    'dd-MMM-yy', '', '',
    '', '', ''
),
(
    (SELECT id FROM mutual_fund WHERE name = 'Nippon India Multi Cap Fund'),
    'https://mf.nipponindiaim.com/InvestorServices/FactsheetsDocuments/NIMF-MONTHLY-PORTFOLIO-${DATE_MAPPER_1}.xlsx',
    false, 'Nippon Multi Cap Fund', 'excel', 'xls',
    1, 2, 3, 4, 5, 6, false,
    'dd-MMM-yy', '', '',
    '', '', ''
),
(
    (SELECT id FROM mutual_fund WHERE name = 'Nippon India Value Fund'),
    'https://mf.nipponindiaim.com/InvestorServices/FactsheetsDocuments/NIMF-MONTHLY-PORTFOLIO-${DATE_MAPPER_1}.xlsx',
    false, 'Nippon Value Fund', 'excel', 'xls',
    1, 2, 3, 4, 5, 6, false,
    'dd-MMM-yy', '', '',
    '', '', ''
),
(
    (SELECT id FROM mutual_fund WHERE name = 'Nippon India ELSS Tax Saver Fund'),
    'https://mf.nipponindiaim.com/InvestorServices/FactsheetsDocuments/NIMF-MONTHLY-PORTFOLIO-${DATE_MAPPER_1}.xlsx',
    false, 'Nippon ELSS Tax Saver Fund', 'excel', 'xls',
    1, 2, 3, 4, 5, 6, false,
    'dd-MMM-yy', '', '',
    '', '', ''
);
