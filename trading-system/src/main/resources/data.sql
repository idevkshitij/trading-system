-- Initial stock data
INSERT IGNORE INTO stocks (symbol, name, sector) VALUES
('AAPL', 'Apple Inc.', 'TECH'),
('MSFT', 'Microsoft Corp', 'TECH'),
('GOOGL', 'Alphabet Inc.', 'TECH'),
('TSLA', 'Tesla Inc.', 'TECH'),
('NVDA', 'NVIDIA Corp', 'TECH'),
('JPM', 'JPMorgan Chase', 'FINANCE'),
('GS', 'Goldman Sachs', 'FINANCE'),
('BAC', 'Bank of America', 'FINANCE'),
('MS', 'Morgan Stanley', 'FINANCE'),
('WFC', 'Wells Fargo', 'FINANCE'),
('XOM', 'Exxon Mobil', 'ENERGY'),
('JNJ', 'Johnson & Johnson', 'HEALTHCARE');