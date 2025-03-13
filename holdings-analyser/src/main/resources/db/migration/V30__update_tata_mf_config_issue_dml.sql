UPDATE mutual_fund_config
SET
    quantity_col_mapper = 5,
    market_value_col_mapper = 6,
    net_asset_perc_col_mapper = 7,
    is_pick_by_system = false
WHERE
    mutual_fund_id in  (SELECT id FROM mutual_fund
        WHERE mutual_fund_house_id = (SELECT id FROM mutual_fund_house WHERE name = 'TATA')
    );
