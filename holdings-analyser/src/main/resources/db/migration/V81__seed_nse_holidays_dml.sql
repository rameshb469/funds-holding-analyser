-- Seed NSE holidays for 2026. Idempotent: ON CONFLICT skips existing rows.
INSERT INTO nse_holiday (holiday_date, description) VALUES
    ('2026-01-26', 'Republic Day'),
    ('2026-02-19', 'Mahashivratri'),
    ('2026-03-03', 'Holi'),
    ('2026-03-31', 'Eid-al-Fitr'),
    ('2026-04-03', 'Good Friday'),
    ('2026-04-14', 'Dr. Baba Saheb Ambedkar Jayanti'),
    ('2026-05-01', 'Maharashtra Day'),
    ('2026-05-27', 'Buddha Purnima'),
    ('2026-06-16', 'Eid-al-Adha'),
    ('2026-08-15', 'Independence Day'),
    ('2026-08-26', 'Ganesh Chaturthi'),
    ('2026-10-02', 'Mahatma Gandhi Jayanti'),
    ('2026-10-19', 'Dussehra'),
    ('2026-11-08', 'Diwali'),
    ('2026-11-25', 'Guru Nanak Jayanti'),
    ('2026-12-25', 'Christmas')
ON CONFLICT (holiday_date) DO NOTHING;
