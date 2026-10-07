CREATE TABLE suppliers (
    id UUID PRIMARY KEY,
    name VARCHAR(200) NOT NULL UNIQUE,
    email VARCHAR(320) NOT NULL UNIQUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE products (
    id UUID PRIMARY KEY,
    sku VARCHAR(80) NOT NULL UNIQUE,
    name VARCHAR(240) NOT NULL,
    description VARCHAR(2000),
    unit_price_minor BIGINT NOT NULL CHECK (unit_price_minor > 0),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE stock_items (
    id UUID PRIMARY KEY,
    sku VARCHAR(80) NOT NULL UNIQUE REFERENCES products(sku),
    on_hand BIGINT NOT NULL DEFAULT 0 CHECK (on_hand >= 0),
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);
