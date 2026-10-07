package com.acme.scm.logistics;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "shipments")
public class Shipment {
    @Id
    private UUID id;
    @Column(name = "order_id", nullable = false, unique = true)
    private UUID orderId;
    @Column(name = "tracking_number", nullable = false, unique = true, length = 100)
    private String trackingNumber;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ShipmentStatus status;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    @Version
    private long version;

    protected Shipment() { }

    Shipment(UUID orderId, String trackingNumber) {
        this.id = UUID.randomUUID();
        this.orderId = orderId;
        this.trackingNumber = trackingNumber;
        this.status = ShipmentStatus.LABEL_CREATED;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public UUID getId() { return id; }
    public UUID getOrderId() { return orderId; }
    public String getTrackingNumber() { return trackingNumber; }
    public ShipmentStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public long getVersion() { return version; }

    void updateStatus(ShipmentStatus nextStatus) {
        boolean allowed = switch (status) {
            case LABEL_CREATED -> nextStatus == ShipmentStatus.IN_TRANSIT || nextStatus == ShipmentStatus.CANCELLED;
            case IN_TRANSIT -> nextStatus == ShipmentStatus.OUT_FOR_DELIVERY || nextStatus == ShipmentStatus.CANCELLED;
            case OUT_FOR_DELIVERY -> nextStatus == ShipmentStatus.DELIVERED || nextStatus == ShipmentStatus.CANCELLED;
            case DELIVERED, CANCELLED -> false;
        };
        if (!allowed) {
            throw new IllegalArgumentException("Shipment status transition is not allowed");
        }
        status = nextStatus;
        updatedAt = Instant.now();
    }
}
