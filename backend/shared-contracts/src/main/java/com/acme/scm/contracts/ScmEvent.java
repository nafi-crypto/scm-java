package com.acme.scm.contracts;

import java.time.Instant;
import java.util.UUID;

public record ScmEvent<T>(
        UUID eventId,
        String eventType,
        int schemaVersion,
        Instant occurredAt,
        String aggregateId,
        T payload) {

    public static <T> ScmEvent<T> of(String eventType, String aggregateId, T payload) {
        return new ScmEvent<>(UUID.randomUUID(), eventType, 1, Instant.now(), aggregateId, payload);
    }
}
