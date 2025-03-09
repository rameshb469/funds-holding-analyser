INSERT INTO MUTUAL_FUND (name, description, mutual_fund_house_id, fund_type_id) VALUES
('Axis Bluechip Fund', 'Invests in large-cap companies with a track record of consistent performance.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'AXIS'),
(SELECT id FROM FUND_TYPE WHERE name = 'LARGE_CAP')),

('Axis Midcap Fund', 'Focuses on investing in mid-cap companies with high growth potential.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'AXIS'),
(SELECT id FROM FUND_TYPE WHERE name = 'MID_CAP')),

('Axis Small Cap Fund', 'Targets investments in small-cap companies to capture early growth opportunities.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'AXIS'),
(SELECT id FROM FUND_TYPE WHERE name = 'SMALL_CAP')),

('Axis Long Term Equity Fund', 'An Equity Linked Savings Scheme (ELSS) offering tax benefits under Section 80C with a diversified equity portfolio.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'AXIS'),
(SELECT id FROM FUND_TYPE WHERE name = 'ELSS')),

('Axis Growth Opportunities Fund', 'Invests in a mix of large and mid-cap companies to balance growth and stability.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'AXIS'),
(SELECT id FROM FUND_TYPE WHERE name = 'LARGE_MID_CAP')),

('Axis Focused 25 Fund', 'Invests in a concentrated portfolio of up to 25 high conviction stocks across market capitalizations.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'AXIS'),
(SELECT id FROM FUND_TYPE WHERE name = 'MULTI_CAP')),

('Axis Value Fund', 'Seeks to invest in undervalued companies with strong fundamentals.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'AXIS'),
(SELECT id FROM FUND_TYPE WHERE name = 'FLEXI_CAP')),

('Axis Nifty 100 Index Fund', 'Aims to replicate the performance of the Nifty 100 Index by investing in the same proportion of stocks.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'AXIS'),
(SELECT id FROM FUND_TYPE WHERE name = 'INDEX'));