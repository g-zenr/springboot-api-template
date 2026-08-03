# Specialization pass — v0.4 (2026-08-01)

## Done

1. **Replace always-on demo-only identity**
   - Optional seeder (`seed-demo-users`)
   - Self-register, admin create, me, change password
2. **Tighten secrets**
   - `JwtSecretValidator` rejects default secret outside local; min length 32
   - prod: seed off, registration off by default
3. **Domain package**
   - `feature.note` owner-scoped notes (Flyway V3) + ≥10 tests

## Validation

`mvn spotless:apply test` → PASS
