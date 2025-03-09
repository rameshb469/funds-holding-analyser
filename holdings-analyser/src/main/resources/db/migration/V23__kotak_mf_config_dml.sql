INSERT INTO mutual_fund_config (
    mutual_fund_id, download_url, is_zipped, sheet_name, content_type, extension,
    isin_col_mapper, stock_col_mapper, industry_col_mapper, quantity_col_mapper,
    market_value_col_mapper, net_asset_perc_col_mapper, is_pick_by_system,
    date_mapper_1, date_mapper_2
) VALUES
((SELECT id FROM mutual_fund WHERE name = 'Kotak Bluechip Fund'),
 'https://vlbapiprodtest.kotakmf.com/kotakapi/portfolio/downloadfile?path=K30/${DATE_MAPPER_1}/${DATE_MAPPER_2}/Consolidated&filename=K30.xlsx',
 False, '', 'excel', 'xlsx', 3, 2, 4, 6, 7, 8, False, 'YYYY', 'MMMM'),

((SELECT id FROM mutual_fund WHERE name = 'Kotak Emerging Equity Fund'),
 'https://vlbapiprodtest.kotakmf.com/kotakapi/portfolio/downloadfile?path=EME/${DATE_MAPPER_1}/${DATE_MAPPER_2}/Consolidated&filename=EME.xlsx',
 False, '', 'excel', 'xlsx', 3, 2, 4, 6, 7, 8, False, 'YYYY', 'MMMM'),

((SELECT id FROM mutual_fund WHERE name = 'Kotak Small Cap Fund'),
 'https://vlbapiprodtest.kotakmf.com/kotakapi/portfolio/downloadfile?path=MID/${DATE_MAPPER_1}/${DATE_MAPPER_2}/Consolidated&filename=MID.xlsx',
 False, '', 'excel', 'xlsx', 3, 2, 4, 6, 7, 8, False, 'YYYY', 'MMMM'),

((SELECT id FROM mutual_fund WHERE name = 'Kotak Flexicap Fund'),
 'https://vlbapiprodtest.kotakmf.com/kotakapi/portfolio/downloadfile?path=SEF/${DATE_MAPPER_1}/${DATE_MAPPER_2}/Consolidated&filename=SEF.xlsx',
 False, '', 'excel', 'xlsx', 3, 2, 4, 6, 7, 8, False, 'YYYY', 'MMMM'),

((SELECT id FROM mutual_fund WHERE name = 'Kotak Focused Equity Fund'),
 'https://vlbapiprodtest.kotakmf.com/kotakapi/portfolio/downloadfile?path=KFE/${DATE_MAPPER_1}/${DATE_MAPPER_2}/Consolidated&filename=KFE.xlsx',
 False, '', 'excel', 'xlsx', 3, 2, 4, 6, 7, 8, False, 'YYYY', 'MMMM'),

((SELECT id FROM mutual_fund WHERE name = 'Kotak Tax Saver Fund'),
 'https://vlbapiprodtest.kotakmf.com/kotakapi/portfolio/downloadfile?path=ELS/${DATE_MAPPER_1}/${DATE_MAPPER_2}/Consolidated&filename=ELS.xlsx',
 False, '', 'excel', 'xlsx', 3, 2, 4, 6, 7, 8, False, 'YYYY', 'MMMM'),

((SELECT id FROM mutual_fund WHERE name = 'Kotak Equity Opportunities Fund'),
 'https://vlbapiprodtest.kotakmf.com/kotakapi/portfolio/downloadfile?path=KOP/${DATE_MAPPER_1}/${DATE_MAPPER_2}/Consolidated&filename=KOP.xlsx',
 False, '', 'excel', 'xlsx', 3, 2, 4, 6, 7, 8, False, 'YYYY', 'MMMM'),

((SELECT id FROM mutual_fund WHERE name = 'Kotak Equity Hybrid Fund'),
 'https://vlbapiprodtest.kotakmf.com/kotakapi/portfolio/downloadfile?path=BAL/${DATE_MAPPER_1}/${DATE_MAPPER_2}/Consolidated&filename=BAL.xlsx',
 False, '', 'excel', 'xlsx', 3, 2, 4, 6, 7, 8, False, 'YYYY', 'MMMM'),

((SELECT id FROM mutual_fund WHERE name = 'Kotak Equity Value Fund'),
 'https://vlbapiprodtest.kotakmf.com/kotakapi/portfolio/downloadfile?path=NVF/${DATE_MAPPER_1}/${DATE_MAPPER_2}/Consolidated&filename=NVF.xlsx',
 False, '', 'excel', 'xlsx', 3, 2, 4, 6, 7, 8, False, 'YYYY', 'MMMM'),

((SELECT id FROM mutual_fund WHERE name = 'Kotak Infrastructure & Economic Reform Fund'),
 'https://vlbapiprodtest.kotakmf.com/kotakapi/portfolio/downloadfile?path=KIE/${DATE_MAPPER_1}/${DATE_MAPPER_2}/Consolidated&filename=KIE.xlsx',
 False, '', 'excel', 'xlsx', 3, 2, 4, 6, 7, 8, False, 'YYYY', 'MMMM'),

((SELECT id FROM mutual_fund WHERE name = 'Kotak Banking and Financial Services Fund'),
 'https://vlbapiprodtest.kotakmf.com/kotakapi/portfolio/downloadfile?path=BFS/${DATE_MAPPER_1}/${DATE_MAPPER_2}/Consolidated&filename=BFS.xlsx',
 False, '', 'excel', 'xlsx', 3, 2, 4, 6, 7, 8, False, 'YYYY', 'MMMM');
