INSERT INTO mutual_fund_config (
    mutual_fund_id, download_url, is_zipped, sheet_name, content_type, extension,
    isin_col_mapper, stock_col_mapper, industry_col_mapper, quantity_col_mapper,
    market_value_col_mapper, net_asset_perc_col_mapper, is_pick_by_system,
    date_mapper_1, date_mapper_2, date_mapper_3, date_mapper_4,
    date_mapper_1_config, date_mapper_2_config, date_mapper_3_config, date_mapper_4_config
) VALUES
    ((SELECT id FROM MUTUAL_FUND WHERE name = 'Quant Active Fund'),
    'https://quantmutual.com/Admin/disclouser/quant_Active_Fund_${DATE_MAPPER_1}_${DATE_MAPPER_2}.xlsx',
    FALSE, '', 'excel', 'xlsx', 1, 2, 4, 5, 6, 7, FALSE, 'MMM', 'YYYY', NULL, NULL, NULL, NULL, NULL, NULL),

    ((SELECT id FROM MUTUAL_FUND WHERE name = 'Quant Small Cap Fund'),
    'https://quantmutual.com/Admin/disclouser/quant_Small_Cap_Fund_${DATE_MAPPER_1}_${DATE_MAPPER_2}.xlsx',
    FALSE, '', 'excel', 'xlsx', 1, 2, 4, 5, 6, 7, FALSE, 'MMM', 'YYYY', NULL, NULL, NULL, NULL, NULL, NULL),

    ((SELECT id FROM MUTUAL_FUND WHERE name = 'Quant Mid Cap Fund'),
    'https://quantmutual.com/Admin/disclouser/quant_Mid_Cap_Fund_${DATE_MAPPER_1}_${DATE_MAPPER_2}.xlsx',
    FALSE, '', 'excel', 'xlsx', 1, 2, 4, 5, 6, 7, FALSE, 'MMM', 'YYYY', NULL, NULL, NULL, NULL, NULL, NULL),

    ((SELECT id FROM MUTUAL_FUND WHERE name = 'Quant Large Cap Fund'),
    'https://quantmutual.com/Admin/disclouser/quant_Large_Cap_Fund_${DATE_MAPPER_1}_${DATE_MAPPER_2}.xlsx',
    FALSE, '', 'excel', 'xlsx', 1, 2, 4, 5, 6, 7, FALSE, 'MMM', 'YYYY', NULL, NULL, NULL, NULL, NULL, NULL),

    ((SELECT id FROM MUTUAL_FUND WHERE name = 'Quant Multi Cap Fund'),
    'https://quantmutual.com/Admin/disclouser/quant_Multi_Cap_Fund_${DATE_MAPPER_1}_${DATE_MAPPER_2}.xlsx',
    FALSE, '', 'excel', 'xlsx', 1, 2, 4, 5, 6, 7, FALSE, 'MMM', 'YYYY', NULL, NULL, NULL, NULL, NULL, NULL),

    ((SELECT id FROM MUTUAL_FUND WHERE name = 'Quant Infrastructure Fund'),
    'https://quantmutual.com/Admin/disclouser/quant_Infrastructure_Fund_${DATE_MAPPER_1}_${DATE_MAPPER_2}.xlsx',
    FALSE, '', 'excel', 'xlsx', 1, 2, 4, 5, 6, 7, FALSE, 'MMM', 'YYYY', NULL, NULL, NULL, NULL, NULL, NULL),

    ((SELECT id FROM MUTUAL_FUND WHERE name = 'Quant Consumption Fund'),
    'https://quantmutual.com/Admin/disclouser/quant_Consumption_Fund_${DATE_MAPPER_1}_${DATE_MAPPER_2}.xlsx',
    FALSE, '', 'excel', 'xlsx', 1, 2, 4, 5, 6, 7, FALSE, 'MMM', 'YYYY', NULL, NULL, NULL, NULL, NULL, NULL),

    ((SELECT id FROM MUTUAL_FUND WHERE name = 'Quant Tax Plan'),
    'https://quantmutual.com/Admin/disclouser/quant_Tax_Plan_${DATE_MAPPER_1}_${DATE_MAPPER_2}.xlsx',
    FALSE, '', 'excel', 'xlsx', 1, 2, 4, 5, 6, 7, FALSE, 'MMM', 'YYYY', NULL, NULL, NULL, NULL, NULL, NULL),

    ((SELECT id FROM MUTUAL_FUND WHERE name = 'Quant Focused Fund'),
    'https://quantmutual.com/Admin/disclouser/quant_Focused_Fund_${DATE_MAPPER_1}_${DATE_MAPPER_2}.xlsx',
    FALSE, '', 'excel', 'xlsx', 1, 2, 4, 5, 6, 7, FALSE, 'MMM', 'YYYY', NULL, NULL, NULL, NULL, NULL, NULL),

    ((SELECT id FROM MUTUAL_FUND WHERE name = 'Quant Value Fund'),
    'https://quantmutual.com/Admin/disclouser/quant_Value_Fund_${DATE_MAPPER_1}_${DATE_MAPPER_2}.xlsx',
    FALSE, '', 'excel', 'xlsx', 1, 2, 4, 5, 6, 7, FALSE, 'MMM', 'YYYY', NULL, NULL, NULL, NULL, NULL, NULL),

    ((SELECT id FROM MUTUAL_FUND WHERE name = 'Quant Healthcare Fund'),
    'https://quantmutual.com/Admin/disclouser/quant_Healthcare_Fund_${DATE_MAPPER_1}_${DATE_MAPPER_2}.xlsx',
    FALSE, '', 'excel', 'xlsx', 1, 2, 4, 5, 6, 7, FALSE, 'MMM', 'YYYY', NULL, NULL, NULL, NULL, NULL, NULL),

    ((SELECT id FROM MUTUAL_FUND WHERE name = 'Quant Technology Fund'),
    'https://quantmutual.com/Admin/disclouser/quant_Technology_Fund_${DATE_MAPPER_1}_${DATE_MAPPER_2}.xlsx',
    FALSE, '', 'excel', 'xlsx', 1, 2, 4, 5, 6, 7, FALSE, 'MMM', 'YYYY', NULL, NULL, NULL, NULL, NULL, NULL),

    ((SELECT id FROM MUTUAL_FUND WHERE name = 'Quant Banking Fund'),
    'https://quantmutual.com/Admin/disclouser/quant_Banking_Fund_${DATE_MAPPER_1}_${DATE_MAPPER_2}.xlsx',
    FALSE, '', 'excel', 'xlsx', 1, 2, 4, 5, 6, 7, FALSE, 'MMM', 'YYYY', NULL, NULL, NULL, NULL, NULL, NULL),

    ((SELECT id FROM MUTUAL_FUND WHERE name = 'Quant Energy Fund'),
    'https://quantmutual.com/Admin/disclouser/quant_Energy_Fund_${DATE_MAPPER_1}_${DATE_MAPPER_2}.xlsx',
    FALSE, '', 'excel', 'xlsx', 1, 2, 4, 5, 6, 7, FALSE, 'MMM', 'YYYY', NULL, NULL, NULL, NULL, NULL, NULL),

    ((SELECT id FROM MUTUAL_FUND WHERE name = 'Quant Agri Fund'),
    'https://quantmutual.com/Admin/disclouser/quant_Agri_Fund_${DATE_MAPPER_1}_${DATE_MAPPER_2}.xlsx',
    FALSE, '', 'excel', 'xlsx', 1, 2, 4, 5, 6, 7, FALSE, 'MMM', 'YYYY', NULL, NULL, NULL, NULL, NULL, NULL),

    ((SELECT id FROM MUTUAL_FUND WHERE name = 'Quant Realty Fund'),
    'https://quantmutual.com/Admin/disclouser/quant_Realty_Fund_${DATE_MAPPER_1}_${DATE_MAPPER_2}.xlsx',
    FALSE, '', 'excel', 'xlsx', 1, 2, 4, 5, 6, 7, FALSE, 'MMM', 'YYYY', NULL, NULL, NULL, NULL, NULL, NULL),

    ((SELECT id FROM MUTUAL_FUND WHERE name = 'Quant Auto Fund'),
    'https://quantmutual.com/Admin/disclouser/quant_Auto_Fund_${DATE_MAPPER_1}_${DATE_MAPPER_2}.xlsx',
    FALSE, '', 'excel', 'xlsx', 1, 2, 4, 5, 6, 7, FALSE, 'MMM', 'YYYY', NULL, NULL, NULL, NULL, NULL, NULL);
