INSERT INTO MUTUAL_FUND (name, mutual_fund_house_id, fund_type_id) VALUES
('Quant Active Fund', (SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'QUANT'),(SELECT id FROM fund_type WHERE name = 'FLEXI_CAP')),
('Quant Small Cap Fund', (SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'QUANT'),(SELECT id FROM fund_type WHERE name = 'SMALL_CAP')),
('Quant Mid Cap Fund', (SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'QUANT'), (SELECT id FROM fund_type WHERE name = 'MID_CAP')),
('Quant Large Cap Fund', (SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'QUANT'), (SELECT id FROM fund_type WHERE name = 'LARGE_CAP')),
('Quant Multi Cap Fund', (SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'QUANT'), (SELECT id FROM fund_type WHERE name = 'MULTI_CAP')),
('Quant Infrastructure Fund', (SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'QUANT'), (SELECT id FROM fund_type WHERE name = 'Sectoral (Infrastructure)')),
('Quant Consumption Fund', (SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'QUANT'), (SELECT id FROM fund_type WHERE name = 'Sectoral (Consumption)')),
('Quant Tax Plan', (SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'QUANT'), (SELECT id FROM fund_type WHERE name = 'ELSS')),
('Quant Focused Fund', (SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'QUANT'), (SELECT id FROM fund_type WHERE name = 'INDEX')),
('Quant Value Fund', (SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'QUANT'), (SELECT id FROM fund_type WHERE name = 'HYBRID')),
('Quant Healthcare Fund', (SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'QUANT'), (SELECT id FROM fund_type WHERE name = 'Sectoral (Healthcare)')),
('Quant Technology Fund', (SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'QUANT'), (SELECT id FROM fund_type WHERE name = 'Sectoral (Technology)')),
('Quant Banking Fund', (SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'QUANT'), (SELECT id FROM fund_type WHERE name = 'Sectoral (Banking & Financial Services)')),
('Quant Energy Fund', (SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'QUANT'), (SELECT id FROM fund_type WHERE name = 'Sectoral (Energy)')),
('Quant Agri Fund', (SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'QUANT'), (SELECT id FROM fund_type WHERE name = 'Sectoral (Agriculture)')),
('Quant Realty Fund', (SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'QUANT'), (SELECT id FROM fund_type WHERE name = 'Sectoral (Real Estate)')),
('Quant Auto Fund', (SELECT id FROM MUTUAL_FUND_HOUSE WHERE name = 'QUANT'), (SELECT id FROM fund_type WHERE name = 'Sectoral (Automobile)'));