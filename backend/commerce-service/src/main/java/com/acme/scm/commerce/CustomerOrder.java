package com.acme.scm.commerce;

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
@Table(name = "customer_orders")
public class CustomerOrder {
    @Id
    private UUID id;
    @Column(name = "customer_reference", nullable = false, length = 120)
    private String customerReference;
    @Column(name = "request_key", nullable = false, unique = true, length = 100)
    private String requestKey;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private OrderStatus status;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Version
    private long version;

    protected CustomerOrder() { }

    CustomerOrder(String customerReference, String requestKey) {
        id = UUID.randomUUID();
        this.customerReference = customerReference;
        this.requestKey = requestKey;
        this.status = OrderStatus.PENDING_VALIDATION;
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public String getCustomerReference() { return customerReference; }
    public String getRequestKey() { return requestKey; }
    public OrderStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public long getVersion() { return version; }
}
