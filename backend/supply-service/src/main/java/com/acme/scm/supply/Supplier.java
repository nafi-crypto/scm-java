package com.acme.scm.supply;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "suppliers")
public class Supplier {
    @Id
    private UUID id;
    @Column(nullable = false, unique = true, length = 200)
    private String name;
    @Column(nullable = false, unique = true, length = 320)
    private String email;
    @Column(nullable = false)
    private boolean active = true;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Version
    private long version;

    protected Supplier() { }

    Supplier(String name, String email) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.email = email.toLowerCase();
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public boolean isActive() { return active; }
}
