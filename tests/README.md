# Cross-service workflow verification

Add Testcontainers-based test suites here as workflows are completed. Target minimum flows:

1. Register a supplier, sign in, create supplier/product, and confirm that only the expected RBAC roles can change catalog data.
2. Adjust stock concurrently and confirm optimistic locking / nonnegative database constraints prevent overselling.
3. Submit an order with an idempotency key twice; verify the same order is returned, while reusing the key for a different request conflicts.
4. Track a shipment through valid transitions and reject terminal-state or backward transitions.
5. Record demand observations and calculate a forecast from recent observations.
6. Rotate a refresh token once and verify reuse is rejected.

The current module unit tests and integration coverage are intentionally incomplete; do not promote this starter to production without these tests and the operational gates in `docs/roadmap.md`.
