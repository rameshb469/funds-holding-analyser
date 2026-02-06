
-- Pharmaceuticals & Drugs
update stock_details
set (industry_id,sector_id) = (SELECT id,sector_id FROM public.industry
where name = 'Online Services')
where symbol in ('BLACKBUCK');

-- Hospital & Healthcare Services
update stock_details
set (industry_id,sector_id) = (SELECT id,sector_id FROM public.industry
where name = 'Hospital & Healthcare Services')
where symbol in ('PARKHOSPS');