INSERT INTO MUTUAL_FUND (name, description, mutual_fund_house_id, fund_type_id) VALUES
('SBI Bluechip Fund', 'An open-ended equity scheme predominantly investing in large-cap stocks.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'SBI'),
(SELECT id FROM FUND_TYPE WHERE name = 'LARGE_CAP')),

('SBI Magnum Midcap Fund', 'An open-ended equity scheme predominantly investing in mid-cap stocks.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'SBI'),
(SELECT id FROM FUND_TYPE WHERE name = 'MID_CAP')),

('SBI Small Cap Fund', 'An open-ended equity scheme predominantly investing in small-cap stocks.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'SBI'),
(SELECT id FROM FUND_TYPE WHERE name = 'SMALL_CAP')),

('SBI Flexicap Fund', 'An open-ended dynamic equity scheme investing across large-cap, mid-cap, and small-cap stocks.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'SBI'),
(SELECT id FROM FUND_TYPE WHERE name = 'FLEXI_CAP')),

('SBI Focused Equity Fund', 'An open-ended equity scheme investing in a maximum of 30 stocks across market capitalizations.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'SBI'),
(SELECT id FROM FUND_TYPE WHERE name = 'FOCUSED')),

('SBI Magnum Taxgain Scheme', 'An open-ended equity-linked savings scheme with a statutory lock-in of 3 years and tax benefit.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'SBI'),
(SELECT id FROM FUND_TYPE WHERE name = 'ELSS')),

('SBI Equity Hybrid Fund', 'An open-ended hybrid scheme investing predominantly in equity and equity-related instruments.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'SBI'),
(SELECT id FROM FUND_TYPE WHERE name = 'HYBRID')),

('SBI Infrastructure Fund', 'An open-ended equity scheme investing in the infrastructure sector.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'SBI'),
(SELECT id FROM FUND_TYPE WHERE name = 'Sectoral (Infrastructure)')),

('SBI Banking & Financial Services Fund', 'An open-ended equity scheme investing in banking and financial services sector.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'SBI'),
(SELECT id FROM FUND_TYPE WHERE name = 'Sectoral (Banking & Financial Services)')),

('SBI Consumption Opportunities Fund', 'An open-ended equity scheme investing in consumption-oriented sectors.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'SBI'),
(SELECT id FROM FUND_TYPE WHERE name = 'Sectoral (Consumption)'));