 -- Quant value fund
UPDATE MUTUAL_FUND
SET fund_type_id = (SELECT id FROM FUND_TYPE WHERE name = 'VALUE')
WHERE id = (SELECT id from MUTUAL_FUND WHERE name = 'Quant Value Fund');

-- Quant focused
UPDATE MUTUAL_FUND
SET fund_type_id = (SELECT id FROM FUND_TYPE WHERE name = 'FOCUSED')
WHERE id = (SELECT id from MUTUAL_FUND WHERE name = 'Quant Focused Fund');


-- TATA funds
UPDATE MUTUAL_FUND
SET fund_type_id = (SELECT id FROM FUND_TYPE WHERE name = 'VALUE')
WHERE id = (SELECT id from MUTUAL_FUND WHERE name = 'Tata Equity P/E Fund');


INSERT INTO MUTUAL_FUND (name, description, mutual_fund_house_id, fund_type_id) VALUES
('Tata Focused Equity Fund Direct Growth', 'This open-ended equity scheme invests in a maximum of 30 stocks across various market capitalizations, aiming for long-term capital appreciation',
(SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'TATA'),
(SELECT id FROM FUND_TYPE WHERE name = 'FOCUSED'));

-- AXIS funds
UPDATE MUTUAL_FUND
SET fund_type_id = (SELECT id FROM FUND_TYPE WHERE name = 'VALUE')
WHERE id = (SELECT id from MUTUAL_FUND WHERE name = 'Axis Value Fund');

UPDATE MUTUAL_FUND
SET fund_type_id = (SELECT id FROM FUND_TYPE WHERE name = 'FOCUSED')
WHERE id = (SELECT id from MUTUAL_FUND WHERE name = 'Axis Focused 25 Fund');

-- Mirae
UPDATE MUTUAL_FUND
SET fund_type_id = (SELECT id FROM FUND_TYPE WHERE name = 'FOCUSED')
WHERE id = (SELECT id from MUTUAL_FUND WHERE name = 'Mirae Asset Focused Fund');