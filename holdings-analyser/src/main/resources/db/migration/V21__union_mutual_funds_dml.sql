INSERT INTO MUTUAL_FUND (name, description, mutual_fund_house_id, fund_type_id) VALUES
('Union Small Cap Fund', 'Focuses on investing in small-cap companies with high growth potential.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'UNION'),
(SELECT id FROM FUND_TYPE WHERE name = 'SMALL_CAP')),

('Union Flexi Cap Fund', 'Invests across large, mid, and small-cap companies to provide flexibility and diversification.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'UNION'),
(SELECT id FROM FUND_TYPE WHERE name = 'FLEXI_CAP')),

('Union Focused Fund', 'Aims to provide optimal returns by managing a dynamic portfolio of debt instruments.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'UNION'),
(SELECT id FROM FUND_TYPE WHERE name = 'FOCUSED')),

('Union Value Fund', 'An open-ended equity scheme predominantly investing in value stocks',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'UNION'),
(SELECT id FROM FUND_TYPE WHERE name = 'VALUE')),

('Union ELSS Tax Saver Fund', 'An open-ended equity scheme predominantly investing for long term investing.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'UNION'),
(SELECT id FROM FUND_TYPE WHERE name = 'ELSS')),

('Union Largecap Fund', 'An open-ended equity scheme predominantly investing in large-cap stocks.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'UNION'),
(SELECT id FROM FUND_TYPE WHERE name = 'LARGE_CAP')),

('Union Midcap Fund', 'An open-ended equity scheme predominantly investing in mid-cap stocks.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'UNION'),
(SELECT id FROM FUND_TYPE WHERE name = 'MID_CAP')),

('Union Large and Midcap Fund', 'An open-ended equity scheme predominantly investing in large and mid cap stocks.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'UNION'),
(SELECT id FROM FUND_TYPE WHERE name = 'LARGE_MID_CAP'));