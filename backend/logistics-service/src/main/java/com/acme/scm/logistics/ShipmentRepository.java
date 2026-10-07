package com.acme.scm.logistics;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ShipmentRepository extends JpaRepository<Shipment, UUID> {
    Optional<Shipment> findByTrackingNumberIgnoreCase(String trackingNumber);
    boolean existsByOrderId(UUID orderId);
}
