package com.acme.scm.supply;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "products")
public class Product {
    @Id
    private UUID id;
    @Column(nullable = false, unique = true, length = 80)
    private String sku;
    @Column(nullable = false, length = 240)
    private String name;
    @Column(length = 2000)
    private String description;
    @Column(name = "unit_price_minor", nullable = false)
    private long unitPriceMinor;
    @Column(nullable = false)
    private boolean active = true;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Version
    private long version;

    protected Product() { }

    Product(String sku, String name, String description, long unitPriceMinor) {
        this.id = UUID.randomUUID();
        this.sku = sku;
        this.name = name;
        this.description = description;
        this.unitPriceMinor = unitPriceMinor;
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public String getSku() { return sku; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public long getUnitPriceMinor() { return unitPriceMinor; }
    public boolean isActive() { return active; }
}
