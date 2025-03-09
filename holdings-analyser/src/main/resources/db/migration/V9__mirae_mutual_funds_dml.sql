INSERT INTO MUTUAL_FUND (name, description, mutual_fund_house_id, fund_type_id) VALUES
('Mirae Asset Large Cap Fund', 'An open-ended equity scheme predominantly investing across large-cap stocks.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'MIRAE'),
(SELECT id FROM FUND_TYPE WHERE name = 'LARGE_CAP')),

('Mirae Asset ELSS Tax Saver Fund', 'An open-ended equity-linked saving scheme with a statutory lock-in of 3 years and tax benefit.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'MIRAE'),
(SELECT id FROM FUND_TYPE WHERE name = 'ELSS')),

('Mirae Asset Focused Fund', 'An open-ended equity scheme investing in a maximum of 30 stocks intending to focus in Large Cap, Mid Cap & Small Cap categories (i.e., Multi-Cap).',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'MIRAE'),
(SELECT id FROM FUND_TYPE WHERE name = 'MULTI_CAP')),

('Mirae Asset Large and Midcap Fund', 'An open-ended equity scheme investing in both large-cap and mid-cap stocks.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'MIRAE'),
(SELECT id FROM FUND_TYPE WHERE name = 'LARGE_MID_CAP')),

('Mirae Asset Great Consumer Fund', 'An open-ended equity scheme following the consumption theme.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'MIRAE'),
(SELECT id FROM FUND_TYPE WHERE name = 'Sectoral (Consumption)')),

('Mirae Asset Healthcare Fund', 'An open-ended equity scheme investing in healthcare and allied sectors.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'MIRAE'),
(SELECT id FROM FUND_TYPE WHERE name = 'Sectoral (Healthcare)')),

('Mirae Asset Midcap Fund', 'An open-ended equity scheme predominantly investing in mid-cap stocks.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'MIRAE'),
(SELECT id FROM FUND_TYPE WHERE name = 'MID_CAP')),

('Mirae Asset Banking and Financial Services Fund', 'An open-ended scheme investing in banking and financial services sector.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'MIRAE'),
(SELECT id FROM FUND_TYPE WHERE name = 'Sectoral (Banking & Financial Services)')),

('Mirae Asset Flexi Cap Fund', 'An open-ended dynamic equity scheme investing across large-cap, mid-cap, and small-cap stocks.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'MIRAE'),
(SELECT id FROM FUND_TYPE WHERE name = 'FLEXI_CAP')),

('Mirae Asset Multicap Fund', 'An open-ended equity scheme investing across large-cap, mid-cap, and small-cap stocks.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'MIRAE'),
(SELECT id FROM FUND_TYPE WHERE name = 'MULTI_CAP')),

('Mirae Asset Smallcap Fund', 'An open-ended equity scheme predominantly investing in small-cap stocks.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'MIRAE'),
(SELECT id FROM FUND_TYPE WHERE name = 'SMALL_CAP'));
