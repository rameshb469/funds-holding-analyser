update mutual_fund_config
set is_active = false
where date_mapper_1 = 'MMM dd yyyy'
and mutual_fund_id in (
select id from mutual_fund where mutual_fund_house_id =
	(select id from mutual_fund_house where name = 'AXIS')
);