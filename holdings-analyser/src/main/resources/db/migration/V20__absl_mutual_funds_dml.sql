INSERT INTO MUTUAL_FUND (name, description, mutual_fund_house_id, fund_type_id) VALUES
('Aditya Birla Sun Life Frontline Equity Fund', 'An open-ended growth scheme investing across large-cap stocks for long-term capital appreciation.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'ABSL'),
(SELECT id FROM FUND_TYPE WHERE name = 'LARGE_CAP')),

('Aditya Birla Sun Life ELSS Tax Relief 96', 'An Equity-Linked Savings Scheme (ELSS) offering tax benefits under Section 80C with a 3-year lock-in period.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'ABSL'),
(SELECT id FROM FUND_TYPE WHERE name = 'ELSS')),

('Aditya Birla Sun Life Mid Cap Fund', 'Aims to provide long-term capital appreciation by investing predominantly in mid-cap stocks.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'ABSL'),
(SELECT id FROM FUND_TYPE WHERE name = 'MID_CAP')),

('Aditya Birla Sun Life Small Cap Fund', 'Focuses on high-growth potential small-cap stocks to generate long-term wealth.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'ABSL'),
(SELECT id FROM FUND_TYPE WHERE name = 'SMALL_CAP')),

('Aditya Birla Sun Life Flexi Cap Fund', 'An open-ended dynamic equity scheme investing across large, mid, and small-cap companies.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'ABSL'),
(SELECT id FROM FUND_TYPE WHERE name = 'FLEXI_CAP')),

('Aditya Birla Sun Life Hybrid 95 Fund', 'A hybrid fund investing in a mix of equity and debt instruments for balanced growth.',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'ABSL'),
(SELECT id FROM FUND_TYPE WHERE name = 'HYBRID'));
