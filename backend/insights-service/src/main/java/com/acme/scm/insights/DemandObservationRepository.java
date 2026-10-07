package com.acme.scm.insights;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DemandObservationRepository extends JpaRepository<DemandObservation, UUID> {
    List<DemandObservation> findTop30BySkuIgnoreCaseOrderByObservedOnDesc(String sku);
}
