INSERT INTO MUTUAL_FUND (name, description, mutual_fund_house_id, fund_type_id) VALUES
('Canara Robeco Blue Chip Equity Fund', 'An open-ended equity scheme predominantly investing in large-cap stocks.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'CR'),
(SELECT id FROM FUND_TYPE WHERE name = 'LARGE_CAP')),

('Canara Robeco Mid Cap Fund', 'An open-ended equity scheme predominantly investing in mid-cap stocks.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'CR'),
(SELECT id FROM FUND_TYPE WHERE name = 'MID_CAP')),

('Canara Robeco Multi Cap Fund', 'An open-ended equity scheme investing across large-cap, mid-cap, and small-cap stocks.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'CR'),
(SELECT id FROM FUND_TYPE WHERE name = 'MULTI_CAP')),

('Canara Robeco Flexi Cap Fund', 'An open-ended dynamic equity scheme investing across large-cap, mid-cap, and small-cap stocks.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'CR'),
(SELECT id FROM FUND_TYPE WHERE name = 'FLEXI_CAP')),

('Canara Robeco Small Cap Fund', 'An open-ended equity scheme predominantly investing in small-cap stocks.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'CR'),
(SELECT id FROM FUND_TYPE WHERE name = 'SMALL_CAP')),

('Canara Robeco Emerging Equities', 'An open-ended equity scheme investing in both large-cap and mid-cap stocks.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'CR'),
(SELECT id FROM FUND_TYPE WHERE name = 'LARGE_MID_CAP')),

('Canara Robeco ELSS Tax Saver', 'An open-ended equity-linked saving scheme with a statutory lock-in of 3 years and tax benefit.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'CR'),
(SELECT id FROM FUND_TYPE WHERE name = 'ELSS')),

('Canara Robeco Consumer Trends Fund', 'An open-ended equity scheme following the consumption and finance theme.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'CR'),
(SELECT id FROM FUND_TYPE WHERE name = 'Sectoral (Consumption)')),

('Canara Robeco Infrastructure Fund', 'An open-ended equity scheme investing in the infrastructure sector.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'CR'),
(SELECT id FROM FUND_TYPE WHERE name = 'Sectoral (Infrastructure)')),

('Canara Robeco Focused Equity Fund', 'An open-ended equity scheme investing in a maximum of 30 stocks across market capitalizations.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'CR'),
(SELECT id FROM FUND_TYPE WHERE name = 'FOCUSED')),

('Canara Robeco Value Fund', 'An open-ended equity scheme following a value investment strategy.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'CR'),
(SELECT id FROM FUND_TYPE WHERE name = 'VALUE'));