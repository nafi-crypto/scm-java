CREATE TABLE user_accounts (
    id UUID PRIMARY KEY,
    email VARCHAR(320) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    role VARCHAR(40) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_user_accounts_email UNIQUE (email),
    CONSTRAINT ck_user_accounts_role CHECK (role IN (
        'ADMIN', 'SUPPLIER', 'PROCUREMENT_MANAGER', 'WAREHOUSE_MANAGER', 'SALES_MANAGER', 'LOGISTICS_MANAGER'
    ))
);

CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES user_accounts(id),
    token_hash CHAR(64) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX ix_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX ix_refresh_tokens_expires_at ON refresh_tokens(expires_at);
