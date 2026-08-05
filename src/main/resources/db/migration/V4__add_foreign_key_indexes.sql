CREATE INDEX idx_oauth_accounts_account ON oauth_accounts (account_id);
CREATE INDEX idx_terms_agreements_account ON terms_agreements (account_id);
CREATE INDEX idx_symbols_category ON symbols (category_id);
CREATE INDEX idx_favorite_symbol ON user_favorite_symbols (symbol_id);

