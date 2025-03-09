
INSERT INTO MUTUAL_FUND (name, description, mutual_fund_house_id, fund_type_id) VALUES
('Edelweiss Large Cap Fund', 'An open-ended equity scheme predominantly investing in large-cap stocks.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'EDELWEISS'),
(SELECT id FROM FUND_TYPE WHERE name = 'LARGE_CAP')),

('Edelweiss Mid Cap Fund', 'An open-ended equity scheme predominantly investing in mid-cap stocks.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'EDELWEISS'),
(SELECT id FROM FUND_TYPE WHERE name = 'MID_CAP')),

('Edelweiss Small Cap Fund', 'An open-ended equity scheme predominantly investing in small-cap stocks.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'EDELWEISS'),
(SELECT id FROM FUND_TYPE WHERE name = 'SMALL_CAP')),

('Edelweiss Flexi Cap Fund', 'An open-ended dynamic equity scheme investing across large-cap, mid-cap, and small-cap stocks.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'EDELWEISS'),
(SELECT id FROM FUND_TYPE WHERE name = 'FLEXI_CAP')),

('Edelweiss Focused Equity Fund', 'An open-ended equity scheme investing in a maximum of 30 stocks across market capitalizations.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'EDELWEISS'),
(SELECT id FROM FUND_TYPE WHERE name = 'FOCUSED')),

('Edelweiss Tax Advantage Fund', 'An open-ended equity-linked savings scheme with a statutory lock-in of 3 years and tax benefit.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'EDELWEISS'),
(SELECT id FROM FUND_TYPE WHERE name = 'ELSS')),

('Edelweiss Large & Mid Cap Fund', 'An open-ended equity scheme investing in both large-cap and mid-cap stocks.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'EDELWEISS'),
(SELECT id FROM FUND_TYPE WHERE name = 'LARGE_MID_CAP')),

('Edelweiss Equity Hybrid Fund', 'An open-ended hybrid scheme investing predominantly in equity and equity-related instruments.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'EDELWEISS'),
(SELECT id FROM FUND_TYPE WHERE name = 'HYBRID'));