update mutual_fund_config 
set download_url = 'https://www.miraeassetmf.co.in/docs/default-source/portfolios/miccf_${DATE_MAPPER_1}.xlsx' 
where 
mutual_fund_id = (select id from mutual_fund where name = 'Mirae Asset Great Consumer Fund');