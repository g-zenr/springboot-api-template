# Non-Functional Requirements (Template)

1. Boot and pass tests with default H2 configuration (no external DB required).
2. Schema changes only via Flyway migrations.
3. HTTP validation failures return 400 with clear field messages.
4. Missing resources return 404; uniqueness conflicts return 409.
5. OpenAPI documents public HTTP endpoints.
6. Actuator health endpoint is available for readiness checks.
7. Code for a feature stays inside one `feature.*` package unless shared by design.
8. **Testing minima:** new feature ≥ 10 `@Test` methods; new function ≥ 5; updates refresh tests (`.wwg/wiki/08-operations/testing-policy.md`).
