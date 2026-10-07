package com.acme.scm.logistics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class ShipmentTest {
    @Test
    void shipmentRequiresForwardTransitionsAndHasTerminalStates() {
        Shipment shipment = new Shipment(UUID.randomUUID(), "TRACK-0001");

        shipment.updateStatus(ShipmentStatus.IN_TRANSIT);
        shipment.updateStatus(ShipmentStatus.OUT_FOR_DELIVERY);
        shipment.updateStatus(ShipmentStatus.DELIVERED);

        assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.DELIVERED);
        assertThatThrownBy(() -> shipment.updateStatus(ShipmentStatus.IN_TRANSIT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not allowed");
    }
}
