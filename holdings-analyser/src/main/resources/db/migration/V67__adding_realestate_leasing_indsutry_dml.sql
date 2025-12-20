INSERT INTO public.industry (
    name,
    description,
    sector_id,
    created_at,
    updated_at
)
SELECT
    'Real Estate Rental, Development & Operations',
    'Real Estate Rental, Development & Operations',
    s.id,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM sector s
WHERE s.name = 'Real Estate'
AND NOT EXISTS (
    SELECT 1
    FROM public.industry i
    WHERE i.name = 'Real Estate Rental, Development & Operations'
);
