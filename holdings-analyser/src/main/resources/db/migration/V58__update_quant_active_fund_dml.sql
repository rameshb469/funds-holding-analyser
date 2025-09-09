update mutual_fund_config
set download_url = 'https://quantmutual.com/Admin/disclouser/quant_Multi_Cap_Fund_${DATE_MAPPER_1}.xlsx'
where download_url = 'https://quantmutual.com/Admin/disclouser/quant_Active_Fund_${DATE_MAPPER_1}.xlsx';

update mutual_fund
set name = 'Quant Multicap Fund'
where name = 'Quant Active Fund';