-- deleting invalid funds

DELETE FROM mutual_fund_config
WHERE mutual_fund_id in (SELECT id from mutual_fund WHERE name in
('Quant Multi Cap Fund', 'Quant Energy Fund', 'Quant Agri Fund', 'Quant Realty Fund', 'Quant Auto Fund' ) );

DELETE FROM mutual_fund WHERE name in
('Quant Multi Cap Fund', 'Quant Energy Fund', 'Quant Agri Fund', 'Quant Realty Fund', 'Quant Auto Fund' );

-- Deleting unused axis config
--DELETE FROM mutual_fund_config
--WHERE download_url = 'https://www.axismf.com/cms/sites/default/files/Statutory/Monthly Portfolio as on ${DATE_MAPPER_1}.xlsx';
--

-- changing  the quant mutual funds name
UPDATE mutual_fund SET name = 'Quant ELSS Tax Saver Fund' WHERE name = 'Quant Tax Plan';
UPDATE mutual_fund_config SET download_url = 'https://quantmutual.com/Admin/disclouser/quant_ELSS_Tax_Saver_Fund_${DATE_MAPPER_1}_${DATE_MAPPER_2}.xlsx'
WHERE download_url = 'https://quantmutual.com/Admin/disclouser/quant_Tax_Plan_${DATE_MAPPER_1}_${DATE_MAPPER_2}.xlsx';


UPDATE mutual_fund SET name = 'Quant Tech Fund' WHERE name = 'Quant Technology Fund';
UPDATE mutual_fund_config SET download_url = 'https://quantmutual.com/Admin/disclouser/quant_Teck_Fund_${DATE_MAPPER_1}_${DATE_MAPPER_2}.xlsx'
WHERE download_url = 'https://quantmutual.com/Admin/disclouser/quant_Technology_Fund_${DATE_MAPPER_1}_${DATE_MAPPER_2}.xlsx';

--
UPDATE mutual_fund SET name = 'Quant BFSI Fund' WHERE name = 'Quant Banking Fund';
UPDATE mutual_fund_config SET download_url = 'https://quantmutual.com/Admin/disclouser/quant_BFSI_Fund_${DATE_MAPPER_1}_${DATE_MAPPER_2}.xlsx'
WHERE download_url = 'https://quantmutual.com/Admin/disclouser/quant_Banking_Fund_${DATE_MAPPER_1}_${DATE_MAPPER_2}.xlsx';


UPDATE mutual_fund_config
SET download_url = REPLACE(download_url, '_${DATE_MAPPER_1}_${DATE_MAPPER_2}.xlsx', '_${DATE_MAPPER_1}.xlsx'),
    date_mapper_1 = 'MMM_YYYY',
    date_mapper_2 = ''
WHERE mutual_fund_id IN
(SELECT id FROM mutual_fund WHERE mutual_fund_house_id  =
    (SELECT id FROM mutual_fund_house WHERE name = 'QUANT'));

UPDATE mutual_fund_config
SET sheet_name = 'GF'
WHERE sheet_name = 'Nippon Growth Fund';

UPDATE mutual_fund_config
SET sheet_name = 'GS'
WHERE sheet_name = 'Nippon Vision Fund';

UPDATE mutual_fund_config
SET sheet_name = 'BF'
WHERE sheet_name = 'Nippon Banking & Financial Services Fund';

UPDATE mutual_fund_config
SET sheet_name = 'SH'
WHERE sheet_name = 'Nippon Hybrid Bond Fund';

UPDATE mutual_fund_config
SET sheet_name = 'PS'
WHERE sheet_name = 'Nippon Power & Infra Fund';

UPDATE mutual_fund_config
SET sheet_name = 'ME'
WHERE sheet_name = 'Nippon Consumption Fund';

UPDATE mutual_fund_config
SET sheet_name = 'PH'
WHERE sheet_name = 'Nippon Pharma Fund';

UPDATE mutual_fund_config
SET sheet_name = 'EO'
WHERE sheet_name = 'Nippon Multi Cap Fund';

UPDATE mutual_fund_config
SET sheet_name = 'SE'
WHERE sheet_name = 'Nippon Value Fund';

UPDATE mutual_fund_config
SET sheet_name = 'TS'
WHERE sheet_name = 'Nippon ELSS Tax Saver Fund';

UPDATE mutual_fund_config
SET sheet_name = 'LE'
WHERE sheet_name = 'Nippon Focused Equity Fund';

UPDATE mutual_fund_config
SET sheet_name = 'EA'
WHERE sheet_name = 'Nippon Large Cap Fund';

UPDATE mutual_fund_config
SET sheet_name = 'NF'
WHERE sheet_name = 'Nippon Nifty 50 Plan';

UPDATE mutual_fund_config
SET sheet_name = 'SC'
WHERE sheet_name = 'Nippon Small Cap Fund';

UPDATE mutual_fund_config
SET sheet_name = 'SF'
WHERE sheet_name = 'Nippon BSE Sensex Plan';

