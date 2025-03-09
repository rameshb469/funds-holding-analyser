INSERT INTO MUTUAL_FUND (name, description, mutual_fund_house_id, fund_type_id) VALUES
('Tata Large Cap Fund', 'An open-ended equity scheme predominantly investing in large-cap stocks.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'TATA'),
(SELECT id FROM FUND_TYPE WHERE name = 'LARGE_CAP')),

('Tata Multicap Fund', 'An open-ended equity scheme investing across large-cap, mid-cap, and small-cap stocks.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'TATA'),
(SELECT id FROM FUND_TYPE WHERE name = 'MULTI_CAP')),

('Tata Large & Mid Cap Fund', 'An open-ended equity scheme investing in both large-cap and mid-cap stocks.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'TATA'),
(SELECT id FROM FUND_TYPE WHERE name = 'LARGE_MID_CAP')),

('Tata Equity P/E Fund', 'An open-ended equity scheme following a value investment strategy.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'TATA'),
(SELECT id FROM FUND_TYPE WHERE name = 'FLEXI_CAP')),

('Tata Mid Cap Growth Fund', 'An open-ended equity scheme predominantly investing in mid-cap stocks.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'TATA'),
(SELECT id FROM FUND_TYPE WHERE name = 'MID_CAP')),

('Tata India Tax Savings Fund', 'An open-ended equity linked saving scheme with a statutory lock-in of 3 years and tax benefit.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'TATA'),
(SELECT id FROM FUND_TYPE WHERE name = 'ELSS')),

('Tata Small Cap Fund', 'An open-ended equity scheme predominantly investing in small-cap stocks.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'TATA'),
(SELECT id FROM FUND_TYPE WHERE name = 'SMALL_CAP')),

('Tata Index Fund - Nifty Plan', 'An open-ended equity scheme replicating/tracking the Nifty 50 Index.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'TATA'),
(SELECT id FROM FUND_TYPE WHERE name = 'INDEX')),

('Tata Index Fund - Sensex Plan', 'An open-ended equity scheme replicating/tracking the S&P BSE Sensex Index.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'TATA'),
(SELECT id FROM FUND_TYPE WHERE name = 'INDEX')),

('Tata Banking & Financial Services Fund', 'An open-ended equity scheme investing in banking and financial services sector.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'TATA'),
(SELECT id FROM FUND_TYPE WHERE name = 'Sectoral (Banking & Financial Services)')),

('Tata Digital India Fund', 'An open-ended equity scheme investing in information technology sector.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'TATA'),
(SELECT id FROM FUND_TYPE WHERE name = 'Sectoral (Technology)')),

('Tata India Consumer Fund', 'An open-ended equity scheme investing in consumption-oriented sectors.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'TATA'),
(SELECT id FROM FUND_TYPE WHERE name = 'Sectoral (Consumption)')),

('Tata India Pharma & Healthcare Fund', 'An open-ended equity scheme investing in pharma and healthcare sectors.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'TATA'),
(SELECT id FROM FUND_TYPE WHERE name = 'Sectoral (Healthcare)')),

('Tata Resources & Energy Fund', 'An open-ended equity scheme investing in resources and energy sectors.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'TATA'),
(SELECT id FROM FUND_TYPE WHERE name = 'Sectoral (Energy)')),

('Tata Infrastructure Fund', 'An open-ended equity scheme investing in infrastructure sector.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'TATA'),
(SELECT id FROM FUND_TYPE WHERE name = 'Sectoral (Infrastructure)'));