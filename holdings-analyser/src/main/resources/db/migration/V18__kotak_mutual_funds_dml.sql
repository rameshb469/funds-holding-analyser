
INSERT INTO MUTUAL_FUND (name, description, mutual_fund_house_id, fund_type_id) VALUES
('Kotak Bluechip Fund', 'An open-ended equity scheme predominantly investing in large-cap stocks.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'KOTAK'),
(SELECT id FROM FUND_TYPE WHERE name = 'LARGE_CAP')),

('Kotak Emerging Equity Fund', 'An open-ended equity scheme predominantly investing in mid-cap stocks.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'KOTAK'),
(SELECT id FROM FUND_TYPE WHERE name = 'MID_CAP')),

('Kotak Small Cap Fund', 'An open-ended equity scheme predominantly investing in small-cap stocks.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'KOTAK'),
(SELECT id FROM FUND_TYPE WHERE name = 'SMALL_CAP')),

('Kotak Flexicap Fund', 'An open-ended dynamic equity scheme investing across large-cap, mid-cap, and small-cap stocks.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'KOTAK'),
(SELECT id FROM FUND_TYPE WHERE name = 'FLEXI_CAP')),

('Kotak Focused Equity Fund', 'An open-ended equity scheme investing in a maximum of 30 stocks across market capitalizations.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'KOTAK'),
(SELECT id FROM FUND_TYPE WHERE name = 'FOCUSED')),

('Kotak Tax Saver Fund', 'An open-ended equity-linked savings scheme with a statutory lock-in of 3 years and tax benefit.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'KOTAK'),
(SELECT id FROM FUND_TYPE WHERE name = 'ELSS')),

('Kotak Equity Opportunities Fund', 'An open-ended equity scheme investing in both large-cap and mid-cap stocks.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'KOTAK'),
(SELECT id FROM FUND_TYPE WHERE name = 'LARGE_MID_CAP')),

('Kotak Equity Hybrid Fund', 'An open-ended hybrid scheme investing predominantly in equity and equity-related instruments.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'KOTAK'),
(SELECT id FROM FUND_TYPE WHERE name = 'HYBRID')),

('Kotak Equity Value Fund', 'An open-ended equity scheme following a value investment strategy.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'KOTAK'),
(SELECT id FROM FUND_TYPE WHERE name = 'VALUE')),

('Kotak Infrastructure & Economic Reform Fund', 'An open-ended equity scheme investing in companies benefiting from infrastructure and economic reforms.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'KOTAK'),
(SELECT id FROM FUND_TYPE WHERE name = 'Sectoral (Infrastructure)')),

('Kotak Banking and Financial Services Fund', 'An open-ended equity scheme investing in the banking and financial services sector.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'KOTAK'),
(SELECT id FROM FUND_TYPE WHERE name = 'Sectoral (Banking & Financial Services)'));