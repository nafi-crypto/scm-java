package com.acme.scm.insights;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class ForecastController {
    private final DemandObservationRepository observations;

    public ForecastController(DemandObservationRepository observations) {
        this.observations = observations;
    }

    @PostMapping("/demand-observations")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_MANAGER', 'PROCUREMENT_MANAGER')")
    ObservationView record(@Valid @RequestBody ObservationRequest request) {
        DemandObservation observation = observations.save(new DemandObservation(
                request.sku().trim().toUpperCase(), request.observedOn(), request.quantitySold()));
        return new ObservationView(observation.getSku(), observation.getObservedOn(), observation.getQuantitySold());
    }

    @GetMapping("/forecasts/{sku}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_MANAGER', 'PROCUREMENT_MANAGER')")
    ForecastView forecast(@PathVariable String sku) {
        List<DemandObservation> recent = observations.findTop30BySkuIgnoreCaseOrderByObservedOnDesc(sku);
        if (recent.isEmpty()) {
            return new ForecastView(sku, 0, BigDecimal.ZERO, List.of());
        }
        BigDecimal average = BigDecimal.valueOf(recent.stream().mapToLong(DemandObservation::getQuantitySold).sum())
                .divide(BigDecimal.valueOf(recent.size()), 2, RoundingMode.HALF_UP);
        return new ForecastView(sku, recent.size(), average,
                recent.stream().map(item -> new ObservationView(
                        item.getSku(), item.getObservedOn(), item.getQuantitySold())).toList());
    }

    record ObservationRequest(@NotBlank @Size(max = 80) String sku,
                              @NotNull @PastOrPresent LocalDate observedOn,
                              @Min(0) long quantitySold) { }
    record ObservationView(String sku, LocalDate observedOn, long quantitySold) { }
    record ForecastView(String sku, int sampleSize, BigDecimal averageUnitsPerObservation,
                        List<ObservationView> observations) { }
}
