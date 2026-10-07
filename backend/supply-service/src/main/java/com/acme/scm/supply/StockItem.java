package com.acme.scm.supply;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "stock_items")
public class StockItem {
    @Id
    private UUID id;
    @Column(nullable = false, unique = true, length = 80)
    private String sku;
    @Column(name = "on_hand", nullable = false)
    private long onHand;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    @Version
    private long version;

    protected StockItem() { }

    StockItem(String sku) {
        this.id = UUID.randomUUID();
        this.sku = sku;
        this.updatedAt = Instant.now();
    }

    public String getSku() { return sku; }
    public long getOnHand() { return onHand; }
    public long getVersion() { return version; }

    void adjust(long delta) {
        if (delta < 0 && onHand < -delta) {
            throw new IllegalArgumentException("Stock adjustment would make on-hand quantity negative");
        }
        onHand = Math.addExact(onHand, delta);
        updatedAt = Instant.now();
    }
}
