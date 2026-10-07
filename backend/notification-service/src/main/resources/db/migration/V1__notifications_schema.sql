CREATE TABLE notification_deliveries (
    id UUID PRIMARY KEY,
    channel VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);
