# Implementation status and production gates

## In this foundation

- Java 21 Maven reactor and service boundaries for identity, supply, commerce, logistics, insights, notifications, and edge routing.
- JWT validation defaults, RBAC annotations, validation/error envelope, versioned routes, OpenAPI and Actuator dependencies.
- Local PostgreSQL, Redis, Kafka, Prometheus, Grafana and Nginx-backed frontend topology.
- Initial workflows for identity, suppliers/products/stock/purchase orders, customer orders and shipment tracking.
- CI build workflows and frontend build.

## Required before production

1. Complete service-owned models, DTO/mappers, authorization policies, and integration tests for every listed role and operation.
2. Implement the transactional outbox/inbox, durable idempotency-key storage, reservation/fulfillment saga, retry/DLQ policy, and reconciliation jobs.
3. Integrate an enterprise OIDC provider, account recovery/MFA, refresh-token reuse detection, session/device revocation, key rotation, and managed secrets.
4. Add privacy/data-retention policy, tamper-evident audit exports, encryption-at-rest/key management, threat modeling, penetration testing, and dependency/image CVE gates.
5. Add Testcontainers coverage for PostgreSQL, Redis, Kafka, API authorization, concurrency, migration upgrades, and end-to-end SCM flows.
6. Establish SLOs, dashboards/alerts, capacity tests, backups/restore drills, disaster recovery, network isolation, TLS, and production-specific deployment manifests.

Do not treat local Docker credentials, single-node Kafka, demo seed users, or generated development keys as production settings.
