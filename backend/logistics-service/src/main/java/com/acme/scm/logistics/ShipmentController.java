package com.acme.scm.logistics;

import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/shipments")
public class ShipmentController {
    private final ShipmentRepository shipments;

    public ShipmentController(ShipmentRepository shipments) {
        this.shipments = shipments;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS_MANAGER')")
    ShipmentView create(@Valid @RequestBody CreateShipment request) {
        if (shipments.existsByOrderId(request.orderId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A shipment already exists for this order");
        }
        return view(shipments.save(new Shipment(request.orderId(), request.trackingNumber().trim())));
    }

    @GetMapping("/{trackingNumber}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS_MANAGER', 'SALES_MANAGER')")
    ShipmentView track(@PathVariable String trackingNumber) {
        Shipment shipment = shipments.findByTrackingNumberIgnoreCase(trackingNumber)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shipment not found"));
        return view(shipment);
    }

    @PatchMapping("/{trackingNumber}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS_MANAGER')")
    ShipmentView updateStatus(@PathVariable String trackingNumber, @Valid @RequestBody StatusChange request) {
        Shipment shipment = shipments.findByTrackingNumberIgnoreCase(trackingNumber)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shipment not found"));
        shipment.updateStatus(request.status());
        return view(shipments.save(shipment));
    }

    private static ShipmentView view(Shipment shipment) {
        return new ShipmentView(shipment.getId(), shipment.getOrderId(), shipment.getTrackingNumber(),
                shipment.getStatus(), shipment.getCreatedAt(), shipment.getUpdatedAt(), shipment.getVersion());
    }

    record CreateShipment(@NotNull UUID orderId,
                          @NotBlank @Pattern(regexp = "[A-Za-z0-9-]{6,100}") String trackingNumber) { }
    record StatusChange(@NotNull ShipmentStatus status) { }
    record ShipmentView(UUID id, UUID orderId, String trackingNumber, ShipmentStatus status,
                        java.time.Instant createdAt, java.time.Instant updatedAt, long version) { }
}
