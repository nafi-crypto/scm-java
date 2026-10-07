package com.acme.scm.supply;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StockItemRepository extends JpaRepository<StockItem, UUID> {
    Optional<StockItem> findBySkuIgnoreCase(String sku);
}
