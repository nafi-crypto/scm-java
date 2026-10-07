package com.acme.scm.commerce;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, UUID> {
    Optional<CustomerOrder> findByRequestKey(String requestKey);
    List<CustomerOrder> findAllByOrderByCreatedAtDesc();
}
