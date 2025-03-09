INSERT INTO MUTUAL_FUND (name, description, mutual_fund_house_id, fund_type_id) VALUES
('HSBC Large Cap Fund', 'An open-ended equity scheme predominantly investing in large-cap stocks.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'HSBC'),
(SELECT id FROM FUND_TYPE WHERE name = 'LARGE_CAP')),

('HSBC Mid Cap Fund', 'An open-ended equity scheme predominantly investing in mid-cap stocks.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'HSBC'),
(SELECT id FROM FUND_TYPE WHERE name = 'MID_CAP')),

('HSBC Small Cap Fund', 'An open-ended equity scheme predominantly investing in small-cap stocks.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'HSBC'),
(SELECT id FROM FUND_TYPE WHERE name = 'SMALL_CAP')),

('HSBC Flexi Cap Fund', 'An open-ended dynamic equity scheme investing across large-cap, mid-cap, and small-cap stocks.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'HSBC'),
(SELECT id FROM FUND_TYPE WHERE name = 'FLEXI_CAP')),

('HSBC Focused Equity Fund', 'An open-ended equity scheme investing in a maximum of 30 stocks across market capitalizations.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'HSBC'),
(SELECT id FROM FUND_TYPE WHERE name = 'FOCUSED')),

('HSBC ELSS Fund', 'An open-ended equity-linked saving scheme with a statutory lock-in of 3 years and tax benefit.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'HSBC'),
(SELECT id FROM FUND_TYPE WHERE name = 'ELSS')),

('HSBC Infrastructure Fund', 'An open-ended equity scheme investing in the infrastructure sector.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'HSBC'),
(SELECT id FROM FUND_TYPE WHERE name = 'Sectoral (Infrastructure)')),

('HSBC Value Fund', 'An open-ended equity scheme following a value investment strategy.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'HSBC'),
(SELECT id FROM FUND_TYPE WHERE name = 'VALUE'));