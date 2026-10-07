package com.acme.scm.commerce;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/orders")
@Validated
public class OrderController {
    private final CustomerOrderRepository orders;

    public OrderController(CustomerOrderRepository orders) {
        this.orders = orders;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_MANAGER')")
    @Transactional
    OrderView create(
            @RequestHeader("Idempotency-Key") @Pattern(regexp = "[A-Za-z0-9._:-]{16,100}") String idempotencyKey,
            @Valid @RequestBody CreateOrder request) {
        return orders.findByRequestKey(idempotencyKey)
                .map(order -> {
                    if (!order.getCustomerReference().equals(request.customerReference().trim())) {
                        throw new ResponseStatusException(
                                HttpStatus.CONFLICT, "Idempotency key was already used for a different order");
                    }
                    return view(order);
                })
                .orElseGet(() -> {
                    CustomerOrder order = orders.saveAndFlush(
                            new CustomerOrder(request.customerReference().trim(), idempotencyKey));
                    return view(order);
                });
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_MANAGER')")
    List<OrderView> list() {
        return orders.findAllByOrderByCreatedAtDesc().stream().map(OrderController::view).toList();
    }

    private static OrderView view(CustomerOrder order) {
        return new OrderView(order.getId(), order.getCustomerReference(), order.getStatus(),
                order.getCreatedAt(), order.getVersion());
    }

    record CreateOrder(@NotBlank @Size(max = 120) String customerReference) { }
    record OrderView(UUID id, String customerReference, OrderStatus status,
                     java.time.Instant createdAt, long version) { }
}
