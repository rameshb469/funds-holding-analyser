
-- Pharmaceuticals & Drugs
update stock_details
set (industry_id,sector_id) = (SELECT id,sector_id FROM public.industry
where name = 'Pharmaceuticals & Drugs')
where symbol in ('ANTHEM', 'SAILIFE', 'AKUMS', 'SENORES', 'EMCURE', 'RUBICON', 'SUDEEPPHRM', 'ANTHEM');

-- Telecommunication - Equipment
update stock_details
set (industry_id,sector_id) = (SELECT id,sector_id FROM public.industry
where name = 'Telecommunication - Equipment')
where symbol = 'BHARTIHEXA';

-- IT Services & Consulting
update stock_details
set (industry_id,sector_id) = (SELECT id,sector_id FROM public.industry
where name = 'IT Services & Consulting')
where symbol in ('HEXT', 'UNIECOM', 'STYL', 'CAPILLARY');

-- Software
update stock_details
set (industry_id,sector_id) = (SELECT id,sector_id FROM public.industry
where name = 'Software')
where symbol in ('IKS', 'NPST');

-- Engineering - Construction
update stock_details
set (industry_id,sector_id) = (SELECT id,sector_id FROM public.industry
where name = 'Engineering - Construction')
where symbol in ('AJAXENGG', 'TRANSRAILL', 'VIKRAN');;

-- Others-Industrial Gases & Fuels
update stock_details
set (industry_id,sector_id) = (SELECT id,sector_id FROM public.industry
where name = 'Others-Industrial Gases & Fuels')
where symbol = 'ELLEN';

--Iron & Steel
update stock_details
set (industry_id,sector_id) = (SELECT id,sector_id FROM public.industry
where name = 'Iron & Steel')
where symbol in ('BANSALWIRE', 'DEEDEV', 'INTERARCH');

-- Finance - NBFC
update stock_details
set (industry_id,sector_id) = (SELECT id,sector_id FROM public.industry
where name = 'Finance - NBFC')
where symbol in ('HDBFS', 'TATACAP');

-- Finance - Housing
update stock_details
set (industry_id,sector_id) = (SELECT id,sector_id FROM public.industry
where name = 'Finance - Housing')
where symbol = 'BAJAJHFL';

-- Life & Health Insurance
update stock_details
set (industry_id,sector_id) = (SELECT id,sector_id FROM public.industry
where name = 'Life & Health Insurance')
where symbol in ('NIVABUPA', 'CANHLIFE');

--- Construction - Infrastructure
update stock_details
set (industry_id,sector_id) = (SELECT id,sector_id FROM public.industry
where name = 'Construction - Infrastructure')
where symbol = 'CEIGALL';

-- Miscellaneous
update stock_details
set (industry_id,sector_id) = (SELECT id,sector_id FROM public.industry
where name = 'Miscellaneous')
where symbol in ('AWFIS', 'SAGILITY', 'SANSTAR');

-- Auto Ancillaries - Brakes
update stock_details
set (industry_id,sector_id) = (SELECT id,sector_id FROM public.industry
where name = 'Auto Ancillaries - Brakes')
where symbol in ('ATHERENERG', 'OLAELEC');

-- Travel Services
update stock_details
set (industry_id,sector_id) = (SELECT id,sector_id FROM public.industry
where name = 'Travel Services')
where symbol in ('TBOTEK', 'IXIGO', 'ECOSMOBLTY');

-- Medical Equipment/Supplies/Accessories
update stock_details
set (industry_id,sector_id) = (SELECT id,sector_id FROM public.industry
where name = 'Medical Equipment/Supplies/Accessories')
where symbol = 'INDGN';

-- Transmission Towers / Equipments
update stock_details
set (industry_id,sector_id) = (SELECT id,sector_id FROM public.industry
where name = 'Transmission Towers / Equipments')
where symbol = 'KALPATARU';

-- Engineering
update stock_details
set (industry_id,sector_id) = (SELECT id,sector_id FROM public.industry
where name = 'Engineering')
where symbol in ('SGLTL', 'KRN');

-- Misc. Commercial Services
update stock_details
set (industry_id,sector_id) = (SELECT id,sector_id FROM public.industry
where name = 'Misc. Commercial Services')
where symbol in ('BLUSPRING', 'SMARTWORKS', 'URBANCO', 'RKSWAMY');

-- Diamond & Jewellery
update stock_details
set (industry_id,sector_id) = (SELECT id,sector_id FROM public.industry
where name = 'Diamond & Jewellery')
where symbol in ('PNGJL', 'BLUESTONE');

-- Power Generation/Distribution
update stock_details
set (industry_id,sector_id) = (SELECT id,sector_id FROM public.industry
where name = 'Power Generation/Distribution')
where symbol = 'ACMESOLAR';

-- Household & Personal Products
update stock_details
set (industry_id,sector_id) = (SELECT id,sector_id FROM public.industry
where name = 'Household & Personal Products')
where symbol = 'EUREKAFORB';

-- Construction - Residential & Commercial Complexes
update stock_details
set (industry_id,sector_id) = (SELECT id,sector_id FROM public.industry
where name = 'Construction - Residential & Commercial Complexes')
where symbol in ('LOTUSDEV', 'RAYMONDREL');

-- Diversified
update stock_details
set (industry_id,sector_id) = (SELECT id,sector_id FROM public.industry
where name = 'Diversified')
where symbol in ('CPPLUS', 'GODAVARIB', 'TRUALT');

