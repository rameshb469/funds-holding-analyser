-- Pharmaceuticals & Drugs
update stock_details
set (industry_id,sector_id) = (SELECT id,sector_id FROM public.industry
where name = 'Courier Services')
where symbol in ('SHADOWFAX');