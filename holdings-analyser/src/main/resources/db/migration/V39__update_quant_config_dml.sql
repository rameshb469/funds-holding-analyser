update mutual_fund_config
set date_mapper_1 = 'MMM_yyyy'
where mutual_fund_id in (
select id from mutual_fund where mutual_fund_house_id =
	(select id from mutual_fund_house where name = 'QUANT')
)