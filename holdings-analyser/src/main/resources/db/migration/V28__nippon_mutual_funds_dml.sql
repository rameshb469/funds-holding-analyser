INSERT INTO MUTUAL_FUND (name, description, mutual_fund_house_id, fund_type_id) VALUES
('Nippon India Growth Fund', 'A mid-cap fund focusing on investing in mid-cap stocks for potential growth.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'NIPPON'),
(SELECT id FROM FUND_TYPE WHERE name = 'MID_CAP')),

('Nippon India Vision Fund', 'Invests in both large-cap and mid-cap stocks aiming for long-term capital appreciation.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'NIPPON'),
(SELECT id FROM FUND_TYPE WHERE name = 'LARGE_MID_CAP')),

('Nippon India Large Cap Fund', 'Predominantly invests in large-cap stocks to provide stable returns.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'NIPPON'),
(SELECT id FROM FUND_TYPE WHERE name = 'LARGE_CAP')),

('Nippon India Index Fund - Nifty 50 Plan', 'Replicates/tracks the Nifty 50 Index.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'NIPPON'),
(SELECT id FROM FUND_TYPE WHERE name = 'INDEX')),

('Nippon India Index Fund - BSE Sensex Plan', 'Replicates/tracks the S&P BSE Sensex.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'NIPPON'),
(SELECT id FROM FUND_TYPE WHERE name = 'INDEX')),

('Nippon India Focused Equity Fund', 'Invests in a maximum of 30 stocks across market capitalizations.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'NIPPON'),
(SELECT id FROM FUND_TYPE WHERE name = 'FOCUSED')),

('Nippon India Small Cap Fund', 'Predominantly invests in small-cap stocks aiming for high growth potential.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'NIPPON'),
(SELECT id FROM FUND_TYPE WHERE name = 'SMALL_CAP')),

('Nippon India Banking & Financial Services Fund', 'An open-ended equity scheme investing in Banking & Financial Services Sector.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'NIPPON'),
(SELECT id FROM FUND_TYPE WHERE name = 'Sectoral (Banking & Financial Services)')),

('Nippon India Power & Infra Fund', 'An Open Ended Equity Scheme Investing In Power & Infrastructure Sectors.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'NIPPON'),
(SELECT id FROM FUND_TYPE WHERE name = 'Sectoral (Infrastructure)')),

('Nippon India Hybrid Bond Fund', 'An Open Ended Hybrid Scheme Investing Predominantly In Debt Instruments.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'NIPPON'),
(SELECT id FROM FUND_TYPE WHERE name = 'HYBRID')),

('Nippon India Pharma Fund', 'An Open Ended Equity Scheme Investing In Pharma Sector.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'NIPPON'),
(SELECT id FROM FUND_TYPE WHERE name = 'Sectoral (Healthcare)')),

('Nippon India Consumption Fund', 'An Open Ended Equity Scheme Following Consumption Theme.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'NIPPON'),
(SELECT id FROM FUND_TYPE WHERE name = 'Sectoral (Consumption)')),

('Nippon India Multi Cap Fund', 'Multi Cap Fund - An Open Ended Equity Scheme Investing Across Large Cap, Mid Cap, Small Cap Stocks.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'NIPPON'),
(SELECT id FROM FUND_TYPE WHERE name = 'MULTI_CAP')),

('Nippon India Value Fund', 'An Open Ended Equity Scheme Following A Value Investment Strategy.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'NIPPON'),
(SELECT id FROM FUND_TYPE WHERE name = 'VALUE')),

('Nippon India ELSS Tax Saver Fund', 'An open-ended equity-linked saving scheme with a statutory lock-in of 3 years and tax benefit.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'NIPPON'),
(SELECT id FROM FUND_TYPE WHERE name = 'ELSS'));
