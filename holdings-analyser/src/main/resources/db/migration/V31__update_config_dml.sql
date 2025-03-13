UPDATE mutual_fund_config
SET date_mapper_1 = 'dd MM yy'
WHERE download_url = 'https://www.axismf.com/cms/sites/default/files/Statutory/Monthly Portfolio-${DATE_MAPPER_1}.xlsx';

UPDATE mutual_fund_config
SET sheet_name = 'SLTEF' WHERE sheet_name = 'SMAGNUMTAXGAIN';
