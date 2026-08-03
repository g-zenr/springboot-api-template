# Controller–Service–Repository Sync

## When to use

Any change that touches HTTP endpoints, DTOs, services, repositories, or entities.

## Steps

1. Identify the feature package under `com.template.api.feature.*`
2. Update or add migration if schema changes (Flyway first)
3. Align entity fields with migration
4. Update repository queries if needed
5. Keep business rules in the service (transactions live here)
6. Keep controllers thin: map HTTP ↔ request/response only
7. Update OpenAPI annotations if user-visible contract changes
8. Apply **Test Coverage Minimums** skill: new feature ≥ 10 tests; new function ≥ 5; updates refresh tests

## Done when

- Layers agree on names and nullability
- Testing policy minima met (see `testing-policy.md`)
- Wiki API/data docs updated if contract changed
