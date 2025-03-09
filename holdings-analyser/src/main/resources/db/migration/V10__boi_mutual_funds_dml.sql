INSERT INTO MUTUAL_FUND (name, description, mutual_fund_house_id, fund_type_id) VALUES
('Bank of India Large & Mid Cap Equity Fund', 'An open-ended equity scheme investing in both large-cap and mid-cap stocks.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'BOI'),
(SELECT id FROM FUND_TYPE WHERE name = 'LARGE_MID_CAP')),

('Bank of India Bluechip Fund', 'An open-ended equity scheme predominantly investing in large-cap stocks.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'BOI'),
(SELECT id FROM FUND_TYPE WHERE name = 'LARGE_CAP')),

('Bank of India ELSS Tax Saver Fund', 'An open-ended equity-linked saving scheme with a statutory lock-in of 3 years and tax benefit.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'BOI'),
(SELECT id FROM FUND_TYPE WHERE name = 'ELSS')),

('Bank Of India small cap fund', 'An open ended equity scheme predominantly investing in small cap stocks',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'BOI'),
(SELECT id FROM FUND_TYPE WHERE name = 'SMALL_CAP')),

('Bank of India Flexi Cap Fund', 'An open-ended dynamic equity scheme investing across large-cap, mid-cap, and small-cap stocks.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'BOI'),
(SELECT id FROM FUND_TYPE WHERE name = 'FLEXI_CAP')),

('Bank of India Mid & Small Cap Equity & Debt Fund', 'An open-ended hybrid scheme investing predominantly in equity and equity-related instruments of mid-cap and small-cap companies.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'BOI'),
(SELECT id FROM FUND_TYPE WHERE name = 'HYBRID'));
