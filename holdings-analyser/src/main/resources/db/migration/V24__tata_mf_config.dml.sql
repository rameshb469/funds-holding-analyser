INSERT INTO mutual_fund_config (
    mutual_fund_id, download_url, is_zipped, sheet_name, content_type,
    extension, isin_col_mapper, stock_col_mapper, industry_col_mapper,
    date_mapper_1, date_mapper_2, date_mapper_1_config
) VALUES
    ((SELECT id FROM mutual_fund WHERE name = 'Tata Large Cap Fund'),
     'https://betacms.tatamutualfund.com/system/files/${DATE_MAPPER_1}/Monthly Portfolio as on ${DATE_MAPPER_2}.xlsx',
     FALSE, 'TTOFE', 'excel', 'xlsx', 4, 1, 3, 'yyyy-MM', 'ddN MMMM yyyy', 'M+1'),

    ((SELECT id FROM mutual_fund WHERE name = 'Tata Multicap Fund'),
     'https://betacms.tatamutualfund.com/system/files/${DATE_MAPPER_1}/Monthly Portfolio as on ${DATE_MAPPER_2}.xlsx',
     FALSE, 'TMULTICF', 'excel', 'xlsx', 4, 1, 3, 'yyyy-MM', 'ddN MMMM yyyy', 'M+1'),

    ((SELECT id FROM mutual_fund WHERE name = 'Tata Large & Mid Cap Fund'),
     'https://betacms.tatamutualfund.com/system/files/${DATE_MAPPER_1}/Monthly Portfolio as on ${DATE_MAPPER_2}.xlsx',
     FALSE, 'TEOF', 'excel', 'xlsx', 4, 1, 3, 'yyyy-MM', 'ddN MMMM yyyy', 'M+1'),

    ((SELECT id FROM mutual_fund WHERE name = 'Tata Mid Cap Growth Fund'),
     'https://betacms.tatamutualfund.com/system/files/${DATE_MAPPER_1}/Monthly Portfolio as on ${DATE_MAPPER_2}.xlsx',
     FALSE, 'TINR', 'excel', 'xlsx', 4, 1, 3, 'yyyy-MM', 'ddN MMMM yyyy', 'M+1'),

    ((SELECT id FROM mutual_fund WHERE name = 'Tata India Tax Savings Fund'),
     'https://betacms.tatamutualfund.com/system/files/${DATE_MAPPER_1}/Monthly Portfolio as on ${DATE_MAPPER_2}.xlsx',
     FALSE, 'TTSF96', 'excel', 'xlsx', 4, 1, 3, 'yyyy-MM', 'ddN MMMM yyyy', 'M+1'),

    ((SELECT id FROM mutual_fund WHERE name = 'Tata Small Cap Fund'),
     'https://betacms.tatamutualfund.com/system/files/${DATE_MAPPER_1}/Monthly Portfolio as on ${DATE_MAPPER_2}.xlsx',
     FALSE, 'TSCAPF', 'excel', 'xlsx', 4, 1, 3, 'yyyy-MM', 'ddN MMMM yyyy', 'M+1'),

    ((SELECT id FROM mutual_fund WHERE name = 'Tata Index Fund - Nifty Plan'),
     'https://betacms.tatamutualfund.com/system/files/${DATE_MAPPER_1}/Monthly Portfolio as on ${DATE_MAPPER_2}.xlsx',
     FALSE, 'TIFNA', 'excel', 'xlsx', 4, 1, 3, 'yyyy-MM', 'ddN MMMM yyyy', 'M+1'),

    ((SELECT id FROM mutual_fund WHERE name = 'Tata Index Fund - Sensex Plan'),
     'https://betacms.tatamutualfund.com/system/files/${DATE_MAPPER_1}/Monthly Portfolio as on ${DATE_MAPPER_2}.xlsx',
     FALSE, 'TIFSA', 'excel', 'xlsx', 4, 1, 3, 'yyyy-MM', 'ddN MMMM yyyy', 'M+1'),

    ((SELECT id FROM mutual_fund WHERE name = 'Tata Banking & Financial Services Fund'),
     'https://betacms.tatamutualfund.com/system/files/${DATE_MAPPER_1}/Monthly Portfolio as on ${DATE_MAPPER_2}.xlsx',
     FALSE, 'TBFSF', 'excel', 'xlsx', 4, 1, 3, 'yyyy-MM', 'ddN MMMM yyyy', 'M+1'),

    ((SELECT id FROM mutual_fund WHERE name = 'Tata Digital India Fund'),
     'https://betacms.tatamutualfund.com/system/files/${DATE_MAPPER_1}/Monthly Portfolio as on ${DATE_MAPPER_2}.xlsx',
     FALSE, 'TDIF', 'excel', 'xlsx', 4, 1, 3, 'yyyy-MM', 'ddN MMMM yyyy', 'M+1'),

    ((SELECT id FROM mutual_fund WHERE name = 'Tata India Consumer Fund'),
     'https://betacms.tatamutualfund.com/system/files/${DATE_MAPPER_1}/Monthly Portfolio as on ${DATE_MAPPER_2}.xlsx',
     FALSE, 'TICF', 'excel', 'xlsx', 4, 1, 3, 'yyyy-MM', 'ddN MMMM yyyy', 'M+1'),

    ((SELECT id FROM mutual_fund WHERE name = 'Tata India Pharma & Healthcare Fund'),
     'https://betacms.tatamutualfund.com/system/files/${DATE_MAPPER_1}/Monthly Portfolio as on ${DATE_MAPPER_2}.xlsx',
     FALSE, 'TIPHF', 'excel', 'xlsx', 4, 1, 3, 'yyyy-MM', 'ddN MMMM yyyy', 'M+1'),

    ((SELECT id FROM mutual_fund WHERE name = 'Tata Resources & Energy Fund'),
     'https://betacms.tatamutualfund.com/system/files/${DATE_MAPPER_1}/Monthly Portfolio as on ${DATE_MAPPER_2}.xlsx',
     FALSE, 'TREF', 'excel', 'xlsx', 4, 1, 3, 'yyyy-MM', 'ddN MMMM yyyy', 'M+1'),

    ((SELECT id FROM mutual_fund WHERE name = 'Tata Infrastructure Fund'),
     'https://betacms.tatamutualfund.com/system/files/${DATE_MAPPER_1}/Monthly Portfolio as on ${DATE_MAPPER_2}.xlsx',
     FALSE, 'TISF', 'excel', 'xlsx', 4, 1, 3, 'yyyy-MM', 'ddN MMMM yyyy', 'M+1'),

    ((SELECT id FROM mutual_fund WHERE name = 'Tata Equity P/E Fund'),
     'https://betacms.tatamutualfund.com/system/files/${DATE_MAPPER_1}/Monthly Portfolio as on ${DATE_MAPPER_2}.xlsx',
     FALSE, 'TEQPEF', 'excel', 'xlsx', 4, 1, 3, 'yyyy-MM', 'ddN MMMM yyyy', 'M+1'),

    ((SELECT id FROM mutual_fund WHERE name = 'Tata Focused Equity Fund Direct Growth'),
     'https://betacms.tatamutualfund.com/system/files/${DATE_MAPPER_1}/Monthly Portfolio as on ${DATE_MAPPER_2}.xlsx',
     FALSE, 'TFEF', 'excel', 'xlsx', 4, 1, 3, 'yyyy-MM', 'ddN MMMM yyyy', 'M+1');
