# Architecture and boundaries

## Deployment topology

The system uses independently deployable Spring Boot applications behind Spring Cloud Gateway. Each service owns its tables and Flyway history; a single PostgreSQL server with separate databases is a local convenience, not shared-table ownership. The gateway validates no business claims itself: services verify signed access tokens and enforce role restrictions at the endpoint. Kafka is the asynchronous integration backbone; synchronous calls should be limited to user-facing reads or commands that require an immediate result.

## Contracts and consistency

REST contracts are explicitly versioned under `/api/v1`. Additive fields are preferred; breaking changes require `/api/v2` and a documented migration window. Events use `ScmEvent<T>` with event ID, type, schema version, occurrence time, and aggregate key. Production event publishers should use the transactional outbox pattern and consumers should persist processed event IDs; a Kafka publish inside a database transaction alone is not atomic. Workflows crossing service boundaries are sagas with compensating actions, not distributed database transactions.

## Data and concurrency

Use UUID identifiers, UTC timestamps, database constraints, and optimistic locking for mutable aggregates. Each service owns its schema and migration scripts. Stock reservation must be idempotent and transactionally guarded against negative availability before enabling order fulfillment. The initial sample implementation is intentionally narrow; see `roadmap.md` before processing real purchase or fulfillment data.

## Security

Identity signs RS256 JWTs; services validate them using a public key. The private key belongs only to identity. Role checks are server-side. Store production signing material, DB credentials, OAuth client secrets, and encryption keys in a managed secret store. Configure TLS, network policy, key rotation, backup encryption, dependency scanning, audit retention, and incident response in deployment infrastructure.

## Availability and observability

Readiness/liveness are exposed through Actuator. Structured application logs should include correlation IDs but must not include credentials, access tokens, refresh tokens, or sensitive personal data. Prometheus scrapes service metrics. Use bounded retries with jitter, circuit breakers for remote dependencies, explicit timeouts, dead-letter topics, and alerting when enabling production integrations.
