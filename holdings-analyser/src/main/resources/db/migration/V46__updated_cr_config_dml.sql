UPDATE mutual_fund_config
SET
    download_url = 'https://www.canararobeco.com/wp-content/uploads/${DATE_MAPPER_1}/LC.xlsx',
    date_mapper_1 = 'yyyy/MM',
    date_mapper_1_config = 'M+1'
WHERE mutual_fund_id = (SELECT id FROM mutual_fund WHERE name = 'Canara Robeco Blue Chip Equity Fund');

UPDATE mutual_fund_config
SET
    download_url = 'https://www.canararobeco.com/wp-content/uploads/${DATE_MAPPER_1}/MD.xlsx',
    date_mapper_1 = 'yyyy/MM',
    date_mapper_1_config = 'M+1'
WHERE mutual_fund_id = (SELECT id FROM mutual_fund WHERE name = 'Canara Robeco Mid Cap Fund');

UPDATE mutual_fund_config
SET
    download_url = 'https://www.canararobeco.com/wp-content/uploads/${DATE_MAPPER_1}/MF.xlsx',
    date_mapper_1 = 'yyyy/MM',
    date_mapper_1_config = 'M+1'
WHERE mutual_fund_id = (SELECT id FROM mutual_fund WHERE name = 'Canara Robeco Multi Cap Fund');

UPDATE mutual_fund_config
SET
    download_url = 'https://www.canararobeco.com/wp-content/uploads/${DATE_MAPPER_1}/DV.xlsx',
    date_mapper_1 = 'yyyy/MM',
    date_mapper_1_config = 'M+1'
WHERE mutual_fund_id = (SELECT id FROM mutual_fund WHERE name = 'Canara Robeco Flexi Cap Fund');

UPDATE mutual_fund_config
SET
    download_url = 'https://www.canararobeco.com/wp-content/uploads/${DATE_MAPPER_1}/SC.xlsx',
    date_mapper_1 = 'yyyy/MM',
    date_mapper_1_config = 'M+1'
WHERE mutual_fund_id = (SELECT id FROM mutual_fund WHERE name = 'Canara Robeco Small Cap Fund');

UPDATE mutual_fund_config
SET
    download_url = 'https://www.canararobeco.com/wp-content/uploads/${DATE_MAPPER_1}/EQ.xlsx',
    date_mapper_1 = 'yyyy/MM',
    date_mapper_1_config = 'M+1'
WHERE mutual_fund_id = (SELECT id FROM mutual_fund WHERE name = 'Canara Robeco Emerging Equities');

UPDATE mutual_fund_config
SET
    download_url = 'https://www.canararobeco.com/wp-content/uploads/${DATE_MAPPER_1}/ET.xlsx',
    date_mapper_1 = 'yyyy/MM',
    date_mapper_1_config = 'M+1'
WHERE mutual_fund_id = (SELECT id FROM mutual_fund WHERE name = 'Canara Robeco ELSS Tax Saver');

UPDATE mutual_fund_config
SET
    download_url = 'https://www.canararobeco.com/wp-content/uploads/${DATE_MAPPER_1}/CF.xlsx',
    date_mapper_1 = 'yyyy/MM',
    date_mapper_1_config = 'M+1'
WHERE mutual_fund_id = (SELECT id FROM mutual_fund WHERE name = 'Canara Robeco Consumer Trends Fund');

UPDATE mutual_fund_config
SET
    download_url = 'https://www.canararobeco.com/wp-content/uploads/${DATE_MAPPER_1}/IN.xlsx',
    date_mapper_1 = 'yyyy/MM',
    date_mapper_1_config = 'M+1'
WHERE mutual_fund_id = (SELECT id FROM mutual_fund WHERE name = 'Canara Robeco Infrastructure Fund');

UPDATE mutual_fund_config
SET
    download_url = 'https://www.canararobeco.com/wp-content/uploads/${DATE_MAPPER_1}/FE.xlsx',
    date_mapper_1 = 'yyyy/MM',
    date_mapper_1_config = 'M+1'
WHERE mutual_fund_id = (SELECT id FROM mutual_fund WHERE name = 'Canara Robeco Focused Equity Fund');

UPDATE mutual_fund_config
SET
    download_url = 'https://www.canararobeco.com/wp-content/uploads/${DATE_MAPPER_1}/VF.xlsx',
    date_mapper_1 = 'yyyy/MM',
    date_mapper_1_config = 'M+1'
WHERE mutual_fund_id = (SELECT id FROM mutual_fund WHERE name = 'Canara Robeco Value Fund');
