package com.acme.scm.notification;

import java.time.Instant;
import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {
    @GetMapping("/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROCUREMENT_MANAGER', 'SALES_MANAGER', 'LOGISTICS_MANAGER')")
    Map<String, Object> status() {
        return Map.of("service", "notification-service", "deliveryProviderConfigured", false,
                "checkedAt", Instant.now());
    }
}
