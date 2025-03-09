INSERT INTO MUTUAL_FUND (name, description, mutual_fund_house_id, fund_type_id) VALUES
('JM Flexicap Fund', 'An open-ended dynamic equity scheme investing across large-cap, mid-cap, and small-cap stocks.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'JM_FIN'),
(SELECT id FROM FUND_TYPE WHERE name = 'FLEXI_CAP')),

('JM Large Cap Fund', 'An open-ended equity scheme predominantly investing in large-cap stocks.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'JM_FIN'),
(SELECT id FROM FUND_TYPE WHERE name = 'LARGE_CAP')),

('JM Mid Cap Fund', 'An open-ended equity scheme predominantly investing in mid-cap stocks.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'JM_FIN'),
(SELECT id FROM FUND_TYPE WHERE name = 'MID_CAP')),

('JM Tax Gain Fund', 'An open-ended equity-linked saving scheme with a statutory lock-in of 3 years and tax benefit.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'JM_FIN'),
(SELECT id FROM FUND_TYPE WHERE name = 'ELSS')),

('JM Value Fund', 'An open-ended equity scheme following a value investment strategy.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'JM_FIN'),
(SELECT id FROM FUND_TYPE WHERE name = 'VALUE')),

('JM Core 11 Fund', 'An open-ended equity scheme investing in a focused portfolio of 11 stocks across market capitalizations.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'JM_FIN'),
(SELECT id FROM FUND_TYPE WHERE name = 'FOCUSED')),

('JM Multi Strategy Fund', 'An open-ended equity scheme investing based on multiple investment strategies.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'JM_FIN'),
(SELECT id FROM FUND_TYPE WHERE name = 'MULTI_CAP')),

('JM Small Cap Fund', 'An open-ended scheme investing in small cap stocks.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'JM_FIN'),
(SELECT id FROM FUND_TYPE WHERE name = 'SMALL_CAP')),

('JM Equity Hybrid Fund', 'An open-ended hybrid scheme investing predominantly in equity and equity-related instruments.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'JM_FIN'),
(SELECT id FROM FUND_TYPE WHERE name = 'HYBRID'));
