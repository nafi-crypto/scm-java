package com.acme.scm.supply;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplierRepository extends JpaRepository<Supplier, UUID> {
    boolean existsByEmailIgnoreCase(String email);
}
