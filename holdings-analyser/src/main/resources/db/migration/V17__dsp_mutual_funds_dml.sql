INSERT INTO MUTUAL_FUND (name, description, mutual_fund_house_id, fund_type_id) VALUES
('DSP Top 100 Equity Fund', 'An open-ended equity scheme predominantly investing in large-cap stocks.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'DSP'),
(SELECT id FROM FUND_TYPE WHERE name = 'LARGE_CAP')),

('DSP Midcap Fund', 'An open-ended equity scheme predominantly investing in mid-cap stocks.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'DSP'),
(SELECT id FROM FUND_TYPE WHERE name = 'MID_CAP')),

('DSP Small Cap Fund', 'An open-ended equity scheme predominantly investing in small-cap stocks.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'DSP'),
(SELECT id FROM FUND_TYPE WHERE name = 'SMALL_CAP')),

('DSP Flexicap Fund', 'An open-ended dynamic equity scheme investing across large-cap, mid-cap, and small-cap stocks.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'DSP'),
(SELECT id FROM FUND_TYPE WHERE name = 'FLEXI_CAP')),

('DSP Focus Fund', 'An open-ended equity scheme investing in a maximum of 30 stocks across market capitalizations.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'DSP'),
(SELECT id FROM FUND_TYPE WHERE name = 'FOCUSED')),

('DSP Tax Saver Fund', 'An open-ended equity-linked savings scheme with a statutory lock-in of 3 years and tax benefit.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'DSP'),
(SELECT id FROM FUND_TYPE WHERE name = 'ELSS')),

('DSP Equity Opportunities Fund', 'An open-ended equity scheme investing in both large-cap and mid-cap stocks.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'DSP'),
(SELECT id FROM FUND_TYPE WHERE name = 'LARGE_MID_CAP')),

('DSP Equity & Bond Fund', 'An open-ended hybrid scheme investing predominantly in equity and equity-related instruments.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'DSP'),
(SELECT id FROM FUND_TYPE WHERE name = 'HYBRID')),

('DSP Value Fund', 'An open-ended equity scheme following a value investment strategy.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'DSP'),
(SELECT id FROM FUND_TYPE WHERE name = 'VALUE'));
