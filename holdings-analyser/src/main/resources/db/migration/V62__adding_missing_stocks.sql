SELECT setval('industry_id_seq', (SELECT MAX(id) FROM industry));

INSERT INTO public.industry (name, description, sector_id, created_at, updated_at)
SELECT
    'Electric Equipment - Transformers',
    'Electric Equipment - Transformers',
    (SELECT id FROM sector WHERE name = 'Infrastructure'),
    current_timestamp,
    current_timestamp
WHERE NOT EXISTS (
    SELECT 1 FROM public.industry WHERE name = 'Electric Equipment - Transformers'
);




update stock_details
set industry_id = (select id from industry where name = 'Electric Equipment - Transformers'),
sector_id = (select id from sector where name = 'Infrastructure')
where symbol = 'ATLANTAELE';

-- ARSSBL -- Finance - Stock Broking
update stock_details
set industry_id = (select id from industry where name = 'Finance - Stock Broking'),
sector_id = (select id from sector where name = 'Finance')
where symbol = 'ARSSBL';

-- SAATVIKGL -- Miscellaneous - Renewables
update stock_details
set industry_id = (select id from industry where name = 'Renewables'),
sector_id = (select id from sector where name = 'Miscellaneous')
where symbol = 'SAATVIKGL';

-- URBANCO -- Miscellaneous -- Misc. Commercial Services
update stock_details
set industry_id = (select id from industry where name = 'Misc. Commercial Services'),
sector_id = (select id from sector where name = 'Miscellaneous')
where symbol = 'URBANCO';

-- STYL -- Software & IT Services -- IT Services & Consulting
update stock_details
set industry_id = (select id from industry where name = 'IT Services & Consulting'),
sector_id = (select id from sector where name = 'Software & IT Services')
where symbol = 'STYL';

-- TRUALT -- Diversified -- Diversified
update stock_details
set industry_id = (select id from industry where name = 'Diversified'),
sector_id = (select id from sector where name = 'Diversified')
where symbol = 'TRUALT';

-- SAATVIKGL -- Miscellaneous - Renewables
update stock_details
set industry_id = (select id from industry where name = 'Renewables'),
sector_id = (select id from sector where name = 'Miscellaneous')
where symbol = 'GKENERGY';

-- JAINREC -- Metals & Mining - Metals - Non Ferrous
update stock_details
set industry_id = (select id from industry where name = 'Metals - Non Ferrous'),
sector_id = (select id from sector where name = 'Metals & Mining')
where symbol = 'JAINREC';