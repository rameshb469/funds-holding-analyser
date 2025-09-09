update mutual_fund
set name = 'Canara Robeco Large Cap Fund'
where name = 'Canara Robeco Blue Chip Equity Fund';

UPDATE mutual_fund_config c
SET download_url =
    REGEXP_REPLACE(
        c.download_url,
        '([^.]+)\.xlsx$',
        '\1-–-' || REPLACE(mf.name, ' ', '-') || '-–-${DATE_MAPPER_2}.xlsx'
    ),
	date_mapper_2 = 'MMMM-yyyy'
FROM mutual_fund mf
WHERE c.mutual_fund_id = mf.id
and mf.mutual_fund_house_id = (select id from mutual_fund_house where name = 'CR')