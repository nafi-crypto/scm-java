CREATE TABLE demand_observations (
    id UUID PRIMARY KEY,
    sku VARCHAR(80) NOT NULL,
    observed_on DATE NOT NULL,
    quantity_sold BIGINT NOT NULL CHECK (quantity_sold >= 0),
    CONSTRAINT uq_demand_sku_date UNIQUE (sku, observed_on)
);

CREATE INDEX ix_demand_observations_sku_date ON demand_observations(sku, observed_on DESC);
