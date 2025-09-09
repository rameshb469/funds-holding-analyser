
update mutual_fund_config
set isin_col_mapper = 2, stock_col_mapper = 1
where download_url = 'https://www.axismf.com/cms/sites/default/files/Statutory/Monthly Portfolio-${DATE_MAPPER_1}.xlsx';