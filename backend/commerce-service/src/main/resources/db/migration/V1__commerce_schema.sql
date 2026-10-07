CREATE TABLE customer_orders (
    id UUID PRIMARY KEY,
    customer_reference VARCHAR(120) NOT NULL,
    request_key VARCHAR(100) NOT NULL UNIQUE,
    status VARCHAR(32) NOT NULL CHECK (status IN ('PENDING_VALIDATION', 'CONFIRMED', 'CANCELLED')),
    created_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX ix_customer_orders_created_at ON customer_orders(created_at DESC);
