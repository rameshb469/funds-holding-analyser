
-- Pharmaceuticals & Drugs
update stock_details
set (industry_id,sector_id) = (SELECT id,sector_id FROM public.industry
where name = 'Finance - Investment')
where symbol in ('ICICIAMC');

-- Retailing
update stock_details
set (industry_id,sector_id) = (SELECT id,sector_id FROM public.industry
where name = 'Retailing')
where symbol in ('MEESHO');

-- Household & Personal Products
update stock_details
set (industry_id,sector_id) = (SELECT id,sector_id FROM public.industry
where name = 'Household & Personal Products')
where symbol = 'WAKEFIT';

-- Cables
update stock_details
set (industry_id,sector_id) = (SELECT id,sector_id FROM public.industry
where name = 'Cables')
where symbol in ('KSHINTL', 'VIDYAWIRES');

-- Engineering - Industrial Equipments
update stock_details
set (industry_id,sector_id) = (SELECT id,sector_id FROM public.industry
where name = 'Engineering - Industrial Equipments')
where symbol = 'AEQUS';