-- Hospital & Healthcare Services
update stock_details
set (industry_id,sector_id) = (SELECT id,sector_id FROM public.industry
where name = 'Hospital & Healthcare Services')
where symbol in ('AGARWALEYE',  'GPTHEALTH');

-- Auto Ancillaries
update stock_details
set (industry_id,sector_id) = (SELECT id,sector_id FROM public.industry
where name = 'Auto Ancillaries')
where symbol in ('KROSS', 'CARRARO', 'BELRISE');

-- Aerospace & Defence
update stock_details
set (industry_id,sector_id) = (SELECT id,sector_id FROM public.industry
where name = 'Aerospace & Defence')
where symbol = 'UNIMECH';

-- Electric Equipment
update stock_details
set (industry_id,sector_id) = (SELECT id,sector_id FROM public.industry
where name = 'Electric Equipment')
where symbol in ('QPOWER', 'VIKRAMSOLR');

-- Rubber Products
update stock_details
set (industry_id,sector_id) = (SELECT id,sector_id FROM public.industry
where name = 'Rubber Products')
where symbol = 'TINNARUBR';

-- Trading
update stock_details
set (industry_id,sector_id) = (SELECT id,sector_id FROM public.industry
where name = 'Trading')
where symbol = 'STYLEBAAZA';

-- Textiles
update stock_details
set (industry_id,sector_id) = (SELECT id,sector_id FROM public.industry
where name = 'Textiles')
where symbol = 'SANATHAN';

-- Engineering - Industrial Equipments
update stock_details
set (industry_id,sector_id) = (SELECT id,sector_id FROM public.industry
where name = 'Engineering - Industrial Equipments')
where symbol in ('MBEL', 'CEWATER');

-- Cement
update stock_details
set (industry_id,sector_id) = (SELECT id,sector_id FROM public.industry
where name = 'Cement')
where symbol in ('JSWCEMENT');

-- Shipping
update stock_details
set (industry_id,sector_id) = (SELECT id,sector_id FROM public.industry
where name = 'Shipping')
where symbol = 'SHREEJISPG';

-- Domestic Appliances
update stock_details
set (industry_id,sector_id) = (SELECT id,sector_id FROM public.industry
where name = 'Domestic Appliances')
where symbol = 'LGEINDIA';

-- Renewables
update stock_details
set (industry_id,sector_id) = (SELECT id,sector_id FROM public.industry
where name = 'Renewables')
where symbol in ('SAATVIKGL', 'GKENERGY', 'EMMVEE');

-- Consumer Food
update stock_details
set (industry_id,sector_id) = (SELECT id,sector_id FROM public.industry
where name = 'Consumer Food')
where symbol = 'ORKLAINDIA';

-- Breweries & Distilleries
update stock_details
set (industry_id,sector_id) = (SELECT id,sector_id FROM public.industry
where name = 'Breweries & Distilleries')
where symbol = 'ABDL';

-- Hotel, Resort & Restaurants
update stock_details
set (industry_id,sector_id) = (SELECT id,sector_id FROM public.industry
where name = 'Hotel, Resort & Restaurants')
where symbol in ('THELEELA', 'BRIGHOTEL', 'TRAVELFOOD');

-- Finance - Investment
update stock_details
set (industry_id,sector_id) = (SELECT id,sector_id FROM public.industry
where name = 'Finance - Investment')
where symbol = 'CRAMC';

-- Electric Equipment - Boilers / Turbines
update stock_details
set (industry_id,sector_id) = (SELECT id,sector_id FROM public.industry
where name = 'Electric Equipment - Boilers / Turbines')
where symbol = 'JNKINDIA';

-- Logistics
update stock_details
set (industry_id,sector_id) = (SELECT id,sector_id FROM public.industry
where name = 'Logistics')
where symbol = 'WCIL';

-- Auto Ancillaries - Others
update stock_details
set (industry_id,sector_id) = (SELECT id,sector_id FROM public.industry
where name = 'Auto Ancillaries - Others')
where symbol = 'STUDDS';

-- Finance - Stock Broking
update stock_details
set (industry_id,sector_id) = (SELECT id,sector_id FROM public.industry
where name = 'Finance - Stock Broking')
where symbol in ('DAMCAPITAL', 'ARSSBL', 'GROWW');

-- Metals - Non Ferrous
update stock_details
set (industry_id,sector_id) = (SELECT id,sector_id FROM public.industry
where name = 'Metals - Non Ferrous')
where symbol = 'JAINREC';

-- Specialty Mining & Metals
update stock_details
set (industry_id,sector_id) = (SELECT id,sector_id FROM public.industry
where name = 'Specialty Mining & Metals')
where symbol = 'MIDWESTLTD';

-- Electric Equipment - Transformers
update stock_details
set (industry_id,sector_id) = (SELECT id,sector_id FROM public.industry
where name = 'Electric Equipment - Transformers')
where symbol = 'ATLANTAELE';

-- Real Estate Rental, Development & Operations
update stock_details
set (industry_id,sector_id) = (SELECT id,sector_id FROM public.industry
where name = 'Real Estate Rental, Development & Operations')
where symbol = 'WEWORK';

-- Electronic Goods
update stock_details
set (industry_id,sector_id) = (SELECT id,sector_id FROM public.industry
where name = 'Electronic Goods')
where symbol = 'EBGNG';

-- Finance - Others
update stock_details
set (industry_id,sector_id) = (SELECT id,sector_id FROM public.industry
where name = 'Finance - Others')
where symbol = 'PINELABS';

-- Educational Institutions
update stock_details
set (industry_id,sector_id) = (SELECT id,sector_id FROM public.industry
where name = 'Educational Institutions')
where symbol = 'PWL';