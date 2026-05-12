-- Update stock_details with sector and industry information

-- Engineering - Industrial Equipments
update stock_details
set (industry_id,sector_id) = (SELECT id,sector_id FROM public.industry
where name = 'Engineering - Industrial Equipments')
where symbol in ('POWERICA', 'OMNI');

-- Shipping
update stock_details
set (industry_id,sector_id) = (SELECT id,sector_id FROM public.industry
where name = 'Shipping')
where symbol in ('SWANDEF');

-- Diamond & Jewellery
update stock_details
set (industry_id,sector_id) = (SELECT id,sector_id FROM public.industry
where name = 'Diamond & Jewellery')
where symbol in ('PNGSREVA');

-- Pharmaceuticals & Drugs
update stock_details
set (industry_id,sector_id) = (SELECT id,sector_id FROM public.industry
where name = 'Pharmaceuticals & Drugs')
where symbol in ('SAIPARENT');

-- IT Services & Consulting
update stock_details
set (industry_id,sector_id) = (SELECT id,sector_id FROM public.industry
where name = 'IT Services & Consulting')
where symbol in ('FRACTAL');

-- Finance - NBFC
update stock_details
set (industry_id,sector_id) = (SELECT id,sector_id FROM public.industry
where name = 'Finance - NBFC')
where symbol in ('KISSHT', 'AYE');

-- Auto Ancillaries - Auto, Truck & Motorcycle Parts
update stock_details
set (industry_id,sector_id) = (SELECT id,sector_id FROM public.industry
where name = 'Auto Ancillaries - Auto, Truck & Motorcycle Parts')
where symbol in ('SEDEMAC');

