package com.acme.scm.insights;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "demand_observations", uniqueConstraints =
        @UniqueConstraint(name = "uq_demand_sku_date", columnNames = {"sku", "observed_on"}))
public class DemandObservation {
    @Id
    private UUID id;
    @Column(nullable = false, length = 80)
    private String sku;
    @Column(name = "observed_on", nullable = false)
    private LocalDate observedOn;
    @Column(name = "quantity_sold", nullable = false)
    private long quantitySold;

    protected DemandObservation() { }

    DemandObservation(String sku, LocalDate observedOn, long quantitySold) {
        this.id = UUID.randomUUID();
        this.sku = sku;
        this.observedOn = observedOn;
        this.quantitySold = quantitySold;
    }

    public String getSku() { return sku; }
    public LocalDate getObservedOn() { return observedOn; }
    public long getQuantitySold() { return quantitySold; }
}
